package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;
import planespotter.constants.Configuration;
import planespotter.constants.UserSettings;
import planespotter.controller.ActionHandler;
import planespotter.dataclasses.*;
import planespotter.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static planespotter.constants.DefaultColor.DEFAULT_BORDER_COLOR;

/**
 * @name MapManager
 * @author jml04
 * @version 1.1
 *
 * @description
 *  manages the map data and contains methods which are executed on the mapView
 */
public final class MapManager {

    @NotNull private final UserInterface ui;

    @NotNull private final TreasureMap mapViewer;

    @Nullable private String selectedICAO;

    /**
     * constructor for LayerPane without parent JPanel
     *
     * @param ui is the {@link UserInterface} where the map is on
     * @param listener is the {@link ActionHandler} which implements
     *                 some listeners and handles user-interactions
     */
    public MapManager(@NotNull UserInterface ui, @NotNull ActionHandler listener, @NotNull TileSource defaultMapSource) {
        this.ui = ui;
        this.mapViewer = defaultMapViewer(ui.getLayerPane(), defaultMapSource);
        this.mapViewer.addMouseListener(listener);
        this.selectedICAO = null;
    }

    /**
     * clears the map from all
     *      {@link MapMarker}s,
     *      {@link MapPolygon}s and
     *      {@link MapRectangle}s
     */
    public void clearMap() {
        this.mapViewer.removeAllMapMarkers();
        this.mapViewer.removeAllMapPolygons();
        this.mapViewer.removeAllMapRectangles();
    }

    public void createSignificanceMap(@NotNull final Map<Airport, Integer> significanceMap, @NotNull final TreasureMap viewer) {
        List<MapMarker> markers = new ArrayList<>();
        var atomRadius = new AtomicInteger();
        var atomCoord = new AtomicReference<Coordinate>();
        significanceMap.keySet()
                .forEach(ap -> {
                    int lvl = significanceMap.get(ap);
                    if (lvl > 9) {
                        atomRadius.set(lvl / 100);
                        atomCoord.set(ap.pos().toCoordinate());
                        var mark = new MapMarkerCircle(atomCoord.get(), atomRadius.get());
                        mark.setColor(Color.RED);
                        mark.setBackColor(null);
                        markers.add(mark);
                    }
                });
        viewer.setMapMarkerList(markers);
    }

    /**
     * @return the default map viewer component ({@link TreasureMap})
     */
    @NotNull
    public TreasureMap defaultMapViewer(@NotNull Component parent, @NotNull TileSource mapType) {
        TreasureMap viewer = new TreasureMap();
        DefaultMapController mapController = new DefaultMapController(viewer);

        mapController.setMovementMouseButton(1);
        viewer.setBounds(parent.getBounds());
        viewer.setBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR.get()));
        viewer.setZoomControlsVisible(false);
        viewer.setTileSource(mapType);
        viewer.setVisible(true);

        return viewer;
    }

    /**
     * @param marker is the map marker coordinate
     * @param clicked is the clicked coordinate
     * @return true, if clicked coord is equals marker coord, with tolarance
     */
    public boolean isMarkerHit(Coordinate marker, ICoordinate clicked) {
        int zoom = this.ui.getMap().getZoom(), // current zoom
            maxZoom = TreasureMap.MAX_ZOOM; // = 22
        double tolerance = 0.005 * (maxZoom - zoom); // could be a bit too high
        return (clicked.getLat() < marker.getLat() + tolerance &&
                clicked.getLat() > marker.getLat() - tolerance &&
                clicked.getLon() < marker.getLon() + tolerance &&
                clicked.getLon() > marker.getLon() - tolerance);
    }

    /**
     * @param clicked is the marker not to reset
     * @return resetted list of all map markers
     */
    // TODO: 19.08.2022 method for all types resetMarkers
    public List<MapMarker> resetTrackingMarkers(MapMarker clicked) {
        ArrayList<MapMarker> mapMarkers = new ArrayList<>();
        List<MapMarker> markerList = this.ui.getMap().getMapMarkerList();
        PlaneMarker marker;
        int heading;

        for (MapMarker m : markerList) {
            if (m instanceof PlaneMarker dmm) {
                heading = dmm.getHeading();
            } else {
                heading = 0;
            }
            marker = new PlaneMarker(m.getCoordinate(), heading, false, false);
            if (m == clicked) {
                marker.setColor(Color.WHITE);
            } else {
                marker.setColor(Color.BLACK);
            }
            marker.setBackColor(m.getBackColor());
            mapMarkers.add(marker);
        }
        return mapMarkers;
    }

    public void createTrackingMap(Vector<DataPoint> dataPoints, @Nullable Flight flight, boolean showPoints) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            return;
        }
        int size = dataPoints.size(), counter = 0, altitude;
        Position dpPos;
        DataPoint lastdp = null;
        PlaneMarker newMarker;
        Coordinate coord1, coord2;
        MapPolygonImpl line;
        Color markerColor;
        List<MapPolygon> polys = new ArrayList<>(size);
        List<MapMarker> markers = new ArrayList<>(size);
        for (DataPoint dp : dataPoints) {
            dpPos = dp.pos();
            altitude = dp.altitude();
            markerColor = Utilities.colorByAltitude(altitude);
            System.out.println(dp.flightID() + ", " + dp.timestamp());
            if (counter++ > 0) {
                // checking if the data points belong to the same flight,
                //          if they are in correct order and
                //          if they make a lon-jump
                if (       dp.flightID() == lastdp.flightID()
                        && dp.timestamp() >= lastdp.timestamp()
                        && !lonJump(lastdp, dp)) {

                    coord1 = dpPos.toCoordinate();
                    coord2 = lastdp.pos().toCoordinate();
                    line = new MapPolygonImpl(coord1, coord2, coord1); // we need a line, so we use one point twice
                    line.setColor(markerColor);
                    polys.add(line);
                }
            }
            if (showPoints) {
                newMarker = PlaneMarker.fromDataPoint(dp, false, false);
                newMarker.setBackColor(markerColor);
                markers.add(newMarker);
            }
            lastdp = dp;
        }

        if (showPoints) {
            mapViewer.setMapMarkerList(markers);
        }
        mapViewer.setMapPolygonList(polys);
        if (dataPoints.size() == 1 && flight != null) {
            ui.showInfo(flight, dataPoints.get(0));
        }
    }

    public void createSearchMap(Vector<DataPoint> dataPoints, boolean showAllPoints) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            return;
        }
        DataPoint lastDp = null;
        List<Integer> paintedFlights = new ArrayList<>();
        List<MapMarker> mapMarkers = new ArrayList<>();
        List<MapPolygon> mapPolys = new ArrayList<>();
        Color color; PlaneMarker marker; MapPolygonImpl poly;
        Coordinate currCoord, lastCoord; int flightID;
        for (DataPoint dp : dataPoints) {
            currCoord = dp.pos().toCoordinate();
            color = Utilities.colorByAltitude(dp.altitude());
            flightID = dp.flightID();
            if (lastDp != null) {
                lastCoord = lastDp.pos().toCoordinate();

                if (   lastDp.flightID() == flightID
                    && lastDp.timestamp() < dp.timestamp()
                    && !lonJump(lastDp, dp)) {

                    poly = new MapPolygonImpl(lastCoord, currCoord, lastCoord);
                    poly.setColor(color);
                    mapPolys.add(poly);
                }
            }
            if (showAllPoints || !paintedFlights.contains(flightID)) {
                marker = PlaneMarker.fromDataPoint(dp, true, false);
                marker.setBackColor(color);
                mapMarkers.add(marker);
                if (!showAllPoints) {
                    paintedFlights.add(flightID);
                }
            }
            lastDp = dp;
        }
        mapViewer.setMapMarkerList(mapMarkers);
        mapViewer.setMapPolygonList(mapPolys);
    }

    @NotNull
    public TreasureMap getMapViewer() {
        return this.mapViewer;
    }

    @Nullable
    public String getSelectedICAO() {
        return this.selectedICAO;
    }

    public void setSelectedICAO(@Nullable String selectedICAO) {
        this.selectedICAO = selectedICAO;
    }

    private boolean lonJump(@NotNull DataPoint a, @NotNull DataPoint b) {
        double lon0 = a.pos().lon();
        double lon1 = b.pos().lon();
        return  (lon0 < -90 && lon1 > 90) || (lon0 > 90 && lon1 < -90);
    }
}

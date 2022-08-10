package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;
import planespotter.constants.UserSettings;
import planespotter.controller.ActionHandler;
import planespotter.dataclasses.*;
import planespotter.display.models.HeatMapRectangle;
import planespotter.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static planespotter.constants.GUIConstants.LINE_BORDER;


/**
 * @name MapManager
 * @author jml04
 * @version 1.1
 *
 *  manages the map data and contains methods which are executed on the mapView
 */
public final class MapManager {

    private final UserInterface ui;

    private volatile TreasureMap mapViewer;

    /**
     * constructor for LayerPane without parent JPanel
     *
     * @param ui is the {@link UserInterface} where the map is on
     * @param listener
     */
    public MapManager(@NotNull UserInterface ui, @NotNull ActionHandler listener) {
        this.ui = ui;
        this.mapViewer = this.defaultMapViewer(ui.getLayerPane());
        this.mapViewer.addMouseListener(listener);
    }

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
                        atomCoord.set(Position.toCoordinate(ap.pos()));
                        var mark = new MapMarkerCircle(atomCoord.get(), atomRadius.get());
                        mark.setColor(Color.RED);
                        mark.setBackColor(null);
                        markers.add(mark);
                    }
                });
        viewer.setMapMarkerList(markers);
    }

    public void createPrototypeHeatMap(@NotNull final HashMap<Position, Integer> heatMap, @NotNull final TreasureMap viewer) {
        var markers = new LinkedList<MapMarker>();
        MapMarkerCircle newMarker;
        for (var pos : heatMap.keySet()) {
            int radius = heatMap.get(pos)/10000;
            //newMarker = new MapMarkerCircle(Position.toCoordinate(pos), radius);
            newMarker = new DefaultMapMarker.HeatMapMarker(Position.toCoordinate(pos), heatMap.get(pos)/8);
            newMarker.setColor(Color.RED);
            newMarker.setBackColor(null);
            markers.add(newMarker);
        }

        viewer.setMapMarkerList(markers);
    }

    public void createRasterHeatMap(@NotNull final BufferedImage heatMapImg, @NotNull final TreasureMap viewer) {
        var rect = new HeatMapRectangle(heatMapImg, viewer);
        viewer.addMapRectangle(rect);
    }

    /**
     * @return the default map viewer component ({@link TreasureMap})
     */
    @NotNull
    public TreasureMap defaultMapViewer(@Nullable Component parent) {
        TreasureMap viewer = new TreasureMap();
        DefaultMapController mapController = new DefaultMapController(viewer);
        TileSource mapType = UserSettings.getCurrentMapSource();

        mapController.setMovementMouseButton(1);
        if (parent != null) {
            viewer.setBounds(parent.getBounds());
        }
        viewer.setBorder(LINE_BORDER);
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
        int zoom = this.ui.getMap().getZoom();
        double tolerance = 0.008 * zoom; // // FIXME: 23.04.2022 falsche formel (exponential?)
        return (clicked.getLat() < marker.getLat() + tolerance &&
                clicked.getLat() > marker.getLat() - tolerance &&
                clicked.getLon() < marker.getLon() + tolerance &&
                clicked.getLon() > marker.getLon() - tolerance);
    }

    /**
     * @param clicked is the marker not to reset
     * @return resetted list of all map markers
     */
    public List<MapMarker> resetTrackingMarkers(MapMarker clicked) {
        var mapMarkers = new ArrayList<MapMarker>();
        var markerList = this.ui.getMap().getMapMarkerList();
        for (var m : markerList) {
            var marker = new DefaultMapMarker(m.getCoordinate(), 90); // FIXME: 19.05.2022
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

    public TreasureMap getMapViewer() {
        return this.mapViewer;
    }

    public void createTrackingMap(@NotNull Vector<DataPoint> dataPoints, @Nullable Flight flight, boolean showPoints) {
        if (dataPoints.isEmpty()) {
            return;
        }
        int size = dataPoints.size(),
                counter = 0,
                altitude,
                heading;
        Position dpPos;
        DataPoint lastdp = null;
        DefaultMapMarker newMarker;
        Coordinate coord1, coord2;
        MapPolygonImpl line;
        Color markerColor;
        List<MapPolygon> polys = new ArrayList<>(size);
        List<MapMarker> markers = new ArrayList<>(size);
        for (DataPoint dp : dataPoints) {
            dpPos = dp.pos();
            altitude = dp.altitude();
            heading = dp.heading();
            markerColor = Utilities.colorByAltitude(altitude);
            if (counter++ > 0) {
                double lon0 = lastdp.pos().lon();
                double lon1 = dp.pos().lon();
                boolean lonJump = (lon0 < -90 && lon1 > 90) || (lon0 > 90 && lon1 < -90);
                // checking if the data points belong to the same flight,
                //          if they are in correct order and
                //          if they make a lon-jump
                if (       dp.flightID() == lastdp.flightID()
                        && dp.timestamp() >= lastdp.timestamp()
                        && !lonJump) {

                    coord1 = Position.toCoordinate(dpPos);
                    coord2 = Position.toCoordinate(lastdp.pos());
                    line = new MapPolygonImpl(coord1, coord2, coord1);
                    line.setColor(markerColor);
                    polys.add(line);
                }
            }
            if (showPoints) {
                newMarker = new DefaultMapMarker(new Coordinate(dpPos.lat(), dpPos.lon()), heading);
                newMarker.setBackColor(markerColor);
                markers.add(newMarker);
            }
            lastdp = dp;
        }

        if (showPoints) {
            this.mapViewer.setMapMarkerList(markers);
        }
        this.mapViewer.setMapPolygonList(polys);
        if (dataPoints.size() == 1 && flight != null) {
            this.ui.showInfo(flight, dataPoints.get(0));
        }
    }

}

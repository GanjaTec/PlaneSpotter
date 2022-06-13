package planespotter.display;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;
import planespotter.constants.UserSettings;
import planespotter.controller.ActionHandler;
import planespotter.dataclasses.*;
import planespotter.util.Utilities;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static planespotter.constants.GUIConstants.DEFAULT_HEAD_TEXT;
import static planespotter.constants.DefaultColor.DEFAULT_MAP_ICON_COLOR;
import static planespotter.constants.GUIConstants.LINE_BORDER;


/**
 * @name BlackBeardsNavigator
 * @author jml04
 * @version 1.1
 *
 *  manages the map data and contains methods which are executed on the mapView
 */
public final class MapManager {

    private final GUI gui;

    private volatile TreasureMap mapViewer;

    /**
     * constructor
     */
    public MapManager(GUI gui, JPanel defaultMapPanel, ActionHandler listener) {
        this.gui = gui;
        this.mapViewer = this.defaultMapViewer(defaultMapPanel);
        this.mapViewer.addKeyListener(listener);
        this.mapViewer.addMouseListener(listener);
        this.mapViewer.addJMVListener(listener);
    }

    /**
     * creates a map with a flight route from a specific flight
     */
    public TreasureMap createTrackingMap(Vector<DataPoint> dataPoints, Flight flight, boolean showPoints, GUIAdapter guiAdapter)
            throws DataNotFoundException {
        var viewer = this.mapViewer;
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
        var polys = new ArrayList<MapPolygon>(size);
        var markers = new ArrayList<MapMarker>(size);
        for (var dp : dataPoints) {
            dpPos = dp.pos();
            altitude = dp.altitude();
            heading = dp.heading();
            markerColor = Utilities.colorByAltitude(altitude);
            if (counter > 0) {
                double latDiff = lastdp.pos().lat() - dp.pos().lat();
                if (dp.flightID() == lastdp.flightID() // check if the data points belong to eachother
                        && dp.timestamp() >= lastdp.timestamp()
                        && latDiff < 350) {
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
            counter++;
            lastdp = dp;
        }

        if (!dataPoints.isEmpty()) {
            if (showPoints) {
                viewer.setMapMarkerList(markers);
            }
            viewer.setMapPolygonList(polys);
            if (dataPoints.size() == 1) {
                new TreePlantation().createFlightInfo(flight, guiAdapter);
            }
        } else throw new DataNotFoundException("Couldn't create Flight Route for this flightID!");
        return viewer;
    }

    /**
     * creates a map with all flights from the given list
     */
    public TreasureMap createLiveMap(final Vector<Position> data, final TreasureMap viewer) {
        DefaultMapMarker newMarker;
        for (var pos : data) {
            newMarker = new DefaultMapMarker(new Coordinate(pos.lat(), pos.lon()), 90); // FIXME: 19.05.2022
            newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
            viewer.addMapMarker(newMarker);
        }
        return viewer;
    }

    public TreasureMap createSignificanceMap(final Map<Airport, Integer> significanceMap, final TreasureMap viewer) {
        var markers = new LinkedList<MapMarker>();
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
        return viewer;
    }

    public TreasureMap createPrototypeHeatMap(final HashMap<Position, Integer> heatMap, final TreasureMap viewer) {
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
        return viewer;
    }

    public MapManager createRasterHeatMap(final BufferedImage heatMapImg, final TreasureMap viewer) {
        var rect = new HeatMapRectangle(heatMapImg, viewer);
        viewer.addMapRectangle(rect);
        return this;
    }

    /**
     * @return a map prototype (TreasureMap)
     */
    public TreasureMap defaultMapViewer(JPanel parent) {
        var viewer = new TreasureMap(new MemoryTileCache());
        var mapController = new DefaultMapController(viewer);
        var mapType = UserSettings.getCurrentMapSource();
        mapController.setMovementMouseButton(1);
        viewer.setBounds(parent.getBounds());
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
        int zoom = gui.getMap().getZoom();
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
        var markerList = this.gui.getMap().getMapMarkerList();
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

    /**
     * sets the TreasureMap in mapViewer
     *
     * @param map is the map to be set
     */
    public void recieveMap(TreasureMap map, String text) {
        // adding MapViewer to panel (needed?)
        this.mapViewer = map;
        var mapPanel = gui.getContainer("mapPanel");
        if (mapPanel.getComponentCount() == 0) {
            mapPanel.add(this.mapViewer);
        }
        var viewHeadTxt = (JLabel) gui.getContainer("viewHeadTxtLabel");
        viewHeadTxt.setText(DEFAULT_HEAD_TEXT + "Map-Viewer > " + text);
        // revalidating window fr24Frame to refresh everything
        mapPanel.setVisible(true);
        this.mapViewer.setVisible(true);
        new GUIAdapter(this.gui).requestComponentFocus(this.mapViewer);
    }

    TreasureMap getMapViewer() {
        return this.mapViewer;
    }

}

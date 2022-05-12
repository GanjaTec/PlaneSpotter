package planespotter.display;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;
import org.openstreetmap.gui.jmapviewer.tilesources.*;
import planespotter.constants.ViewType;
import planespotter.controller.Controller;
import planespotter.controller.DataMaster;
import planespotter.dataclasses.*;
import planespotter.model.Utilities;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static planespotter.constants.GUIConstants.DEFAULT_MAP_ICON_COLOR;
import static planespotter.constants.GUIConstants.LINE_BORDER;


/**
 * @name MapManager
 * @author jml04
 * @version 1.0
 *
 * map manager:
 *  manages the map data and contains methods which are executed on the mapView
 */
public final class BlackBeardsNavigator {

    // static gui instance
    private static GUI gui;
    // current view type ( in action )
    public static ViewType currentViewType;

    /**
     * constructor, is private because @unused
     */
    public BlackBeardsNavigator() {
    }

    /**
     * initializes BlackBeardsNavigator
     */
    public static void initialize () {
        gui = Controller.getInstance().gui();
    }

    /**
     * creates a map with a flight route from a specific flight
     *
     * @param dps is the given tracking-hashmap
     */
    public void createFlightRoute (List<DataPoint> dps, Flight flight, String text, boolean showPoints) throws DataNotFoundException {
        var viewer = gui.mapViewer;
        var ctrl = Controller.getInstance();
        ctrl.allMapData = new HashMap<>();
        int counter = 0;
        DataPoint last = null;
        var polys = new ArrayList<MapPolygon>();
        var markers = new ArrayList<MapMarker>();
        for (var dp : dps) {
            var pos = dp.getPos();
            var newMarker = new CustomMapMarker(new Coordinate(pos.getLat(), pos.getLon()), flight);
            int altitude = dp.getAltitude();
            // TODO in Constants auslagern -> Farben je nach Höhe (oder anders Attribut)
            var color = new BlackBeardsNavigator().colorByAltitude(altitude);
            if (counter > 0) {
                if (dp.getFlightID() == last.getFlightID()) {
                    var pos1 = Position.toCoordinate(dp.getPos());
                    var pos2 = Position.toCoordinate(last.getPos());
                    var line = new MapPolygonImpl(pos1, pos2, pos1);
                    line.setColor(color);
                    polys.add(line);
                }
            }
            newMarker.setBackColor(color);
            if (showPoints) {
                markers.add(newMarker);
            }
            ctrl.allMapData.put(counter, dp);
            counter++;
            last = dp;
        }

        if (!dps.isEmpty()) {
            viewer.setMapMarkerList(markers);
            viewer.setMapPolygonList(polys);
            new GUISlave().recieveMap(viewer, "Route: " + text);
            new TreePlantation().createFlightInfo(flight);
        } else throw new DataNotFoundException("Couldn't create Flight Route for this flightID!");
    }

    /**
     * creates a map with all flights from the given list
     *
     * @param list is the given flight list
     *
     *             // FIXME: 27.04.2022 Methode aufteilen!!
     */ // TODO change to param List<Position>
    public void createAllFlightsMap (List<DataPoint> list) {
        var viewer = gui.mapViewer;
        var viewSize = viewer.getVisibleRect(); // may be used in the future
        var ctrl = Controller.getInstance();
        ctrl.allMapData = new HashMap<>();
        int counter = 0;
        for (var dp : list) {
            var lastPos = dp.getPos();
            var newMarker = new MapMarkerDot(new Coordinate(lastPos.getLat(), lastPos.getLon()));
            newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR);
            viewer.addMapMarker(newMarker);
            ctrl.allMapData.put(counter, dp);
            counter++;
        }
        new GUISlave().recieveMap(viewer, "Live-Map");
    }

    /**
     * @param altitude is the altitude from the given DataPoint
     * @return a specific color, depending on the altitude
     */
    Color colorByAltitude (int altitude) {
        long meters = new Utilities().feetToMeters(altitude);
        int maxHeight = 15000;
        int r = 255,
            g = 0,
            b = 0;
        int factor = (255 / 50);
        for (long i = 0; i < maxHeight;) {
            if (meters <= i) {
                return new Color(r, g, b);
            } else {
                if (i < 7000) {
                    g += factor*2;
                } else {
                    if (r > 0) {
                        r -= factor * 4;
                        if (r < 0) { // gebraucht?
                            r = 0;
                        }
                    } else {
                        b += factor*4;
                    }
                }
                i += 300;
            }
        }
        return new Color(r, g, 255);
    }

    /**
     * @return a map prototype (JMapViewer)
     */
    JMapViewer defaultMapViewer (JPanel parent) {
        // TODO: trying to set up JMapViewer
        var viewer = new JMapViewer(new MemoryTileCache());
        viewer.setBorder(LINE_BORDER);
        var mapController = new MapManager(viewer);
        mapController.setMovementMouseButton(1);
        viewer.setDisplayToFitMapMarkers();
        viewer.setZoomControlsVisible(false);
        var mapType = new UserSettings().getCurrentMapSource();
        viewer.setTileSource(mapType);
        viewer.setVisible(true);
        viewer.setBounds(parent.getBounds());

        return viewer;
    }

    /**
     * is executed when a map marker is clicked
     *
     * @param point is the clicked map point (no coordinate)
     */
    void markerClicked (Point point) {
        var clicked = gui.mapViewer.getPosition(point);
        var bbn = new BlackBeardsNavigator();
        switch (BlackBeardsNavigator.currentViewType) {
            case MAP_ALL, MAP_FROMSEARCH -> bbn.onClick_all(clicked);
            case MAP_TRACKING -> bbn.onClick_tracking(clicked);
        }
    }

    /**
     * is executed when a map marker is clicked and the current is MAP_ALL
     */
    private void onClick_all (ICoordinate clickedCoord) {
        var markers = gui.mapViewer.getMapMarkerList();
        var newMarkerList = new ArrayList<MapMarker>();
        Coordinate markerCoord;
        CustomMapMarker newMarker;
        int counter = 0;
        boolean markerHit = false;
        var bbn = new BlackBeardsNavigator();
        var ctrl = Controller.getInstance();
        for (MapMarker m : markers) {
            markerCoord = m.getCoordinate();
            newMarker = new CustomMapMarker(markerCoord, null);
            if (bbn.markerHit(markerCoord, clickedCoord)) {
                markerHit = true;
                newMarker.setBackColor(Color.RED);
                gui.pMenu.setVisible(false);
                gui.pInfo.removeAll();
                gui.dpleft.moveToFront(gui.pInfo);
                int flightID = ctrl.allMapData.get(counter).getFlightID();
                var flight = new DataMaster().flightByID(flightID);
                new TreePlantation().createFlightInfo(flight);
            } else {
                newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR);
            }
            newMarker.setName(m.getName());
            newMarkerList.add(newMarker);
            counter++;
        }
        if (markerHit) {
            gui.mapViewer.setMapMarkerList(newMarkerList);
        }
    }

    /**
     *
     * @param clickedCoord is the clicked coordinate
     */
    private void onClick_tracking (ICoordinate clickedCoord) {
        var markers = gui.mapViewer.getMapMarkerList();
        Coordinate markerCoord;
        int counter = 0;
        var bbn = new BlackBeardsNavigator();
        var ctrl = Controller.getInstance();
        for (MapMarker m : markers) {
            markerCoord = m.getCoordinate();
            if (bbn.markerHit(markerCoord, clickedCoord)) {
                gui.pInfo.removeAll();
                var dp = ctrl.allMapData.get(counter);
                var flight = new DataMaster().flightByID(dp.getFlightID()); // TODO woanders!!!
                new TreePlantation().createDataPointInfo(flight, dp);
                gui.mapViewer.setMapMarkerList(bbn.resetMarkers(m));
            }
            counter++;
        }
    }

    /**
     * @param marker is the map marker coordinate
     * @param clicked is the clicked coordinate
     * @return true, if clicked coord is equals marker coord, with tolarance
     */
    private boolean markerHit (Coordinate marker, ICoordinate clicked) {
        int zoom = gui.mapViewer.getZoom();
        double tolerance = 0.005 * zoom; // // FIXME: 23.04.2022 falsche formel (exponential?)
        return (clicked.getLat() < marker.getLat() + tolerance &&
                clicked.getLat() > marker.getLat() - tolerance &&
                clicked.getLon() < marker.getLon() + tolerance &&
                clicked.getLon() > marker.getLon() - tolerance);
    }

    /**
     * @param clicked is the marker not to reset
     * @return resetted list of all map markers
     */
    private List<MapMarker> resetMarkers (MapMarker clicked) {
        var mapMarkers = new ArrayList<MapMarker>();
        for (var m : gui.mapViewer.getMapMarkerList()) {
            var marker = new MapMarkerDot(m.getCoordinate());
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

    class MapManager extends DefaultMapController {

        public MapManager(JMapViewer map) {
            super(map);
        }

        /*@Override
        public void mousePressed(MouseEvent e) {
            var point = e.getPoint();
            new JMapViewer().
        }*/
    }

}

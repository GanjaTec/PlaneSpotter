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
    private BlackBeardsNavigator () {
    }

    /**
     * initializes BlackBeardsNavigator
     */
    public static void initialize () {
        gui = Controller.gui();
    }

    /**
     * creates a map with a flight route from a specific flight
     *
     * @param dps is the given tracking-hashmap
     */
    public static void createFlightRoute (List<DataPoint> dps, Flight flight) throws DataNotFoundException {
        var viewer = gui.mapViewer;
        Controller.allMapData = new HashMap<>();
        int counter = 0;
        DataPoint last = null;
        var polys = new ArrayList<MapPolygon>();
        var markers = new ArrayList<MapMarker>();
        for (var dp : dps) {
            var pos = dp.getPos();
            var newMarker = new CustomMapMarker(new Coordinate(pos.getLat(), pos.getLon()), flight);
            int altitude = dp.getAltitude();
            // TODO in Constants auslagern -> Farben je nach Höhe (oder anders Attribut)
            var color = BlackBeardsNavigator.colorByAltitude(altitude);
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
            markers.add(newMarker);
            Controller.allMapData.put(counter, dp);
            counter++;
            last = dp;
        }

        if (!dps.isEmpty()) {
            viewer.setMapMarkerList(markers);
            viewer.setMapPolygonList(polys);
            GUISlave.recieveMap(viewer);
            TreePlantation.createFlightInfo(flight);
        } else throw new DataNotFoundException("Couldn't create Flight Route for this flightID!");
    }

    /**
     * creates a map with all flights from the given list
     *
     * @param list is the given flight list
     *
     *             // FIXME: 27.04.2022 Methode aufteilen!!
     */ // TODO change to param List<Position>
    public static void createAllFlightsMap (List<DataPoint> list) {
        var viewer = gui.mapViewer;
        var viewSize = viewer.getVisibleRect(); // may be used in the future
        Controller.allMapData = new HashMap<>();
        int counter = 0;
        for (var dp : list) {
            var lastPos = dp.getPos();
            var newMarker = new MapMarkerDot(new Coordinate(lastPos.getLat(), lastPos.getLon()));
            newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR);
            viewer.addMapMarker(newMarker);
            Controller.allMapData.put(counter, dp);
            counter++;
        }
        GUISlave.recieveMap(viewer);
    }

    /**
     * @param altitude is the altitude from the given DataPoint
     * @return a specific color, depending on the altitude
     */
    static Color colorByAltitude (int altitude) {
        long meters = Utilities.feetToMeters(altitude);
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
    static JMapViewer defaultMapViewer (JPanel parent) {
        // TODO: trying to set up JMapViewer
        var viewer = new JMapViewer(new MemoryTileCache());
        viewer.setBorder(LINE_BORDER);
        var mapController = new MapManager(viewer);
        mapController.setMovementMouseButton(1);
        viewer.setDisplayToFitMapMarkers();
        viewer.setZoomControlsVisible(false);
        var mapType = UserSettings.getCurrentMapSource();
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
    static void markerClicked (Point point) {
        var clicked = gui.mapViewer.getPosition(point);
        switch (BlackBeardsNavigator.currentViewType) {
            case MAP_ALL, MAP_FROMSEARCH -> BlackBeardsNavigator.onClick_all(clicked);
            case MAP_TRACKING -> BlackBeardsNavigator.onClick_tracking(clicked);
        }
    }

    /**
     * is executed when a map marker is clicked and the current is MAP_ALL
     */
    private static void onClick_all (ICoordinate clickedCoord) {
        var markers = gui.mapViewer.getMapMarkerList();
        var newMarkerList = new ArrayList<MapMarker>();
        Coordinate markerCoord;
        CustomMapMarker newMarker;
        int counter = 0;
        boolean markerHit = false;
        for (MapMarker m : markers) {
            markerCoord = m.getCoordinate();
            newMarker = new CustomMapMarker(markerCoord, null);
            if (BlackBeardsNavigator.markerHit(markerCoord, clickedCoord)) {
                markerHit = true;
                newMarker.setBackColor(Color.RED);
                gui.pMenu.setVisible(false);
                gui.pInfo.removeAll();
                gui.dpleft.moveToFront(gui.pInfo);
                int flightID = Controller.allMapData.get(counter).getFlightID();
                var flight = new DataMaster().flightByID(flightID);
                TreePlantation.createFlightInfo(flight);
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
    private static void onClick_tracking (ICoordinate clickedCoord) {
        var markers = gui.mapViewer.getMapMarkerList();
        Coordinate markerCoord;
        int counter = 0;
        for (MapMarker m : markers) {
            markerCoord = m.getCoordinate();
            if (BlackBeardsNavigator.markerHit(markerCoord, clickedCoord)) {
                gui.pInfo.removeAll();
                var dp = Controller.allMapData.get(counter);
                var flight = new DataMaster().flightByID(dp.getFlightID()); // TODO woanders!!!
                TreePlantation.createDataPointInfo(flight, dp);
                gui.mapViewer.setMapMarkerList(BlackBeardsNavigator.resetMarkers(m));
            }
            counter++;
        }
    }

    /**
     * @param marker is the map marker coordinate
     * @param clicked is the clicked coordinate
     * @return true, if clicked coord is equals marker coord, with tolarance
     */
    private static boolean markerHit (Coordinate marker, ICoordinate clicked) {
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
    private static List<MapMarker> resetMarkers (MapMarker clicked) {
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

    static class MapManager extends DefaultMapController {

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

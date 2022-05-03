package planespotter.display;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
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
    // hash map for all map markers
    private static HashMap<Integer, DataPoint> allMapData = new HashMap<>();
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
    public static void createFlightRoute (HashMap<Integer, DataPoint> dps, Flight flight) throws DataNotFoundException {
        var viewer = gui.mapViewer;
        var keySet = dps.keySet();
        allMapData = new HashMap<>();
        int counter = 0;
        for (int key : keySet) {
            var dp = dps.get(key);
            var pos = dp.getPos();
            var newMarker = new CustomMapMarker(new Coordinate(pos.getLat(), pos.getLon()), flight);
            int altitude = dp.getAltitude();
            // TODO in Constants auslagern -> Farben je nach Höhe (oder anders Attribut)
            var color = BlackBeardsNavigator.colorByAltitude(altitude);
            newMarker.setBackColor(color);
            viewer.addMapMarker(newMarker);
            allMapData.put(counter, dp);
            counter++;
        }

        if (!dps.isEmpty()) {
            GUISlave.recieveMap(viewer);
            TreePlantation.createFlightInfo(flight);
        } else throw new DataNotFoundException("no DataPoints for this flightID!");
    }

    /**
     * creates a map with all flights from the given list
     *
     * @param list is the given flight list
     *
     *             // FIXME: 27.04.2022 Methode aufteilen!!
     */
    public static void createAllFlightsMap (List<Flight> list) {
        var viewer = gui.mapViewer;
        var viewSize = viewer.getVisibleRect(); // may be used in the future
        allMapData = new HashMap<>();
        int counter = 0;
        for (Flight f : list) {
            int lastTrackingID = new DataMaster().lastTrackingID(f.getID());
            //length is 1 TODO fix // NullPointerException: lastDataPoint is null
            var lastDataPoint = f.getDataPoints().get(lastTrackingID);
            var lastPos = lastDataPoint.getPos();
            // TODO: creating new Map Marker // will be optimized
            var newMarker = new CustomMapMarker(new Coordinate(lastPos.getLat(), lastPos.getLon()), f);
            newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR);
            viewer.addMapMarker(newMarker);
            allMapData.put(counter, lastDataPoint);
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
        var mapController = new DefaultMapController(viewer);
        mapController.setMovementMouseButton(1);
        viewer.setDisplayToFitMapMarkers();
        viewer.setZoomControlsVisible(false);
        viewer.setTileSource(new BingAerialTileSource());
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
        gui.pInfo.removeAll();
        var clicked = gui.mapViewer.getPosition(point);
        switch (BlackBeardsNavigator.currentViewType) {
            case MAP_ALL -> BlackBeardsNavigator.onClick_all(clicked);
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
        int zoom = gui.mapViewer.getZoom();
        double tolerance = 0.1 / zoom; // // FIXME: 23.04.2022 falsche formel (exponential?)
        int counter = 0;
        for (MapMarker m : markers) {
            markerCoord = m.getCoordinate();
            newMarker = new CustomMapMarker(markerCoord, null);
            if (clickedCoord.getLat() < markerCoord.getLat() + tolerance &&
                    clickedCoord.getLat() > markerCoord.getLat() - tolerance &&
                    clickedCoord.getLon() < markerCoord.getLon() + tolerance &&
                    clickedCoord.getLon() > markerCoord.getLon() - tolerance) {
                newMarker.setBackColor(Color.RED);
                gui.pMenu.setVisible(false);
                gui.pInfo.setVisible(true);
                gui.dpleft.moveToFront(gui.pInfo);
                int flightID = allMapData.get(counter).getFlightID();
                var flight = new DataMaster().flightByID(flightID);
                TreePlantation.createFlightInfo(flight);
            } else {
                newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR);
            }
            newMarker.setName(m.getName());
            newMarkerList.add(newMarker);
            counter++;
        }
        gui.mapViewer.setMapMarkerList(newMarkerList);
    }

    /**
     *
     * @param clickedCoord is the clicked coordinate
     */
    private static void onClick_tracking (ICoordinate clickedCoord) {
        var markers = gui.mapViewer.getMapMarkerList();
        Coordinate markerCoord;
        int zoom = gui.mapViewer.getZoom();
        double tolerance = 0.1 / zoom; // // FIXME: 23.04.2022 falsche formel (exponential?)
        int counter = 0;
        for (MapMarker m : markers) {
            markerCoord = m.getCoordinate();
            if (clickedCoord.getLat() < markerCoord.getLat() + tolerance &&
                    clickedCoord.getLat() > markerCoord.getLat() - tolerance &&
                    clickedCoord.getLon() < markerCoord.getLon() + tolerance &&
                    clickedCoord.getLon() > markerCoord.getLon() - tolerance) {
                TreePlantation.createDataPointInfo(BlackBeardsNavigator.allMapData.get(counter));
            }
            counter++;
        }
    }

}

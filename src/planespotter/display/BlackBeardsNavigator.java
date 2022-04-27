package planespotter.display;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import planespotter.constants.GUIConstants;
import planespotter.controller.Controller;
import planespotter.controller.DataMaster;
import planespotter.dataclasses.CustomMapMarker;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Flight;

import javax.swing.*;
import java.util.*;
import java.util.List;

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
    // list for all map markers
    static List<CustomMapMarker> allMapMarkers = new ArrayList<>();

    /**
     * constructor, is private because @unused
     */
    private BlackBeardsNavigator() {
    }

    /**
     * initializes BlackBeardsNavigator
     */
    public static void initialize() {
        gui = Controller.gui();
    }

    /**
     * creates a map with a flight route from a specific flight
     *
     * @param dps is the given tracking-hashmap
     */
    public static void createFlightRoute (HashMap<Integer, DataPoint> dps) {
        var viewer = gui.mapViewer;
        var keySet = dps.keySet();
        allMapMarkers = new ArrayList<>();
        int idKey = 0;
        for (int key : keySet) {
            var dp = dps.get(key);
            var pos = dp.getPos();
            var newMarker = new CustomMapMarker(new Coordinate(pos.getLat(), pos.getLon()), new DataMaster().flightByID(dp.getFlightID()));
            viewer.addMapMarker(newMarker);
            allMapMarkers.add(newMarker);
            idKey = key;
        }
        GUISlave.recieveMap(viewer);
        TreePlantation.createInfoTree(dps.get(idKey).getFlightID());
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
        allMapMarkers = new ArrayList<>();
        for (Flight f : list) {
            int lastTrackingID = new DataMaster().lastTrackingID(f.getID());
            //length is 1 TODO fix // NullPointerException: lastDataPoint is null
            var lastDataPoint = f.getDataPoints().get(lastTrackingID);
            var lastPos = lastDataPoint.getPos();
            // TODO: creating new Map Marker // will be optimized
            var newMarker = new CustomMapMarker(new Coordinate(lastPos.getLat(), lastPos.getLon()), f);
            newMarker.setBackColor(GUIConstants.DEFAULT_MAP_ICON_COLOR);
            viewer.addMapMarker(newMarker);
            allMapMarkers.add(newMarker);
        }
        GUISlave.recieveMap(viewer);
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
     */
    void mapMarkerClicked () {
        // needed?
    }

}

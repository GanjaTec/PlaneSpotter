package planespotter.display;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.constants.GUIConstants;
import planespotter.dataclasses.CustomMapMarker;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.Position;
import planespotter.model.DBOut;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * @name MapManager
 * @author jml04
 * @version 1.0
 *
 * map manager:
 *  manages the map data and contains methods which are executed on the mapView
 */
public class BlackBeardsNavigator implements Runnable {

    /**
     * class variables
     */
    private static GUI gui;
    // may be changed to volatile in the future, @deprecated
    public static HashMap<Position, Integer> shownFlights = new HashMap<Position, Integer>();
    public static List<CustomMapMarker> allMapMarkers = new ArrayList<>();

    /**
     * thread run method TODO checken
     */
    @Override
    public void run () {
        Thread.currentThread().setName("BlackBeards-Navigator");
        Thread.currentThread().setPriority(8);
    }

    /**
     * constructor, creates a new BlackBeardsNavigator instance with a gui
     *
     * @param gui is the used gui
     */
    public BlackBeardsNavigator(GUI gui) {
        this.gui = gui;
    }

    /**
     * creates a map with a fliht route from a specific flight
     *
     * @param dps is the given tracking-hashmap
     */
    public void createFlightRoute (HashMap<Integer, DataPoint> dps) {
        JMapViewer viewer = gui.createMap();
        Set<Integer> keySet = dps.keySet();
        allMapMarkers = new ArrayList<>();
        for (int key : keySet) {
            DataPoint dp = dps.get(key);
            Position pos = dp.getPos();
            CustomMapMarker newMarker = new CustomMapMarker(new Coordinate(pos.getLat(), pos.getLon()), new DBOut().getFlightByID(dp.getFlightID()));
            viewer.addMapMarker(newMarker);
            allMapMarkers.add(newMarker);
        }
        gui.recieveMap(viewer);
    }

    /**
     * creates a map with all flights from the given list
     *
     * @param list is the given flight list
     */
    public void createAllFlightsMap (List<Flight> list) {
        JMapViewer viewer = gui.createMap();
        Rectangle viewSize = viewer.getVisibleRect();
        List<ICoordinate> coords = new ArrayList<>();
        allMapMarkers = new ArrayList<>();
        for (Flight f : list) {
            int lastTracking = new DBOut().getLastTrackingIDByFlightID(f.getID());
            //length is 1 TODO fixen // NullPointerException: lastDataPoint is null
            DataPoint lastDataPoint = f.getDataPoints().get(lastTracking);
            Position lastPos = lastDataPoint.getPos();
            // TODO: adding flight id and position to global HashMap
            /*shownFlights = new HashMap<>();
            shownFlights.put(lastPos, f.getID());*/
            // TODO: creating new Map Marker // will be optimized
            CustomMapMarker newMarker = new CustomMapMarker(new Coordinate(lastPos.getLat(), lastPos.getLon()), f);
            newMarker.setBackColor(GUIConstants.DEFAULT_MAP_ICON_COLOR);
            viewer.addMapMarker(newMarker);
            allMapMarkers.add(newMarker);
        //@experimental
            viewer.addMapPolygon(new MapPolygonImpl());
            if (coords.isEmpty() || coords.size() == 1) {
                coords.add(new Coordinate(lastPos.getLat(), lastPos.getLon()));
            }
            else {
                coords.add(new Coordinate(lastPos.getLat(), lastPos.getLon()));
                viewer.addMapPolygon(new MapPolygonImpl(coords));
                coords.remove(0);
            }
        }
        gui.recieveMap(viewer);
    }





}

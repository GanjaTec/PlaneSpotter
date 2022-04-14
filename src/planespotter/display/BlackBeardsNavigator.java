package planespotter.display;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import planespotter.constants.GUIConstants;
import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.Position;
import planespotter.model.DBOut;

import javax.print.attribute.HashAttributeSet;
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
public class BlackBeardsNavigator extends Thread {

    /**
     * class variables
     */
    private static GUI gui;
    // may be changed to volatile in the future
    public static HashMap<Position, Integer> shownFlights = new HashMap<Position, Integer>();

    /**
     * thread run method TODO checken
     */
    @Override
    public void run () {
        new BlackBeardsNavigator(Controller.getGUI());
    }

    /**
     *
     */
    public BlackBeardsNavigator(GUI gui) {
        this.gui = gui;
    }

    /**
     *
     */
    public void createFlightRoute (HashMap<Integer, DataPoint> dps) {
        JMapViewer viewer = gui.createMap();
        Set<Integer> keySet = dps.keySet();
        for (int key : keySet) {
            DataPoint dp = dps.get(key);
            Position pos = dp.getPos();
            MapMarkerDot newMarker = new MapMarkerDot(pos.getLat(), pos.getLon());
            viewer.addMapMarker(newMarker);
        }

        /*int counter = 0;
        //Queue<Coordinate> coords = new ArrayDeque<>();
        for (long g : dataPoints.keySet()) {
            DataPoint dp = dataPoints.get(g);
            Position aPos = dp.getPos();
            MapMarkerDot newPlaneDot = new MapMarkerDot(aPos.getLat(), aPos.getLon());
            if (counter < dataPoints.keySet().size()-1) {
                newPlaneDot.setBackColor(Color.GREEN);
            } else {
                newPlaneDot.setBackColor(GUIConstants.DEFAULT_MAP_ICON_COLOR);
            }
            viewer.addMapMarker(newPlaneDot);
            counter++;*/
            /*if (coords.isEmpty() || coords.size() == 1) {
                coords.add(new Coordinate(aPos.getLat(), aPos.getLon()));
            }
            else {
                coords.remove();
                viewer.addMapPolygon(new MapPolygonImpl(coords.stream().toList()));
                coords.add(new Coordinate(aPos.getLat(), aPos.getLon()));
            }*/
        gui.recieveMap(viewer);
    }

    /**
     *
     */
    public void createAllFlightsMap (List<Flight> list) {
        JMapViewer viewer = gui.createMap();
        Rectangle viewSize = viewer.getVisibleRect();
        List<ICoordinate> coords = new ArrayList<>();
        for (Flight f : list) {
            // TODO: getting the last data point => where the plane is at the moment // BUG:
            //Object[] keySetArray = f.getDataPoints().keySet().toArray(); // keySetArray[keySetArray.length-1]
            int lastID = new DBOut().getLastTrackingIDByFlightID(f.getID());
            HashMap<Integer, DataPoint> dataPoints = f.getDataPoints();
            //DataPoint lastDataPoint = f.getDataPoints().get(f.getDataPoints().size()-1);
            //DataPoint lastDataPoint = (DataPoint) f.getDataPoints().values().toArray()[f.getDataPoints().size()-1];
            //length is 1 TODO fixen // NullPointerException: lastDataPoint is null
            DataPoint lastDataPoint = (DataPoint) f.getDataPoints().get(lastID);
            Position lastPos = lastDataPoint.getPos();
            // TODO: adding flight id and position to global HashMap
            shownFlights = new HashMap<>();
            shownFlights.put(lastPos, f.getID());
            // TODO: creating new Map Marker // will be optimized
            MapMarkerDot newPlaneDot = new MapMarkerDot(lastPos.getLat(), lastPos.getLon());
            newPlaneDot.setBackColor(GUIConstants.DEFAULT_MAP_ICON_COLOR);
            viewer.addMapMarker(newPlaneDot);

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

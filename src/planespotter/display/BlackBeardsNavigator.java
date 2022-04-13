package planespotter.display;

import org.openstreetmap.gui.jmapviewer.*;
import planespotter.constants.GUIConstants;
import planespotter.controller.Controller;
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
public class BlackBeardsNavigator extends Thread {

    /**
     *
     */
    private static GUI gui;

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
    public void createFlightRoute (List<Flight> flights) {
        JMapViewer viewer = gui.createMap();
        Flight f = flights.get(0);
        HashMap<Integer, DataPoint> dataPoints = f.getDataPoints();
        int counter = 0;
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
            counter++;
            /*if (coords.isEmpty() || coords.size() == 1) {
                coords.add(new Coordinate(aPos.getLat(), aPos.getLon()));
            }
            else {
                coords.remove();
                viewer.addMapPolygon(new MapPolygonImpl(coords.stream().toList()));
                coords.add(new Coordinate(aPos.getLat(), aPos.getLon()));
            }*/

        }
        gui.recieveMap(viewer);
    }

    /**
     *
     */
    public void createAllFlightsMap (List<Flight> list) {
        JMapViewer viewer = gui.createMap();
        Rectangle viewSize = viewer.getVisibleRect();
        //Queue<Coordinate> coords = new ArrayDeque<>();
        for (Flight f : list) {
            // TODO: getting the last data point => where the plane is at the moment // BUG:
            //Object[] keySetArray = f.getDataPoints().keySet().toArray(); // keySetArray[keySetArray.length-1]
            //int lastID = new DBOut(9).getLastTrackingIDByFlightID(f.getID());
            //DataPoint lastDataPoint = f.getDataPoints().get(f.getDataPoints().size()-1);
            DataPoint lastDataPoint = (DataPoint) f.getDataPoints().values().toArray()[f.getDataPoints().size()-1];
            Position lastPos = lastDataPoint.getPos();
            MapMarkerDot newPlaneDot = new MapMarkerDot(lastPos.getLat(), lastPos.getLon());
            newPlaneDot.setBackColor(GUIConstants.DEFAULT_MAP_ICON_COLOR);
            viewer.addMapMarker(newPlaneDot);
            //mapMarkers.add(newPlaneDot);

            /*if (coords.isEmpty() || coords.size() == 1) {
                coords.add(new Coordinate(lastPos.getLat(), lastPos.getLon()));
            }
            else {
                coords.remove();
           //     viewer.addMapPolygon(new MapPolygonImpl(coords.stream().toList()));
                coords.add(new Coordinate(lastPos.getLat(), lastPos.getLon()));
            }*/
        }
        gui.recieveMap(viewer);
    }





}

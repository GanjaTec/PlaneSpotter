package planespotter.display;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import planespotter.constants.GUIConstants;
import planespotter.constants.ViewType;
import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.Position;
import planespotter.model.DBOut;
import planespotter.unused.MapView;

import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * @name MapManager
 * @author jml04
 * @version 1.0
 *
 * map manager:
 *  manages the map data and contains methods which are executed on the mapView
 */
public class MapManager extends Thread {

    /**
     *
     */
    private static GUI gui;

    /**
     * thread run method TODO checken
     */
    @Override
    public void run () {
        new MapManager(Controller.getGUI());
    }

    /**
     *
     */
    public MapManager (GUI gui) {
        this.gui = gui;
    }

    /**
     *
     */
    public void createAllFlightsMap () {
        JMapViewer viewer = gui.createMap();
        List<Flight> list = null;
        try {
            list = new DBOut().getAllFlights();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Flight f : list) {
            // TODO: getting the last data point => where the plane is at the moment
            Object[] keySetArray = f.getDataPoints().keySet().toArray();
            DataPoint lastDataPoint = f.getDataPoints().get(keySetArray[keySetArray.length-1]);
            Position lastPos = lastDataPoint.getPos();
            MapMarkerDot newPlaneDot = new MapMarkerDot(lastPos.getLat(), lastPos.getLon());
            newPlaneDot.setBackColor(Color.RED);
            newPlaneDot.setColor(Color.WHITE);
            viewer.addMapMarker(newPlaneDot);
        }

        gui.recieveMap(viewer);
    }





}

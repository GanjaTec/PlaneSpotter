package planespotter.controller;

import planespotter.constants.ViewType;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.SuperData;
import planespotter.display.UserSettings;
import planespotter.model.DBOut;
import planespotter.model.OutputWizard;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static planespotter.controller.Controller.*;

public class DataMaster {

    // controller instance
    private final Controller controller = Controller.getInstance();

    /**
     * preloadedFlights queue ( thread-safe )
     * the data waits here until added to preloadedFlights
     * // TODO test {@link java.util.concurrent.ConcurrentLinkedDeque}
     */
    private static final Queue<List<? extends SuperData>> listQueue = new ConcurrentLinkedQueue<>();

    /**
     * @return tracking for a specific flight
     * @param flightID is the flight id
     */
    HashMap<Integer, DataPoint> loadTracking (int flightID) {
        return new DBOut().getTrackingByFlight(flightID);
    }

    /**
     * loads flights into the preloadedFlights list
     * works parallel ( recursive )
     * when a there are more than 50 flights to load, new ThreadedOutputWizards are created recursively
     */
    void loadFlightsParallel () {
        Controller.loading = true;
        int startID = 14000;
        int endID = UserSettings.getMaxLoadedFlights();
        int flightsPerTask = (endID-startID)/100;
        var outputWizard = new OutputWizard(ViewType.MAP_ALL, exe, 0, startID, endID, flightsPerTask);
        exe.execute(outputWizard);
        this.waitForFinish();
        this.addAllToFlights();
        controller.done();
    }

    /**
     * waits while data is loading and then adds all loaded data to the preloadedFlights list
     * // active waiting
     */
    synchronized void waitForFinish () {
        // waits until there is no running thread, then breaks
        while (true) {
            if (exe.getActiveCount() == 0) break;
        }
    }

    /**
     * adds a data from the queue preloadedFlights
     */
    private void addAllToFlights () {
       while (!listQueue.isEmpty()) { // adding all loaded lists to the main list ( listQueue is threadSafe )
            var list = (List<Flight>) listQueue.poll();
            preloadedFlights.addAll(Objects.requireNonNull(list));
        }
    }

    /**
     * adds a List of SuperData subclasses to the queue
     */
    public static void addToListQueue (List<? extends SuperData> toAdd) {
        listQueue.add(toAdd);
    }

    /**
     * @param id is the flight id to search for
     * @return a flight
     */
    public Flight flightByID (final int id) {
        try {
            return new DBOut().getFlightByID(id);
        } catch (DataNotFoundException e) {
            this.controller.errorLog("flight with the ID " + id + " doesn't exist!");
        } return null;
    }

    /**
     * @param flightID is the flight id where the tracking is from
     * @return last tracking id from a certain flight
     */
    public int lastTrackingID (final int flightID) {
        try {
            return new DBOut().getLastTrackingIDByFlightID(flightID);
        } catch (DataNotFoundException e) {
            this.controller.errorLog("flight doesn't exist or doesn't have last tracking!");
        } return -1;
    }

}


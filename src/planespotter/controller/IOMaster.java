package planespotter.controller;

import planespotter.dataclasses.Flight;
import planespotter.display.UserSettings;
import planespotter.model.DBOut;
import planespotter.model.OutputWizard;
import planespotter.throwables.DataNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static planespotter.controller.Controller.*;

public class IOMaster {

    // controller instance
    private final Controller controller = Controller.getInstance();

    // preloadedFlights queue ( thread-safe )
    private static final Queue<List<Flight>> listQueue = new ConcurrentLinkedQueue<>();

    /**
     * loads flights into the preloadedFlights list
     * works parallel ( recursive )
     * when a there are more than 50 flights to load, new ThreadedOutputWizards are created recursively
     */
    void loadFlightsParallel () {
        int startID = 12000;
        int endID = UserSettings.getMaxLoadedFlights();
        int maxFlightsPerTask = (endID-startID)/100;
        OutputWizard outputWizard;
        if (maxFlightsPerTask <= 20) {
            outputWizard = new OutputWizard(exe, 0, startID, endID, 20);
        } else {
            outputWizard = new OutputWizard(exe, 0, startID, endID, maxFlightsPerTask);
        }
        exe.execute(outputWizard);
        this.waitAndLoadAll();
        controller.done();
    }

    /**
     * waits while data is loading and then adds all loaded data to the preloadedFlights list
     */
    private void waitAndLoadAll () {
        // waits until there is no running thread, then breaks
        while (true) {
            if (exe.getActiveCount() == 0) break;
        }
        while (!listQueue.isEmpty()) { // adding all loaded lists to the main list ( listQueue is threadSafe )
            preloadedFlights.addAll(Objects.requireNonNull(listQueue.poll()));
        }
    }

    /**
     * adds a List of flights to the queue
     */
    public static void addToQueue (List<Flight> toAdd) {
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

    //TODO allWithPlanetype(type)
    // weitere

}

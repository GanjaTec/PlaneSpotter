package planespotter.controller;

import planespotter.dataclasses.Flight;
import planespotter.display.UserSettings;
import planespotter.model.DBOut;
import planespotter.model.OutputWizard;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static planespotter.controller.Controller.*;

public class IOMaster {

    // controller instance
    private Controller controller = Controller.getInstance();

    // preloadedFlights queue ( thread-safe )
    private static volatile Queue<List<Flight>> listQueue = new ConcurrentLinkedQueue<>();

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
        while (exe.getActiveCount() > 0) {
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
     *
     */
    public Flight flightByID (int id) {
        return new DBOut().getFlightByID(id);
    }

}

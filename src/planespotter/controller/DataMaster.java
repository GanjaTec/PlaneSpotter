package planespotter.controller;

import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.SuperData;
import planespotter.display.UserSettings;
import planespotter.model.DBOut;
import planespotter.model.OutputWizard;
import planespotter.throwables.DataNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
     * loads flights into the preloadedFlights list
     * works parallel ( recursive )
     * when a there are more than 50 flights to load, new ThreadedOutputWizards are created recursively
     */
    void load () {
        int startID = 0;
        int endID = new UserSettings().getMaxLoadedData();
        int dataPerTask = 5000; // testen!
        var scheduler = Controller.getScheduler();
        var outputWizard = new OutputWizard(scheduler, 0, startID, endID, dataPerTask);
        scheduler.exec(outputWizard);
        controller.waitForFinish();
        this.addAllToPre();
        controller.done();
    }

    /**
     * adds a data from the queue preloadedFlights
     */
    private void addAllToPre () {
       while (!listQueue.isEmpty()) { // adding all loaded lists to the main list ( listQueue is threadSafe )
            controller.liveData.addAll((Collection<? extends DataPoint>) listQueue.poll());
        }
    }

    /**
     * adds a List of SuperData subclasses to the queue
     */
    public void addToListQueue (List<? extends SuperData> toAdd) {
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
            this.controller.getLogger().errorLog("flight with the ID " + id + " doesn't exist!", this);
        } return null;
    }

}


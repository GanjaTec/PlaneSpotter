package planespotter.model;

import planespotter.constants.ViewType;
import planespotter.controller.Controller;
import planespotter.controller.DataMaster;
import planespotter.dataclasses.DataPoint;
import planespotter.throwables.ThreadOverheadError;

import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static planespotter.constants.GUIConstants.*;

public class OutputWizard extends DBOut implements Runnable {
    // @Nullable thread pool executor instance
    private final ThreadPoolExecutor executor;
    // ints: thread number, start-/end-id, max flights per one task, @Nullable flightID
    private final int threadNumber, from, to, flightsPerTask, flightID;
    // thread name
    private final String threadName;
    // controller instance
    private final Controller controller;
    // view type
    private final ViewType type;

    /**
     * constructor without params, but sets all vars
     */
    public OutputWizard() {
        this.type = null;
        this.executor = null;
        this.threadNumber = -1;
        this.threadName = null;
        this.from = -1;
        this.to = -1;
        this.flightsPerTask = -1;
        this.controller = Controller.getInstance();
        this.flightID = -1;
    }

    /**
     * constructor
     *
     * @param executor is the ThreadPoolExecutor, which executes the OutputWizards
     * @param tNumber is the thread number
     * @param from is the start id
     * @param to is the end id
     * @param dataPerTask is the max. number of loaded flights by one OutputWizard
     */
    public OutputWizard(ViewType type, ThreadPoolExecutor executor, int tNumber, int from, int to, int dataPerTask) {
        this.type = type;
        this.executor = executor;
        this.threadNumber = tNumber;
        this.threadName = "output-wizard" + this.threadNumber;
        this.from = from;
        this.to = to-1;
        this.flightsPerTask = dataPerTask;
        this.controller = Controller.getInstance();
        this.flightID = -1;
    }

    /**
     * constructor
     *
     * @param type is the view type
     * @param tNumber is the thread number
     * @param from is the start id
     * @param to is the end id
     * @param flightID is the flight id of the loaded flight
     */
    public OutputWizard(ViewType type, int tNumber, int from, int to, int flightID) {
        this.type = type;
        this.executor = null;
        this.threadNumber = tNumber;
        this.threadName = "output-wizard" + this.threadNumber;
        this.from = from;
        this.to = to-1;
        this.flightsPerTask = -1;
        this.controller = Controller.getInstance();
        this.flightID = flightID;
    }

    /**
     * ThreadedOutputWizard run method is executed when an
     * output wizard thread is executed by the threadPoolExecutor
     */
    @Override
    public void run () {
        long start = System.nanoTime();
        Thread.currentThread().setPriority(9);
        Thread.currentThread().setName(this.threadName);
        this.controller.log("thread " + ANSI_ORANGE + this.getName() + ANSI_RESET + " created!");
        this.loadFlights(from, to);
        this.controller.log( this.getName() + ANSI_ORANGE + "@" + ANSI_RESET + Thread.currentThread().getId() +
                            ": loaded data in " + ANSI_YELLOW + (System.nanoTime()-start)/Math.pow(1000, 3) +
                            ANSI_RESET + " seconds!");
    }

    /**
     * loads flights into the flight list queue in Controller
     *
     * @param fromID is the start-id,
     * @param toID is the (exclusive) end-id
     */
    public void loadFlights (int fromID, int toID) {
        int flightsToLoad = toID - fromID;
        if (flightsToLoad <= this.flightsPerTask) {
            var flights = super.getAllFlightsFromID(fromID, toID);
            DataMaster.addToListQueue(flights);
        } else {
            int newEndID = to-(flightsToLoad/2);
            var out0 = new OutputWizard(ViewType.MAP_ALL, this.executor, this.threadNumber+1, fromID, newEndID, this.flightsPerTask);
            var out1 = new OutputWizard(ViewType.MAP_ALL, this.executor, this.threadNumber+2, newEndID, toID, this.flightsPerTask);
            try {
                this.executor.execute(out0);
                this.executor.execute(out1);
            } catch (RejectedExecutionException e) {
                throw new ThreadOverheadError();
            }
        }
    }

    //TODO allWithPlanetype(type)
    // weitere

    /**
     * @return name of the running thread
     */
    public String getName () {
        return this.threadName;
    }

}

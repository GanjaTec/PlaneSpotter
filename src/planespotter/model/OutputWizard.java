package planespotter.model;

import planespotter.controller.Controller;
import planespotter.controller.DataMaster;
import planespotter.throwables.ThreadOverheadError;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static planespotter.constants.GUIConstants.*;

/**
 * @name OutputWizard
 * @author jml04
 * @version 1.1
 *
 * class OutputWizard can load the live data threaded or the
 */
public class OutputWizard extends DBOut implements Runnable {
    // @Nullable thread pool executor instance
    private final ThreadPoolExecutor executor;
    // ints: thread number, start-/end-id, max flights per one task, @Nullable flightID
    private final int threadNumber, from, to, flightsPerTask, flightID;
    // thread name
    private final String threadName;
    // controller instance
    private final Controller controller;

    /**
     * constructor with only executor as param for small tasks
     *
     * @param executor is the thread pool executor
     */
    public OutputWizard(ThreadPoolExecutor executor) {
        this(executor, -1, -1, -1, -1);
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
    public OutputWizard(ThreadPoolExecutor executor, int tNumber, int from, int to, int dataPerTask) {
        this.executor = executor;
        this.threadNumber = tNumber;
        this.threadName = "output-wizard" + this.threadNumber;
        this.from = from;
        this.to = to;
        this.flightsPerTask = dataPerTask;
        this.controller = Controller.getInstance();
        this.flightID = -1;
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
        this.loadLiveTrackingBtwn(from, to);
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
    public void loadLiveTrackingBtwn (int fromID, int toID) {
        int flightsToLoad = toID - fromID;
        if (flightsToLoad <= this.flightsPerTask) {
            //var flights = super.getAllFlightsBetween(fromID, toID);
            var dps = super.getLiveTrackingBetween(fromID, toID);
            DataMaster.addToListQueue(dps);
        } else {
            int newEndID = to-(flightsToLoad/2);
            var out0 = new OutputWizard(this.executor, this.threadNumber+1, fromID, newEndID, this.flightsPerTask);
            var out1 = new OutputWizard(this.executor, this.threadNumber+2, newEndID, toID, this.flightsPerTask);
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

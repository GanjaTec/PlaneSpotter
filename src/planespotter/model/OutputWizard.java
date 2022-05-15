package planespotter.model;

import planespotter.controller.Controller;
import planespotter.controller.DataMaster;
import planespotter.controller.Scheduler;
import planespotter.throwables.ThreadOverheadError;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @name OutputWizard
 * @author jml04
 * @version 1.1
 *
 * class OutputWizard can load the live data threaded or the
 */
public class OutputWizard extends DBOut implements Runnable {
    // @Nullable thread pool executor instance
    private final Scheduler scheduler;
    // ints: thread number, start-/end-id, max flights per one task, @Nullable flightID
    private final int threadNumber, from, to, flightsPerTask, flightID;
    // thread name
    private final String threadName;
    // controller instance
    private final Controller controller;

    /**
     * constructor with only executor as param for small tasks
     *
     * @param scheduler is the thread pool executor
     */
    public OutputWizard(Scheduler scheduler) {
        this(scheduler, -1, -1, -1, -1);
    }

    /**
     * constructor
     *
     * @param scheduler is the Scheduler, which executes the OutputWizards
     * @param tNumber is the thread number
     * @param from is the start id
     * @param to is the end id
     * @param dataPerTask is the max. number of loaded flights by one OutputWizard
     */
    public OutputWizard(Scheduler scheduler, int tNumber, int from, int to, int dataPerTask) {
        this.scheduler = scheduler;
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
        var thread = Thread.currentThread();
        thread.setPriority(9);
        thread.setName(this.threadName);
        long threadID = thread.getId();
        this.controller.getLogger().log("thread " + this.threadName + "@" + threadID + " created!", this);
        this.loadLiveTrackingBtwn(from, to);
        this.controller.getLogger().sucsessLog(this.threadName +  "@" + threadID +
                                                ": loaded data in " + (System.nanoTime()-start)/Math.pow(1000, 3) +
                                                " seconds!", this);
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
            new DataMaster().addToListQueue(dps);
        } else {
            int newEndID = to-(flightsToLoad/2);
            var out0 = new OutputWizard(this.scheduler, this.threadNumber+1, fromID, newEndID, this.flightsPerTask);
            var out1 = new OutputWizard(this.scheduler, this.threadNumber+2, newEndID, toID, this.flightsPerTask);
            try {
                this.scheduler.exec(out0);
                this.scheduler.exec(out1);
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

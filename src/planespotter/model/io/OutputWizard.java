package planespotter.model.io;

import planespotter.controller.Controller;
import planespotter.model.Scheduler;
import planespotter.dataclasses.DataPoint;
import planespotter.throwables.ThreadOverheadError;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;

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
    // ints: src-/end-id, max flights per one task, @Nullable flightID
    private final int from;
    private final int to;
    private final int flightsPerTask;
    private final int dataType;
    // thread id
    private long threadID;

    // controller instance
    private final Controller controller;

    // psfi data types

    public static final ConcurrentLinkedQueue<Vector<DataPoint>> dataQueue;

    static {
        dataQueue = new ConcurrentLinkedQueue<>();
    }

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
     * @param from is the src id
     * @param to is the end id
     * @param dataPerTask is the max. number of loaded flights by one OutputWizard
     */
    public OutputWizard(Scheduler scheduler, int from, int to, int dataPerTask, int dataType) {
        this.scheduler = scheduler;
        this.from = from;
        this.to = to;
        this.flightsPerTask = dataPerTask;
        this.controller = Controller.getInstance();
        this.dataType = dataType;
    }

    /**
     * ThreadedOutputWizard run method is executed when an
     * output wizard thread is executed by the threadPoolExecutor
     */
    @Override
    public void run () {
        this.threadID = Thread.currentThread().getId();
        switch (this.dataType) {
            case 0 -> this.run_liveData();
            // further cases
        }
    }

    private void run_liveData() {
        long start = System.nanoTime();
        var log = Controller.getLogger();
        log.log("thread Output-Wizard@" + this.threadID + " created!", this);
        this.loadLiveTrackingBtwn(from, to);
        double elapsed = (System.nanoTime() - start) / Math.pow(1000, 3);
        log.sucsessLog("Output-Wizard@" + this.threadID +
                                                ": loaded data in " + elapsed +
                                                " seconds!", this);
    }

    /**
     * loads flights into the flight list queue in Controller
     *
     * @param fromID is the src-id,
     * @param toID is the (exclusive) end-id
     */
    public void loadLiveTrackingBtwn (int fromID, int toID) {
        int flightsToLoad = toID - fromID;
        if (flightsToLoad <= this.flightsPerTask) {
            var dps = super.getLiveTrackingBetween(fromID, toID);
            controller.liveData.addAll(dps);
            //dataQueue.add(dps);
        } else {
            int newEndID = to-(flightsToLoad/2);
            var out0 = new OutputWizard(this.scheduler, fromID, newEndID, this.flightsPerTask, this.dataType);
            var out1 = new OutputWizard(this.scheduler, newEndID, toID, this.flightsPerTask, this.dataType);
            try {
                this.scheduler.exec(out0, "Output-Wizard", true, 9, true);
                this.scheduler.exec(out1, "Output-Wizard", true, 9, true);
            } catch (RejectedExecutionException e) {
                throw new ThreadOverheadError();
            }
        }
    }

    //TODO allWithPlanetype(type)
    // weitere

}

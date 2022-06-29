package planespotter.model;

import planespotter.controller.Scheduler;
import planespotter.display.models.SupplierDisplay;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @name Collector
 * @author jml04
 * @version 1.0
 *
 * abstract class collector is a 'Collector'-superclass.
 * a Collector represents a complete data collector which can be started and stopped.
 * Create a subclass to implement a new Collector and implement startCollecting()
 * @see planespotter.Fr24Collector
 */
public abstract class Collector {
    // insert period in seconds
    protected static final int INSERT_PERIOD_SEC;
    // runtime instance
    public static final Runtime RUNTIME;
    // monitor object for Collector and its subclasses
    protected static final Object SYNC;
    // initializer
    static {
        INSERT_PERIOD_SEC = 100;
        RUNTIME = Runtime.getRuntime();
        SYNC = new Object();
    }
    // 'paused' and 'enabled' flags
    public volatile boolean paused, enabled;
    // display, variations should be added
    protected final SupplierDisplay display; // TODO: 29.06.2022 add variations to Constructor, sth. like TYPE
    // scheduler to execute tasks
    protected Scheduler scheduler;
    // main thread instance, should be implemented in startCollecting()
    protected Thread mainThread;
    // atomic integers as Frame-, Flight- and Plane-counter
    protected final AtomicInteger insertedNow, insertedFrames,
                                  newPlanesNow, newPlanesAll,
                                  newFlightsNow, newFlightsAll;

    /**
     * collector super-constructor
     *
     * @param exitOnClose indicates if the whole program should exit
     *                    when the 'X'-button is pressed
     */
    protected Collector(boolean exitOnClose) {
        int closeOperation = (exitOnClose)
                ? WindowConstants.EXIT_ON_CLOSE
                : WindowConstants.DISPOSE_ON_CLOSE;
        this.display = new SupplierDisplay(closeOperation, this);
        this.insertedNow = new AtomicInteger(0);
        this.insertedFrames = new AtomicInteger(0);
        this.newPlanesNow = new AtomicInteger(0);
        this.newPlanesAll = new AtomicInteger(0);
        this.newFlightsNow = new AtomicInteger(0);
        this.newFlightsAll = new AtomicInteger(0);
        // setting collector flags to 'running'
        this.paused = false;
        this.enabled = true;
    }

    /**
     * this method must be implemented in every subclass.
     * it is used to start the collector, use the mainThread object
     * as your main-Thread and implement the 'data collecting task'
     * into its runnable (could be implemented in another method).
     * the Scheduler instance can be used to execute tasks in the main task.
     * here is an example how to use startCollecting:
     *
     * {@code // start code block
     * @Override
     * public void startCollecting() {
     *     super.startNewMainThread(() -> {
     *         .....................
     *         ...collecting task...
     *         .....................
     *     }),  "Collector");
     * }
     * } // end code block
     *
     * you could also create a new class implementing runnable and
     * put it into the new Thread as the collecting task
     */
    public abstract void startCollecting();

    /**
     * collector start method, starts the display and the collecting-task
     */
    public synchronized void start() {
        this.display.start();
        this.startCollecting();
    }

    /**
     * stops the collector main thread and shuts down the Scheduler,
     * must be returned in the stop() method
     *
     * @return true if collector was stopped successfully, else false
     */
    public final boolean stopCollecting() {
        this.mainThread.interrupt();
        Thread.currentThread().interrupt(); // ?
        boolean success = this.scheduler.shutdownNow() && this.mainThread.isInterrupted();
        try {
            Scheduler.sleepSec(2);
        } finally {
            System.out.println("Collector stopped successfully!");
        }
        return success;
    }

    /**
     * starts a new main thread in mainThread instance
     *
     * @param newTask is the new Task, must be a 'collecting task' as runnable (can be written as lambda)
     * @param tName is the thread name
     */
    protected void startNewMainThread(Runnable newTask, String tName) {
        this.mainThread = new Thread(newTask);
        this.mainThread.setName(tName);
        this.mainThread.start();
    }

}

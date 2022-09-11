package planespotter.model;

import org.jetbrains.annotations.NotNull;
import planespotter.display.models.SupplierDisplay;
import planespotter.model.nio.Supplier;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @name Collector
 * @author jml04
 * @version 1.0
 *
 * @description
 * abstract class collector is a 'Collector'-superclass.
 * a Collector represents a complete data collector which can be started and stopped.
 * Create a subclass to implement a new Collector and implement startCollecting()
 * @see Fr24Collector
 */
public abstract class Collector<S extends Supplier> {

    // monitor object for Collector and its subclasses
    protected static final Object SYNC;

    // static initializer
    static {
        SYNC = new Object();
    }

    // 'paused' and 'enabled' flags | is set by the display events
    private boolean paused, enabled, isSubTask;

    // main thread instance, should be implemented in startCollecting()
    private Thread mainThread;

    // supplier instance, can be every Supplier-subclass
    protected final S supplier;

    // display, variations should be added
    protected SupplierDisplay display;

    // scheduler to execute tasks
    protected Scheduler scheduler;

    // atomic integers as Frame-, Flight- and Plane-counter
    protected final AtomicInteger insertedNow, insertedFrames,
                                  newPlanesNow, newPlanesAll,
                                  newFlightsNow, newFlightsAll;

    // error queue, collects errors
    protected final Queue<Throwable> errorQueue;

    /**
     * collector super-constructor
     *
     * @param exitOnClose indicates if the whole program should exit
     *                    when the 'X'-button is pressed
     */
    protected Collector(boolean exitOnClose, @NotNull S supplier) {
        this.supplier = supplier;
        this.display = null;
        this.insertedNow = new AtomicInteger(0);
        this.insertedFrames = new AtomicInteger(0);
        this.newPlanesNow = new AtomicInteger(0);
        this.newPlanesAll = new AtomicInteger(0);
        this.newFlightsNow = new AtomicInteger(0);
        this.newFlightsAll = new AtomicInteger(0);
        this.errorQueue = new ArrayDeque<>();
        // setting collector flags to 'running'
        this.paused = false;
        this.enabled = true;
        this.isSubTask = !exitOnClose;
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
     *         .....................
     *         ...collecting task...
     *         .....................
     *      }
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
        this.scheduler = new Scheduler();
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
    protected void startNewMainThread(@NotNull Runnable newTask, @NotNull String tName) {
        this.mainThread = this.scheduler.runThread(newTask, tName, false, Scheduler.MID_PRIO);
        /*this.mainThread = new Thread(newTask);
        this.mainThread.setName(tName);
        this.mainThread.start();*/
    }

    /**
     * getter for 'paused' flag
     *
     * @return true if this {@link Collector} is paused, else false
     */
    public boolean isPaused() {
        return this.paused;
    }

    /**
     * sets the 'paused' flag
     *
     * @param paused indicates if this {@link Collector} should be paused
     * @return new 'paused' values
     */
    public boolean setPaused(boolean paused) {
        return this.paused = paused;
    }

    /**
     * getter for 'enabled' flag
     *
     * @return true if this {@link Collector} is enabled, else false
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * sets the 'enabled' flag
     *
     * @param enabled indicates if this {@link Collector} should be enabled
     * @return
     */
    public boolean setEnabled(boolean enabled) {
        return this.enabled = enabled;
    }

    /**
     * getter for 'is subtask' flag
     *
     * @return true if this {@link Collector} task is a subtask from another program
     */
    public boolean isSubTask() {
        return this.isSubTask;
    }

    /**
     * getter for the error {@link Queue}
     *
     * @return the error {@link Queue}
     */
    @NotNull
    public Queue<Throwable> getErrorQueue() {
        return errorQueue;
    }
}

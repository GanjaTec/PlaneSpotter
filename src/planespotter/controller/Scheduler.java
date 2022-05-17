package planespotter.controller;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.*;

import static planespotter.constants.Configuration.KEEP_ALIVE_TIME;
import static planespotter.constants.Configuration.MAX_THREADPOOL_SIZE;

/**
 * @name Scheduler
 * @author jml04
 * @version 1.0
 *
 * scheduler class contains all thread pool executors and is responsible for threading
 */
public class Scheduler {

    // ThreadPoolExecutor for thread execution in a thread pool -> package-private (only usable in controller package)
    private static final ThreadPoolExecutor exe;
    private static final ScheduledExecutorService scheduled_exe;

    static { // like static constructor
        exe = new ThreadPoolExecutor(0, MAX_THREADPOOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new SynchronousQueue<>());
        scheduled_exe = Executors.newScheduledThreadPool(0);
    }

    /**
     *
     */
    public Scheduler () {
    }

    /**
     *
     * @param task
     * @param initDelay
     * @param period
     */
    public void schedule (Runnable task, int initDelay, int period) {
        scheduled_exe.scheduleAtFixedRate(task, initDelay, period, TimeUnit.SECONDS);
    }

    /**
     *
     * @param task
     */
    public void exec (Runnable task) {
        this.exec(task, null);
    }

    /**
     *
     * @param task
     * @param tName
     */
    public void exec (Runnable task, String tName) {
        var thread = new Thread(task);
        if (tName != null) {
            thread.setName(tName);
        }
        exe.execute(thread);
        Controller.getWatchDog().watch(thread);
    }

    /**
     *
     * @param target
     * @param name
     */
    public void runAsThread(Runnable target, String name, boolean daemon) {
        var thread = new Thread(target);
        thread.setDaemon(daemon);
        thread.setName(name);
        thread.start();
    }

    /**
     *
     * @param targets
     */
    public synchronized void cancel (@NotNull Runnable... targets) {
        var ctrl = Controller.getInstance();
        Controller.getLogger().infoLog("Task cancelled!", this);
        Arrays.stream(targets)
                .forEach(exe::remove);
    }

    public void interruptThread (Thread target) {
        if (!target.isAlive()) {
            target.interrupt();
        }
        if (!target.isInterrupted()) {
            Controller.getLogger().infoLog("Couldn't interrupt thread " + target.getName(), this);
        } else {
            Controller.getLogger().infoLog("Thread " + target.getName() + " interrupted!", this);
        }
    }

    /**
     * @return
     */
    public int active () {
        return exe.getActiveCount();
    }

    /**
     * @return
     */
    public long completed () {
        return exe.getCompletedTaskCount();
    }

    /**
     * @return
     */
    public int largestPoolSize () {
        return exe.getLargestPoolSize();
    }

}

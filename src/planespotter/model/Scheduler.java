package planespotter.model;

import org.jetbrains.annotations.NotNull;
import planespotter.controller.Controller;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static planespotter.constants.Configuration.*;

/**
 * @name Scheduler
 * @author jml04
 * @version 1.1
 *
 * scheduler class contains all thread pool executors and is responsible for threading
 */
public class Scheduler {
    // Thread priority constants
    public static final int LOW_PRIO = 2;
    public static final int MID_PRIO = 5;
    public static final int HIGH_PRIO = 9;

    // ThreadPoolExecutor for (parallel) execution of different threads
    private static final ThreadPoolExecutor exe;
    // ScheduledExecutorService for scheduled execution at a fixed rate
    private static final ScheduledExecutorService scheduled_exe;

    // static constructor
    static {
        exe = new ThreadPoolExecutor(CORE_POOLSIZE, MAX_THREADPOOL_SIZE,
                                     KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                                     new SynchronousQueue<>(), new ThreadMaker());
        scheduled_exe = Executors.newScheduledThreadPool(0);
    }

    // hash code
    private final int hashCode = System.identityHashCode(this);

    /**
     * constructor, creates a new Scheduler
     * all Schedulers use the same executors
     * for thread execution
     */
    public Scheduler() {
    }

    /**
     *
     * @param task is the Runnable to execute in period
     * @param initDelay is the src delay in seconds, must be 1 or higher
     * @param period is the period in seconds, must be 0 or higher
     */
    public void schedule(@NotNull Runnable task, int initDelay, int period) {
        if (initDelay < 0) {
            throw new IllegalArgumentException("init delay must be 0 or higher!");
        } if (period < 1) {
            throw new IllegalArgumentException("period must be 1 or higher!");
        }
        scheduled_exe.scheduleAtFixedRate(task, initDelay, period, TimeUnit.SECONDS);
    }

    /**
     * executes a single thread with custom name
     * created thread will be watched by the WatchDog
     * @update timeout is implemented in invokeAny()
     *
     * @param target is the Runnable to execute
     * @param tName is the Thread-Name
     */
    public void exec(Runnable target, String tName) {
        this.exec(target, tName, false, 5, true);
    }

    /**
     * executes a single task as a thread
     * created thread won't be watched by the WatchDog
     * !! thread runs without timeout !!
     *
     * @param target is the Runnable to execute
     * @param tName is the thread name
     * @param daemon is the value if the thread is a daemon thread
     * @param prio is the priority from 1-10
     * @param withTimeout if the task should have a timeout
     */
    public void exec(@NotNull Runnable target, String tName, boolean daemon, int prio, boolean withTimeout) {
        if (prio < 1 || prio > 10) {
            throw new IllegalArgumentException("priority must be between 1 and 10!");
        }
        ((ThreadMaker) exe.getThreadFactory()).addThreadProperties(tName, daemon, prio);
        if (withTimeout) {
            var currentThread = new AtomicReference<Thread>();
            CompletableFuture.runAsync(target, exe)
                    .orTimeout(10, TimeUnit.SECONDS)
                    .exceptionally(e -> {
                        e.printStackTrace();
                        this.interruptThread(currentThread.get());
                        // FIXME: 22.05.2022 interrupt läuft noch nicht
                        return null;
                    });
        } else {
            var thread = new Thread(target);
            thread.setName(tName);
            thread.setPriority(prio);
            thread.setDaemon(daemon);
            thread.start();
        }
    }

    /**
     * cancels tasks (removes them from the thread pool executor queue)
     *
     * @param targets are the tasks to cancel, min. 1
     */
    public synchronized void cancel(@NotNull Runnable... targets) {
        if (targets.length == 0) {
            throw new IllegalArgumentException("No targets to cancel");
        }
        Controller.getLogger().infoLog("Task cancelled!", this);
        Arrays.stream(targets)
                .forEach(exe::remove);
    }

    /**
     * tries to interrupt a certain thread
     *
     * @param target is the thread to interrupt
     */
    public void interruptThread(@NotNull Thread target) {
        var log = Controller.getLogger();
        if (target.isAlive()) {
            target.interrupt();
            if (!target.isInterrupted()) {
                log.errorLog("Couldn't interrupt thread " + target.getName(), this);
            } else {
                log.infoLog("Thread " + target.getName() + " interrupted!", this);
            }
        } else {
            log.infoLog("Thread " + target.getName() + " is already dead!", this);
        }
    }

    /**
     * @return active thread count from exe-ThreadPoolExecutor,
     *         not from the scheduled executor
     */
    public int active() {
        return exe.getActiveCount();
    }

    /**
     * @return completed task count from exe-ThreadPoolExecutor,
     *         not from the scheduled executor
     */
    public long completed() {
        return exe.getCompletedTaskCount();
    }

    /**
     * @return largest thread pool size from exe-ThreadPoolExecutor,
     *         not from the scheduled executor
     */
    public int largestPoolSize() {
        return exe.getLargestPoolSize();
    }

    /**
     * @return scheduler hash code
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

}

package planespotter.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

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

    // thread maker (thread factory)
    private static final ThreadMaker threadMaker;
    // ThreadPoolExecutor for (parallel) execution of different threads
    private static final ThreadPoolExecutor exe;
    // ScheduledExecutorService for scheduled execution at a fixed rate
    private static final ScheduledExecutorService scheduled_exe;

    // static constructor
    static {
        threadMaker = new ThreadMaker();
        exe = new ThreadPoolExecutor(CORE_POOLSIZE, MAX_THREADPOOL_SIZE,
                                     KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                                     new SynchronousQueue<>(), threadMaker);
        scheduled_exe = new ScheduledThreadPoolExecutor(0, threadMaker);
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
     * executed a thread with a certain name
     *
     * @param tName is the thread name
     * @param task is the Runnable to execute in period
     * @param initDelay is the src delay in seconds, must be 1 or higher
     * @param period is the period in seconds, must be 0 or higher
     */
    public final Scheduler schedule(@NotNull Runnable task, @NotNull String tName, int initDelay, int period) {
        this.schedule(() -> {
            Thread.currentThread().setName(tName);
            task.run();
        }, initDelay, period);
        return this;
    }

    /**
     *
     * @param task is the Runnable to execute in period
     * @param initDelay is the src delay in seconds, must be 1 or higher
     * @param period is the period in seconds, must be 0 or higher
     */
    public final Scheduler schedule(@NotNull Runnable task, int initDelay, int period) {
        if (initDelay < 0) {
            throw new IllegalArgumentException("init delay out of range! must be 0 or higher!");
        } if (period < 1) {
            throw new IllegalArgumentException("period out of range! must be 1 or higher!");
        }
        scheduled_exe.scheduleAtFixedRate(task, initDelay, period, TimeUnit.SECONDS);
        return this;
    }

    /**
     * executes a single thread with custom name
     * created thread will be watched by the WatchDog
     *
     * @param target is the Runnable to execute
     * @param tName is the Thread-Name
     */
    public final Scheduler exec(@NotNull Runnable target, @NotNull String tName) {
        return this.exec(target, tName, false, 5, true);
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
    public final Scheduler exec(@NotNull Runnable target, @NotNull String tName, boolean daemon, int prio, boolean withTimeout) {
        if (prio < 1 || prio > 10) {
            throw new IllegalArgumentException("priority must be between 1 and 10!");
        }
        this.getThreadFactory().addThreadProperties(tName, daemon, prio);
        if (withTimeout) {
            var currentThread = new AtomicReference<Thread>();
            CompletableFuture.runAsync(target, exe)
                    .orTimeout(15, TimeUnit.SECONDS)
                    .exceptionally(e -> {
                        Controller.getInstance().handleException(e);
                        this.interruptThread(currentThread.get());
                        // FIXME: 22.05.2022 interrupt läuft noch nicht
                        return null;
                    });
        } else {
            CompletableFuture.runAsync(target, exe);
        }
        return this;
    }

    /**
     *
     *
     * @param target is the target runnable to execute
     * @param tName is the thread name
     * @param daemon is the daemon flag
     * @param prio is the priority from 1-10
     */
    public Thread runThread(@NotNull Runnable target, String tName, boolean daemon, @Range(from = 1, to = 10) int prio) {
        var thread = new Thread(target);
        thread.setName(String.valueOf(tName));
        thread.setPriority(prio);
        thread.setDaemon(daemon);
        thread.start();
        return thread;
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
    public synchronized void interruptThread(@NotNull Thread target) {
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
     * shuts down the Scheduler
     *
     * @return true if the shutdown was successfully
     */
    public synchronized boolean shutdown() {
        boolean success = false;
        try {
            success = exe.awaitTermination(10, TimeUnit.SECONDS);
            success = scheduled_exe.awaitTermination(10, TimeUnit.SECONDS);
            return success;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    /**
     * @return the thread maker which contains a method to add thread properties
     */
    public ThreadMaker getThreadFactory() {
        return threadMaker;
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

    /**
     * @name ThreadMaker
     * @author jml04
     * @version 1.0
     *
     * ThreadMaker is a custom ThreadFactory which is able to set
     * thread properties like name, daemon or priority
     */
    private static class ThreadMaker implements ThreadFactory {

        // thread name
        private volatile String name;
        // daemon thread?
        private volatile boolean daemon = false;
        // thread priority
        private volatile int priority = -1;

        /**
         * creates a new thread with custom properties
         * and a custom UncaughtExceptionHandler
         *
         * @param r is the thread target (the executed action)
         * @return new Thread with custom properties
         */
        @Override
        public Thread newThread(@NotNull Runnable r) {
            var thread = new Thread(r);
            this.setThreadProperties(thread);
            thread.setUncaughtExceptionHandler((t, e) -> { // t is the thread, e is the exception
                e.printStackTrace();
                Controller.getLogger().errorLog("Exception " + e.getMessage() +
                                " occured in thread " + t.getName(),
                                Controller.getInstance());
            });
            return thread;
        }

        /**
         * adds thread properties parameters to the class fields
         *
         * @param name is the thread name
         * @param daemon should the thread be a daemon thread?
         * @param prio is the thread priority
         */
        public synchronized void addThreadProperties(@NotNull String name, boolean daemon, int prio) {
            if (prio < 1 || prio > 10) {
                throw new IllegalArgumentException("Thread priority out of range! (1-10)");
            }
            this.name = name;
            this.daemon = daemon;
            this.priority = prio;
        }

        /**
         * sets the thread properties on a certain thread, if they are valid
         *
         * @param target is the target thread on which properties are set
         */
        private synchronized void setThreadProperties(@NotNull Thread target) {
            if (this.name != null) {
                target.setName(this.name);
            } if (this.priority != -1) {
                target.setPriority(this.priority);
            }
            target.setDaemon(this.daemon);
        }

    }
}

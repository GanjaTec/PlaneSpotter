package planespotter.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import planespotter.constants.Configuration;
import planespotter.throwables.OutOfRangeException;
import planespotter.util.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @name Scheduler
 * @author jml04
 * @version 1.1
 *
 * Scheduler class contains all thread pool executors and is responsible for threading.
 * It is able to execute tasks once and in period,
 * hold it static if you need only one instance, else use multiple instances
 * which can be started and stopped parallel.
 * @see Controller
 * @see ThreadMaker
 * @see ThreadPoolExecutor
 * @see ScheduledExecutorService
 * @see java.util.concurrent
 */
public class Scheduler {
    /**
     * Thread priority constants:
     *  LOW_PRIO is 2,
     *  MID_PRIO is 5,
     *  HIGH_PRIO is 9
     */
    public static final int LOW_PRIO = 2, MID_PRIO = 5, HIGH_PRIO = 9;

    // thread maker (thread factory)
    private static final ThreadMaker threadMaker;
    // static initializer
    static {
        threadMaker = new ThreadMaker();
    }
    // ThreadPoolExecutor for (parallel) execution of different threads
    private final ThreadPoolExecutor exe;
    // ScheduledExecutorService for scheduled execution at a fixed rate
    private final ScheduledExecutorService scheduled_exe;
    // hash code
    private final int hashCode = System.identityHashCode(this);

    /**
     * constructor, creates a new Scheduler with MAX_THREADPOOL_SIZE
     * constant as max. ThreadPool-size
     */
    public Scheduler() {
        this.exe = new ThreadPoolExecutor(Configuration.CORE_POOLSIZE, Configuration.MAX_THREADPOOL_SIZE,
                                          Configuration.KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                                          new SynchronousQueue<>(), threadMaker);
        this.scheduled_exe = new ScheduledThreadPoolExecutor(0, threadMaker);
    }

    /**
     * second Scheduler constructor with specific pool size
     *
     * @param maxPoolsize is the max. ThreadPool-size
     */
    public Scheduler(int maxPoolsize) {
        this.exe = new ThreadPoolExecutor(Configuration.CORE_POOLSIZE, maxPoolsize,
                Configuration.KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new SynchronousQueue<>(), threadMaker);
        this.scheduled_exe = new ScheduledThreadPoolExecutor(0, threadMaker);
    }

    /**
     * lets the current Thread sleep for a certain amount of seconds
     *
     * @param seconds are the seconds to wait
     * @return always false
     */
    public static boolean sleepSec(long seconds) {
        return sleep(seconds * 1000);
    }

    /**
     * lets the current thread sleep for a certain amount of milliseconds
     *
     * @param millis are the millis to sleep
     * @return always false
     */
    public static boolean sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            System.err.println("Scheduler: sleep interrupted!");
        }
        return false;
    }

    /**
     * executed a thread with a certain name and period
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
     * executes a task in a specific period
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
        this.scheduled_exe.scheduleAtFixedRate(task, initDelay, period, TimeUnit.SECONDS);
        return this;
    }

    /**
     * executes a single thread with custom name
     * executed tasks run with timeout
     *
     * @param target is the Runnable to execute
     * @param tName is the Thread-Name
     */
    @NotNull
    public final CompletableFuture<Void> exec(@NotNull Runnable target, @NotNull String tName) {
        return this.exec(target, tName, false, 5, true);
    }

    /**
     * executes a single task as a thread
     *  @param target is the Runnable to execute
     * @param tName is the thread name
     * @param daemon is the value if the thread is a daemon thread
     * @param prio is the priority from 1-10
     * @param withTimeout if the task should have a timeout
     * @return
     */
    @NotNull
    public final CompletableFuture<Void> exec(@NotNull Runnable target, @NotNull String tName, boolean daemon, int prio, boolean withTimeout) {
        if (prio < 1 || prio > 10) {
            throw new IllegalArgumentException("priority must be between 1 and 10!");
        }
        this.getThreadFactory().addThreadProperties(tName, daemon, prio);
        if (withTimeout) {
            var currentThread = new AtomicReference<Thread>();
            return CompletableFuture.runAsync(target, this.exe)
                    .orTimeout(15, TimeUnit.SECONDS)
                    .exceptionally(e -> {
                        Controller.getInstance().handleException(e);
                        this.interruptThread(currentThread.get());
                        // FIXME: 22.05.2022 interrupt läuft noch nicht
                        return null;
                    });
        } else {
            return CompletableFuture.runAsync(target, this.exe);
        }
    }

    /**
     * runs a task as a new Thread, but not with the ThreadPoolExecutor
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

    public void await(@NotNull Runnable r) {

    }

    /**
     * tries to interrupt a certain thread
     *
     * @param target is the thread to interrupt
     */
    public synchronized void interruptThread(@NotNull Thread target) {
        Logger log = Controller.getInstance().getLogger();
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
    public synchronized boolean shutdown(final int timeout) {
        try {
            final var sec = TimeUnit.SECONDS;
            return this.exe.awaitTermination(timeout, sec) && this.scheduled_exe.awaitTermination(timeout, sec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * shuts down the Scheduler directly
     */
    public synchronized boolean shutdownNow() {
        this.exe.shutdownNow();
        this.scheduled_exe.shutdownNow();
        sleep(1000);
        return (this.exe.isTerminated() || this.exe.isTerminating()) && this.scheduled_exe.isTerminated();
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
        return this.exe.getActiveCount();
    }

    /**
     * @return completed task count from exe-ThreadPoolExecutor,
     *         not from the scheduled executor
     */
    public long completed() {
        return this.exe.getCompletedTaskCount();
    }

    /**
     * @return largest thread pool size from exe-ThreadPoolExecutor,
     *         not from the scheduled executor
     */
    public int largestPoolSize() {
        return this.exe.getLargestPoolSize();
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
            Thread thread = new Thread(r);
            this.setThreadProperties(thread);
            thread.setUncaughtExceptionHandler((t, e) -> { // t is the thread, e is the exception
                Logger log = Controller.getInstance().getLogger();
                e.printStackTrace();
                log.errorLog("Exception " + e.getMessage() +
                                " occured in thread " + t.getName(),
                                Controller.getInstance());
                Controller.getInstance().handleException(e);
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
                throw new OutOfRangeException("Thread priority out of range! (1-10)");
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

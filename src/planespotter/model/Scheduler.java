package planespotter.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import planespotter.constants.Configuration;
import planespotter.controller.Controller;
import planespotter.throwables.OutOfRangeException;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @name Scheduler
 * @author jml04
 * @version 1.1
 *
 * @description
 * Scheduler class contains all thread pool executors and is responsible for threading.
 * It is able to execute tasks once and in period,
 * hold it static if you need only one instance, else use multiple instances
 * which can be started and stopped parallel.
 * @see planespotter.controller.Controller
 * @see planespotter.model.Scheduler.ThreadMaker
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledExecutorService
 */
public class Scheduler {
    /**
     * Thread priority constants:
     *  LOW_PRIO is 2,
     *  MID_PRIO is 5,
     *  HIGH_PRIO is 9
     */
    public static final byte LOW_PRIO, MID_PRIO, HIGH_PRIO;

    // thread maker (thread factory)
    @NotNull private static final ThreadMaker threadMaker;

    // handler for rejected executions
    @NotNull private static final RejectedExecutionHandler rejectedExecutionHandler;

    // initializing static fields
    static {
        LOW_PRIO = 2;
        MID_PRIO = 5;
        HIGH_PRIO = 9;
        threadMaker = new ThreadMaker();
        rejectedExecutionHandler = (r, exe) -> System.out.println("Task " + r.toString() + " rejected from Scheduler!");
    }

    // ThreadPoolExecutor for (parallel) execution of different threads
    @NotNull private final ThreadPoolExecutor exe;

    // ScheduledExecutorService for scheduled execution at a fixed rate
    @NotNull private final ScheduledExecutorService scheduled_exe;

    // hash code
    private final int hashCode = System.identityHashCode(this);

    /**
     * constructor, creates a new Scheduler with MAX_THREADPOOL_SIZE
     * constant as max. ThreadPool-size
     */
    public Scheduler() {
        this(Configuration.MAX_THREADPOOL_SIZE);
    }

    /**
     * second Scheduler constructor with specific pool size
     *
     * @param maxPoolsize is the max. ThreadPool-size
     */
    public Scheduler(int maxPoolsize) {
        this.exe = new ThreadPoolExecutor(Configuration.CORE_POOLSIZE, maxPoolsize, Configuration.KEEP_ALIVE_TIME,
                                          TimeUnit.SECONDS, new SynchronousQueue<>(), threadMaker, rejectedExecutionHandler);
        this.scheduled_exe = new ScheduledThreadPoolExecutor(0, threadMaker, rejectedExecutionHandler);
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
            return true;
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
    @NotNull
    public final ScheduledFuture<?> schedule(@NotNull Runnable task, @NotNull String tName, int initDelay, int period) {
        return this.schedule(() -> {
            Thread.currentThread().setName(tName);
            task.run();
        }, initDelay, period);
    }

    /**
     * executes a task in a specific period
     *
     * @param task is the Runnable to execute in period
     * @param initDelay is the src delay in seconds, must be 1 or higher
     * @param period is the period in seconds, must be 0 or higher
     */
    @NotNull
    public final ScheduledFuture<?> schedule(@NotNull Runnable task, int initDelay, int period) {
        if (initDelay < 0) {
            throw new IllegalArgumentException("init delay out of range! must be 0 or higher!");
        } if (period < 1) {
            throw new IllegalArgumentException("period out of range! must be 1 or higher!");
        }
        return this.scheduled_exe.scheduleAtFixedRate(task, initDelay, period, TimeUnit.SECONDS);
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
     * @return the running task as {@link CompletableFuture}
     */
    @NotNull
    public final CompletableFuture<Void> exec(@NotNull Runnable target, @NotNull String tName, boolean daemon, int prio, boolean withTimeout) {
        if (prio < 1 || prio > 10) {
            throw new IllegalArgumentException("priority must be between 1 and 10!");
        }
        this.getThreadFactory().addThreadProperties(tName, daemon, prio);
        if (withTimeout) {
            AtomicReference<Thread> currentThread = new AtomicReference<>();
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
    @NotNull
    public Thread runThread(@NotNull Runnable target, @NotNull String tName, boolean daemon, @Range(from = 1, to = 10) int prio) {
        Thread thread = new Thread(target);
        thread.setName(tName);
        thread.setPriority(prio);
        thread.setDaemon(daemon);
        thread.start();
        return thread;
    }

    public void await(@NotNull Thread t)
            throws InterruptedException {

        t.join();
    }

    /**
     * tries to interrupt a certain thread
     *
     * @param target is the thread to interrupt
     */
    public synchronized boolean interruptThread(@NotNull Thread target) {
        if (target.isAlive()) {
            target.interrupt();
        }
        return target.isInterrupted();
    }

    /**
     * shuts down the Scheduler
     *
     * @return true if the shutdown was successfully
     */
    public synchronized boolean shutdown(final int timeout) {
        try {
            final TimeUnit sec = TimeUnit.SECONDS;
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
                e.printStackTrace();
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

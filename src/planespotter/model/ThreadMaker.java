package planespotter.model;

import org.jetbrains.annotations.NotNull;
import planespotter.controller.Controller;

import java.util.concurrent.ThreadFactory;

/**
 * @name ThreadMaker
 * @author jml04
 * @version 1.0
 *
 * ThreadMaker is a custom ThreadFactory which is able to set
 * thread properties like name, daemon or priority
 */
public class ThreadMaker implements ThreadFactory {

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

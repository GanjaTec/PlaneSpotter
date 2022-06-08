package planespotter.unused;

import org.jetbrains.annotations.Nullable;
import planespotter.controller.Controller;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * @name WatchDog
 * @author jml04
 * @version 1.1
 *
 * class watch dog is used to watch thread activities
 * @indev
 * TODO evtl. umfunktionieren-> soll statistiken über das programm anzeigen, nicht mehr threads beobachten (nicht mehr benötigt)
 */
@Deprecated(since="1.1")
public class WatchDog implements Runnable {

    private final ConcurrentLinkedQueue<Thread> watchQueue;

    private boolean onLock = false;

    public WatchDog () {
        this.watchQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        for (;;) {
            if (!this.watchQueue.isEmpty()) {
                var currentThread = this.watchQueue.poll();
                var watcher = this.watcher(currentThread, 10, TimeUnit.SECONDS);
                Controller.getScheduler().exec(watcher, "WatchDog-Watcher", false, 5, false); // daemon?
            } else {
                try {
                    synchronized (this) {
                        this.wait(100);
                    }
                    //TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void watch (Thread target) {
        this.watchQueue.add(target);
        if (this.onLock) {
            this.notify();
            this.onLock = false;
        }
    }

    private Runnable watcher (Thread target, int timeout, TimeUnit timeUnit) {
        return () -> {
            try {
                timeUnit.sleep(timeout);
                Controller.getScheduler().interruptThread(target);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * called when a critical program state is reached
     *
     * @param doNext is the task to do when a critical situation occurred
     */
    public void critical (Runnable doNext) {
        this.critical(null, doNext, null);
    }

    /**
     * called when a critical program state is reached
     *
     * @param msg is the error message
     * @param doNext is the task to do when a critical situation occurred
     * @param ref is the reference class instance (executing object)
     */
    public void critical (String msg, Runnable doNext, @Nullable Object ref) {
        if (doNext == null) {
            throw new IllegalArgumentException("Runnable may not be null!");
        }
        if (msg != null) {
            Controller.getLogger().errorLog(msg, ref);
        }
        Controller.getScheduler().exec(doNext, "WatchDog-Critical");
    }

}

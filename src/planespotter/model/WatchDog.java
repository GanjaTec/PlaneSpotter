package planespotter.model;

import org.jetbrains.annotations.Nullable;
import planespotter.controller.Controller;
import planespotter.throwables.TimeoutException;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class WatchDog implements Runnable {

    private ConcurrentLinkedQueue<Thread> watchQueue;

    private boolean onLock = false;

    public WatchDog () {
        this.watchQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        for (;;) {
            if (this.watchQueue.isEmpty()) {
                /*try {
                    onLock = true;
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            } else {
                var currentThread = this.watchQueue.poll();
                var watcher = this.watcher(currentThread, 10, TimeUnit.SECONDS);
                Controller.getScheduler().runAsThread(watcher, "WatchDog-Watcher", false); // daemon?

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
                new Utilities().timeoutTask(timeout, timeUnit);
                Controller.getScheduler().interruptThread(target);
            } catch (TimeoutException e) { // TODO: 16.05.2022 remove redundant code
                if (target.isAlive()) {
                    target.interrupt();
                    if (!target.isInterrupted()) {
                        Controller.getLogger().infoLog("Couldn't interrupt thread " + target.getName(), this);
                    }
                    Controller.getLogger().errorLog("TimeoutException caused by thread " + target.getName() +
                            ", loaded over " + timeout + " " + timeUnit, this);
                }
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

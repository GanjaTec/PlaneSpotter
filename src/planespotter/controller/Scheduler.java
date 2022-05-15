package planespotter.controller;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

import static planespotter.constants.Configuration.KEEP_ALIVE_TIME;
import static planespotter.constants.Configuration.MAX_THREADPOOL_SIZE;

public class Scheduler {

    // ThreadPoolExecutor for thread execution in a thread pool -> package-private (only usable in controller package)
    private static final ThreadPoolExecutor exe;
    private static final ScheduledExecutorService scheduled_exe;

    static { // like static constructor
        exe = new ThreadPoolExecutor(0, MAX_THREADPOOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new SynchronousQueue<>());
        scheduled_exe = Executors.newScheduledThreadPool(0);

    }

    public Scheduler () {
    }

    public void exec (Runnable task) {
        this.exec(task, null);
    }

    public void schedule (Runnable task, int initDelay, int period) {
        scheduled_exe.scheduleAtFixedRate(task, initDelay, period, TimeUnit.SECONDS);
    }

    public void exec (Runnable task, String tName) {
        var thread = new Thread(task);
        if (tName != null) {
            thread.setName(tName);
        }
        exe.execute(thread);
        Controller.getWatchDog().watch(thread);
    }

    public void runAsThread(Runnable target, String name) {
        var thread = new Thread(target);
        thread.setName(name);
        thread.start();
    }

    public synchronized void cancel (@NotNull Runnable... targets) {
        var ctrl = Controller.getInstance();
        ctrl.getLogger().infoLog("Task cancelled!", this);
        for (var r : targets) {
            if (r == null) {
                throw new NullPointerException("Task to cancel");
            }
            exe.remove(r);
        }
    }

    public int active () {
        return this.exe.getActiveCount();
    }

    public long completed () {
        return this.exe.getCompletedTaskCount();
    }

    public int largestPoolSize () {
        return this.exe.getLargestPoolSize();
    }

}

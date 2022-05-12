package planespotter.controller;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static planespotter.constants.Configuration.KEEP_ALIVE_TIME;
import static planespotter.constants.Configuration.MAX_THREADPOOL_SIZE;

public class Scheduler {

    // ThreadPoolExecutor for thread execution in a thread pool -> package-private (only usable in controller package)
    private final ThreadPoolExecutor exe;
    private final ScheduledExecutorService scheduled_exe;


    public Scheduler () {
        exe = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        scheduled_exe = Executors.newScheduledThreadPool(1); // erstmal 1, wird evtl. noch mehr
        this.exe.setKeepAliveTime(KEEP_ALIVE_TIME, TimeUnit.SECONDS);
        this.exe.setMaximumPoolSize(MAX_THREADPOOL_SIZE);
    }

    public void exec (Runnable task) {
        this.exe.execute(task);
    }

    public void schedule (Runnable task, int initDelay, int period) {
        this.scheduled_exe.scheduleAtFixedRate(task, initDelay, period, TimeUnit.SECONDS);
    }

    public void runTask (Runnable task, String tName) {
        var thread = new Thread(task);
        thread.setName(tName);
        thread.start();
    }

    public void cancel () {
        var ctrl = Controller.getInstance();
        ctrl.getLogger().infoLog("All tasks cancelled!", this);
        this.exe.purge();
        //System.exit(1); // TODO soll nicht mehr gemacht werden
        ctrl.getLogger().errorLog("FAIL!", this);
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

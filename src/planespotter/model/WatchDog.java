package planespotter.model;

import org.jetbrains.annotations.Nullable;
import planespotter.controller.Controller;
import planespotter.throwables.TimeoutException;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class WatchDog {

    private final Controller ctrl;

    public WatchDog (Controller ctrl) {
        this.ctrl = ctrl;
    }

    public void watch () {
        this.watchBoolean(this.ctrl.loading, this.ctrl.getScheduler()::cancel);
    }

    private void watchBoolean (boolean target, Runnable action) {
        try {
            var sec = TimeUnit.SECONDS;
            int erased = 0;
            for (int time = 7; time > 0;) {
                if (target) {
                    sec.sleep(time);
                    erased += time;
                    this.ctrl.getLogger().infoLog("WARNING: Controller loading for " + erased + " seconds, Timeout expected!", this);
                    if (time > 4) {
                        time -= 2;
                    } else {
                        time--;
                    }
                } else return;
            }
            throw new TimeoutException(erased);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            this.critical(e.getMessage(), action, this);
        }
    }

    public void watch (SwingWorker<Runnable, Void> target) {
        this.ctrl.getScheduler().runTask(
                () ->this.watchBoolean(this.ctrl.gui().working,
                    () -> this.critical("All Swing-Worker Tasks cancelled!",
                        //() -> target.cancel(true), this)), "WatchDog-Canceller");
                            /*() -> System.exit(1)*/() -> this.ctrl.getLogger().errorLog("FAIL!", this), this)), "WatchDog-Canceller"); // TODO soll nicht mehr gemacht werden
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
            Controller.getInstance().getLogger().errorLog(msg, ref);
        }
        Controller.getInstance().getScheduler().runTask(doNext, "WatchDog-Critical");
    }

}

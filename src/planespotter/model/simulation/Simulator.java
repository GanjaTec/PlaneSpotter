package planespotter.model.simulation;

import de.gtec.util.annotations.Unsigned;
import de.gtec.util.threading.Threading;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledFuture;

public class Simulator<T> {

    public static final int CLOSED = -1, STOPPED = 0, RUNNING = 1;

    // delay and period time in millis
    private int delay, period;
    private Simulation<T> simulation;
    private ScheduledFuture<?> future;
    private int status; // -1 = closed, 0 = stopped, 1 = running
    private Runnable onTick, onStop, onClose;

    public Simulator(@Unsigned int delay, @Unsigned int period, @NotNull Simulation<T> simulation) {
        setDelay(delay);
        setPeriod(period);
        setSimulation(simulation);
        status = STOPPED;
    }

    public void start() {
        if (status > STOPPED) {
            return;
        }
        if (onTick != null) {
            onTick.run();
        }
        future = Threading.runRepeated(() -> {
            Threading.runAsync(() -> {
                if (onTick != null) {
                    onTick.run();
                }
                if (getRemainingMillis() == 0 || !simulation.processFrame()) {
                    stop();
                }
            });
        }, delay, period);
        status = RUNNING;
    }

    public void stop() {
        if (status != RUNNING || future == null) {
            return;
        }
        if (onStop != null) {
            onStop.run();
        }
        status = STOPPED;
        future.cancel(true);
        future = null;
    }

    public void close() {
        stop();
        if (onClose != null) {
            onClose.run();
        }
        status = CLOSED;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public Simulation<T> getSimulation() {
        return simulation;
    }

    public void setSimulation(@NotNull Simulation<T> simulation) {
        this.simulation = simulation;
    }

    public int getStatus() {
        return status;
    }

    public Runnable getOnTick() {
        return onTick;
    }

    public void setOnTick(Runnable onTick) {
        this.onTick = onTick;
    }

    public Runnable getOnStop() {
        return onStop;
    }

    public void setOnStop(Runnable onStop) {
        this.onStop = onStop;
    }

    public Runnable getOnClose() {
        return onClose;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public int getRemainingMillis() {
        Simulation<T> sim = getSimulation();
        return (sim.getFrameCount() - sim.getCurrentIndex()) * period;
    }
}

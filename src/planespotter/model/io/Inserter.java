package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.model.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.nio.LiveLoader;
import planespotter.throwables.Fr24Exception;

import java.util.stream.Stream;

public class Inserter implements Runnable {

    private static final int MIN_INSERT_COUNT = 1000;

    private boolean terminated;

    private final LiveLoader liveLoader;

    public Inserter(@NotNull LiveLoader liveLoader) {
        this.terminated = false;
        this.liveLoader = liveLoader;
    }

    /**
     *
     */
    @Override
    public void run() {
        while (!this.terminated) {
            synchronized (this) {
                    Scheduler.sleep(500);
                try (Stream<Fr24Frame> fr24Frames = this.liveLoader.pollFrames(MIN_INSERT_COUNT)) {
                    DBIn.write(fr24Frames);
                } catch (final Fr24Exception ignored) {
                }
            }
        }
    }

    @NotNull
    public Inserter start() {
        this.terminated = false;
        return this;
    }

    public void stop() {
        this.terminated = true;
    }
}

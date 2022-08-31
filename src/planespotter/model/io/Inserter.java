package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.model.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.nio.LiveLoader;
import planespotter.util.Time;

import java.util.Queue;
import java.util.stream.Stream;

public class Inserter implements Runnable {

    // minimum insert count per write
    private static final int MIN_INSERT_COUNT = 1000;

    // 'terminated' flag, can be reset
    private boolean terminated;

    // LiveLoader instance, for data loading tasks
    private final LiveLoader liveLoader;

    // reference to the error queue, where all errors are added to and displayed from
    private final Queue<Throwable> errorQueue;

    public Inserter(@NotNull LiveLoader liveLoader, @NotNull Queue<Throwable> errorQueue) {
        this.terminated = false;
        this.liveLoader = liveLoader;
        this.errorQueue = errorQueue;
    }

    /**
     * The main DB-insert task. Runs permanently and tries in a synchronized-block to poll {@link Fr24Frame}s
     * from the {@link LiveLoader}-insertLater-queue. Then tries to write the polled frames into the database
     * using {@link DBIn}. Collects all thrown exceptions in a {@link java.util.Deque}, if no exception occurs,
     * one exception is polled from the queue, else the exception is added to the queue. The method gets aborted
     * when this {@link Inserter} is terminated or when the allowed exception count is reached.
     */
    @Override
    public void run() {
        while (!this.terminated) {
            synchronized (this) {
                    Scheduler.sleep(500);
                try (Stream<Fr24Frame> fr24Frames = this.liveLoader.pollFrames(MIN_INSERT_COUNT)) { // try to change to Integer.MAX_VALUE
                    DBIn.write(fr24Frames);
                } catch (final Throwable ex) {
                    this.errorQueue.add(ex);
                }
            }
        }
    }

    @NotNull
    public synchronized Inserter restart() {
        if (!this.terminated) {
            this.stop();
            try {
                this.wait(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.notify();
            }
        }
        this.terminated = false;
        return this;
    }

    public void stop() {
        this.terminated = true;
    }
}

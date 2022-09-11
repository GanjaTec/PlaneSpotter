package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.ADSBFrame;
import planespotter.dataclasses.Frame;
import planespotter.model.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.nio.DataLoader;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.stream.Stream;

public class Inserter implements Runnable {

    // the insert monitor object
    private static final Object INSERT_LOCK = new Object();

    // minimum insert count per write
    private static final int MIN_INSERT_COUNT = 1000;

    // 'terminated' flag, can be reset
    private boolean terminated;

    // LiveLoader instance, for data loading tasks
    private final DataLoader dataLoader;

    // reference to the error queue, where all errors are added to and displayed from
    private final Queue<Throwable> errorQueue;

    /**
     * constructs a new {@link Inserter} instance with {@link DataLoader} and error {@link Queue}
     *
     * @param dataLoader is the {@link DataLoader} which is used to load the data to be inserted
     * @param errorQueue is the error {@link Queue} where errors are collected and handled by the {@link planespotter.controller.Controller}
     */
    public Inserter(@NotNull DataLoader dataLoader, @NotNull Queue<Throwable> errorQueue) {
        this.terminated = false;
        this.dataLoader = dataLoader;
        this.errorQueue = errorQueue;
    }

    /**
     * The main DB-insert task. Runs permanently and tries in a synchronized-block to poll {@link Fr24Frame}s
     * from the {@link DataLoader}-data-queue. Then tries to write the polled frames into the database
     * using {@link DBIn}. Collects all thrown exceptions in a {@link java.util.Deque}, if no exception occurs,
     * one exception is polled from the queue, else the exception is added to the queue. The method gets aborted
     * when this {@link Inserter} is terminated or when the allowed exception count is reached.
     */
    @Override
    public void run() {
        while (!terminated) {
            synchronized (INSERT_LOCK) {
                    Scheduler.sleep(500);
                try (Stream<? extends Frame> frames = dataLoader.pollFrames(MIN_INSERT_COUNT)) { // try to change to Integer.MAX_VALUE
                    // writing frames to DB
                    DBIn.getDBIn().write(frames);
                } catch (final Throwable ex) {
                    Thread.onSpinWait();
                    if (!ex.getMessage().startsWith("Data-Queue is empty")) {
                        errorQueue.add(ex);
                    }
                }
            }
        }
    }

    /**
     * restarts the {@link Inserter} by stopping it, waiting for
     * 5 seconds and restarting it
     *
     * @return this {@link Inserter} instance
     */
    @NotNull
    public synchronized Inserter restart() {
        if (!terminated) {
            stop();
            Scheduler.sleepSec(5);
        }
        terminated = false;
        return this;
    }

    /**
     * stops this {@link Inserter}
     */
    public void stop() {
        terminated = true;
    }
}

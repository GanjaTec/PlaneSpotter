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
        Deque<Fr24Frame> fr24Frames = new ArrayDeque<>();
        Deque<ADSBFrame> adsbFrames = new ArrayDeque<>();
        while (!terminated) {
            synchronized (INSERT_LOCK) {
                    Scheduler.sleep(500);
                try (Stream<? extends Frame> frames = dataLoader.pollFrames(MIN_INSERT_COUNT)) { // try to change to Integer.MAX_VALUE
                    frames.forEach(frame -> {
                        if (frame instanceof Fr24Frame fr24) {
                            fr24Frames.add(fr24);
                        } else if (frame instanceof ADSBFrame adsb) {
                            adsbFrames.add(adsb);
                        }
                    });
                    DBIn dbIn = DBIn.getDBIn();
                    dbIn.writeFr24(fr24Frames);
                    //dbIn.writeADSB(adsbFrames);
                } catch (final Throwable ex) {
                    Thread.onSpinWait();
                    if (!ex.getMessage().startsWith("Data-Queue is empty")) {
                        errorQueue.add(ex);
                    }
                }
            }
        }
    }

    @NotNull
    public synchronized Inserter restart() {
        if (!terminated) {
            stop();
            Scheduler.sleepSec(5);
        }
        terminated = false;
        return this;
    }

    public void stop() {
        terminated = true;
    }
}

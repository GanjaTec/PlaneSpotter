package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.model.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.nio.LiveLoader;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.stream.Stream;

public class Inserter implements Runnable {

    // minimum insert count per write
    private static final int MIN_INSERT_COUNT = 1000;

    // 'terminated' flag, can be reset
    private boolean terminated;

    // LiveLoader instance, for data loading tasks
    private final LiveLoader liveLoader;

    // indicates, how much fails
    private final int allowedFails;

    public Inserter(@NotNull LiveLoader liveLoader, int allowedFails) {
        this.terminated = false;
        this.liveLoader = liveLoader;
        this.allowedFails = allowedFails;
    }

    /**
     *
     */
    @Override
    public void run() {
        Queue<Throwable> exceptions = new ArrayDeque<>();
        while (!this.terminated) {
            synchronized (this) {
                    Scheduler.sleep(500);
                try (Stream<Fr24Frame> fr24Frames = this.liveLoader.pollFrames(MIN_INSERT_COUNT)) { // try to change to Integer.MAX_VALUE
                    DBIn.write(fr24Frames);
                    exceptions.poll();
                    // we could poll an exception here to prevent reaching the exception limit very slow
                } catch (final Exception ex) {
                    exceptions.add(ex);
                }
            }
            if (exceptions.size() > this.allowedFails) {
                System.err.println("Allowed Fails Limit reached, printing and returning...");
                int counter = 0;
                Throwable ex;
                while (!exceptions.isEmpty()) {
                    ex = exceptions.poll();
                    System.err.println("Ex" + counter++ + ": " + ex.getMessage());
                }
                return;
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

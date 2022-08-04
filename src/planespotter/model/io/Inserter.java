package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.nio.LiveLoader;
import planespotter.throwables.Fr24Exception;

import java.util.stream.Stream;

public class Inserter implements Runnable {

    private static final int MIN_INSERT_COUNT = 500;

    private boolean terminated;

    public Inserter() {
        this.terminated = false;
    }

    /**
     *
     */
    @Override
    public void run() {
        while (!this.terminated) {
            synchronized (this) {
                //try {
                    Scheduler.sleep(500);
                /*} catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    //this.notify();
                }*/
                try (Stream<Fr24Frame> fr24Frames = LiveLoader.pollFrames(MIN_INSERT_COUNT)) {
                    DBIn.write(fr24Frames);
                } catch (final Fr24Exception ignored) {
                    System.out.println("No write");
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

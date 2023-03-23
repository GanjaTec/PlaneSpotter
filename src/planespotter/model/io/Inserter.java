package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.controller.Controller;
import planespotter.dataclasses.Fr24Frame;
import planespotter.dataclasses.Frame;
import planespotter.model.Parkable;
import planespotter.model.Scheduler;
import planespotter.model.nio.DataProcessor;
import planespotter.model.nio.client.DataUploader;
import planespotter.throwables.NoAccessException;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Inserter implements Runnable, Parkable {

    // the insert monitor object
    private static final Object INSERT_LOCK = new Object();

    // possible insert mode values
    public static final int INSERT_UNIFORM = 1, INSERT_ALL = 2, UPLOAD_DB = 4, UPLOAD_HTTP = 8;

    // minimum insert count per write
    private static final int MIN_INSERT_COUNT = 1000;

    // 'terminated' flag, can be reset
    private boolean terminated;

    // LiveLoader instance, for data loading tasks
    private final DataProcessor dataProcessor;

    // insert mask, contains one of INSERT_UNIFORM and INSERT_ALL
    private int insertMask;

    /**
     * constructs a new {@link Inserter} instance with {@link DataProcessor} and error {@link Queue}
     *
     * @param dataProcessor is the {@link DataProcessor} which is used to load the data to be inserted
     */
    public Inserter(@NotNull DataProcessor dataProcessor, int insertMask) {
        this.terminated = false;
        this.dataProcessor = dataProcessor;
        this.insertMask = insertMask;
    }

    /**
     * The main DB-insert task. Runs permanently and tries in a synchronized-block to poll {@link Fr24Frame}s
     * from the {@link DataProcessor}-data-queue. Then tries to write the polled frames into the database
     * using {@link DBIn}. Collects all thrown exceptions in a {@link java.util.Deque}, if no exception occurs,
     * one exception is polled from the queue, else the exception is added to the queue. The method gets aborted
     * when this {@link Inserter} is terminated or when the allowed exception count is reached.
     */
    @Override
    public void run() {
        DBIn dbIn = DBIn.getDBIn();
        DataUploader<Frame> restUploader = Controller.getInstance().getRestUploader();
        int pollCount = insertMask == INSERT_ALL ? Integer.MAX_VALUE : MIN_INSERT_COUNT;

        while (!terminated) {
            synchronized (INSERT_LOCK) {
                try {
                    INSERT_LOCK.wait(100);
                } catch (InterruptedException ignored) {
                }
                if (dataProcessor.getQueueSize() < 400) {
                    continue;
                }
                Stream<? extends Frame> frames = dataProcessor.pollFrames(pollCount);
                writeFrames(dbIn, restUploader, frames);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void writeFrames(DBIn dbIn, DataUploader<Frame> restUploader, Stream<? extends Frame> frames) {
        // writing frames to DB and/or to HTTP server
        if (frames != null) {
            Queue<? extends Frame> queue = frames.collect(Collectors.toCollection(ArrayDeque::new));
            if (isDBWriter()) {
                dbIn.write(queue);
            }
            if (isWebWriter()) {
                restUploader.addData((Collection<Frame>) queue);
            }
        }
    }

    /**
     * inserts all remaining data from the insertLater-queue into the DB
     *
     * @param scheduler is the Scheduler which executes tasks
     * @return inserted frames count as an int
     */
    @NotNull
    public synchronized CompletableFuture<Void> insertRemaining(@NotNull final Scheduler scheduler, @NotNull DataProcessor dataProcessor)
            throws NoAccessException {
        DBIn dbIn = DBIn.getDBIn();
        if (!dbIn.isEnabled()) {
            throw new NoAccessException("DB-Writer is disabled!");
        }
        Stream<? extends Frame> frames = dataProcessor.pollFrames(Integer.MAX_VALUE);

        return scheduler.exec(() -> dbIn.write(frames), "Insert Remaining", false, Scheduler.HIGH_PRIO, false);

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

    public int getInsertMask() {
        return insertMask;
    }

    public void setInsertMask(int insertMask) {
        this.insertMask = insertMask;
    }

    public boolean isDBWriter() {
        return (insertMask & UPLOAD_DB) == UPLOAD_DB;
    }

    public boolean isWebWriter() {
        return (insertMask & UPLOAD_HTTP) == UPLOAD_HTTP;
    }

}

package planespotter.model.io;

import planespotter.controller.Controller;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.throwables.DataNotFoundException;

import java.util.Deque;
import java.util.HashMap;

import static planespotter.model.LiveData.*;
import static planespotter.util.Time.elapsedSeconds;
import static planespotter.util.Time.nowMillis;

/**
 * @name DBWriter
 * @author jml04
 * @author Lukas
 * @version 1.0
 *
 * The DBWriter class is an important model part that is responsible for
 * filling the database with data, uses methods from LiveData and takes its data
 * from the insertLater-queue in LiveData
 * @see planespotter.model.LiveData
 */
public abstract class DBWriter {
    // empty DBWriter instance, for class reference
    private static final DBWriter instance;
    // 'data loader enabled' flag
    private static boolean enabled;
    // inserted frames counter for all writeToDB inserts
    private static int frameCount, planeCount, flightCount;
    // initializer
    static {
        frameCount = 0;
        planeCount = 0;
        flightCount = 0;
        enabled = true;
        instance = new DBWriter() {};
    }

    /**
     * writes frames to the database,
     * old strategy from @Lukas, but revised (fixed the memory problem by
     * getting all dbOut-data before the loop instead of in it)
     *
     * @param fr24Frames are the Fr24Frames to write, could be extended to '<? extends Frame>'
     * @param dbo is a DBOut Object for DB-Output
     * @param dbi is a DBIn Object for DB-Inserts
     */
    public static synchronized void write(final Deque<Fr24Frame> fr24Frames, final DBOut dbo, final DBIn dbi) {
        if (enabled) {
            long ts1 = nowMillis();
            var airlineTagsIDs = new HashMap<String, Integer>();
            var planeIcaoIDs = new HashMap<String, Integer>();
            var flightNRsIDs = new HashMap<String, Integer>();
            try {
                airlineTagsIDs = dbo.getAirlineTagsIDs();
                planeIcaoIDs = dbo.getPlaneIcaosIDs();
                flightNRsIDs = dbo.getFlightNRsWithFlightIDs();
            } catch (DataNotFoundException ignored) {
                // something doesn't exist in the DB, this is no error!
                // this usually happens when the DB has empty tables.
                // ( For example when the DB gets cleared )
            }
            int airlineID, planeID, flightID;
            boolean checkPlane, checkFlight;
            Fr24Frame frame;
            while (!fr24Frames.isEmpty()) {
                frame = fr24Frames.poll();
                // insert into planes
                airlineID = airlineTagsIDs.getOrDefault(frame.getAirline(), 1);
                planeID = planeIcaoIDs.getOrDefault(frame.getIcaoAdr(), -1);
                checkPlane = planeID > -1;
                if (!checkPlane) {
                    planeID = dbi.insertPlane(frame, airlineID);
                    increasePlaneCount();
                }
                // insert into flights
                flightID = flightNRsIDs.getOrDefault(frame.getFlightnumber(), -1);
                checkFlight = flightID > -1;
                if (!checkFlight) {
                    flightID = dbi.insertFlight(frame, planeID);
                    increaseFlightCount();
                }
                // insert into tracking
                dbi.insertTracking(frame, flightID);
                // increasing the inserted frames value
                increaseFrameCount();
            }
            System.out.println("[DBWriter] filled DB in " + elapsedSeconds(ts1) + " seconds!");
            // collecting garbage that was created during the insert
            System.gc();
        }

    }

    /**
     * inserts a certain amount of frames from the insertLater-queue into the DB
     *
     * @param scheduler is the Scheduler which executes tasks
     * @param count is the frame count that should be written to DB
     * @return inserted frames count as an int
     */
    public static synchronized int insert(final Scheduler scheduler, final int count) {
        int insertCount = 0;
        if (enabled) {
            var log = Controller.getLogger();
            log.log("Trying to insert frames...", instance);
            if (ableCollect(count)) {
                // insert live data with normal writeToDB
                var dbOut = new DBOut();
                var dbIn = new DBIn();
                var frames = pollFrames(count);

                scheduler.exec(() -> write(frames, dbOut, dbIn),
                        "DB-LiveData Writer", true, Scheduler.LOW_PRIO, true);
                insertCount += count;
            }
            if (insertCount > 0) {
                log.log("Inserting " + insertCount + " frames...", instance);
            }
        }
        return insertCount;
    }

    /**
     * inserts all remaining data from the insertLater-queue into the DB
     *
     * @param scheduler is the Scheduler which executes tasks
     * @param framesPerWrite is the frame count that should be written per one write task
     * @return inserted frames count as an int
     */
    public static synchronized int insertRemaining(final Scheduler scheduler, int framesPerWrite) {
        int inserted = 0;
        if (enabled) {
            final var dbOut = new DBOut();
            final var dbIn = new DBIn();

            while (!isEmpty()) {
                var frames = pollFrames(framesPerWrite);
                scheduler.exec(() -> write(frames, dbOut, dbIn),
                        "Inserter", false, Scheduler.HIGH_PRIO, false);
                inserted += framesPerWrite;
            }
            System.out.println("Inserting " + inserted + " frames...");
        }
        return inserted;
    }

    /**
     * @return true if the DBWriter is enabled, else false
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * enables/disables the DBWriter
     *
     * @param b is the enabled flag, [true -> enabled, false -> disabled]
     */
    public static void setEnabled(boolean b) {
        enabled = b;
    }

    /**
     * @return all inserted frames count
     */
    public static int getFrameCount() {
        return frameCount;
    }

    /**
     * @return all inserted planes count
     */
    public static int getPlaneCount() {
        return planeCount;
    }

    /**
     * @return all inserted flights count
     */
    public static int getFlightCount() {
        return flightCount;
    }

    /**
     * increases the 'all frame count' by 1
     */
    private static synchronized void increaseFrameCount() {
        frameCount++;
    }

    /**
     * increases the 'all planes count' by 1
     */
    private static synchronized void increasePlaneCount() {
        planeCount++;
    }

    /**
     * increases the 'all flights count' by 1
     */
    private static synchronized void increaseFlightCount() {
        flightCount++;
    }
}

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
    }

    static {
        enabled = true;
        instance = new DBWriter() {};
    }

    /**
     * writes frames to the database
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

    public static synchronized int insert(final Scheduler scheduler, final int count) {
        int insertCount = 0;
        if (enabled) {
            var log = Controller.getLogger();
            log.log("Trying to insert frames...", instance);
            if (ableCollect(count)) {
                // insert live data with normal writeToDB
                var frames = pollFrames(count);
                scheduler.exec(() -> write(frames, new DBOut(), new DBIn()),
                        "DB-LiveData Writer", true, 2, true);
                insertCount += count;
            }
            if (insertCount > 0) {
                log.log("Inserting " + insertCount + " frames...", instance);
            }
        }
        return insertCount;
    }

    public static synchronized int insertRemaining(final Scheduler scheduler) {
        int inserted = 0;
        if (enabled) {
            final var log = Controller.getLogger();
            final var dbOut = new DBOut();
            final var dbIn = new DBIn();
            log.log("Trying to insert last live data...", instance);
            //var gui = Controller.getGUI();
            //gui.getContainer("window").setVisible(false);
            while (!isEmpty() /*&& inserted < 5000*/) {
                var frames = pollFrames(500);
                scheduler.exec(() -> write(frames, dbOut, dbIn),
                        "Inserter", false, 9, false);
                inserted += 500;
            }

            log.log("Inserted " + inserted + " frames!", instance);
            System.out.println("Inserted " + inserted + " frames!");
        }
        return inserted;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
    }

    public static int getFrameCount() {
        return frameCount;
    }

    public static int getPlaneCount() {
        return planeCount;
    }

    public static int getFlightCount() {
        return flightCount;
    }

    private static synchronized void increaseFrameCount() {
        frameCount++;
    }

    private static synchronized void increasePlaneCount() {
        planeCount++;
    }

    private static synchronized void increaseFlightCount() {
        flightCount++;
    }
}

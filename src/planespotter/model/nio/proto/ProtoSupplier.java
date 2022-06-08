package planespotter.model.nio.proto;

import planespotter.constants.SQLQueries;
import planespotter.dataclasses.Frame;
import planespotter.model.io.DBOut;
import planespotter.unused.DefaultObject;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static planespotter.util.Time.*;

/**
 * @name SupplierPrototype
 * @author jml04
 * @author Lukas
 * @author Bennet
 * @version 1.0
 *
 * class ProtoSupplier represents a Prototype-Supplier which
 * is able to deserialize Fr24-Data to Frames and write them to DB
 * note: this supplier is much faster than the old one,
 *       improved in-loop I/O-Statements,
 *       the String-magic is no 'magic' anymore, working with fromJson
 */
// FIXME: 04.06.2022 Daten werden eventuell noch mit falschen IDs eingefügt, checken!
public class ProtoSupplier extends DBManager implements Runnable {

    private static volatile boolean running = false;

    private final ProtoDeserializer deserializer;
    private final ProtoKeeper keeper;

    public ProtoSupplier(ProtoDeserializer deserializer, ProtoKeeper keeper) {
        this.deserializer = deserializer;
        this.keeper = keeper;
    }

    @Override
    public void run() {
        if (!running) {
            running = true;
            var exe = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
            exe.setKeepAliveTime(4L, TimeUnit.SECONDS);
            // collecting all areas
            var areas = this.deserializer.getAllAreas();
            // grabbing data from Fr24 and deserializing to Frames
            var frames = this.deserializer.runSuppliers(areas, exe);
            // writing the frames to DB
            this.writeToDB(frames, new DBOut());
            try {
                while (writing) {
                    synchronized (this) {
                        this.wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            exe.execute(this.keeper);
            exe.shutdown();
            running = false;
        } /*else {
            try {
                int sec = 0;
                while (running) {
                    synchronized (lock) {
                        lock.wait(1000);
                        sec++;
                    }
                }
                lock.notify(); // woanders
                if (sec < 60) {
                    this.run();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }

    public void writeToDB(Deque<Frame> frames, DBOut dbOut) {
        long writeStartMillis = nowMillis();
        System.out.println("Writing DB-Data...");

        var writerThread = new Thread(() -> {
            writing = true;
            try {
                var conn = connect();
                var stmts = this.createWriteStatements(conn, frames, dbOut);
                assert stmts != null;
                executeSQL(conn, stmts);
            } catch (Exception e) { // TODO ACHTUNG, auch SQLException
                e.printStackTrace();
            }
            writing = false;
        });
        writerThread.setName("DB-Writer");
        writerThread.setPriority(9);  // (?)
        writerThread.start();
        while (writerThread.isAlive()) {
            System.out.print(":");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
        System.out.println("DB filled in " + elapsedSeconds(writeStartMillis) + " seconds!");

    }

    public PreparedStatement[] createWriteStatements(Connection conn, Deque<Frame> frames, DBOut dbo) {
        try {
            var planeQuery = conn.prepareStatement(SQLQueries.planequerry);
            var flightQuery = conn.prepareStatement(SQLQueries.flightquerry);
            var trackingQuery = conn.prepareStatement(SQLQueries.trackingquerry);

            final var icaoIDMap = dbo.icaoIDMap();
            final int[] planeTableSize = { dbo.getTableSize("planes") };
            final int[] flightTableSize = { dbo.getTableSize("flights") };

            frames.forEach(f -> {
                int planeID = planeTableSize[0],
                    flightID = flightTableSize[0];
                boolean containsPlane = false;
                assert icaoIDMap != null;
                if (!icaoIDMap.isEmpty()) {
                    containsPlane = icaoIDMap.containsKey(f.getIcaoAdr());
                    if (containsPlane) {
                        int[] frameValues = icaoIDMap.get(f.getIcaoAdr());
                        flightID = frameValues[1];
                    }
                }
                try {
                    if (!containsPlane) {
                        planeTableSize[0]++;
                        flightTableSize[0]++;
                        // adding plane insert queries
                        synchronized (this) {
                            planeQuery.setString(1, f.getIcaoAdr());
                            planeQuery.setString(2, f.getTailnr());
                            planeQuery.setString(3, f.getRegistration());
                            planeQuery.setString(4, f.getPlanetype());
                            planeQuery.setString(5, DefaultObject.DEFAULT_AIRLINE.iataTag());
                            planeQuery.addBatch();
                            planeQuery.clearParameters();
                        }
                        // adding flight insert queries
                        // TODO: 03.06.2022 contains flight checken
                        synchronized (this) {
                            flightQuery.setInt(1, planeID);
                            flightQuery.setString(2, f.getSrcAirport());
                            flightQuery.setString(3, f.getDestAirport());
                            flightQuery.setString(4, f.getFlightnumber());
                            flightQuery.setString(5, f.getCallsign());
                            flightQuery.setLong(6, f.getTimestamp());
                            flightQuery.addBatch();
                            flightQuery.clearParameters();
                        }
                    }
                    // adding tracking insert queries
                    synchronized (this) {
                        trackingQuery.setInt(1, flightID);
                        trackingQuery.setDouble(2, f.getLat());
                        trackingQuery.setDouble(3, f.getLon());
                        trackingQuery.setInt(4, f.getAltitude());
                        trackingQuery.setInt(5, f.getGroundspeed());
                        trackingQuery.setInt(6, f.getHeading());
                        trackingQuery.setInt(7, f.getSquawk());
                        trackingQuery.setLong(8, f.getTimestamp());
                        trackingQuery.addBatch();
                        trackingQuery.clearParameters();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            return new PreparedStatement[] {
                    planeQuery, flightQuery, trackingQuery
            };

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

package planespotter.unused;

import org.jetbrains.annotations.TestOnly;
import planespotter.constants.Areas;
import planespotter.constants.SQLQueries;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.io.DBConnector;
import planespotter.model.io.DBOut;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.FastKeeper;
import planespotter.model.nio.Fr24Supplier;
import planespotter.throwables.DataNotFoundException;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
 * note: improved in-loop I/O-Statements,
 *       the String-magic is no 'magic' anymore, working with fromJson
 *
 * ... doesn't work well yet, will probably not be used
 */
// FIXME: 04.06.2022 Daten werden eventuell noch mit falschen IDs eingefügt, checken!

@TestOnly
@Deprecated(since = "writeToDB-update")
public class ProtoSupplier extends DBConnector implements Runnable {

    private static volatile boolean running = false;

    private final Fr24Deserializer deserializer;
    private final FastKeeper keeper;

    public ProtoSupplier(Fr24Deserializer deserializer, FastKeeper keeper) {
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
            var areas = Areas.getAllAreas();
            // grabbing data from Fr24 and deserializing to Frames
            var frames = new Fr24Supplier().getFrames(areas, this.deserializer, new Scheduler());
            // writing the frames to DB
            this.writeToDB(frames, new DBOut());
            /*try {
                while (busyLock) {
                    synchronized (this) {
                        this.wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            exe.execute(this.keeper);
            exe.shutdown();
            running = false;
        }
    }

    public void writeToDB(Deque<Fr24Frame> fr24Frames, DBOut dbOut) {
        long writeStartMillis = nowMillis();
        System.out.println("Writing DB-Data...");

        var writerThread = new Thread(() -> {
            try {
                var conn = getDBConnection();
                /*var newPlanes = this.insertPlanes(conn, frames, dbOut);
                var newFlights = this.insertFlights(conn, frames, dbOut, newPlanes);
                this.insertTracking(conn, frames, dbOut, newFlights);
                conn.close();*/
                var stmts = this.createWriteStatements(conn, fr24Frames, dbOut);
                assert stmts != null;
                executeSQL(conn, stmts);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //busyLock.set(false);
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

    public HashMap<String, Integer> insertPlanes(Connection conn, Deque<Fr24Frame> fr24Frames, DBOut dbo) {
        var map = new HashMap<String, Integer>();
        try {
            final var planeQuery = conn.prepareStatement(SQLQueries.planequerry);
            final var icaoIDMap = dbo.icaoIDMap(); //TODO andere Methode / ohne FlightIDs

            fr24Frames.forEach(f -> {
                boolean containsPlane;
                if (!icaoIDMap.isEmpty()) {
                    containsPlane = icaoIDMap.containsKey(f.getIcaoAdr());
                    if (!containsPlane) {
                        try {
                            planeQuery.setString(1, f.getIcaoAdr());
                            planeQuery.setString(2, f.getTailnr());
                            planeQuery.setString(3, f.getRegistration());
                            planeQuery.setString(4, f.getPlanetype());
                            planeQuery.setString(5, DefaultObject.DEFAULT_AIRLINE.iataTag());
                            planeQuery.addBatch();
                            planeQuery.clearParameters();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            var newIDs = Arrays.stream(executeSQL(planeQuery))
                    .boxed()
                    .collect(Collectors.toCollection(ArrayDeque::new));

            var ids = dbo.getAllPlaneIDs();

            var icaosByPlaneIDs = dbo.getICAOsByPlaneIDs(ids);

            while (!newIDs.isEmpty()) {
                map.put(icaosByPlaneIDs.poll(), newIDs.poll());
            }
            return map;

        } catch (SQLException | DataNotFoundException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    public HashMap<String, Integer> insertFlights(Connection conn, Deque<Fr24Frame> fr24Frames, DBOut dbo, HashMap<String, Integer> icaosPlaneIDs) {
        try {
            var flightQuery = conn.prepareStatement(SQLQueries.flightquerry);
            var fnrsAndPids = dbo.getFlightNRsWithFlightIDs();
            //var icaoIdMap = dbo.icaoIDMap();

            fr24Frames.forEach(f -> {
                boolean containsFlight;
                assert fnrsAndPids != null;
                if (!fnrsAndPids.isEmpty()) {
                    containsFlight = fnrsAndPids.containsKey(f.getFlightnumber());
                    if (!containsFlight) {
                        try {
                            int planeID = icaosPlaneIDs.get(f.getIcaoAdr()); // changed
                            flightQuery.setInt(1, planeID);
                            flightQuery.setString(2, f.getSrcAirport());
                            flightQuery.setString(3, f.getDestAirport());
                            flightQuery.setString(4, f.getFlightnumber());
                            flightQuery.setString(5, f.getCallsign());
                            flightQuery.setLong(6, f.getTimestamp());
                            flightQuery.addBatch();
                            flightQuery.clearParameters();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            var newIDs = Arrays.stream(executeSQL(flightQuery))
                    .boxed()
                    .collect(Collectors.toCollection(ArrayDeque::new));

            var allFlightIDs = dbo.getAllFlightIDs();
            allFlightIDs.addAll(newIDs);

            return dbo.getFlightNRsWithFlightIDs(allFlightIDs);

        } catch (SQLException | DataNotFoundException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    public int[] insertTracking(Connection conn, Deque<Fr24Frame> fr24Frames, DBOut dbo, HashMap<String, Integer> fnrsWithIDs) {
        try {
            var trackingQuery = conn.prepareStatement(SQLQueries.trackingquerry);
            var icaoIdMap = dbo.icaoIDMap();

            fr24Frames.forEach(f -> {
                int flightID = -1;
                var fnr = f.getFlightnumber();
                if (fnrsWithIDs.containsKey(fnr)) {
                    flightID = fnrsWithIDs.get(fnr);
                } else System.out.println("ERROR");
                try {
                    trackingQuery.setInt(1, flightID); // changed
                    trackingQuery.setDouble(2, f.getLat());
                    trackingQuery.setDouble(3, f.getLon());
                    trackingQuery.setInt(4, f.getAltitude());
                    trackingQuery.setInt(5, f.getGroundspeed());
                    trackingQuery.setInt(6, f.getHeading());
                    trackingQuery.setInt(7, f.getSquawk());
                    trackingQuery.setLong(8, f.getTimestamp());
                    trackingQuery.addBatch();
                    trackingQuery.clearParameters();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            return executeSQL(trackingQuery);

        } catch (SQLException | DataNotFoundException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }



    public PreparedStatement[] createWriteStatements(Connection conn, Deque<Fr24Frame> fr24Frames, DBOut dbo) {
        try {
            var planeQuery = conn.prepareStatement(SQLQueries.planequerry);
            var flightQuery = conn.prepareStatement(SQLQueries.flightquerry);
            var trackingQuery = conn.prepareStatement(SQLQueries.trackingquerry);

            final var icaoIDMap = dbo.icaoIDMap();
            final int[] planeTableSize = { dbo.getTableSize("planes") };
            final int[] flightTableSize = { dbo.getTableSize("flights") };

            fr24Frames.forEach(f -> {
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
                    planeTableSize[0]++;
                    flightTableSize[0]++;
                    if (!containsPlane) {
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

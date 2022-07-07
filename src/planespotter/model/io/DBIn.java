package planespotter.model.io;

import planespotter.constants.SQLQueries;

import java.sql.*;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import planespotter.controller.Controller;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.throwables.DataNotFoundException;

import static planespotter.model.LiveData.*;
import static planespotter.model.io.DBOut.getDBOut;
import static planespotter.util.Time.elapsedSeconds;
import static planespotter.util.Time.nowMillis;


/**
 *
 */
public class DBIn extends DBConnector {
	// (ONE and ONLY) main instance
	private static final DBIn INSTANCE;
	// 'data loader enabled' flag
	private static boolean enabled;
	// last inserted frame
	private static Fr24Frame lastFrame;
	// inserted frames counter for all writeToDB inserts
	private static int frameCount, planeCount, flightCount;
	// static initializer
	static {
		INSTANCE = new DBIn();

		lastFrame = null;
		frameCount = 0;
		planeCount = 0;
		flightCount = 0;
		enabled = true;
	}

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

	/**
	 * writes frames to the database,
	 * old strategy from @Lukas, but revised (fixed the memory problem by
	 * getting all dbOut-data before the loop instead of in it)
	 *
	 * @param fr24Frames are the Fr24Frames to write, could be extended to '<? extends Frame>'
	 * @param dbo is a DBOut Object for DB-Output
	 * @param dbi is a DBIn Object for DB-Inserts
	 * @return last inserted frame or null, if nothing was inserted
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
			Fr24Frame frame;
			int airlineID, planeID, flightID;
			boolean checkPlane, checkFlight;
			while (!fr24Frames.isEmpty()) {
				frame = fr24Frames.poll();
				// insert into planes
				airlineID = airlineTagsIDs.getOrDefault(frame.getAirline(), 1);
				planeID = planeIcaoIDs.getOrDefault(frame.getIcaoAdr(), -1);
				checkPlane = planeID > -1;
				if (!checkPlane) {
					planeID = dbi.insertPlane(frame, airlineID);
					// increasing inserted planes value
					increasePlaneCount();
				}
				// insert into flights
				flightID = flightNRsIDs.getOrDefault(frame.getFlightnumber(), -1);
				checkFlight = flightID > -1;
				if (!checkFlight) {
					flightID = dbi.insertFlight(frame, planeID);
					// increasing inserted flights value
					increaseFlightCount();
				}
				// insert into tracking
				dbi.insertTracking(frame, flightID);
				// increasing the inserted frames value
				increaseFrameCount();
				// setting current frame as last frame
				lastFrame = frame;
			}
			System.out.println("[DBWriter] filled DB in " + elapsedSeconds(ts1) + " seconds!");
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
			log.log("Trying to insert frames...", INSTANCE);
			if (ableCollect(count)) {
				// insert live data with normal writeToDB
				var dbOut = getDBOut();
				var dbIn = getDBIn();
				var frames = pollFrames(count);

				scheduler.exec(() -> write(frames, dbOut, dbIn),
						"DB-LiveData Writer", true, Scheduler.LOW_PRIO, true);
				insertCount += count;
			}
			if (insertCount > 0) {
				log.log("Inserting " + insertCount + " frames...", INSTANCE);
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
			final DBOut dbOut = getDBOut();
			final DBIn dbIn = getDBIn();

			while (!isEmpty()) {
				Deque<Fr24Frame> frames = pollFrames(framesPerWrite);

				scheduler.exec(() -> write(frames, dbOut, dbIn),
						"Inserter", false, Scheduler.HIGH_PRIO, false);

				inserted += framesPerWrite;
			}
			System.out.println("Inserting " + inserted + " frames...");
		}
		return inserted;
	}

	/**
	 * getter for the last inserted frame (into DB), which is given by the write method
	 *
	 * @return last inserted frame, or null if there is no last frame
	 */
	public static Fr24Frame getLastFrame() {
		return lastFrame;
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

	/**
	 * getter for DBIn main instance
	 *
	 * @return main instance of DBIn class
	 */
	public static DBIn getDBIn() {
		return INSTANCE;
	}

	/**
	 * private constructor, for main instance
	 */
	private DBIn() {
		// do nothing, no fields to initialize
	}

	public int insertPlane(Fr24Frame f, int airlineID) {
		try {
			synchronized (DB_SYNC) {
				Connection conn = DBConnector.getConnection();
				//TODO Airline ID anfrage
				// insert into planes
				PreparedStatement pstmt = conn.prepareStatement(SQLQueries.PLANEQUERRY, Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, f.getIcaoAdr());
				pstmt.setString(2, f.getTailnr());
				pstmt.setString(3, f.getRegistration());
				pstmt.setString(4, f.getPlanetype());
				pstmt.setInt(5, airlineID);
				pstmt.executeUpdate();

				ResultSet rs = pstmt.getGeneratedKeys();
				int id = -1;
				if (rs.next()) {
					id = rs.getInt(1);
				}
				conn.close();
				return id;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int insertFlight(Fr24Frame f, int planeID) {
		try {
			synchronized (DB_SYNC) {
				Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(SQLQueries.FLIGHTQUERRY, Statement.RETURN_GENERATED_KEYS);

				pstmt.setInt(1, planeID);
				pstmt.setString(2, f.getSrcAirport());
				pstmt.setString(3, f.getDestAirport());
				pstmt.setString(4, f.getFlightnumber());
				pstmt.setString(5, f.getCallsign());
				pstmt.setLong(6, f.getTimestamp());
				pstmt.executeUpdate();

				ResultSet rs = pstmt.getGeneratedKeys();
				int id = -1;
				if (rs.next()) {
					id = rs.getInt(1);
				}
				conn.close();
				return id;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void insertTracking(Fr24Frame f, int id) {
		try {
			synchronized (DB_SYNC) {
				Connection conn = DBConnector.getConnection();
				// insert into tracking
				PreparedStatement pstmt = conn.prepareStatement(SQLQueries.TRACKINGQUERRY);
				pstmt.setInt(1, id);
				pstmt.setDouble(2, f.getLat());
				pstmt.setDouble(3, f.getLon());
				pstmt.setInt(4, f.getAltitude());
				pstmt.setInt(5, f.getGroundspeed());
				pstmt.setInt(6, f.getHeading());
				pstmt.setInt(7, f.getSquawk());
				pstmt.setLong(8, f.getTimestamp());
				pstmt.executeUpdate();
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateFlightEnd(int id, long timestamp) {
		try {
			synchronized (DB_SYNC) {
				Connection conn = DBConnector.getConnection();

				PreparedStatement pstmt = conn.prepareStatement(SQLQueries.UPDATE_FLIGHT_END);
				pstmt.setInt(2, id);
				pstmt.setLong(1, timestamp);
				pstmt.executeUpdate();
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

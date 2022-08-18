package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.SQLQueries;

import java.sql.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import planespotter.controller.Controller;
import planespotter.model.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.nio.LiveLoader;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.NoAccessException;
import planespotter.util.Logger;

import static planespotter.model.nio.LiveLoader.*;
import static planespotter.util.Time.elapsedSeconds;
import static planespotter.util.Time.nowMillis;


/**
 *
 */
public final class DBIn extends DBConnector {

	// (ONE and ONLY) main instance
	private static final DBIn INSTANCE;

	// 'data loader enabled' flag
	private static boolean enabled;

	// last inserted frame
	private static Fr24Frame lastFrame;

	// inserted frames counter for all writeToDB inserts
	private static int frameCount, planeCount, flightCount;

	// initializing instance, counters and flags
	static {
		INSTANCE = new DBIn();

		lastFrame = null;
		frameCount = 0;
		planeCount = 0;
		flightCount = 0;
		enabled = true;
	}

	public static void write(@NotNull final Stream<Fr24Frame> fr24frames) {
		write((Deque<Fr24Frame>) fr24frames.collect(Collectors.toCollection(ArrayDeque::new)));
	}

	/**
	 * writes frames to the database,
	 * old strategy from @Lukas, but revised (fixed the memory problem by
	 * getting all dbOut-data before the loop instead of in it)
	 *
	 * @param fr24Frames are the Fr24Frames to write, could be extended to '<? extends Frame>'
	 */
	public static synchronized void write(@NotNull final Deque<Fr24Frame> fr24Frames) {
		if (enabled) {
			long startTime = nowMillis();
			DBOut dbo = DBOut.getDBOut();
			DBIn dbi = DBIn.getDBIn();
			HashMap<String, Integer> airlineTagsIDs = new HashMap<>(),
									 planeIcaoIDs = new HashMap<>(),
									 flightNRsIDs = new HashMap<>();
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
			System.out.println("[DBWriter] filled DB in " + elapsedSeconds(startTime) + " seconds!");
		}
	}

	/**
	 * inserts a certain amount of frames from the insertLater-queue into the DB
	 *
	 * @param scheduler is the Scheduler which executes tasks
	 * @param count is the frame count that should be written to DB
	 * @return inserted frames count as an int
	 */
	public static synchronized int insert(@NotNull final Scheduler scheduler, @NotNull LiveLoader liveLoader, final int count) {
		int insertCount = 0;
		if (enabled) {
			if (liveLoader.canPoll(count)) {
				// insert live data with normal writeToDB
				Stream<Fr24Frame> frames = liveLoader.pollFrames(count);

				scheduler.exec(() -> write(frames), "DB-LiveData Writer", true, Scheduler.LOW_PRIO, true);
				insertCount += count;
			}
		}
		return insertCount;
	}

	/**
	 * inserts all remaining data from the insertLater-queue into the DB
	 *
	 * @param scheduler is the Scheduler which executes tasks
	 * @return inserted frames count as an int
	 */
	@NotNull
	public static synchronized CompletableFuture<Void> insertRemaining(@NotNull final Scheduler scheduler, @NotNull LiveLoader liveLoader)
			throws NoAccessException {
		if (enabled) {
			Stream<Fr24Frame> frames = liveLoader.pollFrames(Integer.MAX_VALUE);

			return scheduler.exec(() -> write(frames), "Inserter", false, Scheduler.HIGH_PRIO, false);

		}
		throw new NoAccessException("DB-Writer is disabled!");
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


	// vvv instance vvv

	/**
	 * private constructor, for main instance
	 */
	private DBIn() {
		// do nothing, no fields to initialize
	}

	/**
	 *
	 *
	 * @param f
	 * @param airlineID
	 * @return
	 */
	public int insertPlane(@NotNull Fr24Frame f, int airlineID) {
		try {
			synchronized (DB_SYNC) {
				Connection conn = DBConnector.getConnection();
				// TODO Airline ID anfrage
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

	/**
	 *
	 *
	 * @param f
	 * @param planeID
	 * @return
	 */
	public int insertFlight(@NotNull Fr24Frame f, int planeID) {
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

	/**
	 *
	 *
	 * @param f
	 * @param id
	 */
	public void insertTracking(@NotNull Fr24Frame f, int id) {
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

	/**
	 *
	 *
	 * @param id
	 * @param timestamp
	 */
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

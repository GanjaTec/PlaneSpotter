package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.SQLQueries;
import planespotter.dataclasses.Fr24Frame;
import planespotter.dataclasses.Frame;
import planespotter.model.Scheduler;
import planespotter.model.nio.DataLoader;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.EmptyQueueException;
import planespotter.throwables.MalformedFrameException;
import planespotter.throwables.NoAccessException;

import java.sql.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static planespotter.util.Time.elapsedSeconds;
import static planespotter.util.Time.nowMillis;


/**
 *
 */
public final class DBIn extends DBConnector {

	// (ONE and ONLY) main instance
	private static final DBIn INSTANCE;

	// 'data loader enabled' flag
	private boolean enabled;

	// last inserted frame
	private Frame lastFrame;

	// inserted frames counter for all writeToDB inserts
	private int frameCount, planeCount, flightCount;

	// initializing instance
	static {
		INSTANCE = new DBIn();
	}

	/**
	 * private constructor, for main instance
	 */
	private DBIn() {
		this.lastFrame = null;
		this.frameCount = 0;
		this.planeCount = 0;
		this.flightCount = 0;
		this.enabled = true;
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
	 * writes frames to the database,
	 * old strategy from @Lukas, but revised (fixed the memory problem by
	 * getting all dbOut-data before instead of in the loop)
	 *
	 * @param frames is a {@link Stream} of {@link Frame}s to write, can be {@link Fr24Frame}s
	 *               and {@link planespotter.dataclasses.ADSBFrame}s
	 */
	public <E extends Frame> void write(final Stream<E> frames) {
		if (frames == null) {
			return;
		}
		write((Deque<E>) frames.collect(Collectors.toCollection(ArrayDeque::new)));
	}

	/**
	 * writes frames to the database,
	 * old strategy from @Lukas, but revised (fixed the memory problem by
	 * getting all dbOut-data before instead of in the loop)
	 *
	 * @param frames is a {@link Deque} of {@link Frame}s to write, can be {@link Fr24Frame}s
	 *               and {@link planespotter.dataclasses.ADSBFrame}s
	 */
	public synchronized <E extends Frame> void write(final Deque<E> frames) {
		if (!enabled || frames == null || frames.isEmpty()) {
			return;
		}
		Exception ex = null;
		long startTime = nowMillis();
		DBOut dbo = DBOut.getDBOut();
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
		E frame;
		int airlineID, planeID, flightID;
		while (!frames.isEmpty() && enabled) {
			frame = frames.poll();
			// insert into planes
			airlineID = airlineTagsIDs.getOrDefault(frame instanceof Fr24Frame fr24 ? fr24.getAirline() : "None", 1);
			planeID = planeIcaoIDs.getOrDefault(frame.getIcaoAddr(), -1);

			if (planeID <= -1) {
				try {
					planeID = insertPlane(frame, airlineID);
					// increasing inserted planes value
					increasePlaneCount();
				} catch (MalformedFrameException e) {
					ex = e;
				}
			}
			// insert into flights
			flightID = flightNRsIDs.getOrDefault(frame instanceof Fr24Frame fr24 ? fr24.getFlightnumber() : "None", -1);

			if (flightID <= -1) {
				flightID = insertFlight(frame, planeID);
				// increasing inserted flights value
				increaseFlightCount();
			}
			// insert into tracking
			insertTracking(frame, flightID);
			// increasing the inserted frames value
			increaseFrameCount();
			// setting current frame as last frame
			lastFrame = frame;
		}
		System.out.println("[DBWriter] filled DB in " + elapsedSeconds(startTime) + " seconds!");
		if (ex != null) {
			ex.printStackTrace();
		}
	}

	/**
	 * inserts all remaining data from the insertLater-queue into the DB
	 *
	 * @param scheduler is the Scheduler which executes tasks
	 * @return inserted frames count as an int
	 */
	@NotNull
	public synchronized CompletableFuture<Void> insertRemaining(@NotNull final Scheduler scheduler, @NotNull DataLoader dataLoader)
			throws NoAccessException {
		if (!enabled) {
			throw new NoAccessException("DB-Writer is disabled!");
		}
		Stream<? extends Frame> frames = dataLoader.pollFrames(Integer.MAX_VALUE);

		return scheduler.exec(() -> write(frames), "Insert Remaining", false, Scheduler.HIGH_PRIO, false);

	}

	/**
	 * getter for the last inserted frame (into DB), which is given by the write method
	 *
	 * @return last inserted frame, or null if there is no last frame
	 */
	public Frame getLastFrame() {
		return lastFrame;
	}

	/**
	 * @return true if the DBWriter is enabled, else false
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * enables/disables the DBWriter
	 *
	 * @param b is the enabled flag, [true -> enabled, false -> disabled]
	 */
	public void setEnabled(boolean b) {
		enabled = b;
	}

	/**
	 * @return all inserted frames count
	 */
	public int getFrameCount() {
		return frameCount;
	}

	/**
	 * @return all inserted planes count
	 */
	public int getPlaneCount() {
		return planeCount;
	}

	/**
	 * @return all inserted flights count
	 */
	public int getFlightCount() {
		return flightCount;
	}

	/**
	 * increases the 'all frame count' by 1
	 */
	private synchronized void increaseFrameCount() {
		frameCount++;
	}

	/**
	 * increases the 'all planes count' by 1
	 */
	private synchronized void increasePlaneCount() {
		planeCount++;
	}

	/**
	 * increases the 'all flights count' by 1
	 */
	private synchronized void increaseFlightCount() {
		flightCount++;
	}

	/**
	 * inserts a {@link planespotter.dataclasses.Plane} into the database,
	 * gets the plane data from the given {@link Fr24Frame} or {@link planespotter.dataclasses.ADSBFrame}
	 *
	 * @param frame is the {@link Frame} where the {@link planespotter.dataclasses.Plane} data is inserted from
	 * @param airlineID is the airline ID of the {@link planespotter.dataclasses.Plane}
	 * @return inserted {@link planespotter.dataclasses.Plane} ID or -1 if nothing was inserted
	 */
	public <E extends Frame> int insertPlane(@NotNull E frame, int airlineID) throws MalformedFrameException {
		synchronized (DB_SYNC) {
			// insert into planes
			String icao, tailNr, reg, type;
			if ((icao = frame.getIcaoAddr()) == null) {
				throw new MalformedFrameException("Frame has no ICAO!");
			}
			if (frame instanceof Fr24Frame fr24) {
				tailNr = fr24.getTailnr();
				reg = fr24.getRegistration();
				type = fr24.getPlanetype();
			} else {
				tailNr = reg = type = "None";
			}
			try (Connection conn = DBConnector.getConnection(false);
				 PreparedStatement pstmt = conn.prepareStatement(SQLQueries.PLANEQUERRY, Statement.RETURN_GENERATED_KEYS)) {
				pstmt.setString(1, icao);
				pstmt.setString(2, tailNr);
				pstmt.setString(3, reg);
				pstmt.setString(4, type);
				pstmt.setInt(5, airlineID);
				pstmt.executeUpdate();

				ResultSet rs = pstmt.getGeneratedKeys();
				return rs.next() ? rs.getInt(1) : -1;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	/**
	 * inserts a {@link planespotter.dataclasses.Flight} into the database,
	 * gets the flight data from the given {@link Fr24Frame} or {@link planespotter.dataclasses.ADSBFrame}
	 *
	 * @param frame is the {@link Frame} where the {@link planespotter.dataclasses.Flight} data is inserted from
	 * @param planeID is the {@link planespotter.dataclasses.Plane} ID of the {@link planespotter.dataclasses.Flight}
	 * @return inserted {@link planespotter.dataclasses.Flight} ID or -1 if nothing was inserted
	 */
	public <E extends Frame> int insertFlight(@NotNull E frame, int planeID) {
		String src, dest, flightNr, callsign;

		if (frame instanceof Fr24Frame fr24) {
			src = fr24.getSrcAirport();
			dest = fr24.getDestAirport();
			flightNr = fr24.getFlightnumber();
			callsign = fr24.getCallsign();
		} else {
			src = dest = flightNr = "None";
			if ((callsign = frame.getCallsign()) == null) {
				callsign = src;
			}
		}
		synchronized (DB_SYNC) {
			try (Connection conn = getConnection(false);
				 PreparedStatement pstmt = conn.prepareStatement(SQLQueries.FLIGHTQUERRY, Statement.RETURN_GENERATED_KEYS)) {

				pstmt.setInt(1, planeID);
				pstmt.setString(2, src);
				pstmt.setString(3, dest);
				pstmt.setString(4, flightNr);
				pstmt.setString(5, callsign);
				pstmt.setLong(6, frame.getTimestamp());
				pstmt.executeUpdate();

				ResultSet rs = pstmt.getGeneratedKeys();
				return rs.next() ? rs.getInt(1) : -1;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	/**
	 * inserts a {@link planespotter.dataclasses.DataPoint} (tracking data) into the database,
	 * gets the tracking data from the given {@link Fr24Frame} or {@link planespotter.dataclasses.ADSBFrame}
	 *
	 * @param frame is the {@link Frame} where the tracking data is inserted from
	 * @param flightID is the {@link planespotter.dataclasses.Flight} from
	 *                 the corresponding {@link planespotter.dataclasses.Flight}
	 */
	public <E extends Frame> void insertTracking(@NotNull E frame, int flightID) {
		synchronized (DB_SYNC) {
			// insert into tracking
			try (Connection conn = DBConnector.getConnection(false);
				 PreparedStatement pstmt = conn.prepareStatement(SQLQueries.TRACKINGQUERRY)) {
				pstmt.setInt(1, flightID);
				pstmt.setDouble(2, frame.getLat());
				pstmt.setDouble(3, frame.getLon());
				pstmt.setInt(4, frame.getAltitude());
				pstmt.setInt(5, frame.getGroundspeed());
				pstmt.setInt(6, frame.getHeading());
				pstmt.setInt(7, frame.getSquawk());
				pstmt.setLong(8, frame.getTimestamp());
				pstmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * updates a specific {@link planespotter.dataclasses.Flight} regarding the last timestamp,
	 * if a timestamp was null before, the {@link planespotter.dataclasses.Flight} has ended
	 *
	 * @param id is the {@link planespotter.dataclasses.Flight} ID to be updated
	 * @param timestamp is the new timestamp which replaces the old one
	 */
	public void updateFlightEnd(int id, long timestamp) {
		synchronized (DB_SYNC) {
			try (Connection conn = DBConnector.getConnection(false);
				 PreparedStatement pstmt = conn.prepareStatement(SQLQueries.UPDATE_FLIGHT_END)) {
				pstmt.setInt(2, id);
				pstmt.setLong(1, timestamp);
				pstmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}

package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.SQLQueries;

import java.sql.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import planespotter.dataclasses.ADSBFrame;
import planespotter.dataclasses.Frame;
import planespotter.model.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.nio.DataLoader;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.NoAccessException;

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
	private Fr24Frame lastFrame;

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


	public void writeADSB(@NotNull final Stream<ADSBFrame> adsbFrames) {

	}

	public void writeADSB(@NotNull final Deque<ADSBFrame> adsbFrames) {

	}

	public void writeFr24(@NotNull final Stream<Fr24Frame> fr24frames) {
		writeFr24((Deque<Fr24Frame>) fr24frames.collect(Collectors.toCollection(ArrayDeque::new)));
	}

	/**
	 * writes frames to the database,
	 * old strategy from @Lukas, but revised (fixed the memory problem by
	 * getting all dbOut-data before the loop instead of in it)
	 *
	 * @param fr24Frames are the Fr24Frames to write, could be extended to '<? extends Frame>'
	 */
	// TODO: 31.08.2022 use stream instead of deque
	public synchronized void writeFr24(@NotNull final Deque<Fr24Frame> fr24Frames) {
		if (!enabled || fr24Frames.isEmpty()) {
			return;
		}
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
		Fr24Frame frame;
		 int airlineID, planeID, flightID;
		while (!fr24Frames.isEmpty() && enabled) {
			frame = fr24Frames.poll();
			// insert into planes
			airlineID = airlineTagsIDs.getOrDefault(frame.getAirline(), 1);
			planeID = planeIcaoIDs.getOrDefault(frame.getIcaoAddr(), -1);

			if (planeID <= -1) {
				planeID = insertPlane(frame, airlineID);
				// increasing inserted planes value
				increasePlaneCount();
			}
			// insert into flights
			flightID = flightNRsIDs.getOrDefault(frame.getFlightnumber(), -1);

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
	}

	/**
	 * inserts all remaining data from the insertLater-queue into the DB
	 *
	 * @param scheduler is the Scheduler which executes tasks
	 * @return inserted frames count as an int
	 */
	@NotNull
	public synchronized CompletableFuture<Void> insertRemaining(@NotNull final Scheduler scheduler, @NotNull DataLoader dataLoader)
			throws NoAccessException, DataNotFoundException {
		if (!enabled) {
			throw new NoAccessException("DB-Writer is disabled!");
		}
		Stream<? extends Frame> frames = dataLoader.pollFrames(Integer.MAX_VALUE);

		return scheduler.exec(() -> {
			writeFr24(frames.map(frame -> {
						if (frame instanceof Fr24Frame fr24) {
							return fr24;
						}
						return null;
					})
					.filter(Objects::nonNull));
		}, "Inserter", false, Scheduler.HIGH_PRIO, false);

	}

	/**
	 * getter for the last inserted frame (into DB), which is given by the write method
	 *
	 * @return last inserted frame, or null if there is no last frame
	 */
	public Fr24Frame getLastFrame() {
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
	 *
	 *
	 * @param f
	 * @param airlineID
	 * @return
	 */
	public int insertPlane(@NotNull Fr24Frame f, int airlineID) {
		synchronized (DB_SYNC) {
			// insert into planes
			try (Connection conn = DBConnector.getConnection(false)) {

				PreparedStatement pstmt = conn.prepareStatement(SQLQueries.PLANEQUERRY, Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, f.getIcaoAddr());
				pstmt.setString(2, f.getTailnr());
				pstmt.setString(3, f.getRegistration());
				pstmt.setString(4, f.getPlanetype());
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
	 *
	 *
	 * @param f
	 * @param planeID
	 * @return
	 */
	public int insertFlight(@NotNull Fr24Frame f, int planeID) {
		synchronized (DB_SYNC) {
			try (Connection conn = getConnection(false);
				 PreparedStatement pstmt = conn.prepareStatement(SQLQueries.FLIGHTQUERRY, Statement.RETURN_GENERATED_KEYS)) {

				pstmt.setInt(1, planeID);
				pstmt.setString(2, f.getSrcAirport());
				pstmt.setString(3, f.getDestAirport());
				pstmt.setString(4, f.getFlightnumber());
				pstmt.setString(5, f.getCallsign());
				pstmt.setLong(6, f.getTimestamp());
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
	 *
	 *
	 * @param f
	 * @param id
	 */
	public void insertTracking(@NotNull Fr24Frame f, int id) {
		synchronized (DB_SYNC) {
			// insert into tracking
			try (Connection conn = DBConnector.getConnection(false);
				 PreparedStatement pstmt = conn.prepareStatement(SQLQueries.TRACKINGQUERRY)) {
				pstmt.setInt(1, id);
				pstmt.setDouble(2, f.getLat());
				pstmt.setDouble(3, f.getLon());
				pstmt.setInt(4, f.getAltitude());
				pstmt.setInt(5, f.getGroundspeed());
				pstmt.setInt(6, f.getHeading());
				pstmt.setInt(7, f.getSquawk());
				pstmt.setLong(8, f.getTimestamp());
				pstmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 *
	 *
	 * @param id
	 * @param timestamp
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

package planespotter.model.io;

import planespotter.constants.SQLQueries;

import java.sql.*;

import planespotter.dataclasses.Fr24Frame;


/**
 *
 */
public class DBIn extends DBConnector {
	// (ONE and ONLY) main instance
	private static final DBIn INSTANCE;
	// static initializer
	static {
		INSTANCE = new DBIn();
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

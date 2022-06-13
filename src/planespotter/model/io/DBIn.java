package planespotter.model.io;

import planespotter.constants.SQLQueries;

import java.sql.*;

import planespotter.dataclasses.Fr24Frame;
import planespotter.model.SupperDB;
import planespotter.throwables.NoAccessException;


public class DBIn extends SupperDB {

	public int insertPlane(Fr24Frame f, int airlineID) {
		try {
			Connection conn = SupperDB.getDBConnection();
			//TODO Airline ID anfrage
			// insert into planes
			PreparedStatement pstmt = conn.prepareStatement(SQLQueries.planequerry, Statement.RETURN_GENERATED_KEYS);
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
		} catch (SQLException | ClassNotFoundException | NoAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int insertFlight(Fr24Frame f, int planeID) {
		try {
			Connection conn = getDBConnection();
			PreparedStatement pstmt = conn.prepareStatement(SQLQueries.flightquerry, Statement.RETURN_GENERATED_KEYS);

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
		} catch (SQLException | ClassNotFoundException | NoAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void insertTracking(Fr24Frame f, int id) {
		try {
			Connection conn = SupperDB.getDBConnection();
			// insert into tracking
			PreparedStatement pstmt = conn.prepareStatement(SQLQueries.trackingquerry);
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
		} catch (SQLException | ClassNotFoundException | NoAccessException e) {
			e.printStackTrace();
		}
	}

	public void updateFlightEnd(int id, long timestamp) throws Exception {
		Connection conn = SupperDB.getDBConnection();

		PreparedStatement pstmt = conn.prepareStatement(SQLQueries.updateFlightEnd);
		pstmt.setInt(2, id);
		pstmt.setLong(1, timestamp);
		pstmt.executeUpdate();
		conn.close();


	}

}

package planespotter.model.io;

import planespotter.constants.SQLQuerries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import planespotter.dataclasses.Frame;


public class DBIn extends SupperDB {

	public int insertPlane(Frame f, int airlineID) throws Exception {
		Connection conn = super.getDBConnection();		
		//TODO Airline ID anfrage
		// insert into planes
		PreparedStatement pstmt = conn.prepareStatement(SQLQuerries.planequerry, Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, f.getIcaoAdr());
		pstmt.setString(2, f.getTailnr());
		pstmt.setString(3, f.getRegistration());
		pstmt.setString(4, f.getPlanetype());
		
		//TODO Airline ID anfrage
		pstmt.setInt(5, airlineID);
		pstmt.executeUpdate();

		ResultSet rs = pstmt.getGeneratedKeys();
		int id = -1;
		if(rs.next()) {
			id = rs.getInt(1);
		}
		conn.close();
		return id;
	}

	public int insertFlight(Frame f, int planeID) throws Exception {
		Connection conn = getDBConnection();
		PreparedStatement pstmt = conn.prepareStatement(SQLQuerries.flightquerry, Statement.RETURN_GENERATED_KEYS);

		pstmt.setInt(1, planeID);
		pstmt.setString(2, f.getSrcAirport());
		pstmt.setString(3, f.getDestAirport());
		pstmt.setString(4, f.getFlightnumber());
		pstmt.setString(5, f.getCallsign());
		pstmt.setLong(6, f.getTimestamp());
		pstmt.executeUpdate();

		ResultSet rs = pstmt.getGeneratedKeys();
		int id = -1;
		if(rs.next()) {
			id = rs.getInt(1);
		}
		conn.close();
		return id;
	}

	public void insertTracking(Frame f, int id) throws Exception {

		Connection conn = super.getDBConnection();

		// insert into tracking
		PreparedStatement pstmt = conn.prepareStatement(SQLQuerries.trackingquerry);
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

	public void updateFlightEnd(int id, long timestamp) throws Exception {
		Connection conn = super.getDBConnection();

		PreparedStatement pstmt = conn.prepareStatement(SQLQuerries.updateFlightEnd);
		pstmt.setInt(2, id);
		pstmt.setLong(1, timestamp);
		pstmt.executeUpdate();
		conn.close();


	}

}

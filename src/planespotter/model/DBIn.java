package planespotter.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import planespotter.constants.SQLQuerrys;
import planespotter.dataclasses.Frame;

public class DBIn {

	private static Connection getDBConnection() throws Exception {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String db = "jdbc:sqlite:plane.db";
		Connection conn = DriverManager.getConnection(db);
		return conn;
	}

	public static int insertPlane(Frame f) throws Exception {
		Connection conn = getDBConnection();		
		//TODO Airline ID anfrage
		// insert into planes
		PreparedStatement pstmt = conn.prepareStatement(SQLQuerrys.planequerry, Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, f.getIcaoAdr());
		pstmt.setString(2, f.getTailnr());
		pstmt.setString(3, f.getRegistration());
		pstmt.setString(4, f.getPlanetype());
		//TODO Airline ID anfrage
		pstmt.setString(5, f.getAirline());
		pstmt.executeUpdate();

		ResultSet rs = pstmt.getGeneratedKeys();
		int id = -1;
		if(rs.next()) {
			id = rs.getInt(1);
		}
		conn.close();
		return id;
	}

	public static int insertFlight(Frame f, int planeID) throws Exception {
		Connection conn = getDBConnection();
		PreparedStatement pstmt = conn.prepareStatement(SQLQuerrys.flightquerry, Statement.RETURN_GENERATED_KEYS);
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

	public static void insertTracking(Frame f, int id) throws Exception {

		Connection conn = getDBConnection();

		// insert into tracking
		PreparedStatement pstmt = conn.prepareStatement(SQLQuerrys.trackingquerry);
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
	
	public static void updateFlightEnd(int id, long timestamp) throws Exception {
		Connection conn = getDBConnection();
		
		PreparedStatement pstmt = conn.prepareStatement(SQLQuerrys.updateFlightEnd);
		pstmt.setInt(2, id);
		pstmt.setLong(1, timestamp);
		pstmt.executeUpdate();
		conn.close();
		
		
	}

}

package planespotter.model;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import planespotter.dataclasses.*;

public class dbOut {
	
	public ResultSet querryDB(String querry) {
		ResultSet rs;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			String db = "jdbc:sqlite:plane.db";
			Connection conn = DriverManager.getConnection(db);
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(querry);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rs = null;
		}
		return rs;
	}
	
	//public Flight querryFlight() {
		
		
	//	Flight flight = new Flight();
	//	return flight;
	//}
	
	public List<DataPoint> querryTrackingData(int flightID) {
		List<DataPoint> dps = new ArrayList<DataPoint>();
		try {
		String getFlightTracking = "SELECT * from tracking WHERE ID == " + flightID;
		ResultSet rs = querryDB(getFlightTracking);
		while(rs.next()) {
			Position p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
			DataPoint dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), p, rs.getInt("timestamp"),
					rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
			dps.add(dp);
		}
		}catch(Exception e) {
			dps = null;
		}
		return dps;
		
	}
	
	
}

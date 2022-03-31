package planespotter.model;

import planespotter.SqlQuerrys;
import planespotter.dataclasses.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class DBOut {

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

	private Position convertCoords(String coords) {	
		String[] splitCoords = coords.split(",");
		List<Double> processedCoords = new ArrayList<Double>();

		for(String s : splitCoords) {
			processedCoords.add(Double.parseDouble(s));
		}

		Position p = new Position(processedCoords.get(0), processedCoords.get(1));
		return p;
	}

	public Airline getAirlineByTag(String tag) throws SQLException {
		Airline a = null;
		ResultSet rs = querryDB(SqlQuerrys.getAirlineByTag + tag);
		while(rs.next()) {
			a = new Airline(rs.getInt("ID"), rs.getString("icaotag"), rs.getString("name"));
		}

		return a;
	}

	public List<Airport> getAirports(String srcAirport, String destAirport) throws SQLException{
		List<Airport> aps = new ArrayList<Airport>();
		ResultSet rsSrc = querryDB(SqlQuerrys.getAirportByTag + srcAirport);
		ResultSet rsDst = querryDB(SqlQuerrys.getAirportByTag + destAirport);
		
		if(rsSrc.next()) {
			Airport srcAp = new Airport(rsSrc.getInt("ID"), rsSrc.getString("iatatag"), rsSrc.getString("name"), convertCoords(rsSrc.getString("coords")));
			aps.add(srcAp);
		
		} else {
			Airport srcAp = new Airport(0, "None", "None", new Position(0.0f, 0.0f));
			aps.add(srcAp);
		}
		
		if(rsDst.next()) {
			Airport dstAp = new Airport(rsDst.getInt("ID"), rsDst.getString("iatatag"), rsDst.getString("name"), convertCoords(rsDst.getString("coords")));
			aps.add(dstAp);
		} else {
			Airport dstAp = new Airport(0, "None", "None", new Position(0.0f, 0.0f));
			aps.add(dstAp);
		}

		return aps;
	}
	
	public Plane getPlaneByICAO(String icao) throws SQLException {
		Plane p = null;
		ResultSet rs = querryDB(SqlQuerrys.getPlaneByICAO + icao);

		while(rs.next()) {
			Airline a = getAirlineByTag(rs.getString("icaotag"));
			p = new Plane(rs.getInt("ID"), rs.getString("icaonr"), rs.getString("tailn"), rs.getString("type"), rs.getString("registration"), a);
		}

		return p;
	}

	public HashMap<Long, DataPoint> getTrackingByFlight(int flightID) throws SQLException {
		HashMap<Long ,DataPoint> dps = new HashMap<Long, DataPoint>();
		ResultSet rs = querryDB(SqlQuerrys.getTrackingByFlight + flightID);
		while(rs.next()) {
			Position p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
			DataPoint dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), p, rs.getInt("timestamp"),
					rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
			dps.put((long)rs.getInt("timestamp"), dp);
		}
		return dps;

	}
	
	public List<Flight> getAllFlights() throws SQLException {
		List<Flight> flights = new ArrayList<Flight>();

		ResultSet rs = querryDB(SqlQuerrys.getFlights);
		while(rs.next()) {
			HashMap<Long, DataPoint> dps = getTrackingByFlight(rs.getInt("ID"));
			List<Airport> aps = getAirports(rs.getString("src"), rs.getString("dest"));
			Plane plane = getPlaneByICAO(rs.getString("plane"));
 			Flight flight = new Flight(rs.getInt("ID"), aps.get(0), aps.get(1), plane, rs.getString("flightnr"), dps);
			flights.add(flight);
		}
		return flights;
	}
	
        public static void getFlightByID(int id) throws SQLException {
		
		int flid;
		
		Scanner scanner = new Scanner(System.in);
		System.out.print("Gebe die Flug ID an: ");
                flid = scanner.nextInt();
		
		Connection con = DbConnection.connect();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT * flights where ID = ?";
		ps = con.prepareStatement(sql);
		ps.setString(1, + flid );
		rs = ps.executeQuery();
		
		// reading one row
		String flight = rs.getString(1);
		System.out.pringln(flight)
			
	}

}

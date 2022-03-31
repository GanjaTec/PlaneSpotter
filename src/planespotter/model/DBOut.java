package planespotter.model;

import planespotter.SqlQuerrys;
import planespotter.dataclasses.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Lukas
 *
 *
 * This class is used to get Output from the DB
 * It relies heavily on planespotter.SQLQuerrys
 *
 */
public class DBOut {

	
	/**
	 * This method is used to querry the DB
	 * it takes a String and returns a ResultSet
	 * 
	 * @param querry String to use for the Querry
	 * @return ResultSet containing the querried Data
	 */
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

	/**
	 * This method is a quick botch because i messed up the Airports Table in the DB
	 * It takes A string containing the Coordinates received from the DB and converts it
	 * into a Position Object
	 * 
	 * @param coords The string containing the Coords
	 * @return Position containing Latitude and Longitude
	 */
	private Position convertCoords(String coords) {	
		String[] splitCoords = coords.split(",");
		List<Double> processedCoords = new ArrayList<Double>();

		for(String s : splitCoords) {
			processedCoords.add(Double.parseDouble(s));
		}

		Position p = new Position(processedCoords.get(0), processedCoords.get(1));
		return p;
	}

	/**
	 * This method is used to Querry the DB for Airline by its assigned ICAO Tag
	 * It takes a String containing the ICAO Tag and returns
	 * an Airline Object
	 * 
	 * 
	 * @param tag the ICAO-Tag used in the Querry
	 * @return Airline Object
	 * @throws SQLException
	 */
	public Airline getAirlineByTag(String tag) throws SQLException {
		Airline a = null;
		ResultSet rs = querryDB(SqlQuerrys.getAirlineByTag + tag);
		while(rs.next()) {
			a = new Airline(rs.getInt("ID"), rs.getString("icaotag"), rs.getString("name"));
		}

		return a;
	}

	/**
	 * This Method is used to Querry both the Departure (src) and the Arrival (dst)
	 * Airports of a Flight and returns a List containing two Airport Objects 
	 * 
	 * @param srcAirport String containing the Departure Airports IATA Tag
	 * @param destAirport String containing the Arrival Airports IATA Tag
	 * @return List<Airport> the list containing the Airport Objects
	 * @throws SQLException
	 */
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
	
	/**
	 * This Method is used to Querry a single Plane by its ICAO Tag
	 * It takes a String containing the ICAO Tag and returns a Plane Object
	 * 
	 * @param icao Strin containing the ICAO Tag
	 * @return Plane the Object containing all Information about the Plane
	 * @throws SQLException
	 */
	public Plane getPlaneByICAO(String icao) throws SQLException {
		Plane p = null;
		ResultSet rs = querryDB(SqlQuerrys.getPlaneByICAO + icao);

		while(rs.next()) {
			Airline a = getAirlineByTag(rs.getString("icaotag"));
			p = new Plane(rs.getInt("ID"), rs.getString("icaonr"), rs.getString("tailn"), rs.getString("type"), rs.getString("registration"), a);
		}

		return p;
	}

	/**
	 * This Method is used to Querry all Datapoints belonging to a single Flight
	 * It takes an int FlightID and returns a HashMap<Long, DataPoint> containing the Tracking Data
	 * 
	 * 
	 * @param flightID int representing the Flights Database ID
	 * @return HashMap<Long, DataPoint> containing all Datapoints keyed with Timestamp
	 * @throws SQLException
	 */
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
	
	/**
	 * This Method is used to retrieve ALL flights and their representative Data from the DB
	 * It takes no Parameters and returns a List<Flight> containing all Flight Objects
	 * 
	 * It relies on a lot of other Methods in this class to gather the Objects needed to construct the
	 * Flight objects.
	 * 
	 * TODO Fix Bug causing OutOfMemoryError
	 * This will be kinda hard, the method constructs a massive List
	 * that is way to big to hold in memory
	 * 
	 * see errorlog "hs_err_pid30296.log" in the projects root directory
	 * 
	 * @return List<Flight> containing all Flight Objects
	 * @throws SQLException
	 */
	public List<Flight> getAllFlights() throws SQLException {
		List<Flight> flights = new ArrayList<Flight>();

		ResultSet rs = querryDB(SqlQuerrys.getFlights);
		while(rs.next()) {
			HashMap<Long, DataPoint> dps = getTrackingByFlight(rs.getInt("ID"));
			List<Airport> aps = getAirports(rs.getString("src"), rs.getString("dest"));
			Plane plane = getPlaneByICAO(rs.getString("plane"));
 			Flight flight = new Flight(rs.getInt("ID"), aps.get(0), aps.get(1), rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
			flights.add(flight);
		}
		return flights;
	}


}

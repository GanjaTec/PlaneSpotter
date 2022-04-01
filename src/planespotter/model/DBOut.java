package planespotter.model;

import planespotter.constants.SqlQuerrys;
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
	public ResultSet querryDB(String querry) throws Exception {
		ResultSet rs;
			Class.forName("com.mysql.cj.jdbc.Driver");
			String db = "jdbc:sqlite:plane.db";
			Connection conn = DriverManager.getConnection(db);
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(querry);
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
	 *
	 */
	private String stripString (String in) {
		return in.replaceAll("\"", "");
	}

	/**
	 * This method is used to Querry the DB for Airline by its assigned ICAO Tag
	 * It takes a String containing the ICAO Tag and returns
	 * an Airline Object
	 * 
	 * 
	 * @param tag the ICAO-Tag used in the Querry
	 * @return Airline Object
	 * @throws Exception 
	 */
	public Airline getAirlineByTag(String tag) throws Exception {
		Airline a = null;
		tag = stripString(tag);
		ResultSet rs = querryDB(SqlQuerrys.getAirlineByTag + tag); // ist leer TODO fixen
		if (rs.next()) {
			a = new Airline(rs.getInt("ID"), rs.getString("icaotag"), rs.getString("name"));
		} else {
			a = new Airline(-1, "None", "None");
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
	 * @throws Exception 
	 */
	public List<Airport> getAirports(String srcAirport, String destAirport) throws Exception{
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
	 * @throws Exception 
	 */
	public Plane getPlaneByICAO(String icao) throws Exception {
		Plane p;
		// TODO: Bug fixen
		// org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (unrecognized token: "06A1EB")
		//icao = stripString(icao);
		ResultSet rs = querryDB(SqlQuerrys.getPlaneByICAO + icao);

		if (rs.next()) {
			Airline a = getAirlineByTag(rs.getString("airline"));
			p = new Plane(rs.getInt("ID"), rs.getString("icaonr"), rs.getString("tailnr"), rs.getString("type"), rs.getString("registration"), a);
		} else {
			Airline a = new Airline(-1, "None", "None");
			p = new Plane(-1, "None", "None", "None", "None", a);
		}

		return p;
	}
	
	public Plane getPlaneByID(int id) throws Exception {
		Plane p;
		ResultSet rs = querryDB(SqlQuerrys.getPlaneByID + id);

		if (rs.next()) {
			Airline a = new Airline(-1, "BIA", "BUFU Int. Airlines");
			//Airline a = getAirlineByTag(rs.getString("airline"));
			p = new Plane(rs.getInt("ID"), rs.getString("icaonr"), rs.getString("tailnr"), rs.getString("type"), rs.getString("registration"), a);
		} else {
			Airline a = new Airline(-1, "None", "None");
			p = new Plane(-1, "None", "None", "None", "None", a);
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
	 * @throws Exception 
	 */
	public HashMap<Long, DataPoint> getTrackingByFlight(int flightID) throws Exception {
		HashMap<Long ,DataPoint> dps = new HashMap<Long, DataPoint>();
		ResultSet rs = querryDB(SqlQuerrys.getTrackingByFlight + flightID);
		while(rs.next()) {
			// TODO: IF STATEMENT
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
	 * @throws Exception 
	 */
	public List<Flight> getAllFlights() throws Exception {
		List<Flight> flights = new ArrayList<Flight>();

		ResultSet rs = querryDB(SqlQuerrys.getFlights);
		int counter = 0;
		while(rs.next() && counter <= 20) {
			HashMap<Long, DataPoint> dps = getTrackingByFlight(rs.getInt("ID"));
			List<Airport> aps = getAirports(rs.getString("src"), rs.getString("dest"));
			Plane plane = getPlaneByID(5);
 			Flight flight = new Flight(rs.getInt("ID"), aps.get(0), aps.get(1), rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
			flights.add(flight);
			counter++;
		}
		return flights;
	}


}

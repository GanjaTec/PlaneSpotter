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
	 * test: max loaded flights
	 */
	public static int maxLoadedFlights = 2000;


	/**
	 * This method is used to querry the DB
	 * it takes a String and returns a ResultSet
	 * 
	 * @param querry String to use for the Querry
	 * @return ResultSet containing the querried Data
	 */

	public static ResultSet querryDB(String querry) throws Exception {
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
	 * @param in
	 * @return
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
			a = new Airline(rs.getInt("ID"), rs.getString("iatatag"), rs.getString("name"));
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

	/**
	 * @param id
	 * @return
	 * @throws Exception
	 */
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
	 * @param icao
	 * @return
	 * @throws Exception
	 */
	public static int checkPlaneInDB(String icao) throws Exception {
		String planeFilter = "SELECT ID FROM planes WHERE icaonr = '" + icao + "' LIMIT 1";
		ResultSet rs = querryDB(planeFilter);
		int id;
		if(rs.next() == true) {
			id = rs.getInt(1);
		} else {
			id = -1;
		}
		rs.close();
		return id;
	}

	/**
	 * This Method is used to Querry all Datapoints belonging to a single Flight
	 * It takes an int FlightID and returns a HashMap<Long, DataPoint> containing the Tracking Data
	 *
	 *
	 * @param flightID int representing the Flights Database ID
	 * @return HashMap<Integer, DataPoint> containing all Datapoints keyed with Timestamp
	 * @throws Exception
	 */
	public HashMap<Integer, DataPoint> getTrackingByFlight(int flightID) throws Exception {
		HashMap<Integer ,DataPoint> dps = new HashMap<Integer, DataPoint>();
		ResultSet rs = querryDB(SqlQuerrys.getTrackingByFlight + flightID);
		while(rs.next()) {
			// TODO: IF STATEMENT
			Position p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
			DataPoint dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), p, rs.getInt("timestamp"),
					rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
			dps.put(rs.getInt("ID"), dp);
		}
		return dps;

	}

	public static long getLastTrackingByFlightID(int id) throws Exception {
		long timestamp = -1;
		String getLastTracking = "SELECT timestamp FROM tracking WHERE flightid == "+ id +" ORDER BY ID DESC LIMIT 1";
		ResultSet rs = querryDB(getLastTracking);

		while(rs.next()) {
			timestamp = rs.getLong(1);
		}
		return timestamp;
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
		while(rs.next() && counter <= maxLoadedFlights) { // counter: max flights -> to limit the incoming data (prevents a crash)
			HashMap<Integer, DataPoint> dps = getTrackingByFlight(rs.getInt("ID"));
			List<Airport> aps = getAirports(rs.getString("src"), rs.getString("dest"));
			Plane plane = getPlaneByID(rs.getInt("plane"));
			Flight flight = new Flight(rs.getInt("ID"), aps.get(0), aps.get(1), rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
			flights.add(flight);
			counter++;
		}
		return flights;
	}

	/**
	 * @param callsign
	 * @return
	 * @throws Exception
	 */
	public List<Flight> getFlightsByCallsign(String callsign) throws Exception {
		List<Flight> flights = new ArrayList<Flight>();
		ResultSet rs = querryDB(SqlQuerrys.getFlightByCallsign + callsign);

		while(rs.next()) {
			HashMap<Integer, DataPoint> dps = getTrackingByFlight(rs.getInt("ID"));
			List<Airport> aps = getAirports(rs.getString("src"), rs.getString("dest"));
			Plane plane = getPlaneByID(rs.getInt("plane"));
			Flight flight = new Flight(rs.getInt("ID"), aps.get(0), aps.get(1), rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
			flights.add(flight);
		}

		return flights;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public static int getLastFlightID() throws Exception {
		ResultSet rs = querryDB(SqlQuerrys.getLastFlightID);
		int flightid;
		if(rs.next()==true) {
			flightid = rs.getInt("ID");
		} else {
			flightid = -1;
		}
		return flightid;
	} 

	/**
	 * @param f
	 * @param planeid
	 * @return
	 * @throws Exception
	 */
	public static int checkFlightInDB(Frame f, int planeid) throws Exception {
		ResultSet rs = querryDB("SELECT ID FROM flights WHERE plane == " + planeid + " AND flightnr == '" + f.getFlightnumber() + "' AND endTime IS NULL");
		int flightID;
		if(rs.next() == true) {
			flightID = rs.getInt("ID");
		} else {
			flightID = -1;
		}
		rs.close();
		return flightID;
	}

	public static List<Integer> checkEnded() throws Exception{
		ResultSet rs = querryDB(SqlQuerrys.checkEndOfFlight);
		List<Integer> flightIDs= new ArrayList<Integer>();

		while(rs.next()) {
			flightIDs.add(rs.getInt(1));
		}
		rs.close();
		return flightIDs;

	}

        public Plane getFlightByID(int id) throws Exception{
		Flight f;
		ResultSet rs = querryDB(SqlQuerrys.getFlightByID + id);

		if (rs.next()) {
		
			f = new Flight(rs.getInt("ID"), rs.getString("plane"), rs.getString("src"), rs.getString("dest"), rs.getString("flightnr"), rs.getString("callsign"));
		} else {
			
			f = new Flight(-1, "None", "None", "None", "None", "None");
		}

		return f;
}

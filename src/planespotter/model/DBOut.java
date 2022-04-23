package planespotter.model;

import planespotter.constants.SQLQuerries;
import planespotter.dataclasses.*;
import planespotter.display.UserSettings;
import planespotter.throwables.DataNotFoundException;

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

public class DBOut extends SupperDB {

	/**
	 * test: max loaded flights
	 */
	public static int maxLoadedFlights = UserSettings.getMaxLoadedFlights();


	/**
	 * This method is used to querry the DB
	 * it takes a String and returns a ResultSet
	 * 
	 * @param querry String to use for the Querry
	 * @return ResultSet containing the querried Data
	 */

	public ResultSet querryDB(String querry) throws Exception {
		ResultSet rs;
		Connection conn = SupperDB.getDBConnection();
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
		List<Double> processedCoords = new ArrayList<>();

		for(String s : splitCoords) {
			processedCoords.add(Double.parseDouble(s));
		}

		Position p = new Position(processedCoords.get(0), processedCoords.get(1));
		return p;
	}

	/**
	 * @param in is the string to strip
	 * @return input-string, but without ' \" '
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
	 */
	public Airline getAirlineByTag (String tag) {
		Airline a = null;
		try {
			tag = stripString(tag);
			ResultSet rs = querryDB(SQLQuerries.getAirlineByTag + tag); // ist leer TODO fixen
			if (rs.next()) {
				a = new Airline(rs.getInt("ID"), rs.getString("iatatag"), rs.getString("name"));
			} else {
				a = new Airline(-1, "None", "None");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return a;
	}
	
	public int getAirlineIDByTag (String tag) {
		int id = 1;
		try {
			Deserializer ds = new Deserializer();
			tag = ds.stripString(tag);
			tag = "'" + tag + "'";
			ResultSet rs = querryDB(SQLQuerries.getAirlineIDByTag + tag);
			if(rs.next()) {
				id = rs.getInt(1);
			rs.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		return id;
	}

	/**
	 * This Method is used to Querry both the Departure (src) and the Arrival (dst)
	 * Airports of a Flight and returns a List containing two Airport Objects
	 *
	 * @param srcAirport String containing the Departure Airports IATA Tag
	 * @param destAirport String containing the Arrival Airports IATA Tag
	 * @return List<Airport> the list containing the Airport Objects
	 */

	public List<Airport> getAirports (String srcAirport, String destAirport) {
		List<Airport> aps = new ArrayList<>();
		try {
			ResultSet rsSrc = querryDB(SQLQuerries.getAirportByTag + srcAirport);
			ResultSet rsDst = querryDB(SQLQuerries.getAirportByTag + destAirport);

			if (rsSrc.next()) {
				Airport srcAp = new Airport(rsSrc.getInt("ID"), rsSrc.getString("iatatag"), rsSrc.getString("name"), convertCoords(rsSrc.getString("coords")));
				aps.add(srcAp);

			} else {
				Airport srcAp = new Airport(0, "None", "None", new Position(0.0f, 0.0f));
				aps.add(srcAp);
			}

			if (rsDst.next()) {
				Airport dstAp = new Airport(rsDst.getInt("ID"), rsDst.getString("iatatag"), rsDst.getString("name"), convertCoords(rsDst.getString("coords")));
				aps.add(dstAp);
			} else {
				Airport dstAp = new Airport(0, "None", "None", new Position(0.0f, 0.0f));
				aps.add(dstAp);
			}
			rsSrc.close();
			rsDst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aps;
	}

	/**
	 * This Method is used to Querry a single Plane by its ICAO Tag
	 * It takes a String containing the ICAO Tag and returns a Plane Object
	 *
	 * @param icao Strin containing the ICAO Tag
	 * @return Plane the Object containing all Information about the Plane
	 */
	public Plane getPlaneByICAO (String icao) {
		Plane p = null;
		// TODO: Bug fixen
		// org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (unrecognized token: "06A1EB")
		//icao = stripString(icao);
		try {
			ResultSet rs = querryDB(SQLQuerries.getPlaneByICAO + icao);

			if (rs.next()) {
				Airline a = getAirlineByTag(rs.getString("airline"));
				p = new Plane(rs.getInt("ID"), rs.getString("icaonr"), rs.getString("tailnr"), rs.getString("type"), rs.getString("registration"), a);
			} else {
				Airline a = new Airline(-1, "None", "None");
				p = new Plane(-1, "None", "None", "None", "None", a);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}

	/**
	 * @param id is the plane id
	 * @return plane with the given id
	 */
	public Plane getPlaneByID (int id) {
		Plane p = null;
		try {
			ResultSet rs = querryDB(SQLQuerries.getPlaneByID + id);
			if (rs.next()) {
				Airline a = new Airline(-1, "BIA", "BUFU Int. Airlines");
				//Airline a = getAirlineByTag(rs.getString("airline"));
				p = new Plane(rs.getInt("ID"), rs.getString("icaonr"), rs.getString("tailnr"), rs.getString("type"), rs.getString("registration"), a);
			} else {
				Airline a = new Airline(-1, "None", "None");
				p = new Plane(-1, "None", "None", "None", "None", a);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}

	/**
	 * @param icao
	 * @return
	 * @throws Exception
	 */
	public int checkPlaneInDB (String icao) {
		String planeFilter = "SELECT ID FROM planes WHERE icaonr = '" + icao + "' LIMIT 1";
		try {
			ResultSet rs = this.querryDB(planeFilter);
			int id;
			if (rs.next() == true) {
				id = rs.getInt(1);
			} else {
				id = -1;
			}
			rs.close();
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -9999; // weil es -1 schon gibt -> zum besseren debuggen
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
	// TODO fix: HashMap lentgh was 1, but last Tracking id was about 23000
	// TODO hier muss der Fehler sein
	public HashMap<Integer, DataPoint> getTrackingByFlight (int flightID) {
		HashMap<Integer ,DataPoint> dps = new HashMap<Integer, DataPoint>();
		String flight_id = "'" + flightID + "'";
		try {
			ResultSet rs = querryDB("SELECT * FROM tracking WHERE flightid == " + flight_id);
			while (rs.next()) {
				// TODO: IF STATEMENT
				Position p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
				DataPoint dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), p, rs.getInt("timestamp"),
						rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
				dps.put(rs.getInt("ID"), dp);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dps;

	}

	public long getLastTrackingByFlightID (int id) {
		long timestamp = -1;
		String getLastTracking = "SELECT timestamp FROM tracking WHERE flightid == "+ id +" ORDER BY ID DESC LIMIT 1";
		try {
			ResultSet rs = this.querryDB(getLastTracking);

			while (rs.next()) {
				timestamp = rs.getLong(1);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return timestamp;
	}

	public int getLastTrackingIDByFlightID (int id)
			throws DataNotFoundException {
		int trackingid = -1;
		String getLastTrackingIDByFlightID =  "SELECT ID FROM tracking WHERE flightid == '"+ id +"' ORDER BY ID DESC LIMIT 1";
		try {
			ResultSet rs = querryDB(getLastTrackingIDByFlightID);

			if (rs.next()) {
				trackingid = rs.getInt(1);
			} else {
				throw new DataNotFoundException();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trackingid;	
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
	public List<Flight> getAllFlights () {
		List<Flight> flights = new ArrayList<Flight>();
		try {
			ResultSet rs = querryDB(SQLQuerries.getFlights);
			int counter = 0;
			while (rs.next() && counter <= maxLoadedFlights) { // counter: max flights -> to limit the incoming data (prevents a crash)
				HashMap<Integer, DataPoint> dps = getTrackingByFlight(rs.getInt("ID"));
				List<Airport> aps = getAirports(rs.getString("src"), rs.getString("dest"));
				Plane plane = getPlaneByID(rs.getInt("plane"));
				Flight flight = new Flight(rs.getInt("ID"), aps.get(0), aps.get(1), rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
				flights.add(flight);
				counter++;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flights;
	}

	/**
	 * @param callsign
	 * @return
	 * @throws Exception
	 */
	public List<Flight> getFlightsByCallsign (String callsign) {
		List<Flight> flights = new ArrayList<Flight>();
		try {
			ResultSet rs = querryDB(SQLQuerries.getFlightByCallsign + callsign);

			while (rs.next()) {
				HashMap<Integer, DataPoint> dps = getTrackingByFlight(rs.getInt("ID"));
				List<Airport> aps = getAirports(rs.getString("src"), rs.getString("dest"));
				Plane plane = getPlaneByID(rs.getInt("plane"));
				Flight flight = new Flight(rs.getInt("ID"), aps.get(0), aps.get(1), rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
				flights.add(flight);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flights;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public int getLastFlightID () {
		try {
			ResultSet rs = this.querryDB(SQLQuerries.getLastFlightID);
			int flightid;
			if (rs.next() == true) {
				flightid = rs.getInt("ID");
			} else {
				flightid = -1;
			}
			rs.close();
			return flightid;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -9999; // weil es -1 schon gibt -> zum besseren debuggen
	} 

	/**
	 * @param f
	 * @param planeid
	 * @return
	 * @throws Exception
	 */
	public int checkFlightInDB (Frame f, int planeid) {
		try {
			ResultSet rs = this.querryDB("SELECT ID FROM flights WHERE plane == " + planeid + " AND flightnr == '" + f.getFlightnumber() + "' AND endTime IS NULL");
			int flightID;
			if (rs.next() == true) {
				flightID = rs.getInt("ID");
			} else {
				flightID = -1;
			}
			rs.close();
			return flightID;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -9999; // weil es -1 schon gibt -> zum besseren debuggen
	}

	public List<Integer> checkEnded () {
		List<Integer> flightIDs = new ArrayList<Integer>();
		try {
			ResultSet rs = this.querryDB(SQLQuerries.checkEndOfFlight);

			while (rs.next()) {
				flightIDs.add(rs.getInt(1));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flightIDs;
	}

	public Flight getFlightByID (int id)
			throws DataNotFoundException {
		Flight f = null;
		try {
			ResultSet rs = querryDB(SQLQuerries.getFlightByID + id);

			if (rs.next()) {
				Object[] airports = getAirports(rs.getString("src"), rs.getString("dest")).toArray();
				f = new Flight(rs.getInt("ID"), (Airport) airports[0], (Airport) airports[1], rs.getString("callsign"), getPlaneByID(rs.getInt("plane")), rs.getString("flightnr"), getTrackingByFlight(rs.getInt("ID")));
			} else {
				Airline a = new Airline(-1, "None", "None");
				Airport airnull = new Airport(-1, "None", "None", new Position(0d, 0d));
				Plane p = new Plane(-1, "None", "None", "None", "None", a);
				f = new Flight(-1, airnull, airnull, "None", p, "None", new HashMap<Integer, DataPoint>());
				// throws DataNotFoundException, to signal that there were no data found
				throw new DataNotFoundException();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}

	public List<Flight> getAllFlightsFromID (int start_id, int end_id) {
		List<Flight> flights = new ArrayList<Flight>();
		try {
			ResultSet rs = querryDB("SELECT * FROM flights WHERE ID >= " + start_id + " AND ID <= " + end_id + " AND endTime IS NULL");
			int counter = 0;
			while (rs.next() && counter <= end_id-start_id) { // counter: immer begrenzte Anzahl an Datensätzen
				HashMap<Integer, DataPoint> dps = getTrackingByFlight(rs.getInt("ID"));
				List<Airport> aps = getAirports(rs.getString("src"), rs.getString("dest"));
				Plane plane = getPlaneByID(rs.getInt("plane"));
				Flight flight = new Flight(rs.getInt("ID"), aps.get(0), aps.get(1), rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
				flights.add(flight);
				counter++;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flights;
	}
}

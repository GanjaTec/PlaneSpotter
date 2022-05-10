package planespotter.model;

import planespotter.constants.SQLQuerries;
import planespotter.controller.Controller;
import planespotter.dataclasses.*;
import planespotter.dataclasses.Frame;
import planespotter.display.UserSettings;
import planespotter.throwables.DataNotFoundException;

import java.sql.*;
import java.util.*;

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
	private static int maxLoadedFlights = UserSettings.getMaxLoadedData();


	/**
	 * This method is used to querry the DB
	 * it takes a String and returns a ResultSet
	 * 
	 * @param querry String to use for the Querry
	 * @return ResultSet containing the querried Data
	 */

	public ResultSet querryDB(String querry) throws Exception {
		ResultSet rs;
		var conn = SupperDB.getDBConnection();
		var stmt = conn.createStatement();
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
		var splitCoords = coords.split(",");
		var processedCoords = new ArrayList<Double>();

		for(String s : splitCoords) {
			processedCoords.add(Double.parseDouble(s));
		}

		var p = new Position(processedCoords.get(0), processedCoords.get(1));
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
	 */
	public Airline getAirlineByTag (String tag) {
		Airline a = null;
		try {
			tag = Controller.stripString(tag);
			var rs = querryDB(SQLQuerries.getAirlineByTag + tag); // ist leer TODO fixen
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
			var ds = new Deserializer();
			tag = ds.stripString(tag);
			tag = "'" + tag + "'";
			var rs = querryDB(SQLQuerries.getAirlineIDByTag + tag);
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
		var aps = new ArrayList<Airport>();
		try {
			var rsSrc = querryDB(SQLQuerries.getAirportByTag + srcAirport);
			var rsDst = querryDB(SQLQuerries.getAirportByTag + destAirport);

			if (rsSrc.next()) {
				var srcAp = new Airport(rsSrc.getInt("ID"), rsSrc.getString("iatatag"), rsSrc.getString("name"), convertCoords(rsSrc.getString("coords")));
				aps.add(srcAp);

			} else {
				var srcAp = new Airport(0, "None", "None", new Position(0.0f, 0.0f));
				aps.add(srcAp);
			}

			if (rsDst.next()) {
				var dstAp = new Airport(rsDst.getInt("ID"), rsDst.getString("iatatag"), rsDst.getString("name"), convertCoords(rsDst.getString("coords")));
				aps.add(dstAp);
			} else {
				var dstAp = new Airport(0, "None", "None", new Position(0.0f, 0.0f));
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
	public ArrayDeque<Integer> getPlaneIDsByICAO (String icao) throws DataNotFoundException {
		// TODO: Bug fixen
		// org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (unrecognized token: "06A1EB")
		//icao = stripString(icao);
		icao = Utilities.packString(icao);
		var ids = new ArrayDeque<Integer>();
		try {
			var rs = querryDB(SQLQuerries.getPlaneIDsByICAO + icao);
			while (rs.next()) {
				int id = rs.getInt("ID");
				ids.add(id);
			}
			rs.close();
			if (ids.size() < 1) {
				throw new DataNotFoundException("No plane id found for icao " + icao + "!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ids;
	}

	/**
	 * @param id is the plane id
	 * @return plane with the given id
	 */
	public Plane getPlaneByID (int id) throws DataNotFoundException {
		Plane p = null;
		try {
			var rs = querryDB(SQLQuerries.getPlaneByID + id);
			if (rs.next()) {
				var a = new Airline(-1, "BIA", "BUFU Int. Airlines");
				//Airline a = getAirlineByTag(rs.getString("airline"));
				p = new Plane(rs.getInt("ID"), rs.getString("icaonr"), rs.getString("tailnr"), rs.getString("type"), rs.getString("registration"), a);
			} else {
				var a = new Airline(-1, "None", "None");
				p = new Plane(-1, "None", "None", "None", "None", a);
				throw new DataNotFoundException();
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
		var planeFilter = "SELECT ID FROM planes WHERE icaonr = '" + icao + "' LIMIT 1";
		try {
			var rs = this.querryDB(planeFilter);
			int id;
			if (rs.next()) {
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
	public List<DataPoint> getTrackingByFlight (int flightID) {
		var dps = new ArrayList<DataPoint>();
		//var flight_id = Utilities.packString(flightID);
		try {
			var rs = querryDB("SELECT * FROM tracking WHERE flightid = " + flightID);
			while (rs.next()) {
				// TODO: IF STATEMENT
				var p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
				var dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), p, rs.getInt("timestamp"),
						rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
				dps.add(dp);
			}
			if (dps.isEmpty()) {
				throw new DataNotFoundException("No tracking found for flight id " + flightID);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dps;

	}

	public HashMap<Integer, DataPoint> getCompleteTrackingByFlight (int flightID) {
		var dps = new HashMap<Integer, DataPoint>();
		//var flight_id = Utilities.packString(flightID);
		try {
			var rs = querryDB("SELECT * FROM tracking WHERE flightid = " + flightID);
			while (rs.next()) {
				// TODO: IF STATEMENT
				var p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
				var dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), p, rs.getInt("timestamp"),
						rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
				dps.put(rs.getInt("ID"), dp);
			}
			if (dps.isEmpty()) {
				throw new DataNotFoundException("No tracking found for flight id " + flightID);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dps;

	}


	public long getLastTimestempByFlightID (int id) {
		long timestamp = -1;
		var getLastTracking = "SELECT timestamp FROM tracking WHERE flightid == "+ id +" ORDER BY ID DESC LIMIT 1";
		try {
			var rs = this.querryDB(getLastTracking);

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

	public final DataPoint getLastTrackingByFlightID (final int id)
			throws DataNotFoundException {
		DataPoint dp = null;
		var getLastTrackingByFlightID =  "SELECT * FROM tracking WHERE flightid = '"+ id +"' ORDER BY ID DESC LIMIT 1";
		try {
			var rs = querryDB(getLastTrackingByFlightID);
			while (rs.next()) {
				var p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
				dp = new DataPoint(rs.getInt("ID"), id, p, rs.getInt("timestamp"),
						rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));

			}
			if (dp == null) {
				throw new DataNotFoundException("No last tracking found for flight id " + id);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dp;
	}

	public final int getLastTrackingIDByFlightID (final int flightID)
			throws DataNotFoundException {
		int tid = -1;
		var getLastTrackingByFlightID =  "SELECT ID FROM tracking WHERE flightid = '"+ flightID +"' ORDER BY ID DESC LIMIT 1";
		try {
			var rs = querryDB(getLastTrackingByFlightID);
			if (rs.next()) {
				tid = rs.getInt("ID");
			}
			if (tid == -1) {
				throw new DataNotFoundException("No last tracking found for flight id " + flightID);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tid;
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
	public List<Flight> getAllFlights () {  // TODO between
		var flights = new ArrayList<Flight>();
		try {
			var rs = super.querryDB(SQLQuerries.getFlights);
			int counter = 0;
			while (rs.next() && counter <= maxLoadedFlights) { // counter: max flights -> to limit the incoming data (prevents a crash)
				var dps = this.getCompleteTrackingByFlight(rs.getInt("ID"));
				var aps = this.getAirports(rs.getString("src"), rs.getString("dest"));
				var plane = this.getPlaneByID(rs.getInt("plane"));
				var flight = new Flight(rs.getInt("ID"), aps.get(0), aps.get(1), rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
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
	public ArrayDeque<Integer> getFlightIDsByCallsign (String callsign) {
		callsign = Utilities.packString(callsign);
		var ids = new ArrayDeque<Integer>();
		try {
			var rs = querryDB(SQLQuerries.getFlightIDsByCallsign + callsign);
			while (rs.next()) {
				ids.add(rs.getInt("ID"));
			}
			rs.close();
			if (ids.isEmpty()) {
				throw new DataNotFoundException("No flights ids found for callsign " + callsign);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ids;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public int getLastFlightID () {
		try {
			var rs = this.querryDB(SQLQuerries.getLastFlightID);
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
			var rs = this.querryDB("SELECT ID FROM flights WHERE plane == " + planeid + " AND flightnr == '" + f.getFlightnumber() + "' AND endTime IS NULL");
			int flightID;
			if (rs.next()) {
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
		var flightIDs = new ArrayList<Integer>();
		try {
			var rs = this.querryDB(SQLQuerries.checkEndOfFlight);

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
			ResultSet rs = super.querryDB(SQLQuerries.getFlightByID + id);

			if (rs.next()) {
				var airports = getAirports(rs.getString("src"), rs.getString("dest")).toArray();
				var tracking = this.getCompleteTrackingByFlight(rs.getInt("ID"));
				f = new Flight(rs.getInt("ID"), (Airport) airports[0], (Airport) airports[1], rs.getString("callsign"), getPlaneByID(rs.getInt("plane")), rs.getString("flightnr"), tracking);
			} else {
				var nullAirline = new Airline(-1, "None", "None");
				var nullAirport = new Airport(-1, "None", "None", new Position(0d, 0d));
				var nullPlane = new Plane(-1, "None", "None", "None", "None", nullAirline);
				f = new Flight(-1, nullAirport, nullAirport, "None", nullPlane, "None", new HashMap<Integer, DataPoint>());
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

	public List<Flight> getAllFlightsBetween (int start_id, int end_id) {
		var flights = new ArrayList<Flight>();
		try {
			var rs = super.querryDB(	"SELECT * FROM flights " +
												"WHERE (ID BETWEEN " + start_id + " AND " + end_id + ") " +
												"AND endTime IS NULL");
			int counter = 0;
			while (rs.next() && counter <= end_id-start_id) { // counter: immer begrenzte Anzahl an Datensätzen
				var dps = getCompleteTrackingByFlight(rs.getInt("ID"));
				var aps = getAirports(rs.getString("src"), rs.getString("dest"));
				var plane = getPlaneByID(rs.getInt("plane"));
				var flight = new Flight(rs.getInt("ID"), aps.get(0), aps.get(1), rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
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
	 * @return length of a certain database table
	 * @param table is the table name
	 */
	public int getEntriesByFlightID (String table, int flightID) {
		try {
			var lengthRS = super.querryDB("SELECT count(*) FROM " + table + " WHERE flightid == " + flightID);
			int length = lengthRS.getInt(1);
			return length; // no rs.close needed
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -9999;
	}

	public ArrayDeque<Integer> getAllFlightIDsWithPlaneIDs (ArrayDeque<Integer> ids) { // TODO JOIN
		var flights = new ArrayDeque<Integer>();
		try {
			String querry = "SELECT ID FROM flights " +
							"WHERE plane IN " + this.IN_INT(ids);
			var rs = super.querryDB(querry);
			while (rs.next())  {
				int id = rs.getInt("ID");
				flights.add(id);
			}
			if (flights.isEmpty()) {
				throw new DataNotFoundException("No flight IDs found for these plane IDs \n" + ids);
			}
			rs.close();
		} catch (Exception e) { // FIXME: 03.05.2022 sollte querryDB eine Exception werfen? oder eine SQLException?
			e.printStackTrace();
		}
		return flights;
	}

	/**
	 * TODO hier muss ein Fehler drin sein!! oder in der Querry
	 * @param planetypes are the planetypes to search for
	 * @return
	 * @throws DataNotFoundException
	 */
	public ArrayDeque<Integer> getFlightIDsByPlaneTypes (ArrayDeque<String> planetypes) throws DataNotFoundException {
		var ids = new ArrayDeque<Integer>();
		try {
			var querry = 	"SELECT f.ID FROM flights f " +
							"JOIN planes p ON ((p.ID = f.plane) AND (f.endTime IS NULL))" +
							"WHERE p.type IN " + this.IN_STR(planetypes);
			var rs = super.querryDB(querry);
			while (rs.next()) {
				var id = rs.getInt("ID");
				ids.add(id);

			}
			rs.close();
			if (ids.size() < 1) {
				throw new DataNotFoundException("No plane IDs found for type " + planetypes + "!");
			}
		} catch (Exception e) { // FIXME: 03.05.2022 sollte querryDB eine Exception werfen? oder eine SQLException?
			e.printStackTrace();
		}
		return ids;
	}

	/**
	 * @param tailNr is the plane tail number
	 * @return plane with tailNr
	 * @throws DataNotFoundException if there was no plane found
	 */
	public ArrayDeque<Integer> getPlaneIDsByTailNr (String tailNr) throws DataNotFoundException {
		tailNr = Utilities.packString(tailNr);
		var ids = new ArrayDeque<Integer>();
		try {
			var rs = super.querryDB(SQLQuerries.getPlaneIDByTailNr + tailNr);
			while (rs.next()) {
				int id = rs.getInt("ID");
				ids.add(id);
			}
			rs.close();
			if (ids.size() < 1) {
				throw new DataNotFoundException("No plane found for tailnumber " + tailNr + "!");
			}
		} catch (Exception e) { // FIXME: 03.05.2022 sollte querryDB eine Exception werfen? oder eine SQLException?
			e.printStackTrace();
		}
		return ids;
	}

	/**
	 * @return list of all planetypes
	 */
	public ArrayDeque<String> getAllPlanetypesLike (final String planetype) {
		var allTypes = new ArrayDeque<String>();
		try {
			var querry = "SELECT type FROM planes WHERE type LIKE '_" + planetype + "%' GROUP BY type";
			var rs = super.querryDB(querry);
			while (rs.next()) {
				allTypes.add(rs.getString("type"));
			}
			rs.close();
			return allTypes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return list of all callsigns
	 */ // FIXME: 05.05.2022 HIER IST EIN FEHLER !!
	public ArrayDeque<String> getAllCallsignsLike (String callsign) {
		var allCallsigns = new ArrayDeque<String>();
		try {
			var rs = super.querryDB("SELECT DISTINCT callsign FROM flights WHERE callsign LIKE '_" + callsign + "%'");
			while (rs.next()) {
				allCallsigns.add(rs.getString("callsign"));
			}
			rs.close();
			return allCallsigns;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *
	 * @param flightIDs are the flight ids
	 * @return last tracking point for each flight id
	 */ // TODO braucht noch ewig!! -> fixen
	public final ArrayDeque<DataPoint> getLastTrackingsByFlightIDs (final ArrayDeque<Integer> flightIDs) {
		var dps = new ArrayDeque<DataPoint>();
		try {
			var querry = "SELECT max(t.ID) AS ID, t.flightid, t.latitude, t.longitude, t.altitude, t.groundspeed, t.heading, t.squawk, t.timestamp FROM tracking t WHERE flightid IN " + IN_INT(flightIDs) + " GROUP BY flightid";
			var rs = super.querryDB(querry);
			while (rs.next()) {
				var p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
				var dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), p, rs.getInt("timestamp"),
						rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
				dps.add(dp);
			}
			if (dps.isEmpty()) {
				throw new DataNotFoundException("No data points found for " + flightIDs + "!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dps;
	}

	public final ArrayDeque<DataPoint> getLastTrackingsBetweenFlightIDs(final int from, final int to) {
		var dps = new ArrayDeque<DataPoint>();
		try {
			//var querry = "SELECT f.*, t1.* FROM flights f JOIN tracking t1 ON (f.ID = t1.flightid) LEFT OUTER JOIN tracking t2 ON (f.ID = t2.flightid AND (t1.ID < t2.ID)) WHERE t2.ID IS NULL AND f.ID BETWEEN " + from + " AND " + to; //group by flightid
			var querry = 	"SELECT f.ID, f.endTime, max(t.ID), t.ID, flightid, latitude, " +
									"longitude, squawk, groundspeed, heading, altitude, timestamp " +
							"FROM flights f, tracking t " +
							"WHERE (f.endTime IS NULL) " +
							"AND (f.ID = t.flightid) " +
							"AND flightid BETWEEN " + from + " and " + to + " GROUP BY flightid "; // evtl JOIN ?? läuft aber gut, schneller als die obere querry
			/*var querry = 	"SELECT t.*, max(t.ID) FROM tracking t " +
							"JOIN flights f ON ((f.ID = t.flightid) " +
							"AND (f.endTime IS NULL)) " +
							"WHERE (t.flightid BETWEEN " + from + " AND " + to + ")";*/
			var rs = super.querryDB(querry);
			while (rs.next()) {
				var p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
				var dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), p, rs.getInt("timestamp"),
						rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
				dps.add(dp);
			}
			if (dps.isEmpty()) {
				throw new DataNotFoundException("No live flights found between " + from + "-" + to + " !");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dps;
	}

	public final ArrayDeque<Integer> getLiveFlightIDs (final int from, final int to) {
		var ids = new ArrayDeque<Integer>();
		try {
			var rs = super.querryDB(	"SELECT ID FROM flights WHERE endTime IS NOT NULL AND ID BETWEEN "
												+ from + " AND " + to);
			while (rs.next()) {
				ids.add(rs.getInt("ID"));
			}
			rs.close();
			if (ids.isEmpty()) {
				throw new DataNotFoundException("Couldn't load Live Flight IDs!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ids;
	}

	public final List<DataPoint> getLiveTrackingBetween (final int from, final int to) {
		var liveTracking = this.getLastTrackingsBetweenFlightIDs(from, to);
		return new ArrayList<>(liveTracking);

	}

	// TODO public ArrayDeque<Integer> getPlaneIDsWhereTypeLike (String input) // sollte schneller sein

	/**
	 *
	 * @param inThis
	 * @return
	 */ // TODO zu einer Methode machen mit Wildcards
	private String IN_INT(final ArrayDeque<Integer> inThis) {
		var out = "(";
		for (int i : inThis) {
			out += i + ",";
		}
		return out.substring(0, out.length()-2) + ")";
	}
	private String IN_STR (final ArrayDeque<String> inThis) {
		var out = "(";
		int counter = 0;
		for (var s : inThis) {
			if (counter == inThis.size()-1) {
				out += Utilities.packString(s);
			} else {
				out += Utilities.packString(s) + ", ";
			}
			counter++;
		}
		return out + ")";
	}

}

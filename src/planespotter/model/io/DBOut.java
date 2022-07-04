package planespotter.model.io;

import planespotter.constants.SQLQueries;
import planespotter.dataclasses.*;
import planespotter.constants.UserSettings;
import planespotter.dataclasses.DBResult;
import planespotter.throwables.NoAccessException;
import planespotter.util.Utilities;
import planespotter.throwables.DataNotFoundException;

import java.sql.*;
import java.util.*;

/**
 * @name DBOut
 * @author Lukas
 * @author Bennet
 * @author jml04
 * @version 1.1
 * @description
 * This DBConnector-child-class is used to get Output from
 * the DB and uses Queries provided by the SQLQueries Class
 * some methods are deprecated and will be removed, others will
 * be updated and/or improved...
 * @see planespotter.constants.SQLQueries
 * @see planespotter.model.io.DBConnector
 *
 */

public class DBOut extends DBConnector {
	// (ONE and ONLY) main instance
	private static final DBOut INSTANCE;
	// static initializer
	static {
		INSTANCE = new DBOut();
	}

	/**
	 * getter for main DBOut-instance
	 *
	 * @return main instance of the DBOut class
	 */
	public static DBOut getDBOut() {
		return INSTANCE;
	}

	/**
	 * private constructor for main instance
	 */
	protected DBOut() {
		// do nothing, no fields to initialize
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
		// TODO: 12.06.2022 glaube man kann hier auch ein 2er Array machen
		var splitCoords = coords.split(",");
		var processedCoords = new ArrayList<Double>();

		for(String s : splitCoords) {
			processedCoords.add(Double.parseDouble(s));
		}

		return new Position(processedCoords.get(0), processedCoords.get(1));
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
	public final Airline getAirlineByTag(String tag)
			throws DataNotFoundException {

		Airline a = null;
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB(SQLQueries.GET_AIRLINE_BY_TAG + tag);
				var rs = result.resultSet();
				if (rs.next()) {
					a = new Airline(rs.getInt("ID"), rs.getString("iatatag"), rs.getString("name"), rs.getString("country"));
				} else {
					a = new Airline(-1, "None", "None", "None");
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return a;
	}

	/**
	 * This Method is used to query both: the Departure (src) and the Arrival (dest)
	 * Airports of a Flight and returns a List containing two Airport Objects
	 *
	 * @param srcAirport String containing the Departure Airports IATA Tag
	 * @param destAirport String containing the Arrival Airports IATA Tag
	 * @return Airport array with length of 2, containing the Airport Objects
	 */

	public final Airport[] getAirports(final String srcAirport, final String destAirport) {
		// source- and dest-airport
		Airport src, dest;
		// airport-output array
		final var aps = new Airport[2];
		// null-Airport, stands for 'no airport found'
		var nullAirport = new Airport(-1, "None", "None", new Position(0., 0.));
		try {
			// synchronizing on DB-sync to prevent SQLITE_BUSY error
			synchronized (DB_SYNC) {
				// getting db-results
				var srcResult = super.queryDB(SQLQueries.GET_AIRPORT_BY_TAG + Utilities.packString(srcAirport));
				var destResult = super.queryDB(SQLQueries.GET_AIRPORT_BY_TAG + Utilities.packString(destAirport));
				// getting result sets
				var rsSrc = srcResult.resultSet();
				var rsDst = destResult.resultSet();
				// getting airports
				src = (rsSrc.next()) ? new Airport(rsSrc.getInt("ID"), rsSrc.getString("iatatag"), rsSrc.getString("name"), convertCoords(rsSrc.getString("coords")))
						: nullAirport;
				dest = (rsDst.next()) ? new Airport(rsDst.getInt("ID"), rsDst.getString("iatatag"), rsDst.getString("name"), convertCoords(rsDst.getString("coords")))
						: nullAirport;
				// filling airport array
				aps[0] = src;
				aps[1] = dest;
				// closing db-results
				srcResult.close();
				destResult.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return aps;
	}

	/**
	 * This Method is used to query a single Plane by its ICAO Tag
	 * It takes a String containing the ICAO Tag and returns a Plane Object
	 *
	 * @param icao String containing the ICAO Tag
	 * @return Plane the Object containing all Information about the Plane
	 */
	public final ArrayDeque<Integer> getPlaneIDsByICAO(String icao)
			throws DataNotFoundException {

		// TODO: Bug fixen
		// org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (unrecognized token: "06A1EB")
		//icao = stripString(icao);
		icao = Utilities.packString(icao);
		final var ids = new ArrayDeque<Integer>();
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB(SQLQueries.GET_PLANE_IDS_BY_ICAO + icao);
				var rs = result.resultSet();
				while (rs.next()) {
					int id = rs.getInt("ID");
					ids.add(id);
				}
				result.close();
			}
			if (ids.size() < 1) {
				throw new DataNotFoundException("No plane id found for icao " + icao + "!");
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return ids;
	}

	/**
	 * gets a plane by its ID
	 *
	 * @param id is the plane id
	 * @return plane with the given id
	 */
	public final Plane getPlaneByID(int id)
			throws DataNotFoundException {

		Airline airline;
		Plane plane = null;
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB(SQLQueries.GET_PLANE_BY_ID + id);
				var rs = result.resultSet();
				if (rs.next()) {
					airline = this.getAirlineByID(rs.getInt("airline"));
					plane = new Plane(rs.getInt("ID"), rs.getString("icaonr"), rs.getString("tailnr"), rs.getString("type"), rs.getString("registration"), airline);
				}
				result.close();

				/*var pstmt = super.createPreparedStatement(SQLQueries.GET_PLANE_BY_ID + "(?)");
				pstmt.setInt(1, id);
				pstmt.execute();
				var rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
					airline = this.getAirlineByID(rs.getInt("airline"));
					plane = new Plane(rs.getInt("ID"), rs.getString("icaonr"), rs.getString("tailnr"), rs.getString("type"), rs.getString("registration"), airline);
				}
				rs.close();
				*/
				if (plane == null) {
					throw new DataNotFoundException("No plane found for ID " + id + "!");
				}
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		}
		return plane;
	}

	public final Airline getAirlineByID(final int id)
			throws DataNotFoundException {

		Airline airline = null;
		try {
			var query = "SELECT * FROM airlines a " +
					"JOIN planes p " +
					"ON ((p.airline = a.ID) " +
					"AND (p.ID = " + id + "))";
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				if (rs.next()) {
					final int airlineID = rs.getInt("ID");
					final String tag = rs.getString("icaotag"),
							name = rs.getString("name"),
							country = rs.getString("country");
					airline = new Airline(airlineID, tag, name, country);
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		}
		return (airline != null) ? airline : new Airline(-1, "None", "None", "None");
	}

	/**
	 * @param icao
	 * @return
	 */
	public final int checkPlaneInDB(String icao) {

		var planeFilter = "SELECT ID FROM planes WHERE icaonr = '" + icao + "' LIMIT 1";
		try {
			synchronized (DB_SYNC) {
				var result = this.queryDB(planeFilter);
				var rs = result.resultSet();
				int id;
				if (rs.next()) {
					id = rs.getInt(1);
				} else {
					id = -1;
				}
				result.close();
				return id;
			}
		} catch (SQLException | NoAccessException e) {
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
	 * @throws DataNotFoundException
	 */
	// TODO fix: HashMap lentgh was 1, but last Tracking id was about 23000
	// TODO hier muss der Fehler sein
	public final Vector<DataPoint> getTrackingByFlight(int flightID)
			throws DataNotFoundException {

		final var dps = new Vector<DataPoint>();
		//var flight_id = Utilities.packString(flightID);
		try {
			synchronized (DB_SYNC) {
				var result = queryDB("SELECT * FROM tracking WHERE flightid = " + flightID);
				var rs = result.resultSet();
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
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return dps;

	}

	public final HashMap<Integer, DataPoint> getCompleteTrackingByFlight(int flightID)
			throws DataNotFoundException {

		final var dps = new HashMap<Integer, DataPoint>();
		//var flight_id = Utilities.packString(flightID);
		try {
			synchronized (DB_SYNC) {
				var result = queryDB("SELECT * FROM tracking WHERE flightid = " + flightID);
				var rs = result.resultSet();
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
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return dps;

	}


	public final long getLastTimestempByFlightID(int id)
			throws DataNotFoundException {

		long timestamp = -1;
		// TODO eventuell ist das neue schneller
		var getLastTracking = "SELECT max(timestamp) FROM tracking WHERE flightid = " + id;
		//var getLastTracking = "SELECT timestamp FROM tracking WHERE flightid == "+ id +" ORDER BY ID DESC LIMIT 1";
		try {
			synchronized (DB_SYNC) {
				var result = this.queryDB(getLastTracking);
				var rs = result.resultSet();

				while (rs.next()) {
					timestamp = rs.getLong(1);
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return timestamp;
	}

	public final DataPoint getLastTrackingByFlightID(final int id)
			throws DataNotFoundException {

		DataPoint dp = null;
		var query =  "SELECT * FROM tracking WHERE flightid = '"+ id +"' ORDER BY ID DESC LIMIT 1";
		try {
			synchronized (DB_SYNC) {
				var result = queryDB(query);
				var rs = result.resultSet();

				while (rs.next()) {
					var p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
					dp = new DataPoint(rs.getInt("ID"), id, p, rs.getInt("timestamp"),
							rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));

				}
				if (dp == null) {
					throw new DataNotFoundException("No last tracking found for flight id " + id);
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return dp;
	}

	public final int getLastTrackingIDByFlightID(final int flightID)
			throws DataNotFoundException {

		int tid = -1;
		var getLastTrackingByFlightID =  "SELECT ID FROM tracking WHERE flightid = '"+ flightID +"' ORDER BY ID DESC LIMIT 1";
		try {
			synchronized (DB_SYNC) {
				var result = queryDB(getLastTrackingByFlightID);
				var rs = result.resultSet();
				if (rs.next()) {
					tid = rs.getInt("ID");
				}
				if (tid == -1) {
					throw new DataNotFoundException("No last tracking found for flight id " + flightID);
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
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
	 */
	public final List<Flight> getAllFlights()
			throws DataNotFoundException {  // TODO between

		final var flights = new ArrayList<Flight>();
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB(SQLQueries.GET_FLIGHTS);
				var rs = result.resultSet();

				int counter = 0;
				while (rs.next() && counter <= UserSettings.getMaxLoadedData()) { // counter: max flights -> to limit the incoming data (prevents a crash)
					var dps = this.getCompleteTrackingByFlight(rs.getInt("ID"));
					var aps = this.getAirports(rs.getString("src"), rs.getString("dest"));
					var plane = this.getPlaneByID(rs.getInt("plane"));
					var flight = new Flight(rs.getInt("ID"), aps[0], aps[1], rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
					flights.add(flight);
					counter++;
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return flights;
	}

	/**
	 * @param callsign
	 * @return
	 */
	public final ArrayDeque<Integer> getFlightIDsByCallsign(String callsign)
			throws DataNotFoundException {

		callsign = Utilities.packString(callsign);
		final var ids = new ArrayDeque<Integer>();
		try {
			synchronized (DB_SYNC) {
				var result = queryDB(SQLQueries.GET_FLIGHT_IDS_BY_CALLSIGN + callsign);
				var rs = result.resultSet();

				while (rs.next()) {
					ids.add(rs.getInt("ID"));
				}
				result.close();
			}
			if (ids.isEmpty()) {
				throw new DataNotFoundException("No flights ids found for callsign " + callsign);
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return ids;
	}

	/**
	 * @return
	 */
	public int getLastFlightID()
			throws DataNotFoundException {

		try {
			synchronized (DB_SYNC) {
				var result = this.queryDB(SQLQueries.GET_LAST_FLIGHT_ID);
				var rs = result.resultSet();
				int flightid;
				if (rs.next()) {
					flightid = rs.getInt("ID");
				} else {
					flightid = -1;
				}
				result.close();
				return flightid;
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return -9999; // weil es -1 schon gibt -> zum besseren debuggen
	}

	public List<Integer> checkEnded()
			throws DataNotFoundException {

		final var flightIDs = new ArrayList<Integer>();
		try {
			synchronized (DB_SYNC) {
				var result = this.queryDB(SQLQueries.CHECK_END_OF_FLIGHT);
				var rs = result.resultSet();

				while (rs.next()) {
					flightIDs.add(rs.getInt(1));
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return flightIDs;
	}

	public Flight getFlightByID(int id)
			throws DataNotFoundException {

		Flight flight = null;
		Plane plane;
		try {
			synchronized (DB_SYNC) {
				DBResult result = super.queryDB(SQLQueries.GET_FLIGHT_BY_ID + id);
				ResultSet rs = result.resultSet();

				if (rs.next()) {
					var airports = this.getAirports(rs.getString("src"), rs.getString("dest"));
					var tracking = this.getCompleteTrackingByFlight(rs.getInt("ID"));
					plane = this.getPlaneByID(rs.getInt("plane"));
					flight = new Flight(rs.getInt("ID"), airports[0], airports[1], rs.getString("callsign"), plane, rs.getString("flightnr"), tracking);
				}
				result.close();
			}
			if (flight == null) {
				throw new DataNotFoundException("No Flight found for id " + id + "!");
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return flight;
	}

	public final List<Flight> getAllFlightsBetween(int start_id, int end_id)
			throws DataNotFoundException {

		final var flights = new ArrayList<Flight>();
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB("SELECT * FROM flights " +
						"WHERE (ID BETWEEN " + start_id + " AND " + end_id + ") " +
						"AND endTime IS NULL");
				var rs = result.resultSet();
				int counter = 0;
				while (rs.next() && counter <= end_id - start_id) { // counter: immer begrenzte Anzahl an Datensätzen
					var dps = getCompleteTrackingByFlight(rs.getInt("ID"));
					var aps = getAirports(rs.getString("src"), rs.getString("dest"));
					var plane = getPlaneByID(rs.getInt("plane"));
					var flight = new Flight(rs.getInt("ID"), aps[0], aps[1], rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
					flights.add(flight);
					counter++;
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return flights;
	}

	/**
	 * @return length of a certain database table
	 * @param table is the table name
	 */
	public int getEntriesByFlightID(String table, int flightID)
			throws DataNotFoundException {

		int count = -9999;
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB("SELECT count(*) FROM " + table + " WHERE flightid == " + flightID);
				var rs = result.resultSet();
				count = rs.getInt(1);
				rs.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return count;
	}

	public ArrayDeque<Integer> getAllFlightIDsWithPlaneIDs(ArrayDeque<Integer> ids)
			throws DataNotFoundException { // TODO JOIN

		final var flights = new ArrayDeque<Integer>();
		try {
			var query = "SELECT ID FROM flights " +
						"WHERE plane " + SQLQueries.IN_INT(ids);
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					int id = rs.getInt("ID");
					flights.add(id);
				}
				if (flights.isEmpty()) {
					throw new DataNotFoundException("No flight IDs found for these plane IDs \n" + ids);
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
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
	public final ArrayDeque<Integer> getFlightIDsByPlaneTypes(ArrayDeque<String> planetypes)
			throws DataNotFoundException {

		final var ids = new ArrayDeque<Integer>();
		try {
			var query = "SELECT f.ID FROM flights f " +
						"JOIN planes p ON ((p.ID = f.plane) AND (f.endTime IS NULL))" +
						"WHERE (p.type " + SQLQueries.IN_STR(planetypes) + ")";
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					var id = rs.getInt("ID");
					ids.add(id);

				}
				result.close();
			}
			if (ids.size() < 1) {
				throw new DataNotFoundException("No plane IDs found for type " + planetypes + "!");
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return ids;
	}

	/**
	 * @param tailNr is the plane tail number
	 * @return plane with tailNr
	 * @throws DataNotFoundException if there was no plane found
	 */
	public final ArrayDeque<Integer> getPlaneIDsByTailNr(String tailNr)
			throws DataNotFoundException {

		tailNr = Utilities.packString(tailNr);
		final var ids = new ArrayDeque<Integer>();
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB(SQLQueries.GET_PLANE_ID_BY_TAIL_NR + tailNr);
				var rs = result.resultSet();
				while (rs.next()) {
					int id = rs.getInt("ID");
					ids.add(id);
				}
				result.close();
			}
			if (ids.size() < 1) {
				throw new DataNotFoundException("No plane found for tailnumber " + tailNr + "!");
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return ids;
	}

	/**
	 * @return list of all planetypes
	 */
	public final ArrayDeque<String> getAllPlanetypesLike(final String planetype)
			throws DataNotFoundException {

		final var allTypes = new ArrayDeque<String>();
		try {
			var querry = "SELECT type FROM planes WHERE type LIKE '" + planetype + "%' GROUP BY type";
			synchronized (DB_SYNC) {
				var result = super.queryDB(querry);
				var rs = result.resultSet();
				while (rs.next()) {
					allTypes.add(rs.getString("type"));
				}
				result.close();
			}
			return allTypes;
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return null;
	}

	/**
	 * @return list of all callsigns
	 */
	public ArrayDeque<String> getAllCallsignsLike(final String callsign)
			throws DataNotFoundException {

		final var allCallsigns = new ArrayDeque<String>();
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB("SELECT DISTINCT callsign FROM flights WHERE callsign LIKE '" + callsign + "%'");
				var rs = result.resultSet();
				while (rs.next()) {
					allCallsigns.add(rs.getString("callsign"));
				}
				result.close();
			}
			return allCallsigns;
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return null;
	}

	/**
	 * @return list of all callsigns
	 */ // FIXME: 05.05.2022 HIER IST EIN FEHLER !!
	public int[] getFlightIDsIDsByAirportTag(String tag)
			throws DataNotFoundException {

		int[] fids = new int[0];
		var query = "SELECT DISTINCT f.ID " +
					"FROM flights f " +
					"JOIN airports a " +
					"ON (a.iatatag IS f.src " +
					"OR a.iatatag IS f.dest) " +
					"AND a.iatatag IS " + Utilities.packString(tag.toUpperCase());
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				int length;
				while (rs.next()) {
					length = fids.length;
					fids = Arrays.copyOf(fids, length + 1);
					fids[length] = rs.getInt(1);
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		}
		if (fids.length == 0) {
			throw new DataNotFoundException("No flight IDs found for airport tag " + tag + "!");
		}
		return fids;
	}

	/**
	 *
	 * @param flightIDs are the flight ids
	 * @return last tracking point for each flight id
	 */ // TODO braucht noch ewig!! -> fixen
	public final Deque<DataPoint> getLastTrackingsByFlightIDs(final Deque<Integer> flightIDs)
			throws DataNotFoundException {

		var dps = new ArrayDeque<DataPoint>();
		try {
			var querry = 	"SELECT max(t.ID) AS ID, t.flightid, t.latitude, t.longitude, t.altitude, t.groundspeed, t.heading, t.squawk, t.timestamp " +
							"FROM tracking t " +
							"WHERE flightid " + SQLQueries.IN_INT(flightIDs) + " GROUP BY flightid";
			synchronized (DB_SYNC) {
				var result = super.queryDB(querry);
				var rs = result.resultSet();

				while (rs.next()) {
					var p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
					var dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), p, rs.getInt("timestamp"),
							rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
					dps.add(dp);
				}
				result.close();
			}
			if (dps.isEmpty()) {
				throw new DataNotFoundException("No data points found for " + flightIDs + "!");
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return dps;
	}

	public final Deque<DataPoint> getLastTrackingsBetweenFlightIDs(final int from, final int to)
			throws DataNotFoundException {

		final var dps = new ArrayDeque<DataPoint>();
		try {
			var querry = "SELECT f.ID, f.endTime, max(t.ID), t.ID, flightid, latitude, " +
					"longitude, squawk, groundspeed, heading, altitude, timestamp " +
					"FROM flights f, tracking t " +
					"WHERE (f.endTime IS NULL) " +
					"AND (f.ID = t.flightid) " +
					"AND flightid BETWEEN " + from + " and " + to + " GROUP BY flightid"; // evtl JOIN ?? läuft aber gut, schneller als die obere querry
			/*var querry = 	"SELECT t.*, max(t.ID) FROM tracking t " +
							"JOIN flights f ON ((f.ID = t.flightid) " +
							"AND (f.endTime IS NULL)) " +
							"WHERE (t.flightid BETWEEN " + from + " AND " + to + ") "
							"GROUP BY flightid";*/
			synchronized (DB_SYNC) {
				var result = super.queryDB(querry);
				var rs = result.resultSet();
				Position pos;
				DataPoint dp;
				while (rs.next()) {
					pos = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
					dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), pos, rs.getInt("timestamp"),
							rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
					dps.add(dp);
				}
				result.close();
			}
			if (dps.isEmpty()) {
				throw new DataNotFoundException("No live flights found between " + from + "-" + to + " !");
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 


		return dps;
	}

	public final Deque<Integer> getLiveFlightIDs(final int from, final int to)
			throws DataNotFoundException {

		final var ids = new ArrayDeque<Integer>();
		try {
			var query = "SELECT ID " +
						"FROM flights " +
						"WHERE endTime IS NOT NULL " +
						"AND ID BETWEEN " + from + " AND " + to;
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					ids.add(rs.getInt("ID"));
				}
				result.close();
			}
			if (ids.isEmpty()) {
				throw new DataNotFoundException("Couldn't load Live Flight IDs!");
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return ids;
	}

	/**
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public final Vector<DataPoint> getLiveTrackingBetween(final int from, final int to)
			throws DataNotFoundException {

		final var liveTracking = this.getLastTrackingsBetweenFlightIDs(from, to);
		return new Vector<>(liveTracking);
	}

	/**
	 *
	 * @param tag, the airport tag
	 * @return
	 * @throws DataNotFoundException
	 */
	public final Deque<DataPoint> getTrackingsWithAirportTag(String tag)
			throws DataNotFoundException {

		final var dps = new ArrayDeque<DataPoint>();
		try {
			var querry = "SELECT t.* FROM tracking t " + // TODO!!! threaden!, viele daten!
					"JOIN flights f ON ((f.ID = t.flightid) " +
					"AND ((f.src LIKE '" + tag + "') " +
					"OR (f.dest LIKE '" + tag + "')))"; // eventuell vor/hinterher _ einfügen
			synchronized (DB_SYNC) {
				var result = super.queryDB(querry);
				var rs = result.resultSet();
				DataPoint dp;
				while (rs.next()) {
					dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"),
							new Position(rs.getDouble("latitude"), rs.getDouble("longitude")),
							rs.getLong("timestamp"), rs.getInt("squawk"), rs.getInt("groundspeed"),
							rs.getInt("heading"), rs.getInt("altitude"));
					dps.add(dp);
				}
				result.close();
			}
			if (dps.isEmpty()) {
				throw new DataNotFoundException("No flights found with airport tag " + tag + "!");
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		}
		return dps;
	}

	/**
	 * @return array deque off all airports which have flights
	 * @throws DataNotFoundException if there was no airport found
	 */
	public final Deque<Airport> getAllAirports()
			throws DataNotFoundException {

		final var aps = new ArrayDeque<Airport>();
		try {
			var query = SQLQueries.SELECT(false, "f.src", "f.dest") +
						SQLQueries.FROM("flights f");
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				int counter = 0;
				while (rs.next() && counter < 5000) { // TODO remove counter for all airports // could take a little time
					var src = rs.getString("src");
					var dest = rs.getString("dest");
					var nonNullAps = Arrays.stream(this.getAirports(src, dest))
							.filter(a -> !a.iataTag().equalsIgnoreCase("None"))
							.toList();
					aps.addAll(nonNullAps);
					counter++;
				}
				result.close();
			}
			if (aps.isEmpty()) {
				throw new DataNotFoundException("no airports found in all flights!");
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return aps;
	}

	public final Vector<Position> getAllTrackingPositions()
			throws DataNotFoundException {

		final var positions = new Vector<Position>();
		try {
			var querry = "SELECT latitude, longitude " +
						 "FROM tracking ";
			synchronized (DB_SYNC) {
				var result = super.queryDB(querry);
				var rs = result.resultSet();
				Position pos;
				while (rs.next()) {
					pos = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
					positions.add(pos);
				}
				result.close();
			}
			if (positions.isEmpty()) {
				throw new DataNotFoundException("No trackings found!");
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return positions;
	}

	protected final HashMap<Position, Integer> speedMap(final int from, final int to)
			throws DataNotFoundException {

		final var speedMap = new HashMap<Position, Integer>();
		var querry = "SELECT t.latitude, t.longitude, t.groundspeed " +
					 "FROM tracking t " +
					 "WHERE (t.ID BETWEEN " + from + " AND " + to + ")";
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB(querry);
				var rs = result.resultSet();
				Position pos;
				int speed;
				while (rs.next()) {
					pos = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
					speed = rs.getInt("groundspeed");
					speedMap.put(pos, speed);
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		if (speedMap.isEmpty()) {
			throw new DataNotFoundException("No Speed Data found in the DB!");
		}
		return speedMap;
	}

	public final Deque<Integer> getAllFlightIDs()
			throws DataNotFoundException {

		final var ids = new ArrayDeque<Integer>();
		try {
			var query = "SELECT ID " +
						"FROM flights";
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					ids.add(rs.getInt("ID"));
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		if (ids.isEmpty()) {
			throw new DataNotFoundException("Couldn't find any plane! This is unusual!");
		}
		return ids;
	}

	/**
	 *
	 * @return Hash Map: (Key = icao, value[0] = planeID, value[1] = flightID )
	 */
	public final Map<String, int[]> icaoIDMap()
			throws DataNotFoundException {

		final var map = new HashMap<String, int[]>();
		try {
			var query = "SELECT p.icaonr AS icao, p.ID AS pid, f.ID AS fid " +
						"FROM planes p " +
						"JOIN flights f " +
						"ON (f.plane = p.ID)";
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				int[] values;
				while (rs.next()) {
					values = new int[]{
							rs.getInt("pid"),
							rs.getInt("fid")
					};
					map.put(rs.getString("icao"), values);
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return map;
	}

	public final Map<Integer, Long> getLiveFlightIDsWithTimestamp()
			throws DataNotFoundException {

		final var map = new HashMap<Integer, Long>();
		try {
			var query = "SELECT t.flightid AS fid, max(t.timestamp) AS ts " +
						"FROM tracking t " +
						"JOIN flights f " +
						"ON (f.ID = t.flightid) AND (f.endTime IS NULL)" +
						"GROUP BY t.flightid";
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					map.put(rs.getInt(1), rs.getLong(2));
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return map;
	}

	public final HashMap<String, Integer> getFlightNRsWithPlaneIDs()
			throws DataNotFoundException {

		final var map = new HashMap<String, Integer>();
		try {
			var query = "SELECT plane, flightnr " +
						"FROM flights " +
						"WHERE endTime IS NULL";
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					map.put(rs.getString(2), rs.getInt(1));
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return map;
	}

	public HashMap<String, Integer> getFlightNRsWithFlightIDs()
			throws DataNotFoundException {

		final var map = new HashMap<String, Integer>();
		try {
			var query = "SELECT ID, flightnr " +
						"FROM flights " +
						"WHERE (endTime IS NULL)";
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					map.put(rs.getString(2), rs.getInt(1));
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		}
		return map;
	}

	public final HashMap<String, Integer> getFlightNRsWithFlightIDs(Deque<Integer> flightIDs)
			throws DataNotFoundException {

		final var map = new HashMap<String, Integer>();
		try {
			var query = "SELECT ID, flightnr " +
						"FROM flights " +
						"WHERE (ID " + SQLQueries.IN_INT(flightIDs) + ") " +
						"AND (endTime IS NULL)";
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				String fnr;
				int id;
				while (rs.next()) {
					fnr = rs.getString(2);
					id = rs.getInt(1);
					if (!fnr.isBlank()) {
						map.put(fnr, id);
					}
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return map;
	}

	public final int getTableSize(final String table)
			throws DataNotFoundException {

		int size = -1;
		try {
			var query = "SELECT count(ID) AS size " +
						"FROM " + table;
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				if (rs.next()) {
					size = rs.getInt("size");
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		if (size == -1) {
			throw new DataNotFoundException("Table not found!");
		}
		return size;
	}

	public final Deque<String> getICAOsByPlaneIDs(final Deque<Integer> planeIDs)
			throws DataNotFoundException {

		final var icaos = new ArrayDeque<String>();
		try {
			var query = "SELECT icaonr " +
						"FROM planes " +
						"WHERE ID " + SQLQueries.IN_INT(planeIDs);
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					icaos.add(rs.getString(1));
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return icaos;
	}

	public final Deque<Integer> getAllPlaneIDs()
			throws DataNotFoundException {

		var ids = new ArrayDeque<Integer>();
		try {
			var query = "SELECT ID " +
						"FROM planes";
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					ids.add(rs.getInt(1));
				}
				result.close();
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		} 
		return ids;
	}

	public HashMap<String, Integer> getAirlineTagsIDs()
			throws DataNotFoundException {

		var map = new HashMap<String, Integer>();
		try {
			var query = "SELECT icaotag, ID FROM airlines";
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					map.put(rs.getString(1), rs.getInt(2));
				}
				result.close();
			}
			if (map.isEmpty()) {
				throw new DataNotFoundException("No Airline-Tags and IDs found!");
			}
		} catch (NoAccessException | SQLException e) {
			e.printStackTrace();
		} 
		return map;
	}

	public HashMap<String, Integer> getPlaneIcaosIDs()
			throws DataNotFoundException {

		var map = new HashMap<String, Integer>();
		try {
			var query = "SELECT icaonr, ID FROM planes";
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					map.put(rs.getString(1), rs.getInt(2));
				}
				result.close();
			}
			if (map.isEmpty()) {
				throw new DataNotFoundException("No Plane-ICAOs and IDs found!");
			}
		} catch (NoAccessException | SQLException e) {
			e.printStackTrace();
		}
		return map;
	}

	public Deque<String> getAllAirportTags()
			throws DataNotFoundException {

		var tags = new ArrayDeque<String>();
		var query = "SELECT src, dest FROM flights";
		 try {
			 synchronized (DB_SYNC) {
				 var result = super.queryDB(query);
				 var rs = result.resultSet();
				 while (rs.next()) {
					 String src = rs.getString("src");
					 String dest = rs.getString("dest");
					 if (src != null && !src.isBlank()) {
						 tags.add(src);
					 }
					 if (dest != null && !dest.isBlank()) {
						 tags.add(dest);
					 }
				 }
				 result.close();
			 }
			 if (tags.isEmpty()) {
				 throw new DataNotFoundException("Couldn't find any airport tag or flight!");
			 }
		 } catch (NoAccessException | SQLException e) {
			 e.printStackTrace();
		 }
		 return tags;
	}

	public Deque<String> getAllAirlineTags()
			throws DataNotFoundException {

		var tags = new ArrayDeque<String>();
		var query = "SELECT a.icaotag AS tag, p.airline " +
					"FROM airlines a " +
					"JOIN planes p " +
					"ON ((p.airline = a.ID) " +
					"AND (a.ID != 1))";
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				while (rs.next()) {
					tags.add(rs.getString("tag"));
				}
				result.close();
			}
			if (tags.isEmpty()) {
				throw new DataNotFoundException("Couldn't find any airline tag or plane!");
			}
		} catch (NoAccessException | SQLException e) {
			e.printStackTrace();
		}
		return tags;
	}

	public int[] getFlightIDsByAirportName(String name)
			throws DataNotFoundException {

		int[] ids = new int[0];
		var query = "SELECT f.ID " +
				"FROM flights f " +
				"JOIN airports a " +
				"ON (a.name LIKE '%" + name + "%')" +
				"AND (a.iatatag IS f.src OR a.iatatag IS f.dest)";
		try {
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				int length;
				while (rs.next()) {
					length = ids.length;
					ids = Arrays.copyOf(ids, length + 1);
					ids[length] = rs.getInt(1);
				}
				result.close();
			}
		} catch (NoAccessException | SQLException e) {
			e.printStackTrace();
		}
		if (ids.length == 0) {
			throw new DataNotFoundException("No airport tags found for name " + name + "!");
		}
		return ids;
	}

	public Vector<DataPoint> getTrackingsByFlightIDs(int[] ids)
			throws DataNotFoundException {

		final var dps = new Vector<DataPoint>();
		try {
			var query = "SELECT * FROM tracking " +
						"WHERE flightid " + SQLQueries.IN_INT(ids);
			synchronized (DB_SYNC) {
				var result = super.queryDB(query);
				var rs = result.resultSet();
				DataPoint dp;
				while (rs.next()) {
					dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"),
							new Position(rs.getDouble("latitude"), rs.getDouble("longitude")),
							rs.getLong("timestamp"), rs.getInt("squawk"), rs.getInt("groundspeed"),
							rs.getInt("heading"), rs.getInt("altitude"));
					dps.add(dp);
				}
				result.close();
			}
			if (dps.isEmpty()) {
				throw new DataNotFoundException("No tracking found for these IDs!");
			}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		}
		return dps;
	}

}

package planespotter.model.io;

import planespotter.constants.SQLQueries;
import planespotter.dataclasses.*;
import planespotter.dataclasses.Frame;
import planespotter.constants.UserSettings;
import planespotter.model.nio.Deserializer;
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
 *
 * This class is used to get Output from the DB
 * It relies heavily on planespotter.SQLQuerrys
 *
 */

public class DBOut extends SupperDB {

	/**
	 * test: max loaded flights
	 */
	private final int maxLoadedFlights = UserSettings.getMaxLoadedData();


	/**
	 * This method is used to querry the DB
	 * it takes a String and returns a ResultSet
	 * 
	 * @param querry String to use for the Querry
	 * @return ResultSet containing the querried Data
	 */

	public ResultSet querryDB(String querry) throws Exception { // TODO wir sollten die Exception hier catchen, weil sonst catchen wir woanders versehentlich auch andere exceptions
		ResultSet rs;
		var conn = SupperDB.getDBConnection();
		var stmt = conn.createStatement();
		rs = stmt.executeQuery(querry);
		// TODO: könnte man hier ein stmt.closeOnCompletion() einfügen?

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
	public Airline getAirlineByTag (String tag) {
		Airline a = null;
		try {
			tag = Utilities.stripString(tag);
			var rs = querryDB(SQLQueries.getAirlineByTag + tag); // ist leer TODO fixen
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
			var rs = querryDB(SQLQueries.getAirlineIDByTag + tag);
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

	public List<Airport> getAirports (String srcAirport, String destAirport) { // TODO man könnte return array machen (länge ist immer 2)
		var aps = new ArrayList<Airport>();
		try {
			var rsSrc = querryDB(SQLQueries.getAirportByTag + Utilities.packString(srcAirport));
			var rsDst = querryDB(SQLQueries.getAirportByTag + Utilities.packString(destAirport));

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
			var rs = querryDB(SQLQueries.getPlaneIDsByICAO + icao);
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
			var rs = querryDB(SQLQueries.getPlaneByID + id);
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
	public Vector<DataPoint> getTrackingByFlight (int flightID) {
		var dps = new Vector<DataPoint>();
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


	public long getLastTimestempByFlightID(int id) {
		long timestamp = -1;
		// TODO eventuell ist das neue schneller
		var getLastTracking = "SELECT max(timestamp) FROM tracking WHERE flightid = " + id;
		//var getLastTracking = "SELECT timestamp FROM tracking WHERE flightid == "+ id +" ORDER BY ID DESC LIMIT 1";
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
	 */
	public List<Flight> getAllFlights () {  // TODO between
		var flights = new ArrayList<Flight>();
		try {
			var rs = super.querryDB(SQLQueries.getFlights);
			int counter = 0;
			while (rs.next() && counter <= this.maxLoadedFlights) { // counter: max flights -> to limit the incoming data (prevents a crash)
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
	 */
	public ArrayDeque<Integer> getFlightIDsByCallsign (String callsign) {
		callsign = Utilities.packString(callsign);
		var ids = new ArrayDeque<Integer>();
		try {
			var rs = querryDB(SQLQueries.getFlightIDsByCallsign + callsign);
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
	 */
	public int getLastFlightID() {
		try {
			var rs = this.querryDB(SQLQueries.getLastFlightID);
			int flightid;
			if (rs.next()) {
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
	 */
	public int checkFlightInDB(Frame f, int planeid) {
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

	public List<Integer> checkEnded() {
		var flightIDs = new ArrayList<Integer>();
		try {
			var rs = this.querryDB(SQLQueries.checkEndOfFlight);

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

	public Flight getFlightByID(int id)
			throws DataNotFoundException {
		Flight f = null;
		try {
			ResultSet rs = super.querryDB(SQLQueries.getFlightByID + id);

			if (rs.next()) {
				var airports = this.getAirports(rs.getString("src"), rs.getString("dest")).toArray();
				var tracking = this.getCompleteTrackingByFlight(rs.getInt("ID"));
				f = new Flight(rs.getInt("ID"), (Airport) airports[0], (Airport) airports[1], rs.getString("callsign"), getPlaneByID(rs.getInt("plane")), rs.getString("flightnr"), tracking);
			} else {
				throw new DataNotFoundException("No Flight found for id " + id + "!", true);
			}
			rs.close();
		} catch (DataNotFoundException ignored) {

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
			return lengthRS.getInt(1); // no rs.close needed
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -9999;
	}

	public ArrayDeque<Integer> getAllFlightIDsWithPlaneIDs (ArrayDeque<Integer> ids) { // TODO JOIN
		var flights = new ArrayDeque<Integer>();
		try {
			String querry = "SELECT ID FROM flights " +
							"WHERE plane " + SQLQueries.IN_INT(ids);
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
							"WHERE (p.type " + SQLQueries.IN_STR(planetypes) + ")";
			var rs = super.querryDB(querry);
			while (rs.next()) {
				var id = rs.getInt("ID");
				ids.add(id);

			}
			rs.close();
			if (ids.size() < 1) {
				throw new DataNotFoundException("No plane IDs found for type " + planetypes + "!", true);
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
	public ArrayDeque<Integer> getPlaneIDsByTailNr(String tailNr)
			throws DataNotFoundException {

		tailNr = Utilities.packString(tailNr);
		var ids = new ArrayDeque<Integer>();
		try {
			var rs = super.querryDB(SQLQueries.getPlaneIDByTailNr + tailNr);
			while (rs.next()) {
				int id = rs.getInt("ID");
				ids.add(id);
			}
			rs.close();
			if (ids.size() < 1) {
				throw new DataNotFoundException();
			}
		} catch (DataNotFoundException dnfe) {
			throw new DataNotFoundException("No plane found for tailnumber " + tailNr + "!");
		} catch (Exception e) { // FIXME: 03.05.2022 sollte querryDB eine Exception werfen? oder eine SQLException?
			e.printStackTrace();
		}
		return ids;
	}

	/**
	 * @return list of all planetypes
	 */
	public ArrayDeque<String> getAllPlanetypesLike(final String planetype) {
		var allTypes = new ArrayDeque<String>();
		try {
			var querry = "SELECT type FROM planes WHERE type LIKE '" + planetype + "%' GROUP BY type";
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
	 */
	public ArrayDeque<String> getAllCallsignsLike(final String callsign) {
		var allCallsigns = new ArrayDeque<String>();
		try {
			var rs = super.querryDB("SELECT DISTINCT callsign FROM flights WHERE callsign LIKE '" + callsign + "%'");
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
	 * @return list of all callsigns
	 */ // FIXME: 05.05.2022 HIER IST EIN FEHLER !!
	public ArrayDeque<Integer> getAirportIDsLike(String airport) {
		var aids = new ArrayDeque<Integer>();
		try {
			var rs = super.querryDB(	"SELECT DISTINCT ID FROM airports " +
											 	"WHERE ((iatatag LIKE '%" + airport + "%') " +
												"OR (name LIKE '%" + airport + "%'))");
			while (rs.next()) {
				aids.add(rs.getInt("ID"));
			}
			rs.close();
			return aids;
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
	public final Deque<DataPoint> getLastTrackingsByFlightIDs (final Deque<Integer> flightIDs) {
		var dps = new ArrayDeque<DataPoint>();
		try {
			var querry = 	"SELECT max(t.ID) AS ID, t.flightid, t.latitude, t.longitude, t.altitude, t.groundspeed, t.heading, t.squawk, t.timestamp " +
							"FROM tracking t " +
							"WHERE flightid " + SQLQueries.IN_INT(flightIDs) + " GROUP BY flightid";
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

	public final Deque<DataPoint> getLastTrackingsBetweenFlightIDs(final int from, final int to)
			throws DataNotFoundException {

		var dps = new ArrayDeque<DataPoint>();
		try {
			//var querry = "SELECT f.*, t1.* FROM flights f JOIN tracking t1 ON (f.ID = t1.flightid) LEFT OUTER JOIN tracking t2 ON (f.ID = t2.flightid AND (t1.ID < t2.ID)) WHERE t2.ID IS NULL AND f.ID BETWEEN " + from + " AND " + to; //group by flightid
			var querry = "SELECT f.ID, f.endTime, max(t.ID), t.ID, flightid, latitude, " +
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
				throw new DataNotFoundException();
			}
		} catch (DataNotFoundException dnf) {
			throw new DataNotFoundException("No live flights found between " + from + "-" + to + " !", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dps;
	}

	public final Deque<Integer> getLiveFlightIDs(final int from, final int to) {
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

	/**
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public final Vector<DataPoint> getLiveTrackingBetween (final int from, final int to)
			throws DataNotFoundException {

		var liveTracking = this.getLastTrackingsBetweenFlightIDs(from, to);
		return new Vector<>(liveTracking);
	}

	/**
	 *
	 * @param tag, the airport tag
	 * @return
	 * @throws DataNotFoundException
	 */
	public final Deque<DataPoint> getTrackingsWithAirportTag(String tag)
			throws DataNotFoundException { // TODO IN

		var dps = new ArrayDeque<DataPoint>();
		try {
			var querry = "SELECT t.* FROM tracking t " + // TODO!!! threaden!, viele daten!
					"JOIN flights f ON ((f.ID = t.flightid) " +
					"AND ((f.src LIKE '" + tag + "') " +
					"OR (f.dest LIKE '" + tag + "')))"; // eventuell vor/hinterher _ einfügen
			var rs = super.querryDB(querry);
			DataPoint dp;
			while (rs.next()) {
				dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"),
						new Position(rs.getDouble("latitude"), rs.getDouble("longitude")),
						rs.getLong("timestamp"), rs.getInt("squawk"), rs.getInt("groundspeed"),
						rs.getInt("heading"), rs.getInt("altitude"));
				dps.add(dp);
			}
			if (dps.isEmpty()) {
				throw new DataNotFoundException();
			}
		} catch (DataNotFoundException dnfe) {
			throw new DataNotFoundException("No flights found with airport tag " + tag + "!", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dps;
	}

	/**
	 * @return array deque off all airports which have flights
	 * @throws DataNotFoundException if there was no airport found
	 */
	public final Deque<Airport> getAllAirports() throws DataNotFoundException {
		var aps = new ArrayDeque<Airport>();
		try {
			var querry = 	SQLQueries.SELECT(false, "f.src", "f.dest") +
							SQLQueries.FROM("flights f");
			var rs = super.querryDB(querry);
			int counter = 0;
			while (rs.next() && counter < 5000) { // TODO remove counter for all airports // could take a little time
				var src = Utilities.stripString(rs.getString("src"));
				var dest = Utilities.stripString(rs.getString("dest"));
				var nonNullAps = this.getAirports(src, dest)
						.stream()
						.filter(a -> !a.iataTag().equalsIgnoreCase("None"))
						.toList();
				aps.addAll(nonNullAps);
				counter++;
			}
			rs.close();
			if (aps.isEmpty()) {
				throw new DataNotFoundException("no airports found in all flights!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aps;
	}

	public final Vector<Position> getAllTrackingPositions() {
		var positions = new Vector<Position>();
		try {
			//var querry = "SELECT f.*, t1.* FROM flights f JOIN tracking t1 ON (f.ID = t1.flightid) LEFT OUTER JOIN tracking t2 ON (f.ID = t2.flightid AND (t1.ID < t2.ID)) WHERE t2.ID IS NULL AND f.ID BETWEEN " + from + " AND " + to; //group by flightid
			var querry = "SELECT latitude, longitude " +
						 "FROM tracking ";
						 //"GROUP BY flightid"; // evtl JOIN ?? läuft aber gut, schneller als die obere querry
			var rs = super.querryDB(querry);
			while (rs.next()) {
				var pos = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
				positions.add(pos);
			}
			if (positions.isEmpty()) {
				throw new DataNotFoundException("No trackings found!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return positions;
	}

	protected HashMap<Position, Integer> speedMap(final int from, final int to)
			throws DataNotFoundException {

		var speedMap = new HashMap<Position, Integer>();
		var querry = "SELECT t.latitude, t.longitude, t.groundspeed " +
					 "FROM tracking t " +
					 "WHERE (t.ID BETWEEN " + from + " AND " + to + ")";
		try {
			var rs = super.querryDB(querry);
			Position pos;
			int speed;
			while (rs.next()) {
				pos = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
				speed = rs.getInt("groundspeed");
				speedMap.put(pos, speed);
			}
			rs.close();
		} catch (Exception e) { // TODO es sollte keine Exception bis hier geworfen werden
			e.printStackTrace();
		}
		if (speedMap.isEmpty()) {
			throw new DataNotFoundException("No Speed Data found in the DB!");
		}
		return speedMap;
	}

	public Deque<Integer> getAllFlightIDs() throws DataNotFoundException {
		var ids = new ArrayDeque<Integer>();
		try {
			var query = "SELECT ID FROM flights";
			var rs = super.querryDB(query);
			while (rs.next()) {
				ids.add(rs.getInt("ID"));
			}
			rs.close();
		} catch (Exception e) {
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
	public Map<String, int[]> icaoIDMap() {
		var map = new HashMap<String, int[]>();
		try {
			var query = "SELECT p.icaonr AS icao, p.ID AS pid, f.ID AS fid FROM planes p " +
						"JOIN flights f ON (f.plane = p.ID)";
			var rs = super.querryDB(query);
			while (rs.next()) {
				int[] values = {
					rs.getInt("pid"),
					rs.getInt("fid")
				};
				map.put(rs.getString("icao"), values);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public Map<Integer, Long> getLiveFlightIDsWithTimestamp() {
		var map = new HashMap<Integer, Long>();
		try {
			var query = "SELECT t.flightid AS fid, max(t.timestamp) AS ts FROM tracking t " +
						"JOIN flights f ON (f.ID = t.flightid) AND (f.endTime IS NULL)" +
						"GROUP BY t.flightid";
			var rs = super.querryDB(query);
			while (rs.next()) {
				map.put(rs.getInt(1), rs.getLong(2));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public HashMap<String, Integer> getFlightNRsWithPlaneIDs() {
		var map = new HashMap<String, Integer>();
		try {
			var query = "SELECT plane, flightnr FROM flights WHERE endTime IS NULL";
			var rs = super.querryDB(query);
			while (rs.next()) {
				map.put(rs.getString(2), rs.getInt(1));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public HashMap<String, Integer> getFlightNRsWithFlightIDs() {
		var map = new HashMap<String, Integer>();
		try {
			var query = "SELECT ID, flightnr FROM flights WHERE (endTime IS NULL)";
			var rs = super.querryDB(query);
			while (rs.next()) {
				map.put(rs.getString(2), rs.getInt(1));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public HashMap<String, Integer> getFlightNRsWithFlightIDs(Deque<Integer> flightIDs) {
		var map = new HashMap<String, Integer>();
		try {
			var query = "SELECT ID, flightnr FROM flights WHERE (ID " + SQLQueries.IN_INT(flightIDs) + ") AND (endTime IS NULL)";
			var rs = super.querryDB(query);
			while (rs.next()) {
				var fnr = rs.getString(2);
				int id = rs.getInt(1);
				if (!fnr.isBlank()) {
					map.put(fnr, id);
				}
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public int getTableSize(final String table) throws DataNotFoundException {
		int size = -1;
		try {
			var query = "SELECT count(ID) AS size FROM " + table;
			var rs = super.querryDB(query);
			if (rs.next()) {
				size = rs.getInt("size");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (size == -1) {
			throw new DataNotFoundException("Table not found!");
		}
		return size;
	}

	public Deque<String> getICAOsByPlaneIDs(Deque<Integer> planeIDs) {
		var icaos = new ArrayDeque<String>();
		try {
			var query = "SELECT icaonr FROM planes WHERE ID " + SQLQueries.IN_INT(planeIDs);
			var rs = super.querryDB(query);
			while (rs.next()) {
				icaos.add(rs.getString(1));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return icaos;
	}

	public Deque<Integer> getAllPlaneIDs() {
		var ids = new ArrayDeque<Integer>();
		try {
			var query = "SELECT ID FROM planes";
			var rs = super.querryDB(query);
			while (rs.next()) {
				ids.add(rs.getInt(1));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ids;
	}

	// TODO public ArrayDeque<Integer> getPlaneIDsWhereTypeLike (String input) // sollte schneller sein


}

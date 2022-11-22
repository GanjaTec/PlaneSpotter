package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import planespotter.constants.SQLQueries;
import planespotter.dataclasses.*;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.InvalidDataException;
import planespotter.throwables.NoAccessException;
import planespotter.util.HighMemory;
import planespotter.util.Utilities;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @name DBOut
 * @author Lukas
 * @author Bennet
 * @author jml04
 * @version 1.1
 *
 * @description
 * This DBConnector-child-class is used to get Output from
 * the DB and uses Queries provided by the SQLQueries Class
 * some methods are deprecated and will be removed, others will
 * be updated and/or improved...
 * @see planespotter.constants.SQLQueries
 * @see planespotter.model.io.DBConnector
 *
 */

public final class DBOut extends DBConnector {

	// (ONE and ONLY) DBOut instance
	private static final DBOut INSTANCE = new DBOut();

	/**
	 * private constructor for main instance
	 */
	private DBOut() {
	}

	/**
	 * getter for main DBOut-instance
	 *
	 * @return main instance of the DBOut class
	 */
	@NotNull
	public static DBOut getDBOut() {
		return INSTANCE;
	}

	/**
	 * This method is a quick botch because i messed up the Airports Table in the DB
	 * It takes A string containing the Coordinates received from the DB and converts it
	 * into a Position Object
	 *
	 * @param coords The string containing the Coords
	 * @return Position containing Latitude and Longitude
	 */
	@NotNull
	private Position convertCoords(@NotNull String coords) {
		double[] processedCoords = Arrays.stream(coords.split(","))
				.mapToDouble(Double::parseDouble)
				.toArray();
		return switch (processedCoords.length) {
			case 0, 1 -> throw new InvalidDataException("Array length must be 2!");
			default -> new Position(processedCoords[0], processedCoords[1]);
		};
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
	@NotNull
	public Airline getAirlineByTag(@NotNull String tag)
			throws DataNotFoundException {

		Airline a = null;
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(SQLQueries.GET_AIRLINE_BY_TAG + tag);
				 ResultSet rs = result.resultSet()) {
				a = rs.next()
						? new Airline(rs.getInt("ID"), rs.getString("iatatag"), rs.getString("name"), rs.getString("country"))
						: null;
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
		}
		return a == null ?  new Airline(-1, "None", "None", "None") : a;
	}

	/**
	 * This Method is used to query both: the Departure (src) and the Arrival (dest)
	 * Airports of a Flight and returns a List containing two Airport Objects
	 *
	 * @param srcAirport String containing the Departure Airports IATA Tag
	 * @param destAirport String containing the Arrival Airports IATA Tag
	 * @return Airport array with length of 2, containing the Airport Objects
	 */

	@NotNull
	public Airport[] getAirports(@NotNull final String srcAirport, @NotNull final String destAirport) {
		// null-Airport, stands for 'no airport found'
		final Airport nullAirport = new Airport(-1, "None", "None", new Position(0., 0.));
		// airport-output array, initialized with null-airports
		final Airport[] aps = new Airport[] { nullAirport, nullAirport };
		// synchronizing on DB-sync to prevent SQLITE_BUSY error
		synchronized (DB_SYNC) {
			try (DBResult srcResult = queryDB(SQLQueries.GET_AIRPORT_BY_TAG + Utilities.packString(srcAirport));
				 DBResult destResult = queryDB(SQLQueries.GET_AIRPORT_BY_TAG + Utilities.packString(destAirport));
				 ResultSet rsSrc = srcResult.resultSet();
				 ResultSet rsDest = destResult.resultSet()) {
				// getting airports and replacing array
				if (rsSrc.next()) {
					aps[0] = new Airport(rsSrc.getInt("ID"), rsSrc.getString("iatatag"), rsSrc.getString("name"), convertCoords(rsSrc.getString("coords")));
				}
				if (rsDest.next()) {
					aps[1] = new Airport(rsDest.getInt("ID"), rsDest.getString("iatatag"), rsDest.getString("name"), convertCoords(rsDest.getString("coords")));
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
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
	public int[] getFlightIDsByICAOLike(@NotNull String icao) throws DataNotFoundException {
		int[] ids = new int[0];
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(SQLQueries.GET_FLIGHT_IDS_BY_ICAO_LIKE + "'" + icao + "%'");
				 ResultSet rs = result.resultSet()) {
				int id, length;
				while (rs.next()) {
					id = rs.getInt(1);
					length = ids.length;
					ids = Arrays.copyOf(ids, length + 1);
					ids[length] = id;
				}
			} catch (NoAccessException | SQLException e) {
				e.printStackTrace();
			}
		}
		if (ids.length == 0) {
			throw new DataNotFoundException("No plane id found for icao " + icao + "!");
		}
		return ids;
	}

	/**
	 * gets a plane by its ID
	 *
	 * @param id is the plane id
	 * @return plane with the given id
	 * @throws DataNotFoundException if there was no plane found
	 */
	@NotNull
	public Plane getPlaneByID(int id)
			throws DataNotFoundException {

		Airline airline;
		Plane plane = null;
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(SQLQueries.GET_PLANE_BY_ID + id)) {
				 ResultSet rs = result.resultSet();
				if (rs.next()) {
					airline = getAirlineByID(rs.getInt("airline"));
					plane = new Plane(rs.getInt("ID"), rs.getString("icaonr"), rs.getString("tailnr"), rs.getString("type"), rs.getString("registration"), airline);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
		}
		if (plane == null) {
			throw new DataNotFoundException("No plane found for ID " + id + "!");
		}
		return plane;
	}

	/**
	 * gets an Airline by its ID
	 *
	 * @param id is the airline ID
	 * @return Airline with the given ID, or None-Airline,
	 * 		   if no Airline was found with the given ID
	 */
	@NotNull
	public Airline getAirlineByID(final int id) {

		int airlineID;
		Airline airline = null;
		String query, tag, name, country;

		query = "SELECT * FROM airlines a " +
				"JOIN planes p " +
				"ON ((p.airline = a.ID) " +
				"AND (p.ID = " + id + "))";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {

				if (rs.next()) {
					airlineID = rs.getInt("ID");
					tag = rs.getString("icaotag");
					name = rs.getString("name");
					country = rs.getString("country");
					airline = new Airline(airlineID, tag, name, country);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
		}
		return (airline != null) ? airline : new Airline(-1, "None", "None", "None");
	}

	/**
	 * checks, if the plane with the given ICAO-address
	 * exists in the database
	 *
	 * @param icao is the ICAO-address to search for
	 * @return ID of the plane that was found, or -9999, if nothing was found
	 */
	public int checkPlaneInDB(@NotNull String icao) {

		int id;
		String planeFilter = "SELECT ID FROM planes WHERE icaonr = '" + icao + "' LIMIT 1";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(planeFilter);
				 ResultSet rs = result.resultSet()) {
				if (rs.next()) {
					id = rs.getInt(1);
				} else {
					id = -1;
				}
				return id;
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
		}
		return -9999; // because -1 already exists, for better debugging
	}

	/**
	 * This method is used to query all DataPoints belonging to a single Flight
	 * It takes a FlightID (int) and returns a Vector of DataPoints containing the Tracking Data
	 *
	 *
	 * @param flightID represents the Flights Database ID
	 * @return Vector of DataPoints, the tracking data for the given flight
	 * @throws DataNotFoundException if no tracking or no flight was found
	 */
	@NotNull
	public Vector<DataPoint> getTrackingByFlight(int flightID)
			throws DataNotFoundException {

		Position pos;
		DataPoint dp;
		final Vector<DataPoint> dps = new Vector<>();
		String query = "SELECT * FROM tracking WHERE flightid = " + flightID;
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {
				while (rs.next()) {
					pos = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
					dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), pos, rs.getInt("timestamp"),
							rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
					dps.add(dp);
				}
			} catch (NoAccessException | SQLException e) {
				e.printStackTrace();
			}
		}
		if (dps.isEmpty()) {
			throw new DataNotFoundException("No tracking found for flight id " + flightID);
		}
		return dps;

	}

	/**
	 * gets a complete tracking-HashMap with tracking IDs and DataPoint
	 *
	 * @param flightID is the flight ID of the flight, the tracking is loaded from
	 * @return tracking-HashMap with tracking-ID as key and DataPoint as value
	 * @throws DataNotFoundException if no flight or no tracking was found
	 */
	@NotNull
	public HashMap<Integer, DataPoint> getCompleteTrackingByFlight(int flightID)
			throws DataNotFoundException {

		final HashMap<Integer, DataPoint> dps = new HashMap<>();
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB("SELECT * FROM tracking WHERE flightid = " + flightID);
				 ResultSet rs = result.resultSet()) {
				while (rs.next()) {
					// TODO: IF STATEMENT
					var p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
					var dp = new DataPoint(rs.getInt("ID"), rs.getInt("flightid"), p, rs.getInt("timestamp"),
							rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));
					dps.put(rs.getInt("ID"), dp);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (dps.isEmpty()) {
				throw new DataNotFoundException("No tracking found for flight id " + flightID);
			}
		}
		return dps;

	}

	/**
	 * returns the last timestamp of a specific {@link Flight}
	 *
	 * @param id is the flight ID
	 * @return the last timestamp of the {@link Flight} as long
	 * @throws DataNotFoundException if no {@link Flight} was found
	 */
	public long getLastTimestampByFlightID(int id)
			throws DataNotFoundException {

		long timestamp = -1;
		String getLastTracking = "SELECT max(timestamp) FROM tracking WHERE flightid = " + id;
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(getLastTracking);
				ResultSet rs = result.resultSet()) {

				while (rs.next()) {
					timestamp = rs.getLong(1);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
		}
		return timestamp;
	}

	/**
	 * returns the last {@link DataPoint} of a specific {@link Flight}
	 *
	 * @param id is the flight ID
	 * @return the last {@link DataPoint} of the {@link Flight}
	 * @throws DataNotFoundException if the {@link Flight} was not found
	 */
	@NotNull
	public DataPoint getLastTrackingByFlightID(final int id)
			throws DataNotFoundException {

		Position p;
		DataPoint dp = null;
		String query =  "SELECT * FROM tracking WHERE flightid = '"+ id +"' ORDER BY ID DESC LIMIT 1";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {

				while (rs.next()) {
					p = new Position(rs.getDouble("latitude"), rs.getDouble("longitude"));
					dp = new DataPoint(rs.getInt("ID"), id, p, rs.getInt("timestamp"),
							rs.getInt("squawk"), rs.getInt("groundspeed"), rs.getInt("heading"), rs.getInt("altitude"));

				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (dp == null) {
				throw new DataNotFoundException("No last tracking found for flight id " + id);
			}
		}
		return dp;
	}

	/**
	 * returns the ID of the last {@link DataPoint} of a specific {@link Flight},
	 * more efficient than getTrackingByFlightID
	 *
	 * @param flightID is the flight ID
	 * @return the last {@link DataPoint}-ID of the {@link Flight}
	 * @throws DataNotFoundException if no {@link Flight} was found
	 */
	public int getLastTrackingIDByFlightID(final int flightID)
			throws DataNotFoundException {

		int tid = -1;
		String query =  "SELECT ID FROM tracking WHERE flightid = '"+ flightID +"' ORDER BY ID DESC LIMIT 1";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {
				if (rs.next()) {
					tid = rs.getInt("ID");
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (tid == -1) {
				throw new DataNotFoundException("No last tracking found for flight id " + flightID);
			}
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
	 * see error log "hs_err_pid30296.log" in the projects root directory
	 *
	 * @return List<Flight> containing all Flight Objects
	 */
	@HighMemory(msg = "Uses too much memory!")
	public List<Flight> getAllFlights(int dataLimit) throws DataNotFoundException {

		HashMap<Integer, DataPoint> dps; Airport[] aps; Flight flight; Plane plane;
		ArrayList<Flight> flights = new ArrayList<>();
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(SQLQueries.GET_FLIGHTS);
				 ResultSet rs = result.resultSet()) {

				int counter = 0;
				while (rs.next() && counter++ <= dataLimit) { // counter: max flights -> to limit the incoming data (prevents a crash)
					dps = getCompleteTrackingByFlight(rs.getInt("ID"));
					aps = getAirports(rs.getString("src"), rs.getString("dest"));
					plane = getPlaneByID(rs.getInt("plane"));
					flight = new Flight(rs.getInt("ID"), aps[0], aps[1], rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
					flights.add(flight);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (flights.isEmpty()) {
				throw new DataNotFoundException("No flights found!");
			}
		}
		return flights;
	}

	/**
	 * returns all {@link Flight} IDs with a specific callsign
	 *
	 * @param callsign is the call sign to search for
	 * @return a {@link Deque} of the {@link Flight} IDs with matching call sign
	 * @throws DataNotFoundException if no {@link Flight} was found
	 */
	@NotNull
	public Deque<Integer> getFlightIDsByCallsign(@NotNull String callsign)
			throws DataNotFoundException {

		ArrayDeque<Integer> ids = new ArrayDeque<>();
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(SQLQueries.GET_FLIGHT_IDS_BY_CALLSIGN + Utilities.packString(callsign));
				 ResultSet rs = result.resultSet()) {

				while (rs.next()) {
					ids.add(rs.getInt("ID"));
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (ids.isEmpty()) {
				throw new DataNotFoundException("No flights ids found for callsign " + callsign);
			}
		}
		return ids;
	}

	/**
	 * returns the last {@link Flight}-ID (table size of the 'flights' table minus 1),
	 * which is -1 when the table is empty
	 *
	 * @return the last {@link Flight}-ID or -1 if there are no {@link Flight}s
	 */
	public int getLastFlightID() throws DataNotFoundException {
		return getTableSize("flights") - 1;
	}

	/**
	 * returns all {@link Flight}-IDs of non-ended {@link Flight}s
	 *
	 * @return {@link List} of all {@link Flight}-IDs of non-ended {@link Flight}s
	 * @throws DataNotFoundException if no {@link Flight} was found
	 */
	@NotNull
	public List<Integer> checkEnded() throws DataNotFoundException {

		final List<Integer> flightIDs = new ArrayList<>();
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(SQLQueries.CHECK_END_OF_FLIGHT);
				 ResultSet rs = result.resultSet()) {

				while (rs.next()) {
					flightIDs.add(rs.getInt(1));
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (flightIDs.isEmpty()) {
				throw new DataNotFoundException("No flights found!");
			}
		}
		return flightIDs;
	}

	/**
	 * returns a {@link Flight} by its ID
	 *
	 * @param id is the {@link Flight}-ID
	 * @return a specific {@link Flight} by ID
	 * @throws DataNotFoundException if the {@link Flight} was not found
	 */
	@NotNull
	public Flight getFlightByID(@Range(from = 0, to = Integer.MAX_VALUE) int id)
			throws DataNotFoundException {

		Flight flight = null;
		Plane plane;
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(SQLQueries.GET_FLIGHT_BY_ID + id);
				 ResultSet rs = result.resultSet()) {

				if (rs.next()) {
					Airport[] airports = getAirports(rs.getString("src"), rs.getString("dest"));
					HashMap<Integer, DataPoint> tracking = getCompleteTrackingByFlight(rs.getInt("ID"));
					plane = getPlaneByID(rs.getInt("plane"));
					flight = new Flight(rs.getInt("ID"), airports[0], airports[1], rs.getString("callsign"), plane, rs.getString("flightnr"), tracking);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (flight == null) {
				throw new DataNotFoundException("No Flight found for id " + id + "!");
			}
		}
		return flight;
	}

	/**
	 * returns all {@link Flight}s between a start- and end-ID
	 *
	 * @param startID is the first {@link Flight}-ID to get
	 * @param endID is the last {@link Flight}-ID to get
	 * @return all {@link Flight}s between the start- and end-ID (inclusive)
	 * @throws DataNotFoundException if no {@link Flight} was found between the IDs
	 */
	@NotNull
	public List<Flight> getAllFlightsBetween(int startID, int endID)
			throws DataNotFoundException {

		List<Flight> flights = new ArrayList<>();
		HashMap<Integer, DataPoint> dps;
		Airport[] aps;
		Plane plane;
		Flight flight;
		String query = "SELECT * FROM flights " +
					   "WHERE (ID BETWEEN " + startID + " AND " + endID + ") " +
					   "AND endTime IS NULL";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {

				int counter = 0;
				while (rs.next() && counter++ <= endID - startID) { // counter: immer begrenzte Anzahl an Datensätzen
					dps = getCompleteTrackingByFlight(rs.getInt("ID"));
					aps = getAirports(rs.getString("src"), rs.getString("dest"));
					plane = getPlaneByID(rs.getInt("plane"));
					flight = new Flight(rs.getInt("ID"), aps[0], aps[1], rs.getString("callsign"), plane, rs.getString("flightnr"), dps);
					flights.add(flight);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (flights.isEmpty()) {
				throw new DataNotFoundException("No flights found between " + startID + " and " + endID + "!");
			}
		}
		return flights;
	}

	/**
	 * returns the element count of a certain table in the database
	 *
	 * @return length of a certain database table
	 * @param table is the table name
	 * @throws DataNotFoundException if the table was not found
	 */
	public int getEntriesByFlightID(@NotNull String table, int flightID)
			throws DataNotFoundException {

		synchronized (DB_SYNC) {
			try (DBResult result = super.queryDB("SELECT count(*) FROM " + table + " WHERE flightid == " + flightID);
				ResultSet rs = result.resultSet()) {

				return rs.getInt(1);
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
		}
		throw new DataNotFoundException("Table not found!");
	}

	/**
	 * returns all {@link Flight} IDs which {@link Plane} ID matches one of the given ones
	 *
	 * @param ids are the {@link Plane} IDs to search for
	 * @return {@link Deque} of the {@link Flight} IDs as {@link Integer}s
	 * @throws DataNotFoundException if no {@link Flight} was found
	 */
	@NotNull
	public Deque<Integer> getAllFlightIDsWithPlaneIDs(ArrayDeque<Integer> ids)
			throws DataNotFoundException { // TODO JOIN

		Deque<Integer> flights = new ArrayDeque<>();
		String query = "SELECT ID FROM flights " +
					   "WHERE plane " + SQLQueries.IN_INT(ids);
		synchronized (DB_SYNC) {
			try (DBResult result = super.queryDB(query);
				 ResultSet rs = result.resultSet()) {
				int id;
				while (rs.next()) {
					id = rs.getInt("ID");
					flights.add(id);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (flights.isEmpty()) {
				throw new DataNotFoundException("No flight IDs found for these plane IDs \n" + ids);
			}
		}
		return flights;
	}

	/**
	 * TODO hier muss ein Fehler drin sein!! oder in der Querry
	 *
	 * returns all {@link Flight} IDs, which {@link Plane} type matches one of the given ones
	 *
	 * @param planetypes are the plane types to search for
	 * @return int array of the {@link Flight} IDs
	 * @throws DataNotFoundException if no {@link Flight} or {@link Plane} was found
	 */
	public int[] getFlightIDsByPlaneTypes(@NotNull Deque<String> planetypes)
			throws DataNotFoundException {

		int[] ids = new int[0];
		final String query = "SELECT f.ID FROM flights f " +
							 "JOIN planes p ON ((p.ID = f.plane) AND (f.endTime IS NULL))" +
							 "WHERE (p.type " + SQLQueries.IN_STR(planetypes) + ")";
		synchronized (DB_SYNC) {
			try (DBResult result = super.queryDB(query);
				 ResultSet rs = result.resultSet()) {

				int id, length;
				while (rs.next()) {
					id = rs.getInt(1);
					length = ids.length;
					ids = Arrays.copyOf(ids, length + 1);
					ids[length] = id;

				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (ids.length == 0) {
				throw new DataNotFoundException("No plane IDs found for type " + planetypes + "!");
			}
		}
		return ids;
	}

	/**
	 * returns all {@link Plane} IDs which are paired with a {@link Plane} with a specific tail number
	 *
	 * @param tailNr is the {@link Plane} tail number
	 * @return {@link Plane} that matches the given tail number
	 * @throws DataNotFoundException if no {@link Plane} was found
	 */
	public int[] getFlightIDsByTailNrLike(@NotNull String tailNr) throws DataNotFoundException {

		int[] ids = new int[0];
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(SQLQueries.GET_FLIGHT_IDS_BY_TAILNR_LIKE + "'" + tailNr + "%'");
				 ResultSet rs = result.resultSet()) {

				int id, length;
				while (rs.next()) {
					id = rs.getInt("ID");
					length = ids.length;
					ids = Arrays.copyOf(ids, length + 1);
					ids[length] = id;
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (ids.length == 0) {
				throw new DataNotFoundException("No plane found for tailnumber " + tailNr + "!");
			}
		}
		return ids;
	}

	/**
	 * queries all plane types that are like the input type
	 *
	 * @param planetype is the comparison-type
	 * @return {@link Deque} of all plane types like the param-type
	 */
	@NotNull
	public Deque<String> getAllPlanetypesLike(@NotNull final String planetype)
			throws DataNotFoundException {

		final Deque<String> allTypes = new ArrayDeque<>();
		final String query = "SELECT DISTINCT type FROM planes WHERE type LIKE '" + planetype + "%'/* GROUP BY type*/";
		synchronized (DB_SYNC) {
			try (DBResult result = super.queryDB(query);
				 ResultSet rs = result.resultSet()) {

				while (rs.next()) {
					allTypes.add(rs.getString("type"));
				}
				return allTypes;
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (allTypes.isEmpty()) {
				throw new DataNotFoundException("No planetype found!");
			}
		}
		throw new DataNotFoundException("No planetype found!");
	}

	/**
	 * queries all callsigns that are like the input callsign
	 *
	 * @param callsign is the callsign to search for
	 * @return {@link Deque} of all callsigns like the param callsign
	 */
	@NotNull
	public ArrayDeque<String> getAllCallsignsLike(@NotNull final String callsign) throws DataNotFoundException {

		final var allCallsigns = new ArrayDeque<String>();
		String query = "SELECT DISTINCT callsign FROM flights WHERE callsign LIKE '" + callsign + "%'";
		synchronized (DB_SYNC) {
			try (DBResult result = super.queryDB(query);
				 ResultSet rs = result.resultSet()) {

				while (rs.next()) {
					allCallsigns.add(rs.getString("callsign"));
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (allCallsigns.isEmpty()) {
				throw new DataNotFoundException("No callsigns found!");
			}
		}
		return allCallsigns;
	}

	/**
	 * returns all {@link Flight} IDs that flew to a specific {@link Airport}
	 *
	 * @param tag is the {@link Airport} tag
	 * @return int[] of all {@link Flight} IDs that match the {@link Airport}
	 */ // FIXME: 05.05.2022 HIER IST EIN FEHLER !!
	public int[] getFlightIDsByAirportTag(String tag) throws DataNotFoundException {

		int[] fids = new int[0];
		String query = "SELECT DISTINCT f.ID " +
					"FROM flights f " +
					"JOIN airports a " +
					"ON (a.iatatag IS f.src " +
					"OR a.iatatag IS f.dest) " +
					"AND a.iatatag IS " + Utilities.packString(tag.toUpperCase());
		synchronized (DB_SYNC) {
			try (DBResult result = super.queryDB(query);
				 ResultSet rs = result.resultSet()) {

				int length;
				while (rs.next()) {
					length = fids.length;
					fids = Arrays.copyOf(fids, length + 1);
					fids[length] = rs.getInt(1);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
		}
		if (fids.length == 0) {
			throw new DataNotFoundException("No flight IDs found for airport tag " + tag + "!");
		}
		return fids;
	}

	/**
	 * returns all last {@link DataPoint}s (tracking points) from {@link Flight}s with specific {@link Flight} IDs
	 *
	 * @param flightIDs are the flight ids
	 * @return last {@link DataPoint} of each {@link Flight} in a {@link Deque}
	 */
	@NotNull
	public Vector<DataPoint> getLastTrackingsByFlightIDs(final int[] flightIDs)
			throws DataNotFoundException {

		if (flightIDs.length == 0) {
			throw new DataNotFoundException("No given flight IDs!");
		}

		Position p; DataPoint dp;
		Vector<DataPoint> dps = new Vector<>();
		String querry = "SELECT max(t.ID), t.flightid, t.latitude, t.longitude, t.altitude, t.groundspeed, t.heading, t.squawk, t.timestamp " +
						"FROM tracking t " +
						"WHERE flightid " + SQLQueries.IN_INT(flightIDs) + " GROUP BY flightid";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(querry);
				 ResultSet rs = result.resultSet()) {

				while (rs.next()) {
					p = new Position(rs.getDouble(3), rs.getDouble(4));
					dp = new DataPoint(rs.getInt(1), rs.getInt(2), p, rs.getInt(9), rs.getInt(8),
									   rs.getInt(6), rs.getInt(7), rs.getInt(5));
					dps.add(dp);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (dps.isEmpty()) {
				throw new DataNotFoundException("No data points found for " + Arrays.toString(flightIDs) + "!");
			}
		}
		return dps;
	}

	/**
	 * returns all {@link Airport}s
	 *
	 * @return {@link ArrayDeque} off all airports which have flights
	 * @throws DataNotFoundException if there was no airport found
	 */
	@NotNull
	public Deque<Airport> getAllAirports() throws DataNotFoundException {

		String src, dest; List<Airport> nonNullAps;
		Deque<Airport> aps = new ArrayDeque<>();
		String query = SQLQueries.SELECT(false, "f.src", "f.dest") +
					   SQLQueries.FROM("flights f");
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {
				while (rs.next()) {
					src = rs.getString("src");
					dest = rs.getString("dest");
					nonNullAps = Arrays.stream(getAirports(src, dest))
							.filter(a -> !a.iataTag().equalsIgnoreCase("None"))
							.toList();
					aps.addAll(nonNullAps);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (aps.isEmpty()) {
				throw new DataNotFoundException("no airports found in all flights!");
			}
		}
		return aps;
	}

	/**
	 * returns all {@link Position}s by all {@link Flight}s
	 *
	 * @return all {@link Position}s by all {@link Flight}s
	 * @throws DataNotFoundException if no {@link Flight} was found
	 */
	@NotNull
	public Vector<Position> getAllTrackingPositions() throws DataNotFoundException {

		Vector<Position> positions = new Vector<>();
		String query = "SELECT latitude, longitude " +
					   "FROM tracking ";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {
				Position pos;
				while (rs.next()) {
					pos = new Position(rs.getDouble(1), rs.getDouble(2));
					positions.add(pos);
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (positions.isEmpty()) {
				throw new DataNotFoundException("No trackings found!");
			}
		}
		return positions;
	}

	/**
	 * returns all flight IDs that have no endTime, connected to the {@link DataPoint}-timestamp of the flight
	 *
	 * @return all {@link Flight} IDs without an endTime, connected with last timestamp
	 * @throws DataNotFoundException if no {@link Flight} was found
	 */
	@NotNull
	public Map<Integer, Long> getLiveFlightIDsWithTimestamp() throws DataNotFoundException {

		HashMap<Integer, Long> map = new HashMap<>();
		String query = "SELECT t.flightid AS fid, max(t.timestamp) AS ts " +
					   "FROM tracking t " +
					   "JOIN flights f " +
					   "ON (f.ID = t.flightid) AND (f.endTime IS NULL)" +
					   "GROUP BY t.flightid";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {
				while (rs.next()) {
					map.put(rs.getInt(1), rs.getLong(2));
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
			if (map.isEmpty()) {
				throw new DataNotFoundException("ID-timestamp map is empty!");
			}
		}
		return map;
	}

	/**
	 * returns all {@link Flight} numbers connected with their {@link Flight} IDs
	 *
	 * @return all {@link Flight} numbers connected with their {@link Flight} IDs
	 * @throws DataNotFoundException if no {@link Flight} was found
	 */
	@NotNull
	public HashMap<String, Integer> getFlightNRsWithFlightIDs() throws DataNotFoundException {

		final HashMap<String, Integer> map = new HashMap<>();
		String query = "SELECT ID, flightnr " +
					   "FROM flights " +
				  	   "WHERE (endTime IS NULL)";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {
				while (rs.next()) {
					map.put(rs.getString(2), rs.getInt(1));
				}
			} catch (SQLException | NoAccessException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	/**
	 * returns the table size of a specific table from the DB,
	 * if the table does not exist, an exception is thrown
	 *
	 * @param table is the table-name of the requested table
	 * @return size of the requested table, if it exists
	 * @throws DataNotFoundException if the table does not exist
	 */
	public int getTableSize(@NotNull final String table) throws DataNotFoundException {

		if (table.isBlank()) {
			throw new DataNotFoundException("Table name must not be blank!");
		}

		int size = -1;
		String query = "SELECT count(ID) " +
					   "FROM " + table;
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {
				if (rs.next()) {
					size = rs.getInt(1);
				}
			} catch (SQLException sql) {
				throw new DataNotFoundException("Table name does not exist!");
			} catch (NoAccessException e) {
				e.printStackTrace();
			}
		}
		if (size == -1) {
			throw new DataNotFoundException("Table not found!");
		}
		return size;
	}

	/**
	 * returns all {@link Airline} tags connected  to their {@link Airline} IDs
	 *
	 * @return all {@link Airline} tags with their {@link Airline} IDs
	 * @throws DataNotFoundException if no {@link Airline} was found
	 */
	@NotNull
	public HashMap<String, Integer> getAirlineTagsIDs() throws DataNotFoundException {

		HashMap<String, Integer> map = new HashMap<>();
		String query = "SELECT icaotag, ID FROM airlines";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {
				while (rs.next()) {
					map.put(rs.getString(1), rs.getInt(2));
				}
				if (map.isEmpty()) {
					throw new DataNotFoundException("No Airline-Tags and IDs found!");
				}
			} catch (NoAccessException | SQLException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	/**
	 * returns all {@link Plane} ICAOs, connected with {@link Plane} IDs
	 *
	 * @return all ICAOs, connected to their {@link Plane} IDs
	 * @throws DataNotFoundException if no {@link Plane} was found
	 */
	@NotNull
	public HashMap<String, Integer> getPlaneIcaosIDs() throws DataNotFoundException {

		HashMap<String, Integer> map = new HashMap<>();
		String query = "SELECT icaonr, ID FROM planes";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {
				while (rs.next()) {
					map.put(rs.getString(1), rs.getInt(2));
				}
			} catch (NoAccessException | SQLException e) {
				e.printStackTrace();
			}
		}
		if (map.isEmpty()) {
			throw new DataNotFoundException("No Plane-ICAOs and IDs found!");
		}
		return map;
	}

	/**
	 * returns all {@link Airport} tags with duplicates
	 *
	 * @return all {@link Airport} tags (not distinct)
	 * @throws DataNotFoundException if no {@link Airport} was found
	 */
	@NotNull
	public Deque<String> getAllAirportTagsNotDistinct() throws DataNotFoundException {

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

	/**
	 * returns all {@link Airline} tags
	 *
	 * @return all {@link Airline} tags
	 * @throws DataNotFoundException if no {@link Airline} was found
	 */
	@NotNull
	public Deque<String> getAllAirlineTags() throws DataNotFoundException {

		Deque<String> tags = new ArrayDeque<>();
		String query = "SELECT a.icaotag AS tag, p.airline " +
					   "FROM airlines a " +
					   "JOIN planes p " +
					   "ON ((p.airline = a.ID) " +
					   "AND (a.ID != 1))";
		synchronized (DB_SYNC) {
			try (DBResult result = super.queryDB(query);
				 ResultSet rs = result.resultSet()) {
				while (rs.next()) {
					tags.add(rs.getString("tag"));
				}
			} catch (NoAccessException | SQLException e) {
				e.printStackTrace();
			}
			if (tags.isEmpty()) {
				throw new DataNotFoundException("Couldn't find any airline tag or plane!");
			}
		}
		return tags;
	}

	/**
	 * returns all {@link Flight} IDs from {@link Flight}s with specific {@link Airport} names
	 *
	 * @param name is the {@link Airport} name
	 * @return all {@link Flight} IDs with the Airport with the name
	 * @throws DataNotFoundException if no {@link Airport} was found
	 */
	public int[] getFlightIDsByAirportName(String name) throws DataNotFoundException {

		int[] ids = new int[0];
		String query = "SELECT f.ID " +
					   "FROM flights f " +
					   "JOIN airports a " +
					   "ON (a.name LIKE '%" + name + "%')" +
					   "AND (a.iatatag IS f.src OR a.iatatag IS f.dest)";
		synchronized (DB_SYNC) {
			try (DBResult result = queryDB(query);
				 ResultSet rs = result.resultSet()) {
				int length;
				while (rs.next()) {
					length = ids.length;
					ids = Arrays.copyOf(ids, length + 1);
					ids[length] = rs.getInt(1);
				}
			} catch (NoAccessException | SQLException e) {
				e.printStackTrace();
			}
		}
		if (ids.length == 0) {
			throw new DataNotFoundException("No airport tags found for name " + name + "!");
		}
		return ids;
	}

	/**
	 * returns all {@link DataPoint}s with a speific {@link Flight} ID
	 *
	 * @param ids is an int-array of the {@link Flight} IDs
	 * @return {@link Vector} of {@link DataPoint} with the given {@link Flight} IDs
	 * @throws DataNotFoundException if no {@link DataPoint} is found or no {@link Flight} is given
	 */
	@NotNull
	public Vector<DataPoint> getTrackingsByFlightIDs(int[] ids) throws DataNotFoundException {

		if (ids.length == 0) {
			throw new DataNotFoundException("No given flight IDs!");
		}

		DataPoint dp; Position p;
		Vector<DataPoint> dps = new Vector<>();
		String query = "SELECT * FROM tracking " +
					   "WHERE flightid " + SQLQueries.IN_INT(ids);
		try (DBResult result = queryDB(query);
			 ResultSet rs = result.resultSet()) {
				while (rs.next()) {
					p = new Position(rs.getDouble(3), rs.getDouble(4));
					dp = new DataPoint(rs.getInt(1), rs.getInt(2), p,
									   rs.getLong(9), rs.getInt(8), rs.getInt(6),
									   rs.getInt(7), rs.getInt(5));
					dps.add(dp);
				}
		} catch (SQLException | NoAccessException e) {
			e.printStackTrace();
		}
		if (dps.isEmpty()) {
			throw new DataNotFoundException("No tracking found for these flight IDs!");
		}
		return dps;
	}

	/**
	 * returns all {@link Position}s by certain {@link Flight} IDs
	 *
	 * @param fids are the {@link Flight} IDs to search for
	 * @return Vector of {@link Position}s with the given {@link Flight} IDs
	 * @throws DataNotFoundException if no {@link Flight} was found
	 */
	@NotNull
	public Vector<Position> getPositionsByFlightIDs(int[] fids) throws DataNotFoundException {

		if (fids.length == 0) {
			throw new DataNotFoundException("FlightID-array is empty!");
		}
		final String query = "SELECT t.latitude, t.longitude FROM tracking t " +
					   		 "WHERE t.flightid " + SQLQueries.IN_INT(fids);

		Vector<Position> positions = new Vector<>();

		try (DBResult result = queryDB(query);
			 ResultSet rs = result.resultSet()) {

			Position pos;
			while (rs.next()) {
				pos = new Position(rs.getDouble(1), rs.getDouble(2));
				positions.add(pos);
			}
		} catch (NoAccessException | SQLException e) {
			e.printStackTrace();
		}
		if (positions.isEmpty()) {
			throw new DataNotFoundException("No Positions found for the given FlightIDs!");
		}
		return positions;
	}

	/**
	 * returns all {@link Flight} IDs by an {@link Airline} ID
	 *
	 * @param aID is the {@link Airline} ID to search for
	 * @return int array of the {@link Flight} IDs with that {@link Airline} ID
	 */
	public int[] getFlightIDsByAirlineID(int aID) throws DataNotFoundException {
		try (PreparedStatement stmt = createPreparedStatement(SQLQueries.GET_FLIGHT_IDS_BY_AIRL_ID, true)) {

			stmt.setInt(1, aID);
			ResultSet rs = stmt.executeQuery();
			int[] fids = new int[0];
			int last;
			while (rs.next()) {
				last = fids.length;
				fids = Arrays.copyOf(fids, last + 1);
				fids[last] = rs.getInt(1);
			}
			return fids;
		} catch (SQLException e) {
			throw new DataNotFoundException(e);
		}
	}

	/**
	 * returns all {@link Flight} IDs by an {@link Airline} tag
	 *
	 * @param aTag is the {@link Airline} tag to search for
	 * @return int array of the {@link Flight} IDs with that {@link Airline} tag
	 */
	public int[] getFlightIDsByAirlineTag(@NotNull String aTag) throws DataNotFoundException {
		try (PreparedStatement stmt = createPreparedStatement(SQLQueries.GET_FLIGHT_IDS_BY_AIRL_TAG, true)) {

			stmt.setString(1, aTag);
			ResultSet rs = stmt.executeQuery();
			int[] fids = new int[0];
			int last;
			while (rs.next()) {
				last = fids.length;
				fids = Arrays.copyOf(fids, last + 1);
				fids[last] = rs.getInt(1);
			}
			return fids;
		} catch (SQLException e) {
			throw new DataNotFoundException(e);
		}
	}

	/**
	 * returns all {@link Flight} IDs by an {@link Airline} name
	 *
	 * @param aName is the {@link Airline} name to search for
	 * @return int array of the {@link Flight} IDs with that {@link Airline} name
	 */
	public int[] getFlightIDsByAirlineName(@NotNull String aName) throws DataNotFoundException {
		try (PreparedStatement stmt = createPreparedStatement(SQLQueries.GET_FLIGHT_IDS_BY_AIRL_NAME, true)) {

			stmt.setString(1, aName);
			ResultSet rs = stmt.executeQuery();
			int[] fids = new int[0];
			int last;
			while (rs.next()) {
				last = fids.length;
				fids = Arrays.copyOf(fids, last + 1);
				fids[last] = rs.getInt(1);
			}
			return fids;
		} catch (SQLException e) {
			throw new DataNotFoundException(e);
		}
	}

	/**
	 * returns all {@link Flight} IDs by an {@link Airline} country
	 *
	 * @param aCountry is the {@link Airline} country to search for
	 * @return int array of the {@link Flight} IDs with that {@link Airline} country
	 */
	public int[] getFlightIDsByAirlineCountry(@NotNull String aCountry) throws DataNotFoundException {
		try (PreparedStatement stmt = createPreparedStatement(SQLQueries.GET_FLIGHT_IDS_BY_AIRL_COUNTRY, true)) {

			stmt.setString(1, aCountry);
			ResultSet rs = stmt.executeQuery();
			int[] fids = new int[0];
			int last;
			while (rs.next()) {
				last = fids.length;
				fids = Arrays.copyOf(fids, last + 1);
				fids[last] = rs.getInt(1);
			}
			return fids;
		} catch (SQLException e) {
			throw new DataNotFoundException(e);
		}
	}

}

package planespotter.constants;

import planespotter.util.Utilities;

import java.util.Deque;

/**
 * @name SQLQueries
 * @author Bennet
 * @author Lukas
 * @author jml04
 * @description
 *
 * This Class is used to hold All SQL Querries that we need to use
 * Please insert your Query depending on what they do, Mainly Insertion and Selection
 * 
 * TODO Sort Querrys by Table
 */
public final class SQLQueries {

	//insert Querrys
	public static final String PLANEQUERRY = "INSERT INTO planes(icaonr, tailnr, registration, type, airline) VALUES(?,?,?,?,?)";
	public static final String FLIGHTQUERRY = "INSERT INTO flights(plane,src,dest,flightnr,callsign,start) VALUES(?,?,?,?,?,?)";
	public static final String TRACKINGQUERRY = "INSERT INTO tracking(flightid,latitude,longitude,altitude,groundspeed,heading,squawk,timestamp) VALUES(?,?,?,?,?,?,?,?)";
	public static final String CHECK_FLIGHT_IN_DB = "SELECT ID FROM flights WHERE plane == (?) AND flightnr == (?) AND endTime IS NULL";
	
	
	//select Querrys
	public static final String GET_LAST_FLIGHT_ID = "SELECT * from flights ORDER BY ID DESC LIMIT 1";
	public static final String GET_TRACKING_BY_FLIGHT = "SELECT * FROM tracking WHERE ID == "; // FIXME sollte das nicht flightid sein?
	public static final String GET_FLIGHTS = "SELECT * FROM flights";
	public static final String GET_FLIGHT_BY_ID = "SELECT * FROM flights WHERE ID == ";
	public static final String GET_AIRPORT_BY_TAG = "SELECT * FROM airports WHERE iatatag IS ";
	public static final String GET_PLANE_BY_ID = "SELECT * FROM planes WHERE ID == ";
	public static final String GET_AIRLINE_BY_TAG = "SELECT * FROM airlines WHERE icaotag == ";
	public static final String GET_AIRLINE_ID_BY_TAG = "SELECT ID FROM airlines WHERE icaotag == ";
	public static final String GET_FLIGHT_IDS_BY_ICAO_LIKE = "SELECT f.ID FROM flights f JOIN planes p ON p.ID = f.plane AND p.icaonr LIKE ";
	// all flights IDs with a specific callsign
	public static final String GET_FLIGHT_IDS_BY_CALLSIGN = "SELECT ID FROM flights WHERE callsign IS ";
	public static final String CHECK_END_OF_FLIGHT = "SELECT ID FROM flights WHERE endTime IS NULL";
	public static final String GET_LAST_TRACKING = "SELECT timestamp FROM tracking WHERE ID == (?) ORDER BY ID DESC LIMIT 1";
	public static final String GET_LAST_TRACKING_BY_FLIGHT_ID = "SELECT ID FROM tracking WHERE flightid == (?) ORDER BY DESC LIMIT 1";
	// flights with plane id
	public static final String GET_FLIGHTS_WITH_PLANE_ID = "SELECT * FROM flights WHERE plane = ";
	// plane id by planetype
	public static final String GET_PLANE_IDS_BY_TYPE = "SELECT ID FROM planes WHERE type IS ";
	// plane by tailnumber
	public static final String GET_FLIGHT_IDS_BY_TAILNR_LIKE = "SELECT f.ID FROM flights f JOIN planes p ON p.ID = f.plane AND p.tailnr LIKE ";
	// all planetypes
	public static final String GET_ALL_PLANETYPES = 	"SELECT DISTINCT type FROM planes WHERE type IS NOT NULL";
	// all callsigns
	public static final String GET_ALL_CALLSIGNS = "SELECT DISTINCT callsign FROM flights WHERE callsign IS NOT NULL";

	// airline search queries
	public static final String GET_FLIGHT_IDS_BY_AIRL_ID = "SELECT f.ID FROM flights f JOIN planes p ON p.ID = f.plane AND p.airline = (?)";
	public static final String GET_FLIGHT_IDS_BY_AIRL_TAG = "SELECT f.ID FROM flights f JOIN planes p ON p.ID = f.plane JOIN airlines a ON a.ID = p.airline AND a.icaotag IS (?)";
	public static final String GET_FLIGHT_IDS_BY_AIRL_NAME = "SELECT f.ID FROM flights f JOIN planes p ON p.ID = f.plane JOIN airlines a ON a.ID = p.airline AND a.name IS (?)";
	public static final String GET_FLIGHT_IDS_BY_AIRL_COUNTRY = "SELECT f.ID FROM flights f JOIN planes p ON p.ID = f.plane JOIN airlines a ON a.ID = p.airline AND a.country IS (?)";


	/** @unused */
	public static final String GET_FLIGHTS_FROM_ID = "SELECT * FROM flights WHERE ID >= (?) AND ID > (?)";
	// alle flüge ab einer bestimmten id, wird direkt in DBOut gemacht, da ich nicht weiß
	// wie die Platzhalter funktionieren

	//update Querries
	public static final String UPDATE_FLIGHT_END = "UPDATE flights SET endTime = (?) WHERE ID == (?)";

	/**
	 *
	 * @param inThis
	 * @return
	 */ // TODO zu einer Methode machen mit Wildcards & instanceof
	public static <I> String IN_INT(final I inThis) {
		StringBuilder out = new StringBuilder("IN (");
		if (inThis instanceof Deque<?> deq && deq.getFirst() instanceof Integer) {
			for (int i : (Deque<Integer>) deq) {
				out.append(i).append(",");
			}
		} else if (inThis instanceof int[] arr) {
			for (int i : arr) {
				out.append(i).append(",");
			}
		}
		return out.substring(0, out.length()-2) + ")";
	}

	public static String IN_STR(final Deque<String> inThis) {
		StringBuilder out = new StringBuilder("IN (");
		int counter = 0;
		int last = inThis.size() - 1;
		for (String s : inThis) {
			out.append(Utilities.packString(s));
			if (counter++ != last) {
				out.append(",");
			}
		}
		return out + ")";
	}

	public static String IS(String isWhat) {
		return "IS " + Utilities.packString(isWhat);
	}

	public static String SELECT(boolean distinct, String... fields) {
		StringBuilder sbl = new StringBuilder("SELECT ");
		if (distinct) {
			sbl.append("DISTINCT ");
		}
		int length = fields.length;
		int lm1 = length - 1;
		for (int i = 0; i < length; i++) {
			sbl.append(fields[i]);
			if (i < lm1) {
				sbl.append(",");
			}
			sbl.append(" ");
		}
		return sbl.toString();
	}

	public static String FROM(String... tables) {
		StringBuilder sbl = new StringBuilder("FROM ");
		int length = tables.length;
		int lm1 = length - 1;
		for (int i = 0; i < length; i++) {
			sbl.append(tables[i]);
			if (i < lm1) {
				sbl.append(",");
			}
			sbl.append(" ");
		}
		return sbl.toString();
	}

	public static String WHERE(String boolStr) {
		return "WHERE " + boolStr + " ";
	}

	// TODO JOIN, WHERE, ...

}

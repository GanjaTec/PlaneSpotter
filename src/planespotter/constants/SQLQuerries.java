package planespotter.constants;

/**
 * 
 *
 *
 * This Class is used to hold All SQL Querries that we need to use
 * Please insert your Querry depending on what they do, Mainly Insertion and Selection
 * 
 * TODO Sort Querrys by Table
 * 
 * @author Lukas
 * 
 */
public class SQLQuerries {
	
	//insert Querrys
	public static final String planequerry = "INSERT INTO planes(icaonr, tailnr, registration, type, airline) VALUES(?,?,?,?,?)";
	public static final String flightquerry = "INSERT INTO flights(plane,src,dest,flightnr,callsign,start) VALUES(?,?,?,?,?,?)";
	public static final String trackingquerry = "INSERT INTO tracking(flightid,latitude,longitude,altitude,groundspeed,heading,squawk,timestamp) VALUES(?,?,?,?,?,?,?,?)";
	public static final String checkFlightInDB = "SELECT ID FROM flights WHERE plane == (?) AND flightnr == (?) AND endTime IS NULL";
	
	
	//select Querrys
	public static final String getLastFlightID = "SELECT * from flights ORDER BY ID DESC LIMIT 1";
	public static final String getTrackingByFlight = "SELECT * FROM tracking WHERE ID == ";
	public static final String getFlights = "SELECT * FROM flights";
	public static final String getFlightByID = "SELECT * FROM flight WHERE ID == ";
	public static final String getAirportByTag = "SELECT * FROM airports WHERE iatatag == ";
	public static final String getPlaneByID = "SELECT * FROM planes WHERE ID == ";
	public static final String getAirlineByTag = "SELECT * FROM airlines WHERE icaotag == ";
	public static final String getPlaneByICAO = "SELECT * FROM planes WHERE icaonr == ";
	public static final String getFlightByCallsign = "SELECT * FROM flights WHERE callsign == ";
	public static final String checkEndOfFlight = "SELECT ID FROM flights WHERE endTime IS NULL";
	public static final String getLastTracking = "SELECT timestamp FROM tracking WHERE ID == (?) ORDER BY ID DESC LIMIT 1";
	public static final String getLastTrackingByFlightID = "SELECT ID FROM tracking WHERE flightid == (?) ORDER BY DESC LIMIT 1";
	/** @unused */
	public static final String getFlightsFromID = "SELECT * FROM flights WHERE ID >= (?) AND ID > (?)";
	// alle flüge ab einer bestimmten id, wird direkt in DBOut gemacht, da ich nicht weiß
	// wie die Platzhalter funktionieren


	//update Querries
	public static final String updateFlightEnd = "UPDATE flights SET endTime = (?) WHERE ID == (?)";

}

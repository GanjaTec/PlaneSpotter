package planespotter;

public class SqlQuerrys {
	
	//insert Querrys
	public static final String planequerry = "INSERT INTO planes(icaonr, tailnr, registration, type, airline) VALUES(?,?,?,?,?)";
	public static final String flightquerry = "INSERT INTO flights(plane,src,dest,flightnr,callsign) VALUES(?,?,?,?,?)";
	public static final String trackingquerry = "INSERT INTO tracking(flightid,latitude,longitude,altitude,groundspeed,heading,squawk) VALUES(?,?,?,?,?,?,?)";
	
	
	//select Querrys
	public static final String getLastFlightID = "SELECT * from flights ORDER BY ID DESC LIMIT 1";
	public static final String getTrackingByFlight = "SELECT * FROM tracking WHERE ID == ";
	public static final String getFlights = "SELECT * FROM flights";
	public static final String getFlightByID = "SELECT * FROM flight WHERE ID == ";
	public static final String getAirportByTag = "SELECT * FROM airports WHERE iatatag == ";
	public static final String getPlaneByID = "SELECT * FROM planes WHERE ID == ";
	public static final String getAirlineByTag = "SELECT * FROM airlines WHERE icaotag == ";
	public static final String getPlaneByICAO = "SELECT * FROM planes where icaonr == ";
	public String querry12 = "";
	public String querry13 = "";
	public String querry14 = "";

}

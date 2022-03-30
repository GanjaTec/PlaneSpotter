package planespotter;

public class SqlQuerrys {
	
	//insert Querrys
	public String planequerry = "INSERT INTO planes(icaonr, tailnr, registration, type, airline) VALUES(?,?,?,?,?)";
	public String flightquerry = "INSERT INTO flights(plane,src,dest,flightnr,callsign) VALUES(?,?,?,?,?)";
	public String trackingquerry = "INSERT INTO tracking(flightid,latitude,longitude,altitude,groundspeed,heading,squawk) VALUES(?,?,?,?,?,?,?)";
	
	
	//select Querrys
	public String getLastFlightID = "SELECT * from flights ORDER BY ID DESC LIMIT 1";
	public String querry5 = "";
	public String querry6 = "";
	public String querry7 = "";
	public String querry8 = "";
	public String querry9 = "";
	public String querry10 = "";
	public String querry11 = "";
	public String querry12 = "";
	public String querry13 = "";
	public String querry14 = "";

}

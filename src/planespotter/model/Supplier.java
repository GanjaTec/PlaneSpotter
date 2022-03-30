package planespotter.model;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import planespotter.dataclasses.*;

public class Supplier {

	public static HttpResponse<String> fr24get() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		// Request flightradar24 data with Firefox UserAgent
		// URL splitted only for visibility
		HttpRequest request = HttpRequest
				.newBuilder(URI.create("https://data-live.flightradar24.com/zones/fcgi/feed.js?faa=1&"
						// bounds defines the visible area on the live map, directly linked to planes in
						// response, parameterize
						+ "bounds=54.241%2C48.576%2C-14.184%2C13.94&" + "satellite=1&" + "mlat=1&" + "flarm=1&"
						+ "adsb=1&" + "gnd=1&" + "air=1&"
						// Disable vehicles
						+ "vehicles=0&" + "estimated=1&" + "maxage=14400&"
						// Disable gliders and stats
						+ "gliders=0&" + "stats=0"))
				// User agent to prevent Response Code 451
				.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0").build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		return response;
	}

	public static void writeToDB(List<Frame> frames) throws Exception {
		try {
			// Timestamp ts = new Timestamp(System.currentTimeMillis());
			// Instant inst = ts.toInstant();
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			String db = "jdbc:sqlite:plane.db";
			Connection conn = DriverManager.getConnection(db);

			String planequerry = "INSERT INTO planes(icaonr, tailnr, registration, type, airline) VALUES(?,?,?,?,?)";
			String flightquerry = "INSERT INTO flights(plane,src,dest,flightnr,callsign) VALUES(?,?,?,?,?)";
			String trackingquerry = "INSERT INTO tracking(flightid,latitude,longitude,altitude,groundspeed,heading,squawk) VALUES(?,?,?,?,?,?,?)";
			String getFlightID = "SELECT * from flights ORDER BY ID DESC LIMIT 1";
			long ts1 = System.nanoTime();
			for (Frame f : frames) {
				
				String planeFilter = "SELECT icaonr from planes WHERE icaonr = " + f.getIcaoAdr() + " LIMIT 1";
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(planeFilter);
				String icao = new String();
				while (rs.next()) {icao = rs.getString(0);}

				if (f.getIcaoAdr() != null) {}
				// insert into planes
				PreparedStatement pstmt = conn.prepareStatement(planequerry);
				pstmt.setString(1, f.getIcaoAdr());
				pstmt.setString(2, f.getTailnr());
				pstmt.setString(3, f.getRegistration());
				pstmt.setString(4, f.getPlanetype());
				pstmt.setString(5, f.getAirline());
				pstmt.executeUpdate();


				// insert into flights
				pstmt = conn.prepareStatement(flightquerry);
				pstmt.setString(1, f.getIcaoAdr());
				pstmt.setString(2, f.getSrcAirport());
				pstmt.setString(3, f.getDestAirport());
				pstmt.setString(4, f.getFlightnumber());
				pstmt.setString(5, f.getCallsign());
				pstmt.executeUpdate();

				// get FlightID for
				rs = stmt.executeQuery(getFlightID);
				int flightid = 0;
				while (rs.next()) {flightid = rs.getInt("ID");}
				// insert into tracking
				pstmt = conn.prepareStatement(trackingquerry);
				pstmt.setInt(1, flightid);
				pstmt.setDouble(2, f.getLat());
				pstmt.setDouble(3, f.getLon());
				pstmt.setInt(4, f.getAltitude());
				pstmt.setInt(5, f.getGroundspeed());
				pstmt.setInt(6, f.getHeading());
				pstmt.setInt(7, f.getSquawk());
				pstmt.executeUpdate();
			}

			long ts2 = System.nanoTime();
			long tdiff = ts2 - ts1;
			System.out.println("filled DB in " + tdiff + "/10 seconds");
		} catch (Exception e) {
			e.printStackTrace();
			// conn.rollback();
		}
		
	}

	public static void writeToCSV(List<String> data) throws Exception {
		// Create File and write to it
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		Instant inst = ts.toInstant();
		File file = new File("planedata_" + inst.toEpochMilli() + ".csv");
		if (file.createNewFile()) {
			System.out.println("File created: " + file.getName());
			FileWriter myWriter = new FileWriter(file.getName());
			myWriter.write("ICAOaddr,Lat,Lon,Heading,Alt,Speed,Squawk,Tailnr,Type,Reg,unk0,Src,Dest,Flightnr,unk1,unk2,"
					+ "Callsign,unk3,Airline\n");
			for (String a : data)
				myWriter.write(a);
			myWriter.close();
		} else {
			System.out.println("File already exists! This should not happen since epoch is unique");
		}
	}

	// Main loop
	public static void main(String[] args) {
		//try {
			//while(true) {
			//Deserializer ds = new Deserializer();
			//List<String> list = ds.stringMagic(fr24get());
			//List<Frame> frames = ds.deserialize(list);			
			//writeToDB(frames);
			//TimeUnit.SECONDS.sleep(30);
			//}
		//} catch (Exception e) {
		//	// Auto-generated catch block
		//	e.printStackTrace();
		//}
		
		//dbOut out = new DBOut();
		//List<DataPoint> liste = out.querryTrackingData(12);
	}
}

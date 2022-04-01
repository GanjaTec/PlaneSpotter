package planespotter.model;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;

import planespotter.dataclasses.*;

public class Supplier implements Runnable{
	private int threadNumber;
	private String ThreadName;
	//TODO Write getters
	
	public int getThreadNumber() {
		return this.threadNumber;
	}
	
	public String getThreadName() {
		return this.ThreadName;
	}
	
	public Supplier(int threadNumber) {
		this.threadNumber = threadNumber;
		this.ThreadName = "SupplierThread-" + threadNumber;
	}

	
	public void run(){
		try {
			Deserializer ds = new Deserializer();
			System.out.println("Starting Thread \"" + this.ThreadName + "\"");
			writeToDB(ds.deserialize(fr24get()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HttpResponse<String> fr24get() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		// Request flightradar24 data with Firefox UserAgent
		// URL splitted only for visibility
		HttpRequest request = HttpRequest
				.newBuilder(URI.create("https://data-live.flightradar24.com/zones/fcgi/feed.js?faa=1&"
						// bounds defines the visible area on the live map, directly linked to planes in
						// response, parameterize
						+ "bounds=54.241%2C48.576%2C-14.184%2C13.94&"	
						+ "satellite=1&"
						+ "mlat=1&"
						+ "flarm=1&"
						+ "adsb=1&"
						+ "gnd=1&"
						+ "air=1&"
						// Disable vehicles
						+ "vehicles=0&"
						+ "estimated=1&"
						+ "maxage=14400&"
						// Disable gliders and stats
						+ "gliders=0&"
						+ "stats=0"))
				// User agent to prevent Response Code 451
				.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0").build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		return response;
	}

	public void writeToDB(List<Frame> frames) {
		try {
			long ts1 = System.nanoTime();
			for (Frame f : frames) {
				
				// insert into planes
				DBIn.insertPlane(f);
				
				// insert into flights
				DBIn.insertFlight(f);
				
				// insert into tracking
				DBIn.insertTracking(f);
			}

			long ts2 = System.nanoTime();
			long tdiff = ts2 - ts1;
			System.out.println("filled DB in " + tdiff + " seconds");
		
		} catch (Exception e) {
			e.printStackTrace();
			// conn.rollback();
		}

	}

	public void writeToCSV(List<String> data) throws Exception {
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
}

package planespotter.model.nio;

import planespotter.dataclasses.Frame;
import planespotter.model.io.DBIn;
import planespotter.model.io.DBOut;
import planespotter.model.nio.Deserializer;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * @author Lukas
 *
 */
public class Supplier implements Runnable{
	private int threadNumber;
	private String ThreadName;
	private String area = "54.241%2C48.576%2C-14.184%2C13.94"; //Default OG value
	HttpClient client = HttpClient.newHttpClient();
	DBIn dbi = new DBIn();
	DBOut dbo = new DBOut();
	//TODO Write getters
	
	public int getThreadNumber() {
		return this.threadNumber;
	}
	
	public String getThreadName() {
		return this.ThreadName;
	}
	
	public Supplier(int threadNumber, String area) {
		this.threadNumber = threadNumber;
		this.ThreadName = "SupplierThread-" + threadNumber;
		this.area = area;
	}

	@Override
	public void run(){
		try {
			Deserializer ds = new Deserializer();
			System.out.println("Starting Thread \"" + this.ThreadName + "\"");
			writeToDB(ds.deserialize(fr24get()), dbo, dbi);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HttpResponse<String> fr24get() throws Exception {
		//HttpClient client = HttpClient.newHttpClient();
		// Request flightradar24 data with Firefox UserAgent
		// URL splitted only for visibility
		HttpRequest request = HttpRequest
				.newBuilder(URI.create("https://data-live.flightradar24.com/zones/fcgi/feed.js?faa=1&"
						// bounds defines the visible area on the live map, directly linked to planes in
						// response, parameterize
						+ "bounds=" + area + "&"	
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
		return this.client.send(request, BodyHandlers.ofString());
	}

	/***********************************************************************************
	 * WICHTIG:
	 *
	 * "Setting an object in an array to null or to another objects makes it eligible for garbage collection,
	 *  ASSUMING that there are no references to the same object stored anywhere."
	 *
	 *  -> es müssen irgendwo noch Referenzen sein
	 ***********************************************************************************/

	public static synchronized void writeToDB(List<Frame> frames, DBOut dbo, DBIn dbi) {
		try {
			long ts1 = System.nanoTime();
			for (Frame f : frames) {
				
				// insert into planes
				int airlineID = dbo.getAirlineIDByTag(f.getAirline());
				int planeID = dbo.checkPlaneInDB(f.getIcaoAdr());
				boolean checkPlane =  planeID > -1;
				
				if(!checkPlane) {
					planeID = dbi.insertPlane(f, airlineID);
				}
				
				
				// insert into flights
				int flightID = dbo.checkFlightInDB(f, planeID);
				boolean checkFlight = flightID > -1;
				
				if(!checkFlight) {
					flightID = dbi.insertFlight(f, planeID);
				}
				
				
				// insert into tracking
				dbi.insertTracking(f, flightID);
				
				f = null;
			}
			
			frames = null;	
			long ts2 = System.nanoTime();
			long tdiff = ts2 - ts1;
			double tdiffSec = (double) tdiff / 1_000_000_000;
			System.out.println("filled DB in " + tdiffSec + " seconds");
			
			System.gc();
		
		} catch (Exception e) {
			e.printStackTrace();
			// conn.rollback();
		}

	}

	
	/**
	 * @deprecated
	 * 
	 * @param data
	 * @throws Exception
	 */
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

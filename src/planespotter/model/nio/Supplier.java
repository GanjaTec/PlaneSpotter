package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.io.DBIn;
import planespotter.model.io.DBOut;
import planespotter.model.nio.proto.ProtoDeserializer;
import planespotter.throwables.DataNotFoundException;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import static planespotter.util.Time.*;

/**
 * @author Lukas
 *
 */
public class Supplier implements Runnable{
	private final int threadNumber;
	private final String ThreadName;
	private final String area;

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

	public Supplier(int threadNumber, @NotNull String area) {
		this.threadNumber = threadNumber;
		this.ThreadName = "SupplierThread-" + threadNumber;
		this.area = area;
	}

	@Override
	public void run(){
		try {
			Deserializer ds = new Deserializer();
			System.out.println("Starting Thread \"" + this.ThreadName + "\"");

			HttpResponse<String> response = this.fr24get();
			Deque<Fr24Frame> fr24Frames = new ProtoDeserializer().deserialize(response);
			// use Proto-deserialize instead of ds.deserialize(...) for right-way-deserialized frames (no "" anymore)
			// old: ds.deserialize(response)
			writeToDB(fr24Frames, this.dbo, this.dbi);
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

	public static synchronized void writeToDB(Deque<Fr24Frame> fr24Frames, DBOut dbo, DBIn dbi) {
		// TODO: 12.06.2022 mindestens die dbo-Anfragen aus der Schleife raus, alles vorher schon in je eine Collection und darin abfragen
		long ts1 = nowMillis();
		var airlineTagsIDs = new HashMap<String, Integer>();
		var planeIcaoIDs = new HashMap<String, Integer>();
		var flightNRsIDs = new HashMap<String, Integer>();
		try {
			airlineTagsIDs = (HashMap<String, Integer>) dbo.getAirlineTagsIDs();
			planeIcaoIDs = (HashMap<String, Integer>) dbo.getPlaneIcaosIDs();
			flightNRsIDs = dbo.getFlightNRsWithFlightIDs();
		} catch (DataNotFoundException ignored) {
		}
		while (!fr24Frames.isEmpty()) {
				Fr24Frame fr24Frame = fr24Frames.poll();

				// insert into planes
				int airlineID = airlineTagsIDs.getOrDefault(fr24Frame.getAirline(), 1);
				int planeID = planeIcaoIDs.getOrDefault(fr24Frame.getIcaoAdr(), -1);
				//int airlineID = dbo.getAirlineIDByTag(frame.getAirline());
				//int planeID = dbo.checkPlaneInDB(frame.getIcaoAdr());
				boolean checkPlane = planeID > -1;
				if (!checkPlane) {
					planeID = dbi.insertPlane(fr24Frame, airlineID);
				}

				// insert into flights
				int flightID = flightNRsIDs.getOrDefault(fr24Frame.getFlightnumber(), -1);
				//int flightID = dbo.checkFlightInDB(frame, planeID);
				boolean checkFlight = flightID > -1;
				if (!checkFlight) {
					flightID = dbi.insertFlight(fr24Frame, planeID);
				}

				// insert into tracking
				dbi.insertTracking(fr24Frame, flightID);
			}

			System.out.println("filled DB in " + elapsedSeconds(ts1) + " seconds!");

			System.gc();

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

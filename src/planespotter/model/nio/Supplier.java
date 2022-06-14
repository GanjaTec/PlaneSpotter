package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.io.DBIn;
import planespotter.model.io.DBOut;
import planespotter.throwables.DataNotFoundException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static planespotter.util.Time.*;

/**
 * @name Supplier
 * @author Lukas
 * @author jml04
 *
 * Class Supplier is the Data-Supplier,
 * it is able to collect different types of flight-data Frames (Fr24Frames),
 * it contains methods to collect data and some to write data
 * to the database.
 * @see planespotter.SupplierMain
 * @see planespotter.a_test.TestMain
 */
public class Supplier implements Runnable {
	// inserted frames counter for all writeToDB inserts
	private static int insertedFrames, newPlanes, newFlights;
	// initializer
	static {
		insertedFrames = 0;
		newPlanes = 0;
		newFlights = 0;
	}
	// class instance fields
	private final int threadNumber;
	private final String ThreadName;
	private final String area;
	private final HttpClient client;
	private final DBIn dbi;
	private final DBOut dbo;
	//TODO Write getters
	
	public int getThreadNumber() {
		return this.threadNumber;
	}
	
	public String getThreadName() {
		return this.ThreadName;
	}

	public Supplier() {
		this(0, "");
	}

	public Supplier(int threadNumber, @NotNull String area) {
		this.threadNumber = threadNumber;
		this.ThreadName = "SupplierThread-" + threadNumber;
		this.area = area;
		this.client = HttpClient.newHttpClient();
		this.dbi = new DBIn();
		this.dbo = new DBOut();
	}

	@Override
	public void run(){
		try {
			System.out.println("Starting Supplier-Thread \"" + this.ThreadName + "\"...");

			Fr24Deserializer deserializer = new Fr24Deserializer();
			HttpResponse<String> response = this.fr24get();
			// use Proto-deserializer for correct frame data
			Deque<Fr24Frame> fr24Frames = deserializer.deserialize(response);
			writeToDB(fr24Frames, this.dbo, this.dbi);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public HttpResponse<String> fr24get()
			throws IOException, InterruptedException {

		//HttpClient client = HttpClient.newHttpClient(); TODO das geht doch auch hier oder?
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

	/**
	 * gets HttpResponse's for specific areas and deserializes its data to Frames
	 *
	 * @param areas are the Areas where data should be deserialized from
	 * @param scheduler is the Scheduler to allow parallelism
	 * @return Deque of deserialized Frames
	 */
	public synchronized Deque<Fr24Frame> getFr24Frames(String[] areas, final Fr24Deserializer deserializer, final Scheduler scheduler) {
		var concurrentDeque = new ConcurrentLinkedDeque<Fr24Frame>();
		System.out.println("Deserializing Fr24-Data...");

		var ready = new AtomicBoolean(false);
		var counter = new AtomicInteger(areas.length - 1);
		for (var area : areas) {
			scheduler.exec(() -> {
				var supplier = new Supplier(0, area);
				try {
					var data = deserializer.deserialize(supplier.fr24get());
					while (!data.isEmpty()) {
						concurrentDeque.add(data.poll());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (counter.get() == 0) {
					ready.set(true);
				}
				counter.getAndDecrement();
			}, "Fr24-Deserializer");
		}
		while (!ready.get()) {
			System.out.print(":");
			try {
				TimeUnit.MILLISECONDS.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println();
		return concurrentDeque;
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
		long ts1 = nowMillis();
		var airlineTagsIDs = new HashMap<String, Integer>();
		var planeIcaoIDs = new HashMap<String, Integer>();
		var flightNRsIDs = new HashMap<String, Integer>();
		try {
			airlineTagsIDs = dbo.getAirlineTagsIDs();
			planeIcaoIDs = dbo.getPlaneIcaosIDs();
			flightNRsIDs = dbo.getFlightNRsWithFlightIDs();
		} catch (DataNotFoundException ignored) {
			// something doesn't exist in the DB, this is no error!
		}
		int airlineID, planeID, flightID;
		boolean checkPlane, checkFlight;
		Fr24Frame frame;
		while (!fr24Frames.isEmpty()) {
				frame = fr24Frames.poll();
				// insert into planes
				airlineID = airlineTagsIDs.getOrDefault(frame.getAirline(), 1);
				planeID = planeIcaoIDs.getOrDefault(frame.getIcaoAdr(), -1);
				checkPlane = planeID > -1;
				if (!checkPlane) {
					planeID = dbi.insertPlane(frame, airlineID);
					newPlanes++;
				}
				// insert into flights
				flightID = flightNRsIDs.getOrDefault(frame.getFlightnumber(), -1);
				checkFlight = flightID > -1;
				if (!checkFlight) {
					flightID = dbi.insertFlight(frame, planeID);
					newFlights++;
				}
				// insert into tracking
				dbi.insertTracking(frame, flightID);
				// increasing the inserted frames value
				insertedFrames++;
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

	public static int getInserted() {
		return insertedFrames;
	}

	public static int getNewPlanes() {
		return newPlanes;
	}

	public static int getNewFlights() {
		return newFlights;
	}
}

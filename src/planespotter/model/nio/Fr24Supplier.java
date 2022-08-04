package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;

import planespotter.model.Fr24Collector;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.io.DBIn;
import planespotter.throwables.Fr24Exception;

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
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @name Supplier
 * @author Lukas
 * @author jml04
 *
 * Class Supplier is the Data-Supplier,
 * it is able to collect different types of flight-data Frames (Fr24Frames),
 * it contains methods to collect data and some to write data
 * to the database.
 * @see Fr24Collector
 * @see planespotter.a_test.TestMain
 */
public class Fr24Supplier implements Supplier {
	// class instance fields
	private final int threadNumber;
	private final String ThreadName;
	private final String area;
	private final HttpClient httpClient;
	//TODO Write getters

	public Fr24Supplier() {
		this(0, "");
	}

	public Fr24Supplier(int threadNumber, @NotNull String area) {
		this.threadNumber = threadNumber;
		this.ThreadName = "SupplierThread-" + threadNumber;
		this.area = area;
		this.httpClient = HttpClient.newHttpClient();
	}

	@Override
	public void supply() {
		try {
			System.out.println("Thread '" + this.getThreadName() + "' is starting...");
			// use Proto-deserializer for correct frame data
			Fr24Deserializer deserializer = new Fr24Deserializer();
			HttpResponse<String> response = this.sendRequest();
			int statusCode = response.statusCode();
			if (statusCode != 200) {
				throw new Fr24Exception("Fr24Supplier: Status code is invalid! " + statusCode);
			}
			Deque<Fr24Frame> fr24Frames = deserializer.deserialize(response);
			// writing frames to DB
			DBIn.write(fr24Frames);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public HttpResponse<String> sendRequest()
			throws IOException, InterruptedException {

		HttpRequest request = this.createHttpRequest("https://data-live.flightradar24.com/zones/fcgi/feed.js?faa=1&"
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
				+ "stats=0");
		return this.httpClient.send(request, BodyHandlers.ofString());
	}

	private HttpRequest createHttpRequest(final String request) {
		return HttpRequest.newBuilder(URI.create(request))
				// User agent to prevent Response Code 451
				.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0")
				.build();
	}

	/**
	 * gets HttpResponse's for specific areas and deserializes its data to Frames
	 *
	 * @param areas are the Areas where data should be deserialized from
	 * @param scheduler is the Scheduler to allow parallelism
	 * @return Deque of deserialized Frames
	 */
	private static final Object lock = new Object();
	public static synchronized void collectFramesForArea(@NotNull String[] areas, @NotNull final Fr24Deserializer deserializer, @NotNull final Scheduler scheduler, boolean ignoreMaxSize) {
		System.out.println("Deserializing Fr24-Data...");

		// TODO ERSATZ METHOD, with simple Supplier.supply(), threaded

		int length = 5;
		String[] part = new String[length];

		int i = 0;
		AtomicInteger tNumber = new AtomicInteger(0),
					  counter = new AtomicInteger(areas.length-2);
		for (String area : areas) {
			if (i < length) {
				part[i++] = area;
			} else {
				final String[] fPart = part;
				scheduler.exec(() -> Arrays.stream(fPart).forEach(ar -> {
					Fr24Supplier supplier = new Fr24Supplier(tNumber.get(), ar);
					try {
						if (!ignoreMaxSize && LiveLoader.maxSizeReached()) {
							return;
						}
						HttpResponse<String> response = supplier.sendRequest();
						int statusCode = response.statusCode();
						if (statusCode != 200) {
							throw new Fr24Exception("Fr24Supplier: Status code is invalid! " + statusCode);
						}
						Deque<Fr24Frame> data = deserializer.deserialize(response);
						LiveLoader.insertLater(data);
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					synchronized (lock) {
						counter.decrementAndGet();
					}
				}), "Fr24-Deserializer-" + tNumber.getAndIncrement(), false, Scheduler.HIGH_PRIO, true);
				i = 0;
				part = new String[length];
			}
		}

		while (scheduler.active() > 1) {
			Scheduler.sleep(100);
			System.out.print(":" + counter.get());
		}
	}
	
	/**
	 * @deprecated
	 * 
	 * @param data
	 * @throws Exception
	 */
	@Deprecated
	public void writeToCSV(List<String> data) throws IOException {
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

	public int getThreadNumber() {
		return this.threadNumber;
	}

	public String getThreadName() {
		return this.ThreadName;
	}

}

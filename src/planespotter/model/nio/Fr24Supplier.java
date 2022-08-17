package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;

import planespotter.model.Fr24Collector;
import planespotter.model.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.io.DBIn;
import planespotter.throwables.Fr24Exception;
import planespotter.util.Time;
import planespotter.util.Utilities;

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
import java.util.stream.Stream;

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
	 * @param deserializer is the Fr24Deserializer which is used to deserialize the requested data
	 * @param ignoreMaxSize if it's true, allowed max size of insertLater-queue is ignored
	 * @see planespotter.model.nio.LiveLoader
	 */
	@SuppressWarnings(value = "duplicate")
	public static synchronized boolean collectFramesForArea(@NotNull String[] areas, @NotNull final Fr24Deserializer deserializer, @NotNull final Scheduler scheduler, boolean ignoreMaxSize) {
		System.out.println("[Supplier] Collecting Fr24-Data...");

		Arrays.stream(areas)
				.parallel()
				.forEach(area -> {
					Fr24Supplier supplier = new Fr24Supplier(0, area);
					if (!ignoreMaxSize && LiveLoader.maxSizeReached()) {
						System.out.println("Queue is full!");
						return;
					}
					HttpResponse<String> response;
					try {
						response = supplier.sendRequest();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
						return;
					}
					int statusCode = response.statusCode();
					Utilities.checkStatusCode(statusCode);
					Deque<Fr24Frame> data = deserializer.deserialize(response);
					LiveLoader.insertLater(data);
				});

		return true;
	}

	public static synchronized void collectPStream(@NotNull String[] areas, @NotNull final Fr24Deserializer deserializer, boolean ignoreMaxSize) {
		System.out.println("[Supplier] Collecting Fr24-Data with parallel Stream...");

		AtomicInteger tNumber = new AtomicInteger(0);
		long startTime = Time.nowMillis();
		try (Stream<@NotNull String> parallel = Arrays.stream(areas).parallel()) {
			parallel.forEach(area -> {
				if (!ignoreMaxSize && LiveLoader.maxSizeReached()) {
					System.out.println("Max queue-size reached!");
					return;
				}
				Fr24Supplier supplier = new Fr24Supplier(tNumber.get(), area);

				Deque<Fr24Frame> data;
				try {
					data = deserializer.deserialize(supplier.sendRequest());
					LiveLoader.insertLater(data);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			});
		}
		System.out.println("[Supplier] Elapsed time: " + Time.elapsedMillis(startTime) + " ms");
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

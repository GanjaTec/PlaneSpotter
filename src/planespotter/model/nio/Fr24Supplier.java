package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Area;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.ExceptionHandler;
import planespotter.model.Fr24Collector;
import planespotter.throwables.Fr24Exception;
import planespotter.throwables.MalformedAreaException;
import planespotter.util.Time;
import planespotter.util.Utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
public class Fr24Supplier extends HttpSupplier {

	public  static final String FR24_ADDRESS_PATH = "https://data-live.flightradar24.com/zones/fcgi/feed.js";

	// bounds define the visible area on the live map
	private static final String FR24_QUERY_1 	  = "?faa=1&bounds=";

	// Disable vehicles // Disable gliders and stats // Enabling everything else
	private static final String FR24_QUERY_2 	  = "&satellite=1&mlat=1&flarm=1&adsb=1&gnd=1&air=1&vehicles=0&estimated=1&maxage=14400&gliders=0&stats=0";

	// class instance fields
	private final String threadName;
	private final Area area;
	private final DataProcessor dataProcessor;
	private final Fr24Deserializer deserializer;

	/**
	 * constructs a default {@link Fr24Supplier} with thread number 0 and no area,
	 * can be used as utility-supplier if needed.
	 * WARNING: this supplier is not able to collect data
	 */
	public Fr24Supplier(@NotNull DataProcessor dataProcessor) {
		this((Area) null, dataProcessor);
	}

	public Fr24Supplier(Area area, @NotNull DataProcessor dataProcessor) {
		this(area, dataProcessor, new Fr24Deserializer());
	}

	/**
	 * constructs a custom {@link Fr24Supplier} with thread number and area
	 *
	 * @param area is the area String
	 */
	public Fr24Supplier(@NotNull String area, @NotNull DataProcessor dataProcessor) throws MalformedAreaException {
		this(area, dataProcessor, new Fr24Deserializer());
	}

	/**
	 * constructs a custom {@link Fr24Supplier} with thread number and area
	 *
	 * @param area is the area String
	 */
	public Fr24Supplier(@NotNull String area, @NotNull DataProcessor dataProcessor, @NotNull Fr24Deserializer deserializer) throws MalformedAreaException {
		this(Area.fromString(area), dataProcessor, deserializer);
	}

	/**
	 * constructs a custom {@link Fr24Supplier} with thread number and area
	 *
	 * @param area is the area String
	 */
	public Fr24Supplier(Area area, @NotNull DataProcessor dataProcessor, @NotNull Fr24Deserializer deserializer) {
		this.threadName = "Supplier Thread";
		this.area = area;
		this.dataProcessor = dataProcessor;
		this.deserializer = deserializer;
	}

	private static URI getRequestURI(@NotNull Area area) {
		return URI.create(FR24_ADDRESS_PATH + FR24_QUERY_1 + area + FR24_QUERY_2);
	}

	/**
	 * Override for the supply method of the Supplier Interface
	 * prepares and runs the supplier thread
	 */
	@Override
	public void supply() {
		try {
			HttpResponse<String> response = sendRequest(3);

			Utilities.checkStatusCode(response.statusCode());
			Stream<Fr24Frame> fr24Frames = deserializer.deserialize(response);
			// writing frames to DB
			dataProcessor.insertLater(fr24Frames);

		} catch (IOException | InterruptedException | IllegalArgumentException | Fr24Exception e) {
			ExceptionHandler onError = getExceptionHandler();
			if (onError != null) {
				onError.handleException(e);
			} else {
				System.err.println("Unhandled exception occurred during Supplier-Process\n" + e);
			}
		}
	}


	/**
	 * sends a HTTP Request for live Data to Flightradar24, returns a HttpResponse containing the JSON with all flights
	 * contained in the given area. Uses an User-Agent string to mimic a browser in order to circumvent access-restrictions
	 * which would appear as HTTP Status code 451 (unavailabe for legal reasons). A rather strange status code since
	 * it is (as of RFC 9110) just a proposed status code used for censored websites and its probably misused by the devs
	 * of flightradar24.
	 *
	 * To quote RFC 7725:
	 * 	  "This status code indicates that the server is denying access to the
	 *    resource as a consequence of a legal demand.
	 *
	 *    The server in question might not be an origin server.  This type of
	 *    legal demand typically most directly affects the operations of ISPs
	 *    and search engines."
	 *
	 * The different parts of the request itself are as following, all of those are FR24 internal settings:
	 * bounds: the area from where planes are querried
	 * satellite, mlat, flarm, adsb: the datasource for the positioning data itself, leave enabled
	 * gnd, air: planes on the ground and in the air, leave enabled
	 * vehicles: airport support vehicles, leave disabled
	 * estimated: estimate plane positions based on previous dataframes
	 * maxage: dont list planes that had no updates longer than value in seconds
	 * gliders: disable gliders
	 * stats: stats for the json response, contains number of planes and other statistics in the response, leave disabled or it will break the deserializer
	 *
	 * @return HttpResponse, the json containing the response of the server
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Override
	@NotNull
	public HttpResponse<String> sendRequest(int timeoutSec) throws IOException, InterruptedException {

		HttpRequest request = HttpRequest.newBuilder(getRequestURI(area))
				.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0")
				.timeout(Duration.ofSeconds(timeoutSec))
				.build();
		return HTTP_CLIENT.send(request, BodyHandlers.ofString());
	}
	
	/**
	 * old method to write json to csv, no longer used but maybe in the future
	 *
	 * @deprecated
	 * 
	 * @param data are the String-lines which are written to CSV
	 * @throws IOException
	 */
	@Deprecated
	public void writeToCSV(List<String> data) throws IOException {
		// Create File and write to it
		Timestamp ts = new Timestamp(Time.nowMillis());
		Instant inst = ts.toInstant();
		File file = new File("planedata_" + inst.toEpochMilli() + ".csv");
		if (!file.createNewFile()) {
			System.err.println("File already exists! This should not happen since epoch is unique");
			return;
		}
		System.out.println("File created: " + file.getName());
		try (FileWriter writer = new FileWriter(file.getName())) {
			writer.write("ICAOaddr,Lat,Lon,Heading,Alt,Speed,Squawk,Tailnr,Type,Reg," +
							"unk0,Src,Dest,Flightnr,unk1,unk2,Callsign,unk3,Airline\n");
			for (String val : data)
				writer.write(val);
		}
	}

	/**
	 * getter for threadname
	 *
	 * @return String threadname
	 */
	public String getThreadName() {
		return this.threadName;
	}

	/**
	 * getter for {@link DataProcessor}
	 *
	 * @return data loader of this supplier
	 */
	public DataProcessor getDataLoader() {
		return dataProcessor;
	}
}

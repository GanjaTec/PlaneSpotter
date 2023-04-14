package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.dataclasses.Frame;
import planespotter.dataclasses.ReceiverFrame;
import planespotter.model.ExceptionHandler;
import planespotter.throwables.URIException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Stream;

/**
 * @name ADSBSupplier
 * @author jml04
 * @author Lukas
 * @version 1.0
 *
 * @description
 * The {@link ADSBSupplier} represents a {@link HttpSupplier},
 * specified for ADSB antenna data
 */
public class ADSBSupplier extends HttpSupplier {

    // the request URIs for data requests
    @NotNull private URI requestUri;
    @Nullable private URI receiverRequestUri;

    // DataLoader instance with data queue
    @NotNull private final DataProcessor dataProcessor;

    // Deserializer for deserializing ADSB data (ADSBDeserializer)
    @NotNull private final ADSBDeserializer deserializer;

    // receiver data frame
    @Nullable private ReceiverFrame currentReceiverData;

    // 1 if initialized, 0 if not
    private byte initialized = 0;

    /**
     * constructs a new {@link ADSBSupplier} with {@link URI} and {@link DataProcessor}
     *
     * @param uri is the data request {@link URI}, can be changed later
     * @param dataProcessor is a {@link DataProcessor} which contains the data queue where data can be taken from
     */
    public ADSBSupplier(@NotNull String uri, @NotNull DataProcessor dataProcessor, @Nullable String receiverUri) {
        this(uri, dataProcessor, receiverUri, new ADSBDeserializer());
    }

    /**
     * constructs a new {@link ADSBSupplier} with {@link URI} and {@link DataProcessor}
     *
     * @param uri is the data request {@link URI}, can be changed later
     * @param dataProcessor is a {@link DataProcessor} which contains the data queue where data can be taken from
     */
    public ADSBSupplier(@NotNull String uri, @NotNull DataProcessor dataProcessor, @Nullable String receiverUri, @NotNull ADSBDeserializer deserializer) {
        this.requestUri = URI.create(uri);
        this.receiverRequestUri = receiverUri != null ? URI.create(receiverUri) : null;
        this.dataProcessor = dataProcessor;
        this.deserializer = deserializer;
    }

    /**
     * {@link Supplier}-task, supplies ADSB data from the
     * request URI to the {@link DataProcessor}-data queue,
     * handles exceptions with the {@link HttpSupplier}-{@link ExceptionHandler}
     */
    @Override
    public synchronized void supply() {
        try {
            if (initialized == 0) {
                supplyReceiverData();
                initialized++;
            }
            HttpResponse<String> response = sendRequest(2);
            Stream<? extends Frame> frames = deserializer.deserialize(response); // ADSB frames
            dataProcessor.insertLater(frames);
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            ExceptionHandler onError = getExceptionHandler();
            if (onError != null) {
                onError.handleException(e);
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * sends a {@link HttpRequest} to the specified data cloud
     * {@link URI} and returns the received {@link HttpResponse}
     *
     * @param timeoutSec is the time in seconds, where the Request should time out if already running
     * @return the received {@link HttpResponse}
     * @throws IOException if an I/O error occurs while sending the request
     * @throws InterruptedException if the sending was interrupted
     * @throws IllegalArgumentException if the timeout seconds are negative
     */
    @Override
    @NotNull
    public HttpResponse<String> sendRequest(int timeoutSec) throws IOException, InterruptedException, IllegalArgumentException {
        HttpRequest request = HttpRequest.newBuilder(requestUri)
                .timeout(Duration.ofSeconds(timeoutSec))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * supplies the ADSB-receiver data
     */
    private void supplyReceiverData() {
        System.out.println("Supplying receiver data...");
        try {
            HttpResponse<String> response = sendReceiverRequest(2);
            currentReceiverData = deserializer.deserializeReceiverData(response);
            return;
        } catch (URIException uriex) {
            System.out.println(uriex.getMessage());
        } catch (IOException | InterruptedException e) {
            ExceptionHandler onError = getExceptionHandler();
            if (onError != null) {
                onError.handleException(e);
            } else {
                e.printStackTrace();
            }
        }
        currentReceiverData = null;
    }

    /**
     *
     *
     * @param timeoutSec
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @NotNull
    private HttpResponse<String> sendReceiverRequest(int timeoutSec) throws IOException, InterruptedException, URIException {
        if (receiverRequestUri == null) {
            throw new URIException("Receiver request URI not found!");
        }
        HttpRequest request = HttpRequest.newBuilder(receiverRequestUri)
                .timeout(Duration.ofSeconds(timeoutSec))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * sets the request {@link URI} to make it possible to send requests to different {@link URI}s
     *
     * @param requestUri is the new request {@link URI}
     */
    public void setRequestUri(@NotNull URI requestUri) {
        this.requestUri = requestUri;
    }

    /**
     * sets the request {@link URI} for the receiver data request
     *
     * @param requestUri is the new request {@link URI}
     */
    public void setReceiverRequestUri(@NotNull URI requestUri) {
        this.receiverRequestUri = requestUri;
    }

    @Nullable
    public ReceiverFrame getReceiverData() {
        return currentReceiverData;
    }
}

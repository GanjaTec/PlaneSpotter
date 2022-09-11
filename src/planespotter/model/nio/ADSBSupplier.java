package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Frame;
import planespotter.model.ExceptionHandler;

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

    // the request URI for requests
    private URI requestUri;

    // DataLoader instance with data queue
    private final DataLoader dataLoader;

    // Deserializer for deserializing ADSB data (ADSBDeserializer)
    private final Deserializer<HttpResponse<String>> deserializer;

    /**
     * constructs a new {@link ADSBSupplier} with {@link URI} and {@link DataLoader}
     *
     * @param uri is the data request {@link URI}, can be changed later
     * @param dataLoader is a {@link DataLoader} which contains the data queue where data can be taken from
     */
    public ADSBSupplier(@NotNull String uri, @NotNull DataLoader dataLoader) {
        this.requestUri = URI.create(uri);
        this.dataLoader = dataLoader;
        this.deserializer = new ADSBDeserializer();
    }

    /**
     * {@link Supplier}-task, supplies ADSB data from the
     * request URI to the {@link DataLoader}-data queue,
     * handles exceptions with the {@link HttpSupplier}-{@link ExceptionHandler}
     */
    @Override
    public synchronized void supply() {
        try {
            HttpResponse<String> response = sendRequest(2);
            Stream<? extends Frame> frames = deserializer.deserialize(response); // ADSB frames
            dataLoader.insertLater(frames);
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
     * sets the request {@link URI} to make it possible to send requests to different {@link URI}s
     *
     * @param requestUri is the new request {@link URI}
     */
    public void setRequestUri(@NotNull URI requestUri) {
        this.requestUri = requestUri;
    }
}

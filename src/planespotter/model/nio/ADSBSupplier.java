package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;

import planespotter.dataclasses.ADSBFrame;
import planespotter.dataclasses.Frame;
import planespotter.model.ExceptionHandler;
import planespotter.model.Scheduler;
import planespotter.util.Utilities;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.stream.Stream;

public class ADSBSupplier extends HttpSupplier {

    private final URI requestUri;

    private final DataLoader dataLoader;

    public ADSBSupplier(@NotNull String uri, @NotNull DataLoader dataLoader) {
        this.requestUri = URI.create(uri);
        this.dataLoader = dataLoader;
    }

    @Override
    public synchronized void supply() {

        try {
            HttpResponse<String> response = sendRequest(2);
            Stream<ADSBFrame> frames = new ADSBDeserializer().deserialize(response);
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

    @Override
    @NotNull
    public HttpResponse<String> sendRequest(int timeoutSec) throws IOException, InterruptedException, IllegalArgumentException {
        HttpRequest request = HttpRequest.newBuilder(requestUri)
                .timeout(Duration.ofSeconds(timeoutSec))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

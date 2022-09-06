package planespotter.model.nio;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.ADSBFrame;
import planespotter.model.Scheduler;
import planespotter.util.Utilities;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ADSBSupplier implements HttpSupplier {

    private final URI requestUri;

    public ADSBSupplier(@NotNull String uri) {
        this.requestUri = URI.create(uri);
    }

    @Override
    public void supply() {

        new Scheduler().schedule(() -> {
            try {
                HttpResponse<String> response = sendRequest();
                new ADSBDeserializer().deserialize(response);
                System.out.println();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }, 0, 10);
    }

    @Override
    @NotNull
    public HttpResponse<String> sendRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(requestUri).build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Frame;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;

public class ADSBSupplier implements HttpSupplier {

    private static final URI REQUEST_URI = URI.create("http://192.168.178.47:8080/data/aircraft.json");

    public ADSBSupplier() {

    }

    @Override
    public void supply() {
        HttpResponse<String> response;
        try {
            response = sendRequest();
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    @NotNull
    public HttpResponse<String> sendRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(REQUEST_URI).build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

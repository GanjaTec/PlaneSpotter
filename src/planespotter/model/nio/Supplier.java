package planespotter.model.nio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface Supplier extends Runnable {

    HttpClient httpClient = HttpClient.newHttpClient();

    void supply();

    HttpResponse<String> sendRequest() throws IOException, InterruptedException;

    default HttpRequest createHttpRequest(final String request) {
        return HttpRequest
                .newBuilder(URI.create(request))
                // User agent to prevent Response Code 451
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0")
                .build();
    }

    default void run() {
        this.supply();
    }

}

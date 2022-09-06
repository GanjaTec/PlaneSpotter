package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface HttpSupplier extends Supplier {

    // static HttpClient instance, fixed thread overhead
    HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @NotNull
    HttpResponse<String> sendRequest() throws IOException, InterruptedException;

    // optional http request template with dynamic URI but static header
    @NotNull
    default HttpResponse<String> sendRequest(@NotNull String requestUri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(requestUri))
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0")
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }


}

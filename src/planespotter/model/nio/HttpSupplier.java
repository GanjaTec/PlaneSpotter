package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

public interface HttpSupplier extends Supplier {

    // static HttpClient instance, fixed thread overhead
    HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @NotNull HttpResponse<String> sendRequest() throws IOException, InterruptedException;


}

package planespotter.model.nio.client.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Collection;

public interface HttpSender<D> {

    HttpClient senderClient = HttpClient.newHttpClient();

    void send(URI uri, D[] data) throws IOException;

    void send(URI uri) throws IOException;

    Collection<D> getData(URI uri) throws IOException;
}

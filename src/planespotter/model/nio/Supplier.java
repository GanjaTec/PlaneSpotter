package planespotter.model.nio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface Supplier extends Runnable {

    void supply();

    HttpResponse<String> sendRequest() throws IOException, InterruptedException;

    default void run() {
        this.supply();
    }

}

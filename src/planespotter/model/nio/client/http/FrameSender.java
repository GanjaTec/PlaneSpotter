package planespotter.model.nio.client.http;

import com.google.gson.Gson;
import planespotter.dataclasses.Frame;
import planespotter.dataclasses.UniFrame;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Flow;

public class FrameSender implements HttpSender<UniFrame> {

    private static final Gson gson = new Gson();

    @Override
    public void send(URI uri, UniFrame[] data) throws IOException {
        String body = gson.toJson(data);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString(body);

        HttpResponse<?> resp;
        try {
            resp = httpPOST(uri, HttpResponse.BodyHandlers.ofString(), publisher, 20);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        checkStatus(resp.statusCode());
        System.out.println(resp.body());
    }

    @Override
    public void send(URI uri) throws IOException {
        HttpResponse<?> resp;
        try {
            resp = httpGET(uri, HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        checkStatus(resp.statusCode());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<UniFrame> getData(URI uri) throws IOException {

        HttpResponse<String> resp;
        try {
            resp = (HttpResponse<String>) httpGET(uri, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new IOException(e);
        }
        checkStatus(resp.statusCode());

        String body = resp.body();
        UniFrame[] frames = gson.fromJson(body, UniFrame[].class);
        return Arrays.asList(frames);
    }

    private void checkStatus(int status) throws IOException {
        if (status != 200)
            throw new IOException("HTTP Status code: " + status);
    }

    private HttpResponse<?> httpPOST(URI uri, HttpResponse.BodyHandler<?> bodyHandler, HttpRequest.BodyPublisher bodyPublisher, int timeoutS) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("content-type", "application/json")
                .POST(bodyPublisher)
                .timeout(Duration.ofSeconds(timeoutS))
                .build();
        return senderClient.send(req, bodyHandler);
    }

    private HttpResponse<?> httpGET(URI uri, HttpResponse.BodyHandler<?> bodyHandler) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        return senderClient.send(req, bodyHandler);
    }
}

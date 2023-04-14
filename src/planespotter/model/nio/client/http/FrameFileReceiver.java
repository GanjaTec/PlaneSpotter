package planespotter.model.nio.client.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class FrameFileReceiver extends FrameSender {

    public FrameFileReceiver() {
    }

    public void getFile(String uriStr, Path dir, int timeoutS) throws IOException {
        URI uri = URI.create(uriStr);
        HttpResponse<?> resp;
        try {
            resp = httpGET(uri, HttpResponse.BodyHandlers.ofFileDownload(dir), timeoutS);
            System.out.println("File downloaded");
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        checkStatus(resp.statusCode());
    }

}

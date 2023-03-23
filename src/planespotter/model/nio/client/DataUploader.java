package planespotter.model.nio.client;

import de.gtec.util.threading.Threading;
import de.gtec.util.time.Time;
import planespotter.dataclasses.Frame;
import planespotter.dataclasses.UniFrame;
import planespotter.model.Scheduler;
import planespotter.model.nio.client.http.FrameSender;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class DataUploader<D extends Frame> {

    private static final double A = 0.125, B = 1 - 0.125;

    private final FrameSender sender;

    private final Scheduler scheduler;
    private Queue<D> uploadQueue;
    private Queue<Throwable> errorQueue;
    private String host;
    private boolean running;

    private int minUploadCount;

    private int flow, byteFlow;

    public DataUploader(String host, int minUploadCount, Scheduler scheduler) {
        this.sender = new FrameSender();
        this.scheduler = scheduler;
        this.uploadQueue = Threading.concurrentQueue();
        this.errorQueue = Threading.concurrentQueue();
        this.host = "http://" + host + ":8080";
        this.running = false;
        this.minUploadCount = minUploadCount;
        this.flow = 1;
    }

    public void start() {
        if (isRunning()) { // check for running uploader
            stop();
        }
        setRunning(true);
    }

    public void stop() {
        setRunning(false);
    }

    public void addData(Collection<D> data) {
        uploadQueue.addAll(data);

        // TODO: 23.03.2023 use scheduler
        if (uploadQueue.size() >= minUploadCount) {
            scheduler.exec(this::upload, "Data Upload");
        }
    }

    public void upload() {
        if (uploadQueue.isEmpty()) {
            return;
        }
        long start = Time.nowMillis();
        URI uri = URI.create(host + "/data/api/upload/frames");
        UniFrame[] buffer = new UniFrame[uploadQueue.size()];
        int idx = 0;
        while (!uploadQueue.isEmpty()) {
            D frame = uploadQueue.poll();
            if (frame != null) {
                buffer[idx++] = UniFrame.of(frame);
            }
        }
        try {
            System.out.println("[UP]: Uploading " + buffer.length + " frames...");
            sender.send(uri, buffer);
        } catch (IOException e) {
            errorQueue.add(e);
        } finally {
            long elapsed = Time.elapsedMillis(start);
            this.flow = (int) (B * (idx / elapsed) + A * getFlow());
            this.byteFlow = (int) (B * ((idx * UniFrame.SIZE) / elapsed) + A * getByteFlow());
        }
    }

    public int getFlow() {
        return flow;
    }

    public int getByteFlow() {
        return byteFlow;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public FrameSender getSender() {
        return sender;
    }

    public Queue<D> getUploadQueue() {
        return uploadQueue;
    }

    public int getQueueSize() {
        return uploadQueue.size();
    }

    public void setUploadQueue(Queue<D> uploadQueue) {
        this.uploadQueue = Threading.concurrentQueue(uploadQueue);
    }

    public void setErrorQueue(Queue<Throwable> errorQueue) {
        this.errorQueue = Threading.concurrentQueue(errorQueue);
    }

    public Queue<Throwable> getErrorQueue() {
        return errorQueue;
    }

    public void setHost(String host) {
        this.host = "http://" + host + ":8080";
    }

    public String getHost() {
        return host;
    }

    public int getMinUploadCount() {
        return minUploadCount;
    }

    public void setMinUploadCount(int minUploadCount) {
        this.minUploadCount = minUploadCount;
    }
}

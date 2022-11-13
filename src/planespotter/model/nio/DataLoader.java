package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import planespotter.constants.Areas;
import planespotter.constants.Configuration;
import planespotter.controller.Controller;
import planespotter.dataclasses.*;
import planespotter.dataclasses.Frame;
import planespotter.display.TreasureMap;
import planespotter.model.ExceptionHandler;
import planespotter.model.Scheduler;
import planespotter.throwables.InvalidArrayException;
import planespotter.unused.ANSIColor;
import planespotter.util.Time;
import planespotter.util.Utilities;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static planespotter.util.Time.elapsedMillis;
import static planespotter.util.Time.nowMillis;

/**
 * @name LiveData
 * @author jml04
 * @version 1.0
 *
 * @description
 * abstract class LiveData represents a Live-Data-Manager,
 * it is able to load live data directly from Fr24 into Flight Objects,
 * it contains a queue 'dataQueue' where all the frames are added to,
 * these frames get collected from there by another class.
 * @see planespotter.model.io.DBIn
 * @see planespotter.model.nio.Fr24Supplier
 * @see planespotter.model.nio.Fr24Deserializer
 * @see planespotter.constants.Areas
 * @see java.util.concurrent.ConcurrentLinkedQueue
 */
public class DataLoader {

    private static final Object LIVE_THREAD_LOCK = new Object();

    public static final int NO_MASK   = 0,
                            ADSB_MASK = 1,
                            FR24_MASK = 2,
                            MIXED_MASK = ADSB_MASK | FR24_MASK,
                            ONLY_MILITARY_MASK =  4;

    // max. size for data queue
    private final int maxQueueSize;

    /*
     * boolean live represents a live-flag and
     * indicates if the live map is shown at the moment
     */
    private boolean live;

    // live data loading period in milliseconds
    private int liveDataPeriod, adsbDataPeriod;

    @Nullable private ADSBSupplier adsbSupplier;

    // frames, which will be inserted later (first loaded into the view)
    @NotNull private final Queue<Frame> dataQueue;

    /**
     * default {@link DataLoader} constructor, uses a maxQueueSize
     * of 20000 and a liveDataPeriod of 2 seconds
     */
    public DataLoader() {
        this(20000, 2);
    }

    /**
     * {@link DataLoader} constructor with specific maxQueueSize and period
     *
     * @param maxQueueSize is the max. size of the data-{@link Queue}
     * @param liveDataPeriodSec is the loading period in seconds
     */
    public DataLoader(int maxQueueSize, int liveDataPeriodSec) {
        this.maxQueueSize = maxQueueSize;
        this.dataQueue = new ConcurrentLinkedQueue<>();
        this.liveDataPeriod = liveDataPeriodSec * 1000;
        this.adsbDataPeriod = 2000;
    }

    public void run(@NotNull Controller ctrl) {
        long waitTime;

        while (true) {
            waitTime = spin(ctrl);
            if (waitTime <= 0) {
                continue;
            }
            try {
                synchronized (LIVE_THREAD_LOCK) {
                    LIVE_THREAD_LOCK.wait(waitTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private long spin(@NotNull Controller ctrl) {
        int updateMillis = Math.min(liveDataPeriod, adsbDataPeriod);
        if (!isLive()) {
            return updateMillis;
        }

        long startTime = nowMillis();
        String[] area = Areas.getCurrentArea(ctrl.getUI().getMap());

        // TODO: 05.10.2022 do always when connecting to ADSBSupplier
        ReceiverFrame crdata = ctrl.isAdsbEnabled() ? getReceiverFrame() : null;

        // updating live data list and map via Controller / MapManager
        Vector<Flight> liveData = liveDataSpin(area, ctrl.getMask());
        if (liveData != null) {
            ctrl.setLiveDataList(liveData);
            ctrl.updateMap(crdata);
        }

        // calculating wait time
        return updateMillis - elapsedMillis(startTime);
    }

    private Vector<Flight> liveDataSpin(@NotNull String @NotNull[] area, int mask) {
        boolean onlyMilitary = (mask & ONLY_MILITARY_MASK) == ONLY_MILITARY_MASK;
        return switch (mask & MIXED_MASK) {
            case MIXED_MASK -> getLiveFlights(area, onlyMilitary, true, true);
            case FR24_MASK -> getLiveFlights(area, onlyMilitary, false, true);
            case ADSB_MASK -> getLiveFlights(area, onlyMilitary, true, false);
            default -> null;
        };
    }

    @Nullable
    private ReceiverFrame getReceiverFrame() {
        if (adsbSupplier == null) {
            return null;
        }
        ReceiverFrame crdata;
        if ((crdata = adsbSupplier.getReceiverData()) != null) {
            adsbDataPeriod = crdata.getRefresh();
        } else {
            // prototype receiver frame
            Position pos = new Position(51.664064, 9.373964);
            crdata = new ReceiverFrame("1.0", 2000, 100, pos);
        }
        return crdata;
    }

    /**
     * loads live-data directly from Fr24 by running suppliers
     * and turns them directly into Flight objects.
     * This method doesn't load the data into the DB, but adds it
     * to the data-queue where the data can be added from,
     * but we poll it directly after putting it into
     * and parse it to {@link Flight}s
     *
     * @param currentArea is a {@link String} array (length always 1 here) with the current area string
     * @param onlyMilitary indicates if only military data should be loaded
     * @return Vector of {@link Flight} objects, loaded directly with {@link Supplier}s
     * @see planespotter.constants.Areas
     */
    @Nullable
    public Vector<Flight> getLiveFlights(@NotNull final String@NotNull[] currentArea, boolean onlyMilitary, boolean useAdsb, boolean useFr24) {

        Fr24Deserializer deserializer = new Fr24Deserializer();
        if (onlyMilitary) {
            deserializer.setFilter("NATO", "LAGR", "FORTE", "DUKE", "MULE", "NCR", "JAKE", "BART", "RCH", "MMF", "VIVI", "CASA", "K35R", "Q4", "REDEYE", "UAV");
        }

        if (useAdsb) {
            if (adsbSupplier == null) {
                Controller ctrl = Controller.getInstance();
                Configuration config = ctrl.getConfig();
                adsbSupplier = new ADSBSupplier(config.getProperty("adsbRequestUri").toString(), this, config.getProperty("receiverRequestUri").toString());
                adsbSupplier.setExceptionHandler(ctrl);
            }
            adsbSupplier.supply();
        }
        if (useFr24) {
            collectData(currentArea, deserializer, false, false);
        }

        AtomicInteger pseudoID = new AtomicInteger(0);
        Stream<? extends Frame> frames = pollFrames(Integer.MAX_VALUE);
        return frames == null ? null : frames
                .map(frame -> Flight.parseFlight(frame, pseudoID.getAndIncrement()))
                .collect(Collectors.toCollection(Vector::new));
    }

    /**
     * polls a certain amount of frames from the
     * insert-later-deque if it is not empty
     *
     * @param maxElements is the max. pull count, if it is higher than the
     *                    queue size, it's decremented to the queue size
     * @return Stream of Frames with @param maxElements (or queue size) as length
     */
    @Nullable
    public Stream<? extends Frame> pollFrames(@Range(from = 1, to = Integer.MAX_VALUE) int maxElements) {

        if (this.isEmpty()) { // checking for empty queue
            return null;
        }
        // iterating over polled frames from data-queue and limiting to size
        int size = Math.min(maxElements, getQueueSize());
        return Stream.iterate(dataQueue.poll(), x -> dataQueue.poll())
                .limit(size);
    }

    /**
     * gets HttpResponse's for specific areas and deserializes its data to Frames,
     * then directly adds the frames to the data-{@link Queue}
     *
     * @param areas are the Areas where data should be deserialized from
     * @param deserializer is the {@link Fr24Deserializer} which is used to deserialize the requested data
     * @param ignoreMaxSize if it's true, allowed max size of data-queue is ignored
     */
    public synchronized boolean collectData(@NotNull String[] areas, @NotNull final Fr24Deserializer deserializer, boolean ignoreMaxSize, boolean parallel) {
        AtomicInteger tNumber = new AtomicInteger(0);
        Stream<String> areaStream = Arrays.stream(areas);
        if (parallel) {
            areaStream = areaStream.parallel();
        }
        areaStream.forEach(area -> {
            if (!ignoreMaxSize && maxSizeReached()) {
                System.out.println("Max queue-size reached!");
                return;
            }
            Fr24Supplier supplier = new Fr24Supplier(tNumber.getAndIncrement(), area);
            supplier.setExceptionHandler(Controller.getInstance());

            try {
                HttpResponse<String> response = supplier.sendRequest(2);
                Utilities.checkStatusCode(response.statusCode());
                Stream<Fr24Frame> data = deserializer.deserialize(response);
                insertLater(data);
            } catch (IOException | InterruptedException e) {
                ExceptionHandler onError = supplier.getExceptionHandler();
                if (onError != null && e instanceof SSLHandshakeException ssl) {
                    onError.handleException(ssl);
                } else {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    /**
     * adds a Collection of Frames to the data-queue,
     * from where the frames are inserted into DB later
     *
     * @param data is the data to add to the data-{@link Queue}
     */
    public void insertLater(@NotNull final Collection<Frame> data) {
        dataQueue.addAll(data);
    }

    /**
     * adds a {@link Stream} of {@link Fr24Frame}s to the data-queue
     *
     * @param data is the data to add to the data-{@link Queue}
     */
    public void insertLater(@NotNull final Stream<? extends Frame> data) {
        data.forEach(dataQueue::add);
    }

    /**
     * indicates if a method may load frames into the data-deque
     * by checking if the max. Size (MAX_QUEUE_SIZE) is reached.
     *
     * @return true if the data-size is greater than MAX_QUEUE_SIZE, else false
     */
    protected boolean maxSizeReached() {
        return dataQueue.size() >= getMaxQueueSize();
    }

    /**
     * indicates if the data-deque is empty
     *
     * @return true if the insert-later-deque is empty, else false
     */
    public boolean isEmpty() {
        return dataQueue.isEmpty();
    }

    /**
     * getter for the live-flag
     *
     * @return true if the live map is shown at the moment, else false
     */
    public boolean isLive() {
        return this.live;
    }

    /**
     * sets the live-flag, should only be used if
     * the map goes live or when it disposes
     *
     * @param b is the boolean to set
     */
    public void setLive(boolean b) {
        this.live = b;
    }

    /**
     * sets the period for the live-data loader in milliseconds
     *
     * @param millis is the period in milliseconds
     */
    public void setLiveDataPeriod(@Range(from = 1, to = 10) int millis) {
        this.liveDataPeriod = millis;
    }

    /**
     * sets the {@link ADSBSupplier} request period in milliseconds
     *
     * @param millis is the request period in milliseconds
     */
    public void setAdsbDataPeriodSec(int millis) {
        this.adsbDataPeriod = millis;
    }

    /**
     * getter for the maximum size of the data-queue
     *
     * @return maximum queue size of data-queue
     */
    public int getMaxQueueSize() {
        return this.maxQueueSize;
    }

    /**
     * getter for the current data-queue size
     *
     * @return current size of the data-queue
     */
    public int getQueueSize() {
        return this.dataQueue.size();
    }

}

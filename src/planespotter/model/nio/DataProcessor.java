package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import planespotter.constants.props.Configuration;
import planespotter.controller.Controller;
import planespotter.dataclasses.*;
import planespotter.model.nio.client.DataUploader;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
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
 * @see Fr24Supplier
 * @see Fr24Deserializer
 * @see planespotter.constants.Areas
 * @see ConcurrentLinkedQueue
 */
public class DataProcessor {

    private static final Object LIVE_THREAD_LOCK = new Object();

    public static final int NO_MASK   = 0x0,
                            ADSB_MASK = 0x1,
                            FR24_MASK = 0x2,
                            MIXED_MASK = ADSB_MASK | FR24_MASK,
                            ONLY_MILITARY_MASK =  0x4,
                            UPLOAD_MASK = 0x8,
                            LOCAL_WRITE_MASK = 0x10;

    private static final String[] MILITARY_FILTERS = new String[] {
            "NATO", "LAGR", "FORTE", "DUKE", "MULE",
            "NCR", "JAKE", "BART", "RCH", "MMF", "VIVI",
            "CASA", "K35R", "Q4", "REDEYE", "UAV"
    };

    // max. size for data queue
    private final int maxQueueSize;

    /*
     * boolean live represents a live-flag and
     * indicates if the live map is shown at the moment
     */
    private boolean live;

    // live data loading period in milliseconds
    private int liveDataPeriod, adsbDataPeriod;

    private ADSBSupplier adsbSupplier;

    // frames, which will be inserted later (first loaded into the view)
    private final Queue<Frame> dataQueue;

    private final ADSBDeserializer adsbDeserializer;
    private final Fr24Deserializer fr24Deserializer;



    /**
     * default {@link DataProcessor} constructor, uses a maxQueueSize
     * of 20000 and a liveDataPeriod of 2 seconds
     */
    public DataProcessor() {
        this(20000, 2);
    }

    /**
     * {@link DataProcessor} constructor with specific maxQueueSize and period
     *
     * @param maxQueueSize is the max. size of the data-{@link Queue}
     * @param liveDataPeriodSec is the loading period in seconds
     */
    public DataProcessor(int maxQueueSize, int liveDataPeriodSec) {
        this.maxQueueSize = maxQueueSize;
        this.dataQueue = new ConcurrentLinkedQueue<>();
        this.liveDataPeriod = liveDataPeriodSec * 1000;
        this.adsbDataPeriod = 2000;
        this.adsbDeserializer = new ADSBDeserializer();
        this.fr24Deserializer = new Fr24Deserializer();
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
            } catch (InterruptedException ignored) {
            }

        }

    }

    private long spin(@NotNull Controller ctrl) {
        int updateMillis = Math.min(liveDataPeriod, adsbDataPeriod);
        if (!isLive()) {
            return updateMillis;
        }

        long startTime = nowMillis();
        Area area = ctrl.isFr24Enabled() ? Area.currentArea(ctrl.getUI().getMap()) : null;

        // TODO: 05.10.2022 do always when connecting to ADSBSupplier
        ReceiverFrame crdata = ctrl.isAdsbEnabled() ? getReceiverFrame() : null;

        // updating live data list and map via Controller / MapManager
        Vector<Flight> liveData = getLiveFlights(area, ctrl.getDataMask(), ctrl.getRestUploader());
        ctrl.setLiveDataList(liveData);
        ctrl.updateMap(crdata);

        // calculating wait time
        return updateMillis - elapsedMillis(startTime);
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
     * and turns it directly into Flight objects.
     * This method doesn't load the data into the DB, but adds it
     * to the data-queue where the data can be added from,
     * but we poll it directly after putting it into
     * and parse it to {@link Flight}s
     *
     * @param area is a {@link String} array (length always 1 here) with the current area string
     * @param mask is the data mask that indicates which type of data should be loaded
     * @param uploader is the {@link DataUploader} which uploads the data to a data server
     * @return Vector of {@link Flight} objects, loaded directly with {@link Supplier}s
     * @see Area
     */
    @NotNull
    public Vector<Flight> getLiveFlights(final Area area, final int mask, DataUploader<Frame> uploader) {

        if ((mask & ONLY_MILITARY_MASK) == ONLY_MILITARY_MASK) {
            fr24Deserializer.setFilter(MILITARY_FILTERS);
        }
        collectData(area, mask);

        AtomicInteger pseudoID = new AtomicInteger(0);
        Stream<? extends Frame> frames = pollFrames(Integer.MAX_VALUE);
        boolean framesAvailable = frames != null;
        boolean streamClosed = false;
        List<Frame> frameList = null;

        if (    framesAvailable && uploader != null && uploader.isRunning()
            && (mask & UPLOAD_MASK) == UPLOAD_MASK) {

            frameList = frames.collect(Collectors.toList());
            streamClosed = true;
            uploader.addData(frameList);
        }

        // frameList is initialized or not used at this point
        return !framesAvailable ? new Vector<>() : (streamClosed ? frameList.stream() : frames)
                .map(frame -> Flight.parseFlight(frame, pseudoID.getAndIncrement()))
                .collect(Collectors.toCollection(Vector::new));
    }

    public boolean collectData(Area area, int mask) {
        if (mask == NO_MASK) {
            return false;
        }
        if ((mask & ADSB_MASK) == ADSB_MASK) {
            collectADSB(adsbDeserializer);
        }
        if (area != null && (mask & FR24_MASK) == FR24_MASK) {
            return collectFr24(fr24Deserializer, false, area);
        }
        return true;
    }

    private void collectADSB(@NotNull ADSBDeserializer deserializer) {
        if (adsbSupplier == null) {
            Controller ctrl = Controller.getInstance();
            Configuration config = ctrl.getConfig();
            adsbSupplier = new ADSBSupplier(config.getProperty("adsbRequestUri").toString(), this, config.getProperty("receiverRequestUri").toString(), deserializer);
            adsbSupplier.setExceptionHandler(ctrl);
        }
        adsbSupplier.supply();
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
     * @param ignoreMaxSize if it's true, allowed max size of data-queue is ignored
     * @param areas are the Areas where data should be deserialized from
     */
    private synchronized boolean collectFr24(@NotNull Fr24Deserializer deserializer, boolean ignoreMaxSize, @NotNull Area @NotNull ... areas) {
        for (Area area : areas) {
            if (!ignoreMaxSize && maxSizeReached()) {
                System.out.println("Max queue-size reached!");
                return false;
            }
            Fr24Supplier supplier = new Fr24Supplier(area, this, deserializer);
            supplier.setExceptionHandler(Controller.getInstance());
            supplier.supply();
        }
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

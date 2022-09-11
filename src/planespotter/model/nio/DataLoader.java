package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.constants.Areas;
import planespotter.constants.Configuration;
import planespotter.controller.Controller;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.Fr24Frame;
import planespotter.dataclasses.Frame;
import planespotter.dataclasses.PlaneMarker;
import planespotter.display.TreasureMap;
import planespotter.model.ExceptionHandler;
import planespotter.model.Scheduler;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.InvalidArrayException;
import planespotter.throwables.NoAccessException;
import planespotter.util.Time;
import planespotter.util.Utilities;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    // max. size for data queue
    private final int maxQueueSize;

    /*
     * boolean live represents a live-flag and
     * indicates if the live map is shown at the moment
     */
    private boolean live;

    // live data loading period
    private int liveDataPeriodSec, adsbDataPeriodSec;

    @Nullable private ADSBSupplier adsbSupplier;

    // frames, which will be inserted later (first loaded into the view)
    @NotNull private final ConcurrentLinkedQueue<Frame> dataQueue;

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
     * @param maxQueueSize is the max. size for the data-{@link Queue}
     * @param liveDataPeriodSec is the loading period in seconds
     */
    public DataLoader(int maxQueueSize, int liveDataPeriodSec) {
        this.maxQueueSize = maxQueueSize;
        this.dataQueue = new ConcurrentLinkedQueue<>();
        this.liveDataPeriodSec = liveDataPeriodSec;
        this.adsbDataPeriodSec = 1;
    }

    /**
     * The live-data-task, endless data loading, while the controller is alive.
     * Waits on the liveLock object and tries to load live-data.
     *
     * @param ctrl is the {@link Controller} instance
     * @param flags are the flags where the task is initialized with, in this case the
     *              'onlyMilitary' flag, indicates if only military data should be loaded
     */
    public void runTask(@NotNull Controller ctrl, boolean... flags) {
        if (flags == null || flags.length == 0) {
            throw new InvalidArrayException("Flags array needs the onlyMilitary flag!");
        }
        boolean onlyMilitary = flags[0];
        Scheduler scheduler = new Scheduler(3, 4L);
        Queue<Flight> mixedFrames = new ConcurrentLinkedQueue<>();
        Queue<Flight> adsbFrames = new ConcurrentLinkedQueue<>();
        boolean mixed = ctrl.isAdsbEnabled() && ctrl.isFr24Enabled();

        scheduler.schedule(() -> {
            if (ctrl.isAdsbEnabled() && isLive()) {
                try {
                    if (mixed) {
                        mixedFrames.addAll(loadLiveData(ctrl, onlyMilitary, ctrl.isAdsbEnabled(), false));
                    } else {
                        adsbFrames.addAll(loadLiveData(ctrl, onlyMilitary, ctrl.isAdsbEnabled(), false));
                    }
                } catch (DataNotFoundException | NoAccessException ignored) {
                }
            }
        }, 0, liveDataPeriodSec);
        scheduler.schedule(() -> {
            if (ctrl.isFr24Enabled() && isLive()) {
                try {
                    mixedFrames.addAll(loadLiveData(ctrl, onlyMilitary, false, ctrl.isFr24Enabled()));
                } catch (DataNotFoundException | NoAccessException ignored) {
                }
            }
        }, 0, adsbDataPeriodSec);

        int updatePeriod = Math.min(liveDataPeriodSec, adsbDataPeriodSec);
        scheduler.schedule(() -> scheduler.exec(() -> {
                    if (mixedFrames.isEmpty() && adsbFrames.isEmpty()) {
                        return;
                    }
                    List<MapMarker> markerList;
                    ctrl.setLiveDataList(
                            Stream.iterate(mixed || ctrl.isFr24Enabled() ? mixedFrames.poll() : adsbFrames.poll(),
                                            f -> mixed || ctrl.isFr24Enabled() ? mixedFrames.poll() : adsbFrames.poll())
                                    .limit(mixed || ctrl.isFr24Enabled() ? mixedFrames.size() : adsbFrames.size())
                                    .collect(Collectors.toCollection(Vector::new)));
                    markerList = ctrl.getLiveDataList()
                            .stream()
                            .map(flight -> PlaneMarker.fromFlight(flight, ctrl.getUI().getMapManager().getSelectedICAO(), true))
                            .collect(Collectors.toList());
                    // setting new map marker list on the map
                    ctrl.getUI().getMap().setMapMarkerList(markerList);
                    ctrl.done(false);
                }, "Set Map Markers", true, Scheduler.HIGH_PRIO, false)
                .orTimeout(updatePeriod, TimeUnit.SECONDS)
                .exceptionally(e -> null)
                .join(), 0, updatePeriod);


        /*// loading init-live-data
        loadLiveData(ctrl, onlyMilitary, ctrl.isAdsbEnabled(), ctrl.isFr24Enabled());
        // endless live-data task
        synchronized (liveLock) {
            while (!ctrl.isTerminated()) {
                // trying to await the live-data period
                try {
                    liveLock.wait(TimeUnit.SECONDS.toMillis(this.liveDataPeriodSec));
                } catch (InterruptedException e) {
                    ctrl.handleException(e);
                } finally {
                    liveLock.notify();
                }
                // loading live-data, if live-map is enabled
                if (isLive()) {
                    loadLiveData(ctrl, onlyMilitary, ctrl.isAdsbEnabled(), ctrl.isFr24Enabled());

                }
            }
        }*/
    }

    /**
     * loads Fr24-data directly into the liveData-Collection (ctrl.liveData)
     * and into the {@link TreasureMap}, but not into the database.
     *
     * @param ctrl is the {@link Controller} instance
     * @param onlyMilitary indicates if only military data should be loaded
     */
    private Vector<Flight> loadLiveData(@NotNull Controller ctrl, boolean onlyMilitary, boolean useAdsb, boolean useFr24)
            throws DataNotFoundException, NoAccessException {

        TreasureMap map;
        // checking for 'already loading' and setting controller loading
        if (!ctrl.isLoading()) {
            ctrl.setLoading(true);

            map = ctrl.getUI().getMap();
            // transforming liveData-flight-Vector into list of MapMarkers
            // after loading it directly from fr24
            try {
                return getLiveFlights(map, onlyMilitary, useAdsb, useFr24);
            } finally {
                ctrl.done(false);
            }
        }
        throw new NoAccessException("cannot access the Controller right now!");
    }

    /**
     * loads live-data directly from Fr24 by running suppliers
     * and turns them directly into Flight objects.
     * This method doesn't load the data into the DB, but adds it
     * to the data-queue where the data can be added from,
     * but we poll it directly after putting it into
     * and parse it to {@link Flight}s
     *
     * @param map is the {@link TreasureMap} where the area, in which the {@link Flight}s are loaded,
     *            comes from, can be found in the {@link planespotter.display.UserInterface} class
     * @param onlyMilitary indicates if only military data should be loaded
     * @return Vector of {@link Flight} objects, loaded directly with {@link Supplier}s
     */
    @NotNull
    public Vector<Flight> getLiveFlights(@NotNull final TreasureMap map, boolean onlyMilitary, boolean useAdsb, boolean useFr24)
            throws DataNotFoundException {

        Fr24Deserializer deserializer = new Fr24Deserializer();
        if (onlyMilitary) {
            deserializer.setFilter("NATO", "LAGR", "FORTE", "DUKE", "MULE", "NCR", "JAKE", "BART", "RCH", "MMF", "VIVI", "CASA", "K35R", "Q4");
        }

        String[] currentArea = Areas.getCurrentArea(map);
        if (useAdsb) {
            if (adsbSupplier == null) {
                Controller ctrl = Controller.getInstance();
                Configuration config = ctrl.getConfig();
                adsbSupplier = new ADSBSupplier(config.getProperty("adsbRequestUri").toString(), this);
                adsbSupplier.setExceptionHandler(ctrl);
            }
            adsbSupplier.supply();
        }
        if (useFr24) {
            collectData(currentArea, deserializer, false);
        }

        AtomicInteger pseudoID = new AtomicInteger(0);
        return pollFrames(Integer.MAX_VALUE)
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
    @NotNull
    public Stream<? extends Frame> pollFrames(@Range(from = 1, to = Integer.MAX_VALUE) int maxElements)
            throws DataNotFoundException {

        if (this.isEmpty()) { // checking for empty queue
            throw new DataNotFoundException("Data-Queue is empty, make sure it is not empty!");
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
    public synchronized boolean collectData(@NotNull String[] areas, @NotNull final Fr24Deserializer deserializer, boolean ignoreMaxSize) {
        System.out.println("[Supplier] Collecting Fr24-Data with parallel Stream...");

        AtomicInteger tNumber = new AtomicInteger(0);
        long startTime = Time.nowMillis();
        try (Stream<String> parallel = Arrays.stream(areas).parallel()) {
            parallel.forEach(area -> {
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
        }
        System.out.println("[Supplier] Elapsed time: " + Time.elapsedMillis(startTime) + " ms");
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
     * sets the period for the live-data loader in seconds
     *
     * @param sec is the period in seconds
     */
    public void setLiveDataPeriod(@Range(from = 1, to = 10) int sec) {
        this.liveDataPeriodSec = sec;
    }

    /**
     * sets the {@link ADSBSupplier} request period in seconds
     *
     * @param adsbDataPeriodSec is the request period in seconds
     */
    public void setAdsbDataPeriodSec(int adsbDataPeriodSec) {
        this.adsbDataPeriodSec = adsbDataPeriodSec;
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

package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.constants.Areas;
import planespotter.controller.Controller;
import planespotter.dataclasses.*;
import planespotter.display.TreasureMap;
import planespotter.model.DataTask;
import planespotter.model.Scheduler;
import planespotter.throwables.InvalidArrayException;
import planespotter.throwables.InvalidDataException;
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
 * it contains a queue 'insertLater' where all the frames are added to,
 * these frames get collected from there by another class.
 * @see planespotter.model.io.DBIn
 * @see planespotter.model.nio.Fr24Supplier
 * @see planespotter.model.nio.Fr24Deserializer
 * @see planespotter.constants.Areas
 * @see java.util.concurrent.ConcurrentLinkedQueue
 */
public class LiveLoader implements DataTask {

    private static final Object liveLock = new Object();

    // max. size for insertLater queue
    private final int maxQueueSize;

    /*
     * boolean live represents a live-flag and
     * indicates if the live map is shown at the moment
     */
    private boolean live;

    // live data loading period
    private int liveDataPeriodSec;

    /**
     * frames, which will be inserted later (first loaded into the view)
     */
    @NotNull private final ConcurrentLinkedQueue<Fr24Frame> insertLater;

    /**
     * default {@link LiveLoader} constructor, uses a maxQueueSize
     * of 20000 and a liveDataPeriod of 2 seconds
     */
    public LiveLoader() {
        this(20000, 2);
    }

    /**
     * {@link LiveLoader} constructor with specific maxQueueSize and period
     *
     * @param maxQueueSize is the max. size for the insertLater-{@link Queue}
     * @param liveDataPeriodSec is the loading period in seconds
     */
    public LiveLoader(int maxQueueSize, int liveDataPeriodSec) {
        this.maxQueueSize = maxQueueSize;
        this.insertLater = new ConcurrentLinkedQueue<>();
        this.liveDataPeriodSec = liveDataPeriodSec;
    }

    /**
     * The live-data-task, endless data loading, while the controller is alive.
     * Waits on the liveLock object and tries to load live-data.
     *
     * @param ctrl is the {@link Controller} instance
     * @param flags are the flags where the task is initialized with, in this case the
     *              'onlyMilitary' flag, indicates if only military data should be loaded
     */
    @Override
    public void runTask(@NotNull Controller ctrl, boolean... flags) {
        if (flags == null || flags.length == 0) {
            throw new InvalidArrayException("Flags array needs the onlyMilitary flag!");
        }
        boolean onlyMilitary = flags[0];
        // loading init-live-data
        loadLiveData(ctrl, onlyMilitary);
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
                    loadLiveData(ctrl, onlyMilitary);
                }
            }
        }
    }

    /**
     * loads Fr24-data directly into the liveData-Collection (ctrl.liveData)
     * and into the {@link TreasureMap}, but not into the database.
     *
     * @param ctrl is the {@link Controller} instance
     * @param onlyMilitary indicates if only military data should be loaded
     */
    private void loadLiveData(@NotNull Controller ctrl, boolean onlyMilitary) {

        ctrl.getScheduler().exec(() -> {
                    List<MapMarker> markerList;
                    TreasureMap map;
                    // checking for 'already loading' and setting controller loading
                    if (!ctrl.isLoading()) {
                        ctrl.setLoading(true);

                        map = ctrl.getUI().getMap();
                        // transforming liveData-flight-Vector into list of MapMarkers
                        // after loading it directly from fr24
                        Vector<Flight> liveFlights = getLiveFlights(map, onlyMilitary);
                        ctrl.setLiveDataList(liveFlights);
                        markerList = ctrl.getLiveDataList()
                                .stream()
                                .map(flight -> PlaneMarker.fromFlight(flight, ctrl.getUI().getMapManager().getSelectedICAO(), true))
                                .collect(Collectors.toList());
                        // setting new map marker list on the map
                        map.setMapMarkerList(markerList);
                        ctrl.done(false);
                    }
                }, "LiveData Loader", true, Scheduler.HIGH_PRIO, true)
                .join();
    }

    /**
     * loads live-data directly from Fr24 by running suppliers
     * and turns them directly into Flight objects.
     * This method doesn't load the data into the DB, but adds it
     * to the insertLater-queue where the data can be added from,
     * but we poll it directly after putting it into
     * and parse it to {@link Flight}s
     *
     * @param map is the {@link TreasureMap} where the area, in which the {@link Flight}s are loaded,
     *            comes from, can be found in the {@link planespotter.display.UserInterface} class
     * @param onlyMilitary indicates if only military data should be loaded
     * @return Vector of {@link Flight} objects, loaded directly with {@link Supplier}s
     */
    @NotNull
    public Vector<Flight> getLiveFlights(@NotNull final TreasureMap map, boolean onlyMilitary) {
        Fr24Deserializer deserializer = new Fr24Deserializer();
        if (onlyMilitary) {
            deserializer.setFilter("NATO", "LAGR", "FORTE", "DUKE", "MULE", "NCR", "JAKE", "BART", "RCH", "MMF", "VIVI", "CASA", "K35R", "Q4");
        }

        String[] currentArea = Areas.getCurrentArea(map);
        if (collectData(currentArea, deserializer, false)) {

            AtomicInteger pseudoID = new AtomicInteger(0);
            return pollFrames(Integer.MAX_VALUE)
                    .map(frame -> Flight.parseFlight(frame, pseudoID.getAndIncrement()))
                    .collect(Collectors.toCollection(Vector::new));
        }
        throw new InvalidDataException("Couldn't load Live-Data!");
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
    public Stream<Fr24Frame> pollFrames(@Range(from = 1, to = Integer.MAX_VALUE) int maxElements) {
        if (this.isEmpty()) { // checking for empty queue
            throw new InvalidDataException("Insert-later-Queue is empty, make sure it is not empty!");
        }
        // iterating over polled frames from insertLater-queue and limiting to size
        int size = Math.min(maxElements, getQueueSize());
        return Stream.iterate(insertLater.poll(), x -> insertLater.poll())
                .limit(size);
    }

    /**
     * gets HttpResponse's for specific areas and deserializes its data to Frames,
     * then directly adds the frames to the insertLater-{@link Queue}
     *
     * @param areas are the Areas where data should be deserialized from
     * @param deserializer is the {@link Fr24Deserializer} which is used to deserialize the requested data
     * @param ignoreMaxSize if it's true, allowed max size of insertLater-queue is ignored
     */
    public synchronized boolean collectData(@NotNull String[] areas, @NotNull final Fr24Deserializer deserializer, boolean ignoreMaxSize) {
        System.out.println("[Supplier] Collecting Fr24-Data with parallel Stream...");

        AtomicInteger tNumber = new AtomicInteger(0);
        long startTime = Time.nowMillis();
        try (Stream<@NotNull String> parallel = Arrays.stream(areas).parallel()) {
            parallel.forEach(area -> {
                if (!ignoreMaxSize && maxSizeReached()) {
                    System.out.println("Max queue-size reached!");
                    return;
                }
                Fr24Supplier supplier = new Fr24Supplier(tNumber.getAndIncrement(), area);

                Deque<Fr24Frame> data;
                try {
                    HttpResponse<String> response = supplier.sendRequest();
                    Utilities.checkStatusCode(response.statusCode());
                    data = deserializer.deserialize(response);
                    insertLater(data);
                } catch (IOException | InterruptedException e) {
                    if (e instanceof SSLHandshakeException ssl) {
                        Controller.getInstance().handleException(ssl);
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
     * adds a Collection of Frames to the insertLater-queue,
     * from where the frames are inserted into DB later
     *
     * @param data is the data to add to insert later
     */
    public void insertLater(@NotNull final Collection<Fr24Frame> data) {
        insertLater.addAll(data);
    }

    /**
     * indicates if a method may load frames into the insertLater-deque
     * by checking if the max. Size (MAX_QUEUE_SIZE) is reached.
     *
     * @return true if the insertLater-size is greater than MAX_QUEUE_SIZE, else false
     */
    protected boolean maxSizeReached() {
        return insertLater.size() >= getMaxQueueSize();
    }

    /**
     * indicates if the insertLater-deque is empty
     *
     * @return true if the insert-later-deque is empty, else false
     */
    public boolean isEmpty() {
        return insertLater.isEmpty();
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
     * getter for the maximum size of the insertLater-queue
     *
     * @return maximum queue size of insertLater-queue
     */
    public int getMaxQueueSize() {
        return this.maxQueueSize;
    }

    /**
     * getter for the current insertLater-queue size
     *
     * @return current size of the insertLater-queue
     */
    public int getQueueSize() {
        return this.insertLater.size();
    }
}

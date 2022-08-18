package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.constants.Areas;
import planespotter.controller.Controller;
import planespotter.dataclasses.*;
import planespotter.display.TreasureMap;
import planespotter.model.io.DBIn;
import planespotter.throwables.Fr24Exception;
import planespotter.throwables.InvalidDataException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static planespotter.constants.DefaultColor.DEFAULT_MAP_ICON_COLOR;

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
 * @see DBIn
 * @see Fr24Supplier
 * @see Fr24Deserializer
 * @see Areas
 * @see ConcurrentLinkedDeque
 */
// TODO: 10.08.2022 not static anymore
public class LiveLoader {

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
    private final ConcurrentLinkedQueue<Fr24Frame> insertLater;

    /**
     *
     */
    public LiveLoader() {
        this(20000, 2);
    }

    /**
     *
     *
     * @param maxQueueSize
     */
    public LiveLoader(int maxQueueSize, int liveDataPeriodSec) {
        this.maxQueueSize = maxQueueSize;
        this.insertLater = new ConcurrentLinkedQueue<>();
        this.liveDataPeriodSec = liveDataPeriodSec;
    }

    public synchronized void liveDataTask(@NotNull Controller ctrl) {
        // loading init-live-data
        this.loadLiveData(ctrl);
        // endless live-data task
        while (!ctrl.isTerminated()) {
            // trying to await the live-data period
            try {
                this.wait(TimeUnit.SECONDS.toMillis(this.liveDataPeriodSec));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.notify();
            }
            // loading live-data, if live-map is enabled
            if (this.isLive()) {
                this.loadLiveData(ctrl);
            }
        }
    }

    /**
     * loads Fr24-data directly into the liveData-Collection,
     * but not into the DB
     * @see LiveLoader
     */
    private synchronized void loadLiveData(@NotNull Controller ctrl) {

        List<MapMarker> markerList;
        TreasureMap map;
        // checking for 'already loading' and setting controller loading
        if (!ctrl.isLoading()) {
            ctrl.setLoading(true);

            map = ctrl.getUI().getMap();
            // transforming liveData-flight-Vector into list of MapMarkers
            // after loading it directly from fr24
            ctrl.liveData = this.loadDirectly(map);
            markerList = ctrl.liveData
                    .stream()
                    .map(flight -> {
                        // transforming to MapMarker
                        final DataPoint dataPoint = flight.dataPoints().get(0);
                        return DefaultMapMarker.fromDataPoint(dataPoint, DEFAULT_MAP_ICON_COLOR.get());
                    })
                    .collect(Collectors.toList());
            // setting new map marker list on the map
            map.setMapMarkerList(markerList);
            ctrl.done(false);
        }
    }

    /**
     * loads live-data directly from Fr24 by running suppliers
     * and turns them directly into Flight objects.
     * This method doesn't load the data into the DB, but adds it
     * to the insertLater-queue where the data is added from
     *
     * @return Vector of Flight objects, loaded directly by a supplier
     */
    @NotNull
    public Vector<Flight> loadDirectly(@NotNull final TreasureMap map) {
        Fr24Deserializer deserializer = new Fr24Deserializer();
        //deserializer.setFilter("NATO", "LAGR", "FORTE", "DUKE", "MULE", "NCR", "JAKE", "BART", "RCH", "MMF");

        String[] currentArea = Areas.getCurrentArea(map);
        if (Fr24Supplier.collectFramesForArea(this, currentArea, deserializer, false)) {

            AtomicInteger flightID = new AtomicInteger(0);
            return this.pollFrames(Integer.MAX_VALUE)
                    .map(frame -> Flight.parseFlight(frame, flightID.getAndIncrement()))
                    .collect(Collectors.toCollection(Vector::new));
        }
        throw new InvalidDataException("Couldn't load Live-Data!");
    }

    /**
     * polls a certain amount of frames from the
     * insert-later-deque if it is not empty
     *
     * @param count is the pull count
     * @return Stream of Frames with @param count as length
     */
    @NotNull
    public Stream<Fr24Frame> pollFrames(@Range(from = 1, to = Integer.MAX_VALUE) int count) {
        if (isEmpty()) { // checking for empty queue
            throw new Fr24Exception("Insert-later-Queue is empty, make sure it is not empty!");
        } else if (!canPoll(count)) { // checking queue size
            count = this.insertLater.size();
        }
        // iterating over polled frames from insertLater-queue and limiting to count
        return Stream.iterate(this.insertLater.poll(), x -> this.insertLater.poll())
                .limit(count);
    }

    /**
     * adds a Collection of Frames to the insertLater-queue,
     * from where the frames are inserted into DB later
     *
     * @param data is the data to add to insert later
     */
    public void insertLater(@NotNull final Collection<Fr24Frame> data) {
        this.insertLater.addAll(data);
    }

    /**
     * indicates if a method may load frames into the insertLater-deque
     * by checking if the max. Size (MAX_QUEUE_SIZE) is reached.
     *
     * @return true if the insertLater-size is greater than 10000, else false
     */
    protected boolean maxSizeReached() {
        return this.insertLater.size() > this.maxQueueSize;
    }

    /**
     * indicates if a method may collect data from the insertLater-deque
     *
     * @param count is the exclusive minimum size, the deque must have to return true
     * @return true, if insertLater.size() is greater or equals count, else false
     *         if true, another Method gets ac
     */
    public boolean canPoll(final int count) {
        return this.insertLater.size() > count;
    }

    /**
     * indicates if the insertLater-deque is empty
     *
     * @return true if the insert-later-deque is empty, else false
     */
    public boolean isEmpty() {
        return this.insertLater.isEmpty();
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

    public int getMaxQueueSize() {
        return this.maxQueueSize;
    }

    public int getQueueSize() {
        return this.insertLater.size();
    }
}

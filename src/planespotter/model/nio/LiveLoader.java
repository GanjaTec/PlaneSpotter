package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import planespotter.constants.Areas;
import planespotter.controller.Controller;
import planespotter.model.Scheduler;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.Fr24Frame;
import planespotter.display.TreasureMap;
import planespotter.model.io.DBIn;
import planespotter.throwables.Fr24Exception;
import planespotter.throwables.InvalidDataException;
import planespotter.util.Logger;

import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
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
 * @see DBIn
 * @see Fr24Supplier
 * @see Fr24Deserializer
 * @see Areas
 * @see ConcurrentLinkedDeque
 */
// TODO: 10.08.2022 not static anymore
public abstract class LiveLoader {

    private static final int MAX_QUEUE_SIZE;
    /*
     * boolean live represents a live-flag and
     * indicates if the live map is shown at the moment
     */
    private static boolean live;
    /**
     * frames, which will be inserted later (first loaded into the view)
     */
    private static final ConcurrentLinkedQueue<Fr24Frame> insertLater;
    // static initializer
    static {
        MAX_QUEUE_SIZE = 20000;
        insertLater = new ConcurrentLinkedQueue<>();
    }

    /**
     * loads live-data directly from Fr24 by running suppliers
     * and turns them directly into Flight objects.
     * This method doesn't load the data into the DB, but adds it
     * to the insertLater-queue where the data is added from
     *
     * @param scheduler is the Scheduler which executes tasks
     * @return Vector of Flight objects, loaded directly by a supplier
     */
    @NotNull
    public static Vector<Flight> loadDirectly(@NotNull final Scheduler scheduler, @NotNull final TreasureMap map) {
        var deserializer = new Fr24Deserializer();
        //deserializer.setFilter("NATO", "LAGR", "FORTE", "DUKE", "MULE", "NCR", "JAKE", "BART", "RCH", "MMF");
        // area with panel size
        var topLeft = map.getPosition(0, 0);
        var bottomRight = map.getPosition(map.getWidth(), map.getHeight());
        var currentArea = new String[] {
                Areas.newArea(topLeft, bottomRight)
        };
        if (Fr24Supplier.collectFramesForArea(currentArea, deserializer, scheduler, false)) {
            // termorary if // daten gehen verloren
            AtomicInteger flightID = new AtomicInteger(0);
            return LiveLoader.pollFrames(Integer.MAX_VALUE)
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
    public static Stream<Fr24Frame> pollFrames(@Range(from = 1, to = Integer.MAX_VALUE) int count) {
        if (isEmpty()) { // checking for empty queue
            throw new Fr24Exception("Insert-later-Queue is empty, make sure it is not empty!");
        } else if (!canPoll(count)) { // checking queue size
            try {
                Logger log = Controller.getInstance().getLogger();
                log.infoLog("Insert-later-Queue has not that much content, limiting frames to size...", new LiveLoader() {});
            } catch (IllegalStateException ignored) {
            } finally {
                count = insertLater.size();
            }
        }
        // iterating over polled frames from insertLater-queue and limiting to count
        return Stream.iterate(insertLater.poll(), x -> insertLater.poll())
                .limit(count);
    }

    /**
     * adds a Collection of Frames to the insertLater-queue,
     * from where the frames are inserted into DB later
     *
     * @param data is the data to add to insert later
     */
    public static void insertLater(@NotNull final Collection<Fr24Frame> data) {
        insertLater.addAll(data);
    }

    /**
     * indicates if a method may load frames into the insertLater-deque
     * by checking if the max. Size (MAX_QUEUE_SIZE) is reached.
     *
     * @return true if the insertLater-size is greater than 10000, else false
     */
    protected static boolean maxSizeReached() {
        return insertLater.size() > MAX_QUEUE_SIZE;
    }

    /**
     * indicates if a method may collect data from the insertLater-deque
     *
     * @param count is the exclusive minimum size, the deque must have to return true
     * @return true, if insertLater.size() is greater or equals count, else false
     *         if true, another Method gets ac
     */
    public static boolean canPoll(final int count) {
        return insertLater.size() > count;
    }

    /**
     * indicates if the insertLater-deque is empty
     *
     * @return true if the insert-later-deque is empty, else false
     */
    public static boolean isEmpty() {
        return insertLater.isEmpty();
    }

    /**
     * getter for the live-flag
     *
     * @return true if the live map is shown at the moment, else false
     */
    public static boolean isLive() {
        return live;
    }

    /**
     * sets the live-flag, should only be used if
     * the map goes live or when it disposes
     *
     * @param b is the boolean to set
     */
    public static void setLive(boolean b) {
        live = b;
    }

}

package planespotter.model;

import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Supplier;

import java.util.Collection;
import java.util.Deque;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @name LiveData
 * @author jml04
 * @version 1.0
 *
 * abstract class LiveData represents a Live-Data-Manager,
 * it is able to load live data directly from Fr24 into Flight Objects,
 * it contains a queue 'insertLater' where all the frames are added to,
 * these frames get collected from there by another class.
 * @see DataLoader
 * @see Supplier
 * @see Fr24Deserializer
 * @see Areas
 * @see AreaFactory
 * @see ConcurrentLinkedDeque
 */
public abstract class LiveData {
    /**
     * boolean live represents a live-flag and
     * indicates if the live map is shown at the moment
     */
    private static boolean live;
    /**
     * frames, which will be inserted later (first loaded into the view)
     */
    private static final ConcurrentLinkedQueue<Fr24Frame> insertLater;
    // static initializer, initializes static variables
    static {
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
    public static Vector<Flight> directLiveData(final Scheduler scheduler) {
        var supplier = new Supplier();
        var deserializer = new Fr24Deserializer();
        var world = Areas.getWorldAreas();
        var testArea = new String[] { Areas.TEST };
        /*
        var gui = Controller.getGUI();
        var map = gui.getMap();
        var bottomLeft = map.getPosition(0, map.getHeight());
        var topRight = map.getPosition(map.getWidth(), 0);
        var testArea = new String[] {
                AreaFactory.createArea(Position.parsePosition(bottomLeft), Position.parsePosition(topRight))
        };
        */
        var frames = supplier.getFr24Frames(world, deserializer, scheduler);
        // termorary if // daten gehen verloren
        if (maxSizeReached()) {
            insertLater(frames);
        }
        var id = new AtomicInteger(0);
        return frames.stream()
                .map(frame -> Flight.parseFlight(frame, id.getAndIncrement()))
                .collect(Collectors.toCollection(Vector::new));
    }

    /**
     * polls a certain amount of frames from the
     * insert-later-deque if it is not empty
     *
     * @param count is the pull count
     * @return Deque of Frames with @param count es length
     */
    public static Deque<Fr24Frame> pollFrames(final int count) {
        if (isEmpty()) {
            throw new NullPointerException("Insert-later-Deque is empty, use isEmpty() first!");
        }
        var counter = new AtomicInteger();
        var frames = new ConcurrentLinkedDeque<Fr24Frame>();

        insertLater.parallelStream()
                .forEach(frame -> {
                    if (counter.get() < count) {
                        frames.add(insertLater.poll());
                        counter.getAndIncrement();
                    }
                });
        return frames;
    }

    /**
     * adds a Collection of Frames
     *
     * @param data is the data to add to insert later
     */
    public static void insertLater(final Collection<Fr24Frame> data) {
        insertLater.addAll(data);
    }

    /**
     * indicates if a method may load frames into the insertLater-deque
     * by checking if the max. Size (10000) is reached.
     *
     * @return true if the insertLater-size is greater than 10000, else false
     */
    protected static boolean maxSizeReached() {
        return insertLater.size() > 10000;
    }

    /**
     * indicates if a method may collect data from the insertLater-deque
     *
     * @param count is the exclusive minimum size, the deque must have to return true
     * @return true, if insertLater.size() is greater or equals 1000, else false
     *         if true, another Method gets ac
     */
    protected static boolean ableToCollect(final int count) {
        return insertLater.size() > count;
    }

    /**
     * indicates if the insertLater-deque is emoty
     *
     * @return true if the insert-later-deque is empty, else false
     */
    protected static boolean isEmpty() {
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

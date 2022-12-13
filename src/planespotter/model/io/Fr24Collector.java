package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import planespotter.controller.Controller;
import planespotter.dataclasses.Area;
import planespotter.dataclasses.Frame;
import planespotter.display.models.SupplierDisplay;
import planespotter.model.Collector;
import planespotter.model.Scheduler;
import planespotter.model.nio.DataLoader;
import planespotter.model.nio.FilterManager;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Fr24Supplier;
import planespotter.throwables.DataNotFoundException;
import planespotter.util.Utilities;
import planespotter.util.math.MathUtils;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @name Fr24Collector
 * @author jml04
 * @version 1.0
 *
 * @description
 * Class Fr24Collector is a Collector subclass for Fr24-Data
 * @see planespotter.model.Collector
 * @see planespotter.model.ADSBCollector
 * @see planespotter.model.nio.Fr24Supplier
 * @see planespotter.model.nio.Fr24Deserializer
 */
public final class Fr24Collector extends Collector<Fr24Supplier> {

    private static final Object REQ_LOCK;

    // inserted frames per write
    public static final int FRAMES_PER_WRITE;

    // insert period in milliseconds
    private static final int REQUEST_PERIOD;

    // static initializer
    static {
        REQ_LOCK = new Object();
        FRAMES_PER_WRITE = 800;
        REQUEST_PERIOD = 250;
    }

    private final FilterManager filterManager;

    private final DataLoader dataLoader;

    private final Inserter inserter;

    // raster of areas (whole world) in 1D-Array
    private final Area[] areas;
    private final Queue<Area> areaQueue;

    private final int dataMask;

    /**
     * Fr24-Collector Main-method
     *
     * @param args can be ignored
     */
    public static void main(String[] args) {
        // only military with a small grid size
        //int sLat = 2, sLon = 4;
        int sLat = 6, sLon = 12;
        Fr24Collector collector;
        try {
            collector = new Fr24Collector(true, true, sLat, sLon, 1, Inserter.INSERT_ALL);
            collector.start();
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fr24Collector constructor, creates a new Collector for Fr24-Data
     *
     * @param exitOnClose indicates if the program should exit when the 'X'-button is clicked
     */
    public Fr24Collector(boolean exitOnClose,
                         boolean withFilters,
                         @Range(from = 2, to = 180) int gridSizeLat,
                         @Range(from = 2, to = 360) int gridSizeLon,
                         @Range(from = 0, to = 2) int dataMask,
                         @Range(from = 1, to = 2) int insertMode) throws DataNotFoundException {
        super(exitOnClose, new Fr24Supplier(new DataLoader()));
        int closeOperation = (exitOnClose)
                ? WindowConstants.EXIT_ON_CLOSE
                : WindowConstants.DISPOSE_ON_CLOSE;
        super.display = new SupplierDisplay(closeOperation, onPause(), onStartStop());
        this.filterManager = withFilters
                ? (FilterManager) Controller.getInstance().getConfig().getProperty("collectorFilters")
                : null;
        this.dataLoader = super.supplier.getDataLoader();
        this.inserter = new Inserter(this.dataLoader, super.getErrorQueue(), insertMode);
        this.areas = Utilities.calculateInterestingAreas3(gridSizeLat, gridSizeLon, 0)
                              .toArray(Area[]::new);
        this.dataMask = dataMask;
        this.areaQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * starts the collecting task for the collector by
     * creating a new Deserializer and Keeper,
     * optional filters and collecting task in a new thread
     */
    @Override
    public void startCollecting() {
        Fr24Deserializer deserializer = new Fr24Deserializer();
        Keeper keeper = new KeeperOfTheArchives(1200L);
        if (this.filtersEnabled()) {
            deserializer.setFilter("NATO", "LAGR", "FORTE", "DUKE", "MULE", "NCR", "JAKE", "BART", "RCH", "MMF", "CASA", "VIVI");
        }
        super.startNewMainThread(() -> this.collect(keeper), "Fr24-Collector");
    }

    /**
     * collecting task for the collector
     */
    private synchronized void collect(@NotNull final Keeper keeper) {

        scheduler.schedule(() -> {
            synchronized (REQ_LOCK) {
                if (areaQueue.isEmpty()) {
                    areaQueue.addAll(Arrays.stream(areas).toList());
                }
                Area next = areaQueue.poll();
                if (!dataLoader.collectData(next, dataMask)) {
                    System.err.println("No data collected!");
                }

            }
        }, "Collector Thread", 0, REQUEST_PERIOD);

        scheduler.runThread(inserter, "Inserter Thread", true, Scheduler.MID_PRIO);
        // executing the keeper every 400 seconds
        scheduler.schedule(() -> scheduler.exec(keeper, "Keeper Thread", true, Scheduler.LOW_PRIO, false),
                100 * 1000, 400 * 1000);
        // updating display
        DBIn dbIn = DBIn.getDBIn();
        scheduler.schedule(() -> {
            insertedNow.set(dbIn.getFrameCount() - insertedFrames.get());
            newPlanesNow.set(dbIn.getPlaneCount() - newPlanesAll.get());
            newFlightsNow.set(dbIn.getFlightCount() - newFlightsAll.get());

            insertedFrames.set(dbIn.getFrameCount());
            newPlanesAll.set(dbIn.getPlaneCount());
            newFlightsAll.set(dbIn.getFlightCount());

            Frame lastFrame = dbIn.getLastFrame();
            Throwable nextError = errorQueue.poll();
            Controller.getInstance().handleException(nextError);
            display.update(insertedNow.get(), newPlanesNow.get(), newFlightsNow.get(),
                    (lastFrame != null) ? lastFrame.toShortString() : "None",
                    dataLoader.getQueueSize(), nextError);
        }, 0, 1000);
    }

    @Override
    public boolean stopCollecting() {
        inserter.stop();
        inserter.park();
        return super.stopCollecting();
    }

    /**
     * getter for 'filters enabled' flag
     *
     * @return 'filters enabled' flag
     */
    public boolean filtersEnabled() {
        return filterManager != null;
    }

    /**
     * {@link ActionListener} for start / stop button
     *
     * @return the {@link ActionListener}, called on start / stop click
     */
    @NotNull
    public ActionListener onStartStop() {
        return e -> {
            DBIn.getDBIn().setEnabled(setEnabled(!isEnabled()));
            setPaused(isEnabled());
            switch (MathUtils.toBinary(isEnabled())) {
                case 0 -> startCollecting();
                case 1 -> System.out.println(stopCollecting() ? "Interrupted successfully!" : "Couldn't stop the Collector!");
            }
            display.setStatus((isEnabled() ? "enabled, " : "disabled, ") +
                              (isPaused() ? "paused" : "running"));
        };
    }

    /**
     * {@link ActionListener} for pause click
     *
     * @return ActionListener that is called on pause click
     */
    @NotNull
    public ActionListener onPause() {
        return e -> {
            DBIn.getDBIn().setEnabled(setPaused(!isPaused()));
            display.setStatus((isEnabled() ? "enabled, " : "disabled, ") +
                              (isPaused() ? "paused" : "running"));
        };
    }

}

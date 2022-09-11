package planespotter.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import planespotter.constants.Areas;
import planespotter.controller.Controller;
import planespotter.dataclasses.Frame;
import planespotter.display.models.SupplierDisplay;
import planespotter.model.io.*;
import planespotter.model.nio.FilterManager;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.nio.DataLoader;
import planespotter.util.math.MathUtils;

import javax.swing.*;
import java.awt.event.ActionListener;

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
public class Fr24Collector extends Collector<Fr24Supplier> {
    // inserted frames per write
    public static final int FRAMES_PER_WRITE;
    // insert period in seconds
    protected static final int REQUEST_PERIOD;
    // static initializer
    static {
        FRAMES_PER_WRITE = 800;
        REQUEST_PERIOD = 60;
    }

    @Nullable private final FilterManager filterManager;

    @NotNull private final DataLoader dataLoader;

    @NotNull private final Inserter inserter;

    // raster of areas (whole world) in 1D-Array
    @NotNull private final String[] worldAreaRaster1D;

    /**
     * Fr24-Collector Main-method
     *
     * @param args can be ignored
     */
    public static void main(String[] args) {
        // only military with a small grid size
        int sLat = 2, sLon = 4;
        new Fr24Collector(true, true, sLat, sLon).start();
    }

    /**
     * Fr24Collector constructor, creates a new Collector for Fr24-Data
     *
     * @param exitOnClose indicates if the program should exit when the 'X'-button is clicked
     */
    public Fr24Collector(boolean exitOnClose,
                         boolean withFilters,
                         @Range(from = 2, to = 180) int gridSizeLat,
                         @Range(from = 2, to = 360) int gridSizeLon) {
        super(exitOnClose, new Fr24Supplier());
        int closeOperation = (exitOnClose)
                ? WindowConstants.EXIT_ON_CLOSE
                : WindowConstants.DISPOSE_ON_CLOSE;
        super.display = new SupplierDisplay(closeOperation, this, onPause(), onStartStop());
        this.filterManager = withFilters
                ? (FilterManager) Controller.getInstance().getConfig().getProperty("collectorFilters")
                : null;
        this.dataLoader = new DataLoader();
        this.inserter = new Inserter(this.dataLoader, super.getErrorQueue());
        this.worldAreaRaster1D = Areas.getWorldAreaRaster1D(gridSizeLat, gridSizeLon);
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
        String[] specialAreas = Areas.EASTERN_FRONT;
        super.startNewMainThread(() -> this.collect(deserializer, keeper, specialAreas), "Fr24-Collector");
    }

    /**
     * collecting task for the collector
     */
    private synchronized void collect(@NotNull final Fr24Deserializer deserializer, @NotNull final Keeper keeper, @NotNull String @Nullable ... extAreas) {

        scheduler.schedule(() -> scheduler.exec(() -> {
            // executing suppliers to collect Fr24-Data
            dataLoader.collectData(extAreas, deserializer, true);
            // adding all deserialized world-raster-areas to frames deque
            dataLoader.collectData(worldAreaRaster1D, deserializer, true);
            
        }, "Data Collector Thread", false, Scheduler.HIGH_PRIO, true),
                "Collect Data", 0, REQUEST_PERIOD);

        scheduler.runThread(inserter, "Inserter Thread", true, Scheduler.MID_PRIO);
        // executing the keeper every 400 seconds
        scheduler.schedule(() -> scheduler.exec(keeper, "Keeper Thread", true, Scheduler.LOW_PRIO, false),
                100, 400);
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
        }, 0, 1);
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

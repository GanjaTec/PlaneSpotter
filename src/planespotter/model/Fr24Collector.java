package planespotter.model;

import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.Range;
import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.io.*;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Fr24Supplier;

/**
 * @name Fr24Collector
 * @author jml04
 * @version 1.0
 *
 * Class Fr24Collector is a Collector subclass for Fr24-Data
 * @see planespotter.model.Collector
 * @see planespotter.model.ADSBCollector
 * @see planespotter.model.nio.Fr24Supplier
 * @see planespotter.model.nio.Fr24Deserializer
 */
public class Fr24Collector extends Collector<Fr24Supplier> {
    // inserted frames per write
    public static final int FRAMES_PER_WRITE;
    // static initializer
    static {
        FRAMES_PER_WRITE = 800;
    }
    // 'filters enabled' flag
    private final boolean withFilters;
    private final Inserter inserter;

    /**
     * Fr24-Collector Main-method
     *
     * @param args can be ignored
     */
    public static void main(String[] args) {
        new Fr24Collector(true, false, 20, 10).start();
    }

    // raster of areas (whole world) in 1D-Array
    private final String[] worldAreaRaster1D;

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
        this.withFilters = withFilters;
        this.inserter = new Inserter();
        this.worldAreaRaster1D = Areas.getWorldAreaRaster1D(gridSizeLon, gridSizeLat);
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
        if (this.withFilters) {
            deserializer.setFilter("NATO", "LAGR", "FORTE", "DUKE", "MULE", "NCR", "JAKE", "BART", "RCH", "MMF", "CASA", "VIVI");
        }
        String[] specialAreas = Areas.EASTERN_FRONT;
        super.startNewMainThread(() -> this.collect(deserializer, keeper, specialAreas), "Fr24-Collector");
    }

    /**
     * collecting task for the collector
     */
    private synchronized void collect(final Fr24Deserializer deserializer, final Keeper keeper, @Nullable String... extAreas) {

        super.scheduler.schedule(() -> super.scheduler.exec(() -> {
            System.out.println("Collecting data...");
            // synchronizing on collector-monitor
            //synchronized (Collector.SYNC) {
            // executing suppliers to collect Fr24-Data
            Fr24Supplier.collectFramesForArea(extAreas, deserializer, super.scheduler, true);
            // adding all deserialized world-raster-areas to frames deque
            Fr24Supplier.collectFramesForArea(this.worldAreaRaster1D, deserializer, super.scheduler, true)/*)*/;
            // adding all deserialized frames to insertLater-queue
            //LiveLoader.insertLater(frames);
            // inserting all Fr24Frames from insertLater-queue into database
            // replaced with Inserter Thread
            //DBIn.insertRemaining(super.scheduler, FRAMES_PER_WRITE);
            //}
        }, "Data Collector Thread", false, Scheduler.HIGH_PRIO, true),
                "Collect Data", 0, REQUEST_PERIOD);

        super.scheduler.runThread(this.inserter, "Inserter Thread", true, Scheduler.MID_PRIO);
        // executing the keeper every 400 seconds
        super.scheduler.schedule(() -> super.scheduler.exec(keeper, "Keeper Thread", true, Scheduler.LOW_PRIO, false),
                100, 400);
        // updating display
        super.scheduler.schedule(() -> {
            super.insertedNow.set(DBIn.getFrameCount() - super.insertedFrames.get());
            super.newPlanesNow.set(DBIn.getPlaneCount() - super.newPlanesAll.get());
            super.newFlightsNow.set(DBIn.getFlightCount() - super.newFlightsAll.get());

            super.insertedFrames.set(DBIn.getFrameCount());
            super.newPlanesAll.set(DBIn.getPlaneCount());
            super.newFlightsAll.set(DBIn.getFlightCount());

            Fr24Frame lastFrame = DBIn.getLastFrame();
            super.display.update(super.insertedNow.get(), super.newPlanesNow.get(), super.newFlightsNow.get(),
                    (lastFrame != null) ? lastFrame.toShortString() : "None");
        }, 0, 1);
    }

}

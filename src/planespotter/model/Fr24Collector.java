package planespotter.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.Range;
import planespotter.constants.Areas;
import planespotter.constants.UserSettings;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.io.*;
import planespotter.model.nio.Filters;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.nio.LiveLoader;

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
    // insert period in seconds
    protected static final int REQUEST_PERIOD;
    // static initializer
    static {
        FRAMES_PER_WRITE = 800;
        REQUEST_PERIOD = 60;
    }

    /**
     * Fr24-Collector Main-method
     *
     * @param args can be ignored
     */
    public static void main(String[] args) {
        new Fr24Collector(true, false, UserSettings.getGridsizeLat(), UserSettings.getGridsizeLon()).start();
    }

    @Nullable private final Filters filters;

    @NotNull private final LiveLoader liveLoader;

    @NotNull private final Inserter inserter;

    // raster of areas (whole world) in 1D-Array
    @NotNull private final String[] worldAreaRaster1D;

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
        this.filters = withFilters ? UserSettings.getCollectorFilters() : null;
        this.liveLoader = new LiveLoader();
        this.inserter = new Inserter(this.liveLoader, 100);
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
    private synchronized void collect(final Fr24Deserializer deserializer, final Keeper keeper, @Nullable String... extAreas) {

        super.scheduler.schedule(() -> super.scheduler.exec(() -> {
            // executing suppliers to collect Fr24-Data
            this.liveLoader.collectPStream(extAreas, deserializer, true);
            // adding all deserialized world-raster-areas to frames deque
            this.liveLoader.collectPStream(this.worldAreaRaster1D, deserializer, true);
            
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
                    (lastFrame != null) ? lastFrame.toShortString() : "None", this.liveLoader.getQueueSize());
        }, 0, 1);
    }

    public boolean filtersEnabled() {
        return this.filters != null;
    }

}

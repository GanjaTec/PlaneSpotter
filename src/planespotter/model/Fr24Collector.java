package planespotter.model;

import org.jetbrains.annotations.Nullable;
import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.io.DBIn;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.io.FastKeeper;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

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
    // raster of areas (whole world) in 1D-Array
    private static final String[] WORLD_AREA_RASTER_1D;
    // initializer
    static {
        FRAMES_PER_WRITE = 800;
        WORLD_AREA_RASTER_1D = Areas.getWorldAreaRaster1D(12, 6);
    }

    private Fr24Frame lastFrame;

    /**
     * Fr24-Collector Main-method
     *
     * @param args can be ignored
     */
    public static void main(String[] args) {
        new Fr24Collector(true).start();
    }

    /**
     * Fr24Collector constructor, creates a new Collector for Fr24-Data
     *
     * @param exitOnClose indicates if the program should exit when the 'X'-button is clicked
     */
    public Fr24Collector(boolean exitOnClose) {
        super(exitOnClose, new Fr24Supplier());
    }

    @Override
    public void startCollecting() {
        var deserializer = new Fr24Deserializer();
        var keeper = new FastKeeper(1200L);
        // TODO: 02.07.2022 optional Filters
        //deserializer.setFilter("NATO", "LAGR", "FORTE", "DUKE", "MULE", "NCR", "JAKE", "BART", "RCH", "MMF");
        var specialAreas = Areas.EASTERN_FRONT;
        super.startNewMainThread(() -> this.collect(deserializer, keeper, specialAreas), "Collector");
    }

    /**
     *
     */
    private synchronized void collect(final Fr24Deserializer deserializer, final FastKeeper keeper, @Nullable String... extAreas) {

        super.scheduler.schedule(() -> {
            // concurrent linked deque for collected frames
            Deque<Fr24Frame> frames;
            // synchronizing on collector-monitor
            synchronized (Collector.SYNC) {
                // executing suppliers to collect Fr24-Data
                frames = (extAreas != null && extAreas.length > 0)
                        // = all deserialized extra-areas if extAreas is not null or empty, else = new empty deque
                        ? (ConcurrentLinkedDeque<Fr24Frame>) Fr24Supplier.framesForArea(extAreas, deserializer, super.scheduler)
                        : new ConcurrentLinkedDeque<>();
                // adding all deserialized world-raster-areas to frames deque
                frames.addAll(Fr24Supplier.framesForArea(WORLD_AREA_RASTER_1D, deserializer, super.scheduler));
                // adding all deserialized frames to insertLater-queue
                LiveData.insertLater(frames);
                // inserting all Fr24Frames from insertLater-queue into database
                DBIn.insertRemaining(super.scheduler, FRAMES_PER_WRITE);
            }
        }, "Supplier", 0, INSERT_PERIOD_SEC);
        // executing the keeper every 400 seconds
        super.scheduler.schedule(() -> super.scheduler.exec(keeper, "Keeper", true, Scheduler.LOW_PRIO, false),
                100, 400);
        // updating display
        super.scheduler.schedule(() -> {
            super.insertedNow.set(DBIn.getFrameCount() - super.insertedFrames.get());
            super.newPlanesNow.set(DBIn.getPlaneCount() - super.newPlanesAll.get());
            super.newFlightsNow.set(DBIn.getFlightCount() - super.newFlightsAll.get());

            this.lastFrame = DBIn.getLastFrame();
            super.display.update(super.insertedNow.get(), super.newPlanesNow.get(), super.newFlightsNow.get(),
                                 (this.lastFrame != null) ? this.lastFrame.toShortString() : "None");

            super.insertedFrames.set(DBIn.getFrameCount());
            super.newPlanesAll.set(DBIn.getPlaneCount());
            super.newFlightsAll.set(DBIn.getFlightCount());
        }, 0, 1);
    }

}

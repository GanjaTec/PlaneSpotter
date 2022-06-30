package planespotter.model;

import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.model.io.DBWriter;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.nio.FastKeeper;

/**
 * @name Fr24Collector
 * @author jml04
 * @version 1.0
 *
 * Class Fr24Collector is a Collector subclass for Fr24-Data
 * @see Collector
 * @see planespotter.model.nio.Fr24Supplier
 * @see planespotter.model.nio.Fr24Deserializer
 */
public class Fr24Collector extends Collector {
    // inserted frames per write
    public static final int FRAMES_PER_WRITE;
    // raster of areas (whole world) in 1D-Array
    private static final String[] worldAreaRaster1D;
    // initializer
    static {
        FRAMES_PER_WRITE = 800;
        worldAreaRaster1D = Areas.getWorldAreaRaster1D();
    }

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
        super(exitOnClose);
    }

    @Override
    public void startCollecting() {
        super.startNewMainThread(() -> this.collect(
                new Fr24Supplier(),
                new Fr24Deserializer(),
                new FastKeeper(1200L)
        ), "Collector");
    }

    /**
     *
     */
    private synchronized void collect(Fr24Supplier supplier, Fr24Deserializer deserializer, FastKeeper keeper) {

        super.scheduler.schedule(() -> {
            synchronized (Collector.SYNC) {
                // executing suppliers to collect Fr24-Data
                var frames = supplier.getFrames(worldAreaRaster1D, deserializer, super.scheduler);
                LiveData.insertLater(frames);
                DBWriter.insertRemaining(super.scheduler, FRAMES_PER_WRITE);
            }
        }, "Supplier-Main", 0, INSERT_PERIOD_SEC);
        // executing the keeper every 400 seconds
        super.scheduler.schedule(() -> super.scheduler.exec(keeper, "Keeper", true, Scheduler.LOW_PRIO, false),
                100, 400);
        // executing the GC every 20 seconds
        super.scheduler.schedule(System::gc, 30, 20);
        // updating display
        super.scheduler.schedule(() -> {
            super.insertedNow.set(DBWriter.getFrameCount() - super.insertedFrames.get());
            super.newPlanesNow.set(DBWriter.getPlaneCount() - super.newPlanesAll.get());
            super.newFlightsNow.set(DBWriter.getFlightCount() - super.newFlightsAll.get());

            super.display.update(super.insertedNow.get(), super.newPlanesNow.get(), super.newFlightsNow.get());

            super.insertedFrames.set(DBWriter.getFrameCount());
            super.newPlanesAll.set(DBWriter.getPlaneCount());
            super.newFlightsAll.set(DBWriter.getFlightCount());
        }, 0, 1);
    }

}

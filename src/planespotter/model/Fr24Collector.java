package planespotter.model;

import org.jetbrains.annotations.Nullable;
import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.io.DBIn;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.io.FastKeeper;

import java.awt.geom.Area;
import java.util.ArrayDeque;

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
    private static final String[] worldAreaRaster1D;
    // initializer
    static {
        FRAMES_PER_WRITE = 800;
        worldAreaRaster1D = Areas.getWorldAreaRaster1D(30., 15.);
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
        super(exitOnClose, new Fr24Supplier());
    }

    @Override
    public void startCollecting() {
        var deserializer = new Fr24Deserializer();
        var keeper = new FastKeeper(1200L);
        // TODO: 02.07.2022 optional Filters
        //deserializer.setFilter("NATO", "LAGR", "FORTE", "DUKE", "MULE", "NCR");
        var specialAreas = Areas.EASTERN_FRONT;
        super.startNewMainThread(() -> this.collect(deserializer, keeper/* , specialAreas */), "Collector");
    }

    /**
     *
     */
    private synchronized void collect(Fr24Deserializer deserializer, FastKeeper keeper, @Nullable String... extAreas) {

        var supplier = (Fr24Supplier) super.supplier;

        super.scheduler.schedule(() -> {
            synchronized (Collector.SYNC) {
                // executing suppliers to collect Fr24-Data
                var frames = (extAreas != null && extAreas.length > 0)
                        ? supplier.getFrames(extAreas, deserializer, super.scheduler)
                        : new ArrayDeque<Fr24Frame>();
                frames.addAll(supplier.getFrames(worldAreaRaster1D, deserializer, super.scheduler));
                LiveData.insertLater(frames);
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

            super.display.update(super.insertedNow.get(), super.newPlanesNow.get(), super.newFlightsNow.get());

            super.insertedFrames.set(DBIn.getFrameCount());
            super.newPlanesAll.set(DBIn.getPlaneCount());
            super.newFlightsAll.set(DBIn.getFlightCount());
        }, 0, 1);
    }

}

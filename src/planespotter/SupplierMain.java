package planespotter;

import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.display.models.PaneModels;
import planespotter.model.LiveData;
import planespotter.model.io.DBIn;
import planespotter.model.io.DBOut;
import planespotter.model.io.DBWriter;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.nio.FastKeeper;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class SupplierMain {
    public static final Object sync = new Object();

    public static final int INSERT_PERIOD_SEC = 100; // seconds

    /**
     * Second Supplier Test-Main, single, scheduled World-Supplier
     *
     * @param args can be ignored
     */
    public static void main(String[] args) {

        final var scheduler = new Scheduler();
        final var worldAreaRaster1D = Areas.getWorldAreaRaster1D();
        final var display = new PaneModels.SupplierDisplay();
        final AtomicInteger insertedNow = new AtomicInteger(0), insertedFrames = new AtomicInteger(0),
                newPlanesNow = new AtomicInteger(0), newPlanesAll = new AtomicInteger(0),
                newFlightsNow = new AtomicInteger(0), newFlightsAll = new AtomicInteger(0);

        final var supplier = new Fr24Supplier();
        final var deserializer = new Fr24Deserializer();
        final var keeper = new FastKeeper(1200L);

        display.start();

        scheduler.schedule(() -> {
            synchronized (sync) {
                // executing suppliers to collect Fr24-Data
            var frames = supplier.getFrames(worldAreaRaster1D, deserializer, scheduler);
                LiveData.insertLater(frames);
                DBWriter.insertRemaining(scheduler, 800);
            }
        }, "Supplier-Main", 0, INSERT_PERIOD_SEC);
        // executing the keeper every 400 seconds
        scheduler.schedule(() -> scheduler.exec(keeper, "Keeper", true, Scheduler.LOW_PRIO, false),
                    100, 400);
        // executing the GC every 20 seconds
        scheduler.schedule(System::gc, 30, 20);
        // updating display
        scheduler.schedule(() -> {
                    insertedNow.set(DBWriter.getFrameCount() - insertedFrames.get());
                    newPlanesNow.set(DBWriter.getPlaneCount() - newPlanesAll.get());
                    newFlightsNow.set(DBWriter.getFlightCount() - newFlightsAll.get());

                    display.update(insertedNow.get(), newPlanesNow.get(), newFlightsNow.get());

                    insertedFrames.set(DBWriter.getFrameCount());
                    newPlanesAll.set(DBWriter.getPlaneCount());
                    newFlightsAll.set(DBWriter.getFlightCount());
                }, 0, 1);
    }

}

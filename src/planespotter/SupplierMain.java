package planespotter;

import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.display.models.PaneModels;
import planespotter.model.io.DBWriter;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.nio.FastKeeper;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SupplierMain {
    public static final int INSERT_PERIOD_SEC = 60; // seconds

    /**
     * Second Supplier Test-Main, single, scheduled World-Supplier
     *
     * @param args can be ignored
     */
    public static void main(String[] args) {

        final var scheduler = new Scheduler();
        final var worldAreaRaster1D = Areas.getWorldAreaRaster1D();
        final var keeper = new FastKeeper(1200L);
        final var display = new PaneModels.SupplierDisplay();
        final AtomicInteger insertedNow = new AtomicInteger(0), insertedFrames = new AtomicInteger(0),
                newPlanesNow = new AtomicInteger(0), newPlanesAll = new AtomicInteger(0),
                newFlightsNow = new AtomicInteger(0), newFlightsAll = new AtomicInteger(0);

        display.start();

        scheduler.schedule(() -> {
                    // executing suppliers to collect Fr24-Data
                    var threadNr = new AtomicInteger();
                    Arrays.stream(worldAreaRaster1D)
                            .parallel()
                            .forEach(area -> new Fr24Supplier(threadNr.getAndIncrement(), area).supply());
                }, "Supplier-Main", 0, INSERT_PERIOD_SEC)
                // executing the keeper every 400 seconds
                .schedule(keeper, "Keeper", 100, 400)
                // executing the GC every 20 seconds
                .schedule(System::gc, "GC Caller", 30, 20)
                // updating display
                .schedule(() -> {
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

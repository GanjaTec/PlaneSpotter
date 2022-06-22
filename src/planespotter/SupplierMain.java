package planespotter;

import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.model.LiveData;
import planespotter.model.io.DBWriter;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.nio.FastKeeper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SupplierMain {

    public static final int INSERT_PERIOD_SEC = 100; // seconds


    public static final Object sync;
    public static Thread mainThread;
    private static Scheduler scheduler;
    private static final String[] worldAreaRaster1D;
    private static final SupplierDisplay display;
    private static final AtomicInteger insertedNow, insertedFrames,
                                        newPlanesNow, newPlanesAll,
                                        newFlightsNow, newFlightsAll;
    static {
        sync = new Object();
        worldAreaRaster1D = Areas.getWorldAreaRaster1D();
        display = new SupplierDisplay();
        insertedNow = new AtomicInteger(0);
        insertedFrames = new AtomicInteger(0);
        newPlanesNow = new AtomicInteger(0);
        newPlanesAll = new AtomicInteger(0);
        newFlightsNow = new AtomicInteger(0);
        newFlightsAll = new AtomicInteger(0);
    }
    /**
     * Second Supplier Test-Main, single, scheduled World-Supplier
     *
     * @param args can be ignored
     */
    public static void main(String[] args) {
        display.start();
        startCollecting();
    }

    public static void startCollecting() {
        runCollector(new Scheduler(),
                     new Fr24Supplier(),
                     new Fr24Deserializer(),
                     new FastKeeper(1200L));
    }

    public static boolean stopCollecting() {
        mainThread.interrupt();
        boolean success = scheduler.shutdownNow() && mainThread.isInterrupted();
        Scheduler.sleepSec(2);
        return success;
    }

    /**
     *
     */
    private static synchronized void runCollector(Scheduler mainScheduler, Fr24Supplier supplier, Fr24Deserializer deserializer, FastKeeper keeper) {
        scheduler = mainScheduler;
        mainThread = new Thread(() -> {
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
        });
        mainThread.setName("Collector");
        mainThread.start();
    }

}

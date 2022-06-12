package planespotter;

import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Frame;
import planespotter.model.DataLoader;
import planespotter.model.LiveData;
import planespotter.model.io.DBIn;
import planespotter.model.io.DBOut;
import planespotter.model.nio.Supplier;
import planespotter.model.nio.proto.ProtoKeeper;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static planespotter.model.LiveData.*;

public abstract class SupplierMain {
    // monitor object
    public static final Object lock = new Object();

    /**
     * Second Supplier Test-Main, single, scheduled World-Supplier
     *
     * @param args can be ignored
     */
    public static void main(String[] args) {

        final var scheduler = new Scheduler();
        final var supplier0 = new Supplier(0, Areas.AMERICA);
        final var supplier1 = new Supplier(1, Areas.EURASIA);
        final var keeper = new ProtoKeeper(1200L);

        scheduler.schedule(() -> {
            scheduler.exec(supplier0, "Supplier-0", true, 2, false)
                     .exec(supplier1, "Supplier-1", true, 2, false);
        }, "Supplier-Main", 0, 300)
                .schedule(keeper, "Keeper", 100, 400)
                .schedule(System::gc, "GC Caller", 30, 20);
    }

}

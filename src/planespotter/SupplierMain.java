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
            supplier0.run();
            supplier1.run();
            keeper.run();
            Runtime.getRuntime().gc();
        }, "Supplier-Main", 0, 420);
    }

}

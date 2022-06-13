package planespotter;

import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.model.nio.Supplier;
import planespotter.model.nio.proto.ProtoKeeper;

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
            // executing two suppliers to collect Fr24-Data
            scheduler.exec(supplier0, "Supplier-0", true, 2, false)
                     .exec(supplier1, "Supplier-1", true, 2, false);
        }, "Supplier-Main", 0, 120)
                // executing the keeper every 400 seconds
                .schedule(keeper, "Keeper", 100, 400)
                // executing the GC every 20 seconds
                .schedule(System::gc, "GC Caller", 30, 20);
    }

}

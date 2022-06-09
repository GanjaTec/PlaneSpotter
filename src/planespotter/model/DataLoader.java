package planespotter.model;

import planespotter.controller.Controller;
import planespotter.controller.Scheduler;
import planespotter.model.nio.Supplier;

import static planespotter.model.LiveMap.*;

public class DataLoader {
    // 'monitor' object
    private static final Object lock;
    // 'data loader enabled' flag
    private static boolean enabled;

    private static DataLoader instance;

    static {
        lock = new Object();
        enabled = true;
        instance = new DataLoader();
    }

    private DataLoader() {
    }

    public static int load() {
        int inserted = 0;
        if (enabled) {
            var log = Controller.getLogger();
            log.log("Trying to insert frames...", instance);
            if (canInsert()) {
                // insert live data with normal writeToDB
                var frames = pollFromQueue(200).stream().toList();
                new Supplier(0, null).writeToDB(frames);
                inserted += 200;
            }
            log.log("Inserted " + inserted + " frames!", instance);
        }
        return inserted;
    }

    public static synchronized int insertRemaining(Scheduler scheduler) {
        int inserted = 0;
        if (enabled) {
            var log = Controller.getLogger();
            log.log("Trying to insert last live data...", instance);
            //var gui = Controller.getGUI();
            //gui.getContainer("window").setVisible(false);
            final var supplier = new Supplier(0, null);
            while (isReady()) {
                var frames = pollFromQueue(500).stream().toList();
                scheduler.exec(() -> supplier.writeToDB(frames),
                                "Inserter", false, 9, false);
                inserted += 500;
            }

            log.log("Inserted " + inserted + " frames!", instance);
            System.out.println("Inserted " + inserted + " frames!");
        }
        return inserted;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
    }
}

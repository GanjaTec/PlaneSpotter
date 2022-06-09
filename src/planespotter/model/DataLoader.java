package planespotter.model;

import planespotter.controller.Controller;
import planespotter.controller.Scheduler;
import planespotter.model.io.DBIn;
import planespotter.model.io.DBOut;
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
                Supplier.writeToDB(frames, new DBOut(), new DBIn());
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
            while (isReady()) {
                var frames = pollFromQueue(500).stream().toList();
                scheduler.exec(() -> Supplier.writeToDB(frames, new DBOut(), new DBIn()),
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

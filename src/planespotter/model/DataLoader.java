package planespotter.model;

import planespotter.controller.Controller;
import planespotter.controller.Scheduler;
import planespotter.model.io.DBIn;
import planespotter.model.io.DBOut;
import planespotter.model.nio.Supplier;

import static planespotter.model.LiveData.*;

public abstract class DataLoader {
    // 'data loader enabled' flag
    private static boolean enabled;

    static {
        enabled = true;
    }

    private DataLoader() {
    }

    public static synchronized int insert(final Scheduler scheduler, final int count) {
        int insertCount = 0;
        if (enabled) {
            var log = Controller.getLogger();
            log.log("Trying to insert frames...", DataLoader.class);
            if (canInsert(count)) {
                // insert live data with normal writeToDB
                var frames = pollFromQueue(count).stream().toList();
                scheduler.exec(() -> Supplier.writeToDB(frames, new DBOut(), new DBIn()),
                        "DB-LiveData Writer", true, 2, true);
                insertCount += count;
            }
            if (insertCount > 0) {
                log.log("Inserting " + insertCount + " frames...", DataLoader.class);
            }
        }
        return insertCount;
    }

    public static synchronized int insertRemaining(final Scheduler scheduler) {
        int inserted = 0;
        if (enabled) {
            final var log = Controller.getLogger();
            final var dbOut = new DBOut();
            final var dbIn = new DBIn();
            log.log("Trying to insert last live data...", DataLoader.class);
            //var gui = Controller.getGUI();
            //gui.getContainer("window").setVisible(false);
            while (!isEmpty()) {
                var frames = pollFromQueue(500).stream().toList();
                scheduler.exec(() -> Supplier.writeToDB(frames, dbOut, dbIn),
                        "Inserter", false, 9, false);
                inserted += 500;
            }

            log.log("Inserted " + inserted + " frames!", DataLoader.class);
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

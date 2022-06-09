package planespotter.model;

import planespotter.throwables.NoAccessException;

public class DataLoader {
    // 'monitor' object
    private static final Object lock;
    // 'data loader enabled' flag
    private static boolean enabled;

    static {
        lock = new Object();
        enabled = true;
    }


    public static int load() {
        int inserted = 0;
        while (enabled) {
            if (LiveMap.canInsert()) {
                // insert live data with normal writeToDB
                inserted += 1000;
            } else {
                synchronized (lock) {
                    try {
                        lock.wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.notify();
                    }
                }
            }
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

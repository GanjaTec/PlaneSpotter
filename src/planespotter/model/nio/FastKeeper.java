package planespotter.model.nio;

import planespotter.model.io.DBConnector;
import planespotter.model.io.DBIn;
import planespotter.model.io.DBOut;
import planespotter.throwables.DataNotFoundException;

import static planespotter.util.Time.*;

/**
 * @name ProtoKeeper
 * @author jml04
 * @ideaFrom Lukas
 * @version 1.0
 *
 * Class ProtoKeeper represents an improved KeeperOfTheArchives,
 * which runs faster and uses less memory.
 * The code is build after the idea of the KeeperOfTheArchives,
 * but with a seconds look on efficiency.
 * This ProtoKeeper fixes the previous Problem, that the KeeperOfTheArchives
 * runs for a very long time on big amount of data, time doesn't increase that fast in here,
 * even with big amount of data.
 * @see planespotter.model.nio.KeeperOfTheArchives
 * @see planespotter.model.nio.Keeper
 * @see DBConnector
 */
public class FastKeeper implements Keeper {
    // threshold milliseconds
    private final long thresholdMillis;
    // DB-In/Out for database communication
    private final DBOut dbo;
    private final DBIn dbi;

    /**
     * contrtuctor of ProtoKeeper, creates a ProtoKeeper with custom
     * endThreshold and new DBIn and DBOut
     *
     * @param endThreshold is the end threshold in milliseconds
     */
    public FastKeeper(final long endThreshold) {
        this.thresholdMillis = endThreshold;
        this.dbo = new DBOut();
        this.dbi = new DBIn();
    }

    /**
     * keeps the database clean
     * sorts out ended flights and updates them
     */
    @Override
    public void keep() {
        long startMillis = nowMillis();
        System.out.println("ProtoKeeper has started working...");
        int rowsUpdated = 0;
        try {
            var fIDsAndTimestamps = dbo.getLiveFlightIDsWithTimestamp();
            if (!fIDsAndTimestamps.isEmpty()) {
                var fids = fIDsAndTimestamps.keySet();
                long ts;
                for (int id : fids) {
                    ts = fIDsAndTimestamps.get(id);
                    long tdiff = nowMillis()/1000 - ts;
                    if (tdiff > this.thresholdMillis) {
                        dbi.updateFlightEnd(id, ts);
                        rowsUpdated++;
                    }
                }
            }
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        long elapsed = elapsedSeconds(startMillis);
        System.out.println("ProtoKeeper finished work on the DB in " + elapsed +
                           " seconds!\n" + rowsUpdated + " rows updated");
    }
}

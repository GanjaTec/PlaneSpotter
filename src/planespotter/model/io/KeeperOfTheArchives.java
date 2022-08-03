package planespotter.model.io;

import planespotter.throwables.DataNotFoundException;
import planespotter.unused.KeeperOfTheArchivesSenior;

import java.util.Map;
import java.util.Set;

import static planespotter.util.Time.*;

/**
 * @name ProtoKeeper
 * @author jml04
 * @ideaFrom Lukas
 * @version 1.0
 *
 * @description
 * Class ProtoKeeper represents an improved KeeperOfTheArchivesSenior,
 * which runs faster and uses less memory.
 * The code is build after the idea of the KeeperOfTheArchivesSenior,
 * but with a second look on efficiency.
 * This KeeperOfTheArchives fixes the previous Problem, that the KeeperOfTheArchivesSenior
 * runs for a very long time on big amount of data, time doesn't increase that fast here,
 * even with big amount of data.
 * @see KeeperOfTheArchivesSenior
 * @see Keeper
 * @see DBConnector
 */
public class KeeperOfTheArchives implements Keeper {
    // threshold milliseconds
    private final long thresholdMillis;
    // DB-In/Out for database communication
    private final DBOut dbo;
    private final DBIn dbi;

    /**
     * constructor of KeeperOfTheArchives, creates a Keeper with custom
     * endThreshold and DBIn and DBOut reference
     *
     * @param endThreshold is the end threshold in milliseconds
     */
    public KeeperOfTheArchives(final long endThreshold) {
        this.thresholdMillis = endThreshold;
        this.dbo = DBOut.getDBOut();
        this.dbi = DBIn.getDBIn();
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
        Map<Integer, Long> fIDsAndTimestamps;
        try {
            fIDsAndTimestamps = dbo.getLiveFlightIDsWithTimestamp();
        } catch (DataNotFoundException e) {
            e.printStackTrace();
            return;
        }
        if (fIDsAndTimestamps.isEmpty()) {
            return;
        }
        Set<Integer> fids = fIDsAndTimestamps.keySet();
        long ts;
        for (int id : fids) {
            ts = fIDsAndTimestamps.get(id);
            long tDiff = nowMillis()/1000 - ts;
            if (tDiff > this.thresholdMillis) {
                dbi.updateFlightEnd(id, ts);
                rowsUpdated++;
            }
        }
        long elapsed = elapsedSeconds(startMillis);
        System.out.println("ProtoKeeper finished work on the DB in " + elapsed +
                           " seconds!\n" + rowsUpdated + " rows updated");
    }
}

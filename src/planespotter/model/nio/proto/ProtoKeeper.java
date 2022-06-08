package planespotter.model.nio.proto;

import planespotter.model.io.DBIn;
import planespotter.model.io.DBOut;

import static planespotter.util.Time.*;

public class ProtoKeeper extends DBManager implements Runnable {

    private final long thresholdMillis;
    private final DBOut dbo;
    private final DBIn dbi;

    public ProtoKeeper(final long endThreshold) {
        this.thresholdMillis = endThreshold;
        this.dbo = new DBOut();
        this.dbi = new DBIn();
    }

    @Override
    public void run() {
        long startMillis = nowMillis();
        writing = true;
        System.out.println("ProtoKeeper has started working...");
        int rowsUpdated = 0;
        try {
            var fIDsAndTimestamps = dbo.getLiveFlightIDsWithTimestamp();
            if (fIDsAndTimestamps != null) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        long elapsed = elapsedSeconds(startMillis);
        System.out.println("ProtoKeeper finished work on the DB in " + elapsed +
                " seconds!\n" + rowsUpdated + " rows updated");
        writing = false;
    }
}

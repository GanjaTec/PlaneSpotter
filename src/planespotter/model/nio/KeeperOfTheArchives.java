package planespotter.model.nio;

import planespotter.model.io.DBIn;
import planespotter.model.io.DBOut;

import java.util.List;

/**
 * The Keeper of the Archives is a mysterious entity that inhabits the DB and keeps the records in order.
 * 
 * It does so by comparing the elapsed time since the last tracking point for a given Flight against a threshold value.
 * If the last tracking point is older than that, the Keeper will update the corresponding DB Entry with an endTime.
 * 
 * @author Lukas
 *
 */
public class KeeperOfTheArchives implements Runnable{ // TODO: 11.06.2022 can implement Keeper
	private final long threshold;
	private final int threadNumber;
	private final String threadName;
	private final DBOut dbo = new DBOut();
	private final DBIn dbi = new DBIn();
	
	public KeeperOfTheArchives(int threadNumber, long endThreshold) {
		this.threadNumber = threadNumber;
		this.threshold = endThreshold;
		this.threadName = "KeeperOfTheArchives   |";
	}
	

	
	@Override
	public void run() {
		System.out.println(this.threadName + " is starting to Work!");
		int i = 0;
		try {
			List<Integer> IDs = dbo.checkEnded();
			for(int id : IDs) {
				long ts = dbo.getLastTimestempByFlightID(id);
				long tdiff = (System.currentTimeMillis() / 1000L) - ts; 
				if(tdiff > this.threshold) {
					dbi.updateFlightEnd(id, ts);
					i = i + 1;
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(this.threadName + " finished work on the DB!\n" + i + " rows updated");
	}
	
	
}

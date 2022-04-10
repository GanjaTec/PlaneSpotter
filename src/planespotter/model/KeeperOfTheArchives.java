package planespotter.model;

import java.util.List;

/**
 * The Keeper of the Archives is a mysterious entity that inhabits the DB and keeps the records in order.
 * 
 * It does so by comparing the elapsed time since the last tracking point for a given Flight against a threshold value.
 * If the last tracking point was not updated in this amount of time, the Keeper will update the corresponding DB Entry with an endTime.
 * 
 * @author Lukas
 *
 */
public class KeeperOfTheArchives implements Runnable{
	private long threshold;
	private int threadNumber;
	private String threadName;
	
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
			List<Integer> IDs = DBOut.checkEnded();
			for(int id : IDs) {
				long ts = DBOut.getLastTrackingByFlightID(id);
				long tdiff = (System.currentTimeMillis() / 1000L) - ts; 
				if(tdiff > this.threshold) {
					new DBIn().updateFlightEnd(id, ts);
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

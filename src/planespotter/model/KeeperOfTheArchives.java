package planespotter.model;

import java.util.List;

/**
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
		this.threadName = "KeeperOfTheArchives | Thread-" + this.threadNumber;
	}
	

	
	@Override
	public void run() {
		System.out.println(this.threadName + " is starting Work!");
		try {
			List<Integer> IDs = DBOut.checkEnded();
			for(int id : IDs) {
				long ts = DBOut.getLastTrackingByFlightID(id);
				long currentTime = System.currentTimeMillis() / 1000L;
				if(ts > (currentTime - this.threshold)) {
					DBIn.updateFlightEnd(id, ts);
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(this.threadName + " finished work on the DB!");
	}
	
	
}

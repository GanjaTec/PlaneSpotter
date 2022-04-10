package planespotter.model;

import planespotter.constants.Areas;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import planespotter.constants.*;
public class Paralel {
	private int poolsize = 1;
	private ThreadPoolExecutor exe =  (ThreadPoolExecutor) Executors.newFixedThreadPool(poolsize);
	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(poolsize);
	
	public Paralel() {
	}

	public void startThreads() throws InterruptedException {
		String[] areay= Areas.EASTERN_FRONT;
		for(int i=0; i < areay.length; i++) {
			Supplier s = new Supplier(i, areay[i]);
			ses.scheduleAtFixedRate(s, 10+(i*5), 60, TimeUnit.SECONDS);
		}
		KeeperOfTheArchives bofh = new KeeperOfTheArchives(areay.length, 1200L);
		ses.scheduleAtFixedRate(bofh, 0, 20, TimeUnit.MINUTES);
	}
	
	
	/**
	 * 
	 * @throws InterruptedException
	 */
	public void runThreads() throws InterruptedException {
		ThreadPoolExecutor executor =  (ThreadPoolExecutor) Executors.newFixedThreadPool(poolsize);
		exe.setKeepAliveTime(20, TimeUnit.SECONDS);
		
		String[] areay= Areas.EASTERN_FRONT;
		for(int i=0; i < areay.length; i++) {
			executor.execute(new Supplier(i, areay[i]));
		}
		
		KeeperOfTheArchives bofh = new KeeperOfTheArchives(areay.length, 1200L);
		executor.execute(bofh);
		executor.shutdown();
		}
	}


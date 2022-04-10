package planespotter.model;

import planespotter.constants.Areas;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Paralel {
	private int poolsize = 1;
	private ScheduledExecutorService exe = Executors.newScheduledThreadPool(poolsize);

	public Paralel() {


	}

	public void startThreads() throws InterruptedException {
		String[] areay= Areas.EASTERN_FRONT;
		for(int i=0; i < areay.length; i++) {
			Supplier s = new Supplier(i, areay[i]);
			exe.scheduleAtFixedRate(s, 10+(i*5), 60, TimeUnit.SECONDS);
		}
		KeeperOfTheArchives bofh = new KeeperOfTheArchives(areay.length, 1200L);
		exe.scheduleAtFixedRate(bofh, 0, 20, TimeUnit.MINUTES);
	}
}

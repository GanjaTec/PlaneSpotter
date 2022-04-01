package planespotter.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Paralel {
	private int poolsize = 5;
	private ScheduledExecutorService exe = Executors.newScheduledThreadPool(poolsize);

	public Paralel() {


	}

	public void startThreads() throws InterruptedException {
		for(int i=0; i < poolsize; i++) {
			Supplier s = new Supplier(i);
			exe.scheduleAtFixedRate(s, i, 5, TimeUnit.MINUTES);
			//TimeUnit.SECONDS.sleep(30);
		}

	}
}

package planespotter.a_test;

import planespotter.constants.Areas;
import planespotter.model.nio.DataLoader;
import planespotter.model.nio.Fr24Supplier;
import planespotter.throwables.MalformedAreaException;
import planespotter.unused.KeeperOfTheArchivesSenior;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// TODO class can use Scheduler instead of the executer services, Scheduler contains both
@Deprecated
public class Paralel {
	private int poolsize = 8;
	private ThreadPoolExecutor exe =  (ThreadPoolExecutor) Executors.newFixedThreadPool(poolsize);
	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(poolsize);
	
	public Paralel() {
	}

	public void startThreads() throws InterruptedException, MalformedAreaException {
		DataLoader loader = new DataLoader();

		String[] areay= Areas.EASTERN_FRONT;
		for(int i=0; i < areay.length; i++) {
			Fr24Supplier s = new Fr24Supplier(areay[i], loader);
			ses.scheduleAtFixedRate(s, 10+(i*5), 60, TimeUnit.SECONDS);
		}

		KeeperOfTheArchivesSenior bofh = new KeeperOfTheArchivesSenior(areay.length, 1200L);
		ses.scheduleAtFixedRate(bofh, 0, 20, TimeUnit.MINUTES);
	}
	
	
	/**
	 * 
	 * @throws InterruptedException
	 */
	public void runThreads() throws InterruptedException, MalformedAreaException {
		DataLoader dataLoader = new DataLoader();
		ThreadPoolExecutor executor =  (ThreadPoolExecutor) Executors.newFixedThreadPool(poolsize);
		exe.setKeepAliveTime(20, TimeUnit.SECONDS);
		
		
		String[] resultList = Areas.getAllAreas();
	    int i = 0;
	    for (String s : resultList) {
			executor.execute(new Fr24Supplier(s, dataLoader));
			i += 1;
	    }

		KeeperOfTheArchivesSenior bofh = new KeeperOfTheArchivesSenior(resultList.length, 1200L);
		executor.execute(bofh);
		executor.shutdown();
		}
	}


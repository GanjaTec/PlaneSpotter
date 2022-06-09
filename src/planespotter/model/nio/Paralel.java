package planespotter.model.nio;

import planespotter.constants.Areas;
import planespotter.model.nio.proto.ProtoKeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static planespotter.util.Time.elapsedSeconds;
import static planespotter.util.Time.nowMillis;

public class Paralel {
	private int poolsize = 8;
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
		
		
		List<String> resultList = new ArrayList<String>(Areas.EASTERN_FRONT.length + Areas.GERMANY.length + Areas.ITA_SWI_AU.length);
	    Collections.addAll(resultList, Areas.EASTERN_FRONT);
	    Collections.addAll(resultList, Areas.GERMANY);
	    Collections.addAll(resultList, Areas.ITA_SWI_AU);
	    int i = 0;
	    for (String s : resultList) {
			executor.execute(new Supplier(i, s));
			i += 1;
	    }

		KeeperOfTheArchives bofh = new KeeperOfTheArchives(resultList.size(), 1200L);
		executor.execute(bofh);
		executor.shutdown();
		}
	}


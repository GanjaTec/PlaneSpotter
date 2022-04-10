package planespotter;

import java.util.concurrent.TimeUnit;

import planespotter.model.Paralel;

public class TestMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Paralel p = new Paralel();
		
		try {
			while(true) {
			p.runThreads();
			TimeUnit.MINUTES.sleep(5);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

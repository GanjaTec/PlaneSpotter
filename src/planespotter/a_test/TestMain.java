package planespotter.a_test;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;
import planespotter.model.nio.Paralel;

@TestOnly
public class TestMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Paralel p = new Paralel();
		
		try {
			while (true) {
			p.runThreads();
			TimeUnit.MINUTES.sleep(5);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
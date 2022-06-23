package planespotter.a_test;

import java.security.AccessController;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;
import planespotter.SupplierMain;
import planespotter.controller.Scheduler;
import planespotter.model.nio.ADSBSupplier;
import planespotter.model.nio.Paralel;

@TestOnly
public class TestMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ADSBSupplier adsb = new ADSBSupplier("localhost", 47806, true);
		Scheduler s = new Scheduler();
		s.exec(adsb, "Supplier");
		//s.exec(()-> SupplierMain.main(null), "supllier");



		/*Paralel p = new Paralel();
		
		try {
			while (true) {
			p.runThreads();
			TimeUnit.MINUTES.sleep(5);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}

package planespotter.a_test;

import org.jetbrains.annotations.TestOnly;
import planespotter.util.Utilities;

@TestOnly
public class TestMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//decimal(255);
		//ADSBSupplier adsb = new ADSBSupplier("localhost", 47806, true);
		//Scheduler s = new Scheduler();
		//s.exec(adsb, "Supplier");
		//s.exec(()-> SupplierMain.main(null), "supllier");
		System.out.println(Utilities.linesCode("", ".db"));




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

	public static void decimal(int i) {
		String hex = Integer.toHexString(i);
		System.out.println(hex);

	}

}

package planespotter;

import planespotter.model.Paralel;

public class TestMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Paralel p = new Paralel();
		
		try {
			p.startThreads();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

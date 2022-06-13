package planespotter.unused;

import planespotter.dataclasses.Fr24Frame;

@Deprecated
public class Fr24Data {
	private int count;
	private int version;
	private Fr24Frame fr24Frame;

	private Fr24Data(int count, int ver, Fr24Frame f) {
		this.count = count;
		this.version = ver;
		this.fr24Frame = f;
		
	}
	
	public int getCount() {
		return this.count;
	}
	
	public int getVersion() {
		return this.version;
	}
	
	public Fr24Frame getFrame() {
		return this.fr24Frame;
	}



}
package planespotter.unused;

import planespotter.dataclasses.Frame;

@Deprecated
public class Fr24Data {
	private int count;
	private int version;
	private Frame frame;

	private Fr24Data(int count, int ver, Frame f) {
		this.count = count;
		this.version = ver;
		this.frame = f;
		
	}
	
	public int getCount() {
		return this.count;
	}
	
	public int getVersion() {
		return this.version;
	}
	
	public Frame getFrame() {
		return this.frame;
	}



}
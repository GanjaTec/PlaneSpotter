package planespotter;

public class Frame{
	private String icaoaddr;
	private double lat;
	private double lon;
	private int heading;
	private int altitude;
	private int groundspeed;
	private int squawk;
	private String tailnumber;
	private String planetype;
	private String registration;
	private String unknown0;
	private String srcairport;
	private String destairport;
	private String flightnumber;
	private String unknown1;
	private String unknown2;
	private String callsign;
	private String unknown3;
	private String airline;

	private Frame(String icao, double lat, double lon, int heading, int alt, int speed,
			int squawk, String tail, String type, String registration, String unk0, 
			String src, String dest, String flight, String unk1, String unk2, String callsign, String unk3,
			String airline) {
		this.icaoaddr = icao;
		this.lat = lat;
		this.lon = lon;
		this.heading = heading;
		this.altitude = alt;
		this.groundspeed = speed;
		this.squawk = squawk;
		this.tailnumber = tail;
		this.planetype = type;
		this.registration = registration;
		this.unknown0 = unk0;
		this.srcairport = src;
		this.destairport = dest;
		this.flightnumber = flight;
		this.unknown1 = unk1;
		this.unknown2 = unk2;
		this.callsign = callsign;
		this .unknown3 = unk3;
		this.airline = airline;

	}
	
	public String getIcaoAdr() {
		return this.icaoaddr;
	}
	
	public double getLat() {
		return this.lat;
	}
	
	public double getLon() {
		return this.lon;
	}
	
	public int getHeading() {
		return this.heading;
	}
	
	public int getAltitude() {
		return this.altitude;
	}
	
	public int getGroundspeed() {
		return this.groundspeed;
	}
	
	public int getSquawk() {
		return this.squawk;
	}
	
	public String getTailnr() {
		return this.tailnumber;
	}
	
	public String getPlanetype() {
		return this.planetype;
	}
	
	public String getRegistration() {
		return this.registration;
	}
	
	public String getUnknown0() {
		return this.unknown0;
	}
	
	public String getSrcAirport() {
		return this.srcairport;
	}
	
	public String getDestAirport() {
		return this.destairport;
	}
	
	public String getFlightnumber() {
		return this.flightnumber;
	}
	
	public String getUnknown1() {
		return this.unknown1;
	}
	
	public String getUnknown2() {
		return this.unknown2;
	}
	
	public String getCallsign() {
		return this.callsign;
	}
	
	public String getUnknown3() {
		return this.unknown3;
	}
	
	public String getAirline() {
		return this.airline;
	}
	
	
}


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

}


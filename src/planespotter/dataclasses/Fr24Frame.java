package planespotter.dataclasses;

/**
 * @name Fr24Frame
 * @author Lukas
 * @version 1.0
 *
 * @description
 * class Fr24Frame is a Frame-class-child which represents
 * a Frame with additional FlightRadar24-Data
 */
// TODO: 13.06.2022 move fields to superclass Frame
// TODO: 13.06.2022 change to record
public class Fr24Frame extends Frame {
	private final String icaoaddr;
	private final double lat;
	private final double lon;
	private final int heading;
	private final int altitude;
	private final int groundspeed;
	private final int squawk;
	private final String tailnumber;
	private final String planetype;
	private final String registration;
	private final int timestamp;
	private final String srcairport;
	private final String destairport;
	private final String flightnumber;
	private final String unknown1;
	private final String unknown2;
	private final String callsign;
	private final String unknown3;
	private final String airline;

	public Fr24Frame(String icao, double lat, double lon, int heading, int alt, int speed,
					 int squawk, String tail, String type, String registration, int time,
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
		this.timestamp = time;
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
	
	public int getTimestamp() {
		return this.timestamp;
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

	public String toShortString() {
		return this.getIcaoAdr() + ";" + this.getCallsign() + ";" + this.getPlanetype() + ";" +
				this.getAirline() + ";" + this.getSrcAirport() + ";" + this.getDestAirport();
	}

	@Override
	public String toString() {
		return  "ICAO: " + this.getIcaoAdr() + ',' +
				" Lat: " + this.getLat() + ',' +
				" Lon: " + this.getLon() + ',' +
				" Heading: " + this.getHeading() + ',' +
				" Altitude: " + this.getAltitude() + ',' +
				" Registration: " + this.getRegistration() + ',' +
				" Squawk: " + this.getSquawk() + ',' +
				" TailNr.: " + this.getTailnr() + ',' +
				" Planetype: " + this.getPlanetype() + ',' +
				" Registration: " + this.getRegistration() + ',' +
				" TailNr.: " + this.getTailnr() + ',' +
				" SrcAirport: " + this.getSrcAirport() + ',' +
				" DestAirport: " + this.getDestAirport() + ',' +
				" FlightNr.: " + this.getFlightnumber() + ',' +
				" Unkwn1: " + this.getUnknown1() + ',' +
				" Unkwn2: " + this.getUnknown2() + ',' +
				" Callsign: " + this.getCallsign() + ',' +
				" Unkwn3: " + this.getUnknown3() + ',' +
				" Airline: " + this.getAirline();
	}

	/**
	 * prints all values of this class, using toString method
	 */
	public void printValues() {
		System.out.println(this);
		
	}
	
}


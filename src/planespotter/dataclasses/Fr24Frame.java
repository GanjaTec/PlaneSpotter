package planespotter.dataclasses;

/**
 * @name Fr24Frame
 * @author Lukas
 * @version 1.0
 *
 * @description
 * class Fr24Frame is a Frame-class-child which represents
 * a Frame with additional FlightRadar24-Data
 * We cannot use camel case to name our instance fields here, because the
 * Gson-fromJson needs equals field names in both, Class and JSON to deserialize right
 */
public class Fr24Frame extends Frame {

	// additional fields
	private final String icaoaddr;
	private final String tailnumber;
	private final String planetype;
	private final String registration;
	private final String srcairport;
	private final String destairport;
	private final String flightnumber;
	private final String unknown1;
	private final String callsign;
	private final String unknown2;
	private final String unknown3;
	private final String airline;

	/**
	 *
	 *
	 * @param icao
	 * @param lat
	 * @param lon
	 * @param heading
	 * @param alt
	 * @param speed
	 * @param squawk
	 * @param tail
	 * @param type
	 * @param registration
	 * @param time
	 * @param src
	 * @param dest
	 * @param flight
	 * @param unk1
	 * @param unk2
	 * @param callsign
	 * @param unk3
	 * @param airline
	 */
	public Fr24Frame(String icao, double lat, double lon, int heading, int alt, int speed,
					 int squawk, String tail, String type, String registration, int time,
					 String src, String dest, String flight, String unk1, String unk2,
					 String callsign, String unk3, String airline) {

		super(lat, lon, heading, alt, speed, squawk, time);
		this.icaoaddr = icao;
		this.tailnumber = tail;
		this.planetype = type;
		this.registration = registration;
		this.srcairport = src;
		this.destairport = dest;
		this.flightnumber = flight;
		this.unknown1 = unk1;
		this.callsign = callsign;
		this.unknown2 = unk2;
		this .unknown3 = unk3;
		this.airline = airline;

	}

	public String getIcaoAddr() {
		return this.icaoaddr;
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

	public String getCallsign() {
		return this.callsign;
	}

	public String getUnknown2() {
		return this.unknown2;
	}

	
	public String getUnknown3() {
		return this.unknown3;
	}
	
	public String getAirline() {
		return this.airline;
	}

	@Override
	public String toShortString() {
		return getIcaoAddr() + ";" + getCallsign() + ";" + getPlanetype() + ";" +
				getAirline() + ";" + getSrcAirport() + ";" + getDestAirport();
	}

	@Override
	public String toString() {
		return  "ICAO: " + getIcaoAddr() + ',' +
				" Lat: " + getLat() + ',' +
				" Lon: " + getLon() + ',' +
				" Heading: " + getHeading() + ',' +
				" Altitude: " + getAltitude() + ',' +
				" Registration: " + getRegistration() + ',' +
				" Squawk: " + getSquawk() + ',' +
				" TailNr.: " + getTailnr() + ',' +
				" Planetype: " + getPlanetype() + ',' +
				" Registration: " + getRegistration() + ',' +
				" TailNr.: " + getTailnr() + ',' +
				" SrcAirport: " + getSrcAirport() + ',' +
				" DestAirport: " + getDestAirport() + ',' +
				" FlightNr.: " + getFlightnumber() + ',' +
				" Unkwn1: " + getUnknown1() + ',' +
				" Unkwn2: " + getUnknown2() + ',' +
				" Callsign: " + getCallsign() + ',' +
				" Unkwn3: " + getUnknown3() + ',' +
				" Airline: " + getAirline();
	}
	
}


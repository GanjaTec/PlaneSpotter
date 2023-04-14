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

	// size of a Fr24Frame in bytes
	public static final int SIZE = 360;

	// additional fields

	// Icao Address
	private String icaoaddr;

	// tailnumber
	private final String tailnumber;

	// Planetype
	private final String planetype;

	// Aircraft Registration
	private final String registration;

	// Source Airport Tag
	private final String srcairport;

	// Destination Airport Tag
	private final String destairport;

	// Flightnumber
	private final String flightnumber;

	// Unknown String 1
	private final String unknown1;

	// Callsign
	private final String callsign;

	// Unknown String 2
	private final String unknown2;

	// Unknown String 3
	private final String unknown3;

	// Airline Tag
	private final String airline;

	/**
	 * Constructor
	 *
	 * @param icao String ICAO-Address
	 * @param lat double latitude
	 * @param lon double longitude
	 * @param heading int compass heading
	 * @param alt int altitude
	 * @param speed int groundspeed in knots
	 * @param squawk int squawkcode
	 * @param tail String tailnumber
	 * @param type String planetype
	 * @param registration String aircraft registration
	 * @param time int timestamp
	 * @param src String source airport
	 * @param dest String destination airport
	 * @param flight String flightnumber
	 * @param unk1 String unknown
	 * @param unk2 String unknown
	 * @param callsign String callsign
	 * @param unk3 String unknown
	 * @param airline String airline identifier
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

	@Override
	public void setIcaoAddr(String icao) {
		this.icaoaddr = icao;
	}

	/**
	 * Getter for ICAO-Address
	 * @return String icaoaddr
	 */
	public String getIcaoAddr() {
		return this.icaoaddr;
	}

	/**
	 * Getter for Tailnumber
	 * @return String tailnumber
	 */
	public String getTailnr() {
		return this.tailnumber;
	}

	/**
	 * Getter for Planetype
	 * @return String planetype
	 */
	public String getPlanetype() {
		return this.planetype;
	}

	/**
	 * Getter for Aircraft Registration
	 * @return String registration
	 */
	public String getRegistration() {
		return this.registration;
	}

	/**
	 * Getter for Source Airport Tag
	 * @return String srcairport
	 */
	public String getSrcAirport() {
		return this.srcairport;
	}

	/**
	 * Getter for Destination Airport Tag
	 * @return String destairport
	 */
	public String getDestAirport() {
		return this.destairport;
	}

	/**
	 * Getter for Flightnumber
	 * @return String flightnumber
	 */
	public String getFlightnumber() {
		return this.flightnumber;
	}

	/**
	 * Getter for unknown String 1
	 * @return String unknown1
	 */
	public String getUnknown1() {
		return this.unknown1;
	}

	/**
	 * Getter for Callsign
	 * @return String callsign
	 */
	public String getCallsign() {
		return this.callsign;
	}

	/**
	 * Getter for unknown String 2
	 * @return String unknown2
	 */
	public String getUnknown2() {
		return this.unknown2;
	}

	/**
	 * Getter for unknown String 3
	 * @return String unknown3
	 */
	public String getUnknown3() {
		return this.unknown3;
	}

	/**
	 * Getter for Airline Tag
	 * @return String airline
	 */
	public String getAirline() {
		return this.airline;
	}

	/**
	 * prepares a String with a short summary of the Frame, just values, no bloat
	 * @return String containing ICAO, Callsign, Planetype, Airline, Source and Destination
	 */
	@Override
	public String toShortString() {
		return getIcaoAddr() + ";" + getCallsign() + ";" + getPlanetype() + ";" +
				getAirline() + ";" + getSrcAirport() + ";" + getDestAirport();
	}

	/**
	 * Returns a list of the attributes and their values, basically prettyPrint without new lines
	 * @return String containing all values of the Frame
	 */
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


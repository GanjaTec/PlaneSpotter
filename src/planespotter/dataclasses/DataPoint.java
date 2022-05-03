package planespotter.dataclasses;

import java.io.Serializable;

/**
 * @author Janne Matti
 * @author Lukas
 *
 * This Class is used to represent a single DB Entry from the 'Tracking'-Table
 */
public class DataPoint extends SuperData implements Serializable {
	private int id;
	private int flightid;
	private Position pos;
	private long timestemp;
	private int sqawk;
	private int speed;
	private int heading;
	private int altitude;

	/**
	 * Constructor
	 * 
	 * @param id int  Database ID
	 * @param flightid int Database FlightID
	 * @param pos Position Coordinates
	 * @param timestamp long Unix epoche
	 * @param sqawk int 4-Digit Sqaqkcode
	 * @param speed int Groundspeed TODO Check units
	 * @param heading int Compass Direction, 0-360
	 * @param altitude int Alitude TODO Check units
	 */
	public DataPoint(int id, int flightid, Position pos, long timestamp, int sqawk, int speed, int heading, int altitude) {
		this.id = id;
		this.flightid = flightid;
		this.pos = pos;
		this.timestemp = timestamp;
		this.sqawk = sqawk;
		this.speed = speed;
		this.heading = heading;
		this.altitude = altitude;
	}

	//Getter
	/**
	 * @return int Database ID
	 */
	public int getID() {
		return this.id;
	}
	/**
	 * @return int Database FlightID
	 */
	public int getFlightID() {
		return this.flightid;
	}

	/**
	 * @return Position Coordinates
	 */
	public Position getPos() {
		return this.pos;
	}


	/**
	 * @return long Unix Epoche
	 */
	public long getTimestemp() {
		return this.timestemp;
	}

	/**
	 * @return int 4-Digit Sqawkcode
	 */
	public int getSqawk() {
		return this.sqawk;
	}

	/**
	 * @return int Groundspeed in TODO check units
	 */
	public int getSpeed() {
		return this.speed;
	}

	/**
	 * @return int Compass Heading, 0-360
	 */
	public int getHeading() {
		return this.heading;
	}

	/**
	 * @return int Altitude in TODO check units
	 */
	public int getAltitude() {
		return this.altitude;
	}
}

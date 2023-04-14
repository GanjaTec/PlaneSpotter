package planespotter.dataclasses;

/**
 * @name Frame
 * @author jml04
 * @author Lukas
 * @version 1.0
 *
 * @description
 * abstract class Frame is a frame-superclass which should
 * have all default frame fields e.g. latitude, longitude ,etc...
 */
public abstract class Frame implements DataFrame {

    // size of a Frame in bytes
    public static final int SIZE = 200;

    // default Frame fields
    private final double lat;
    private final double lon;
    private final int heading;
    private final int altitude;
    private final int groundspeed;
    private final int squawk;
    private final long timestamp;

    /**
     * {@link Frame} super-constructor
     *
     * @param lat is the current latitude
     * @param lon is the current longitude
     * @param heading is the current heading
     * @param alt is the current altitude
     * @param speed is the current ground speed
     * @param squawk is the current squawk code
     * @param timestamp is the current timestamp
     */
    protected Frame(double lat, double lon, int heading, int alt, int speed, int squawk, long timestamp) {
        this.lat = lat;
        this.lon = lon;
        this.heading = heading;
        this.altitude = alt;
        this.groundspeed = speed;
        this.squawk = squawk;
        this.timestamp = timestamp;
    }

    /**
     *
     *
     * @param icao
     */
    public abstract void setIcaoAddr(String icao);

    /**
     * abstract getter for ICAO address,
     * every frame needs it
     *
     * @return ICAO address of this frame
     */
    public abstract String getIcaoAddr();

    /**
     * abstract getter for callsign,
     * every frame needs it
     *
     * @return callsign of this frame
     */
    public abstract String getCallsign();

    /**
     * getter for the current latitude
     *
     * @return the current latitude
     */
    public double getLat() {
        return lat;
    }

    /**
     * getter for the current longitude
     *
     * @return the current longitude
     */
    public double getLon() {
        return lon;
    }

    /**
     * getter for current heading
     *
     * @return current heading
     */
    public int getHeading() {
        return heading;
    }

    /**
     * getter for current altitude
     *
     * @return current altitude
     */
    public int getAltitude() {
        return altitude;
    }

    /**
     * getter for current ground speed
     *
     * @return current ground speed
     */
    public int getGroundspeed() {
        return groundspeed;
    }

    /**
     * getter for current squawk code
     *
     * @return current squawk code
     */
    public int getSquawk() {
        return squawk;
    }

    /**
     * getter for current timestamp
     *
     * @return current timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * makes a short String of this Frame and returns it
     *
     * @return this {@link Frame} as a short String
     */
    public String toShortString() {
        return getIcaoAddr() + ";" + getCallsign() + ";" + getGroundspeed() + ";" + getAltitude() + ";" + getSquawk();
    }
}
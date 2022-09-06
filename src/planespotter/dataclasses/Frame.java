package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

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
// TODO: 15.08.2022 move default fields here
public abstract class Frame implements Serializable {

    private final double lat;
    private final double lon;
    private final int heading;
    private final int altitude;
    private final int groundspeed;
    private final int squawk;
    private final long timestamp;

    protected Frame(double lat, double lon, int heading, int alt, int speed, int squawk, long timestamp) {
        this.lat = lat;
        this.lon = lon;
        this.heading = heading;
        this.altitude = alt;
        this.groundspeed = speed;
        this.squawk = squawk;
        this.timestamp = timestamp;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getHeading() {
        return heading;
    }

    public int getAltitude() {
        return altitude;
    }

    public int getGroundspeed() {
        return groundspeed;
    }

    public int getSquawk() {
        return squawk;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
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

    private final String icaoaddr;
    private final double lat;
    private final double lon;
    private final int heading;
    private final int altitude;
    private final int groundspeed;
    private final int squawk;

    protected Frame(@NotNull String icao, double lat, double lon, int heading, int alt, int speed, int squawk) {
        this.icaoaddr = icao;
        this.lat = lat;
        this.lon = lon;
        this.heading = heading;
        this.altitude = alt;
        this.groundspeed = speed;
        this.squawk = squawk;
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

}

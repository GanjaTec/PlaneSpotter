package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;

/**
 * @name ADSBFrame
 * @author Lukas
 * @author Bennet
 * @author jml04
 * @version 1.0
 *
 * @description
 * Objects of the {@link ADSBFrame} class are {@link Frame}s that
 * are collected with the ADSBSupplier (with Antenna).
 */
public class ADSBFrame extends Frame {

    // size of an ADSBFrame in bytes
    public static final int SIZE = 132;

    /*
     * we need to name the class fields EQUAL to the JSON fields,
     * so some field names might not be that meaningful
     */

    // hex (ICAO) and flight (mostly call sign) Strings
    private String hex;
    private final String flight;

    // track (heading) and gs (ground speed)
    private final int track, gs;

    /**
     * {@link ADSBFrame} constructur, constructs a new {@link ADSBFrame}
     *
     * @param hex is the ICAO address
     * @param lat is the current latitude
     * @param lon is the current longitude
     * @param track is the current headings
     * @param alt is the current altitude
     * @param trueAirSpeed is the current speed
     * @param squawk is the current squawk code
     * @param flight is the callsign
     * @param now is the current timestamp
     */
    public ADSBFrame(@NotNull String hex, double lat, double lon, int track, int alt, int trueAirSpeed, int squawk, @NotNull String flight, int now) {
        super(lat, lon, track, alt, trueAirSpeed, squawk, now);
        // we need to save some fields more than once, because they need
        // different field names regarding the JSON deserializer
        this.hex = hex;
        this.flight = flight;
        this.track = track;
        this.gs = trueAirSpeed;
    }

    @Override
    public void setIcaoAddr(String icao) {
        this.hex = icao;
    }

    /**
     * getter for the ICAO address (hex)
     *
     * @return the ICAO address (hex)
     */
    @Override
    public String getIcaoAddr() {
        return hex;
    }

    /**
     * getter for the callsign (flight)
     *
     * @return the callsign (flight)
     */
    @Override
    public String getCallsign() {
        return flight;
    }

    /**
     * getter for current heading (track)
     *
     * @return the current heading (track)
     */
    @Override
    public int getHeading() {
        return track;
    }

    /**
     * getter for current ground speed (gs)
     *
     * @return current ground speed (gs)
     */
    @Override
    public int getGroundspeed() {
        return gs;
    }
}

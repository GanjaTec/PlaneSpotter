package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;

/**
 * @name ADSBFrame
 * @author
 * @version 1.0
 *
 * @description
 * Objects of the {@link ADSBFrame} class are {@link Frame}s that
 * are collected with the ADSBSupplier (with Antenna).
 */
public class ADSBFrame extends Frame {

    private final String hex, flight;

    private final int track, gs;

    public ADSBFrame(@NotNull String hex, double lat, double lon, int heading, int alt, int speed, int squawk, @NotNull String flight, int now, int track, int trueAirSpeed) {
        super(lat, lon, heading, alt, speed, squawk, now);
        this.hex = hex;
        this.flight = flight;
        this.track = track;
        this.gs = trueAirSpeed;
    }

    public String getIcaoAddr() {
        return hex;
    }

    public String getCallsign() {
        return flight;
    }

    public int getHeading() {
        return track;
    }

    public int getGroundspeed() {
        return gs;
    }
}

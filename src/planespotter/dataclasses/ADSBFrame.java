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

    protected ADSBFrame(@NotNull String icao, double lat, double lon, int heading, int alt, int speed, int squawk, String flight, int now) {
        super(icao, lat, lon, heading, alt, speed, squawk, flight, now);
    }
}

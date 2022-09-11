package planespotter.dataclasses;
import org.jetbrains.annotations.NotNull;
import planespotter.throwables.InvalidDataException;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @name Flight
 * @author jml04
 * @author Lukas
 *
 *@description
 * This Class is used to Represent a single Flight with all off its Data Points
 */
public record Flight(int id,
                     Airport src,
                     Airport dest,
                     String callsign,
                     Plane plane,
                     String flightNr,
                     HashMap<Integer, DataPoint> dataPoints)
        implements Serializable {

    /**
     * parses a {@link Frame} to {@link Flight} object
     *
     * @param frame is the {@link Frame} to be parsed, can be {@link Fr24Frame} or {@link ADSBFrame}
     * @param id is the {@link Flight} ID, that the flight gets
     * @return new {@link Flight} object, parsed from {@link Frame}
     */
    public static Flight parseFlight(@NotNull final Frame frame, final int id) {
        HashMap<Integer, DataPoint> dataPoints = new HashMap<>();
        // putting first data point to map
        dataPoints.put(0, new DataPoint(0, id,
                new Position(frame.getLat(),
                        frame.getLon()),
                frame.getTimestamp(),
                frame.getSquawk(),
                frame.getGroundspeed(),
                frame.getHeading(),
                frame.getAltitude()));
        // returning new flight object
        if (frame instanceof Fr24Frame fr24) {
            return new Flight(id,
                    new Airport(-1, fr24.getSrcAirport(), null, null),
                    new Airport(-1, fr24.getDestAirport(), null, null),
                    fr24.getCallsign(),
                    new Plane(-1, fr24.getIcaoAddr(), fr24.getTailnr(),
                            fr24.getPlanetype(), fr24.getRegistration(),
                            new Airline(-1, fr24.getAirline(), null, null)),
                    fr24.getFlightnumber(),
                    dataPoints);
        } else if (frame instanceof ADSBFrame adsb) {
            return new Flight(id,
                    new Airport(-1, null, null, null),
                    new Airport(-1, null, null, null),
                    adsb.getCallsign(),
                    new Plane(-1, adsb.getIcaoAddr(), "None", "None", "None", new Airline(-1, null, null, null)),
                    "None", dataPoints);
        }
        throw new InvalidDataException("Couldn't parse flight!");
    }

}
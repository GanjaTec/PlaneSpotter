package planespotter.dataclasses;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Janne Matti
 * @author Lukas
 *
 *@description
 * This Class is used to Represent a single Flight with all off its Datapoints
 */
public record Flight(int id,
                     Airport src,
                     Airport dest,
                     String callsign,
                     Plane plane,
                     String flightNr,
                     HashMap<Integer, DataPoint> dataPoints)
        implements Serializable {

    public static Flight parseFlight(final Fr24Frame fr24Frame, final int id) {
        var dataPoints = new HashMap<Integer, DataPoint>();
        // putting first data point to map
        dataPoints.put(0, new DataPoint(0, id,
                new Position(fr24Frame.getLat(),
                        fr24Frame.getLon()),
                fr24Frame.getTimestamp(),
                fr24Frame.getSquawk(),
                fr24Frame.getGroundspeed(),
                fr24Frame.getHeading(),
                fr24Frame.getAltitude()));
        // returning new flight object
        return new Flight(id,
                new Airport(-1, fr24Frame.getSrcAirport(), null, null),
                new Airport(-1, fr24Frame.getDestAirport(), null, null),
                fr24Frame.getCallsign(),
                new Plane(-1, fr24Frame.getIcaoAdr(), fr24Frame.getTailnr(),
                        fr24Frame.getPlanetype(), fr24Frame.getRegistration(),
                        new Airline(-1, fr24Frame.getAirline(), null,  null)),
                fr24Frame.getFlightnumber(),
                dataPoints);
    }

}
package planespotter.dataclasses;
import java.util.HashMap;

/**
 * @author Janne Matti
 * @author Lukas
 *	
 * This Class is used to Represent a single Flight with all off its Datapoints
 */
public record Flight(int id,
                     Airport src,
                     Airport dest,
                     String callsign,
                     Plane plane,
                     String flightNr,
                     HashMap<Integer, DataPoint> dataPoints)
        implements Data {

    public static Flight parseFlight(final Frame frame, final int id) {
        var dataPoints = new HashMap<Integer, DataPoint>();
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
        return new Flight(id,
                new Airport(-1, frame.getSrcAirport(), null, null),
                new Airport(-1, frame.getDestAirport(), null, null),
                frame.getCallsign(),
                new Plane(-1, frame.getIcaoAdr(), frame.getTailnr(),
                        frame.getPlanetype(), frame.getRegistration(),
                        new Airline(-1, frame.getAirline(), null,  null)),
                frame.getFlightnumber(),
                dataPoints);
    }

}
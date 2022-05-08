package planespotter.dataclasses;
import java.util.HashMap;

/**
 * @author Janne Matti
 * @author Lukas
 *	
 * This Class is used to Represent a single Flight with all off its Datapoints
 */
public class Flight implements SuperData {
    private int id;
    private Airport start;
    private Airport dest;
    private String callsign;
    private String flightnr;
    private Plane plane;
    private HashMap<Integer, DataPoint> datapoints;

    /**
     * Constructor
     * 
     * @param id int Database ID
     * @param start Airport Starting Airport
     * @param dest Airport Destination Airport
     * @param callsign String Callsign of the Flight
     * @param plane Plane a single Plane
     * @param flightnr String Flightnumber, not the FlightID
     * @param datapoints HashMap<Integer, DataPoint> Hashmap containing all Datapoint keyed by Timestamp
     */
    public Flight (int id, Airport start, Airport dest, String callsign, Plane plane, String flightnr, HashMap<Integer, DataPoint> datapoints) {
        this.id = id;
        this.start = start;
        this.dest = dest;
        this.callsign = callsign;
        this.plane = plane;
        this.flightnr = flightnr;
        this.datapoints = datapoints;
    }

    /**
     * getter
     * TODO: return the Flight attribute
     */
    /**
     * @return int Database ID
     */
    public int getID() {
    	return this.id;
    	}

    /**
     * @return Airport Starting Airport
     */
    public Airport getStart() {
    	return this.start;
    	}

    /**
     * @return Airport Destination Airport
     */
    public Airport getDest() {
    	return this.dest;
    	}
    
    /**
     * @return String Callsign of the Flight
     */
    public String getCallsign() {
    	return this.callsign;
    }

    /**
     * @return Plane Plane used for the Flight
     */
    public Plane getPlane(){
    	return this.plane;
    	}

    /**
     * @return String Flightnumber, !Important! do not confuse with FlightID
     */
    public String getFlightnr() {
    	return this.flightnr;
    	}

    /**
     * @return HashMap<Long, DataPoint> All of the Tracking Data of the Flight
     */
    public HashMap<Integer, DataPoint> getDataPoints() {
    	return this.datapoints;
    }
}

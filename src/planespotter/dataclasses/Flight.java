package planespotter.dataclasses;

import java.util.ArrayList;
import java.util.HashMap;

public class Flight {
    int id;
    private Airport start, dest;
    private String callsign, flightnr;
    private Plane plane;
    private HashMap<Long, ArrayList<DataPoint>> route;

    public Flight (int id, Airport start, Airport dest, Plane plane, String flightnr, HashMap<Long, ArrayList<DataPoint>> route) {
        this.id = id;
        this.start = start;
        this.dest = dest;
        this.plane = plane;
        this.flightnr = flightnr;
        this.route = route;
    }

    /**
     * getter
     * TODO: return the Flight attribute
     */
    public int getID () { return id; }

    public Airport getStart () { return start; }

    public Airport getDest () { return dest; }

    public Plane getPlane () { return plane; }

    public String getFlightnr () { return flightnr; }


}

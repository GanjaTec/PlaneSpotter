package planespotter.dataclasses;

public class Flight {
    int id;
    private Airport start, dest;
    private String callsign, flightnr;
    private Plane plane;

    public Flight (int id, Airport start, Airport dest, Plane plane, String flightnr) {
        this.id = id;
        this.start = start;
        this.dest = dest;
        this.plane = plane;
        this.flightnr = flightnr;
    }

}

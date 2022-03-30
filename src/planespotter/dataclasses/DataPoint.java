package planespotter.dataclasses;

/**
 * DataPoint Class
 *
 * represents a Flight
 */
public class DataPoint {
    int id;
    private int icao;
    private int timestemp;
    private int sqawk;
    private Flight flight;
    private Position pos;
    private int speed;
    private int heading;
    private int altitude;

    public DataPoint (Flight flight, Position pos, int icao, int timestamp, int sqawk, int speed, int heading, int altitude) {
        this.flight = flight;
        this.pos = pos;
    }
}

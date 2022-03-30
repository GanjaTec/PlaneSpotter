package planespotter.dataclasses;

/**
 * DataPoint Class
 *
 * represents a Flight
 */
public class DataPoint {
    int id;
    private int timestemp;
    private int sqawk;
    private int flightid;
    private Position pos;
    private int speed;
    private int heading;
    private int altitude;

    public DataPoint (int id, int flightid, Position pos, int timestamp, int sqawk, int speed, int heading, int altitude) {
        this.id = id;
    	this.flightid = flightid;
        this.pos = pos;
        this.timestemp = timestamp;
        this.sqawk = sqawk;
        this.speed = speed;
        this.heading = heading;
        this.altitude = altitude;
    }
}

package planespotter.dataclasses;

/**
 * DataPoint Class
 *
 * represents a Flight
 */
public class DataPoint {
    private int id;
    private int flightid;
    private long timestemp;
    private int sqawk;
    private Position pos;
    private int speed;
    private int heading;
    private int altitude;

    public DataPoint (int id, int flightid, Position pos, long timestamp, int sqawk, int speed, int heading, int altitude) {
        this.id = id;
        this.flightid = flightid;
        this.pos = pos;
        this.timestemp = timestamp;
        this.sqawk = sqawk;
        this.speed = speed;
        this.heading = heading;
        this.altitude = altitude;
    }

    /**
     * getter
     * TODO: return the DataPoint attributes
     */
    public int getFlightID () { return flightid; }

    public Position getPos () { return pos; }


    public long getTimestemp () { return timestemp; }

    public int getSqawk () { return sqawk; }

    public int getSpeed() { return speed; }

    public int getHeading () { return heading; }

    public int getAltitude () { return altitude; }
}

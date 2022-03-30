package planespotter.dataclasses;

/**
 * DataPoint Class
 *
 * represents a Flight
 */
public class DataPoint {
    private int flightid;
    private Flight flight;

    private int timestemp;
    private int sqawk;
    private Position pos;
    private int speed;
    private int heading;
    private int altitude;

    public DataPoint (int flightid, Flight flight, Position pos, int timestamp, int sqawk, int speed, int heading, int altitude) {
        this.flightid = flightid;
        this.flight = flight;
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

    public Flight getFlight () { return flight; }

    public Position getPos () { return pos; }


    public int getTimestemp () { return timestemp; }

    public int getSqawk () { return sqawk; }

    public int getSpeed() { return speed; }

    public int getHeading () { return heading; }

    public int getAltitude () { return altitude; }
}

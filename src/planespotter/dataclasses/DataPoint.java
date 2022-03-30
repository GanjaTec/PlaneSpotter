package planespotter.dataclasses;

/**
 * DataPoint Class
 *
 * represents a Flight
 */
public class DataPoint {
    int id;
    private String icao;
    private int timestemp;
    private int sqawk;
    private Flight flight;
    private Position pos;
    private int speed;
    private int heading;
    private int altitude;

    public DataPoint (Flight flight, Position pos, String icao, int timestamp, int sqawk, int speed, int heading, int altitude) {
        this.flight = flight;
        this.pos = pos;
        this.icao = icao;
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
    public Flight getFlight () { return flight; }

    public Position getPos () { return pos; }

    public String getIcao () { return icao; }

    public int getTimestemp () { return timestemp; }

    public int getSqawk () { return sqawk; }

    public int getSpeed() { return speed; }

    public int getHeading () { return heading; }

    public int getAltitude () { return altitude; }
}

package planespotter.dataclasses;

/**
 * ReceiverFrame Class
 *
 * This class is used to retrieve information about the ADSB Receiver using Json
 */
public class ReceiverFrame {
    //the version of dump1090 in use
    private final String version;

    //how often aircraft.json is updated (for the file version), in milliseconds
    private final int refresh;

    //the current number of valid history files
    private final int history;

    //the latitude of the receiver in decimal degrees
    private final double lat;

    //the longitude of the receiver in decimal degrees
    private final double lon;

    /**
     * Constructor
     * @param version String
     * @param refresh int
     * @param history int
     * @param lat double
     * @param lon double
     */
    public ReceiverFrame(String version, int refresh, int history, double lat, double lon){
        this.version = version;
        this.refresh = refresh;
        this.history = history;
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Getter for Version
     * @return String version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Getter for refresh
     * @return int refresh
     */
    public int getRefresh() {
        return refresh;
    }

    /**
     * Getter for history
     * @return int history
     */
    public int getHistory() {
        return history;
    }

    /**
     * Getter for lat
     * @return double lat
     */
    public double getLat() {
        return lat;
    }

    /**
     * Getter for lon
     * @return double lon
     */
    public double getLon() {
        return lon;
    }
}

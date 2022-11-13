package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;

/**
 * ReceiverFrame Class
 *
 * This class is used to retrieve information about the ADSB Receiver using Json
 */
public class ReceiverFrame implements DataFrame {

    //the version of dump1090 in use
    @NotNull private final String version;

    //how often aircraft.json is updated (for the file version), in milliseconds
    private final int refresh;

    //the current number of valid history files
    private final int history;

    // lat and lon in decimal degrees
    private double lat, lon;

    /**
     * constructs a new {@link ReceiverFrame}
     *
     * @param version
     * @param refresh is the refresh period in milliseconds
     * @param history
     * @param position is the receiver {@link Position}
     */
    public ReceiverFrame(@NotNull String version, int refresh, int history, @NotNull Position position) {
        this.version = version;
        this.refresh = refresh;
        this.history = history;
        this.lat = position.lat();
        this.lon = position.lon();
    }

    /**
     * constructs a new {@link ReceiverFrame}
     *
     * @param version String
     * @param refresh int
     * @param history int
     * @param lat double
     * @param lon double
     */
    public ReceiverFrame(@NotNull String version, int refresh, int history, double lat, double lon) {
        this(version, refresh, history, new Position(lat, lon));
    }

    /**
     * Getter for Version
     *
     * @return String version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Getter for refresh
     *
     * @return int refresh
     */
    public int getRefresh() {
        return refresh;
    }

    /**
     * Getter for history
     *
     * @return int history
     */
    public int getHistory() {
        return history;
    }

    /**
     * Getter for receiver position
     *
     * @return the receiver {@link Position}
     */
    @NotNull
    public Position getPosition() {
        return new Position(lat, lon);
    }

    /**
     * Getter for
     *
     * @return double lat
     */
    public double getLat() {
        return lat;
    }

    /**
     * Getter for lon
     *
     * @return double lon
     */
    public double getLon() {
        return lon;
    }
}

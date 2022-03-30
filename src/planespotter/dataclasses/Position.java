package planespotter.dataclasses;

public class Position {
    private double lat;
    private double lon;

    public Position (double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * getter
     * TODO: return the Flight attribute
     */
    public double getLat () { return lat; }

    public double getLon () { return lon; }

}

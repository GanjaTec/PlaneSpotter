package planespotter.dataclasses;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

/**
 * @name CustomMapMarker
 * @author jml04
 * @version 1.0
 *
 * class CustomMapMarker is a custom map marker which extends from a normal MapMarkerDot
 */
public class CustomMapMarker extends MapMarkerDot {

    // flight at the marker position // one marker has one flight
    private Flight flight;

    /**
     * constructor for CustomMapMarker
     * @param coord is the Map Marker coord,
     * @param flight is the flight at the coord-Position
     */
    public CustomMapMarker(Coordinate coord, Flight flight) {
        super(coord);
    }

    /**
     * @return flight, the flight at the map marker point
     */
    public Flight getFlight () {
        return flight;
    }
}

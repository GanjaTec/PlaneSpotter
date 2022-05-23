package planespotter.dataclasses;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.constants.Paths;

import javax.swing.*;
import java.awt.*;

/**
 * @name CustomMapMarker
 * @author jml04
 * @version 1.0
 *
 * class CustomMapMarker is a custom map marker which extends from a normal MapMarkerDot
 */
public class CustomMapMarker extends MapMarkerDot implements MapMarker {

    public static final Image img = new ImageIcon(Paths.RESSOURCE_PATH + "flying_plane_icon.png").getImage();
    private final int heading;

    /**
     * constructor for CustomMapMarker
     * @param coord is the Map Marker coord,
     */
    public CustomMapMarker(Coordinate coord, int heading) {
        super(coord);
        this.heading = heading;
    }

    /*@Override
    public void paint(Graphics g, Point position, int radius) {
        var g2d = (Graphics2D) g;
        g2d.clearRect(0, 0, 100, 100);
        g2d.drawImage(img, position.x+5, position.y+5, null);
        super.paint(g2d, position, radius);
    }*/
}

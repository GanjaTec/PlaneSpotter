package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.constants.Paths;
import planespotter.display.MarkerPainter;
import planespotter.throwables.OutOfRangeException;
import planespotter.util.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * @name CustomMapMarker
 * @author jml04
 * @version 1.0
 *
 * class CustomMapMarker is a custom map marker which extends from a normal MapMarkerDot
 */
public class DefaultMapMarker extends MapMarkerDot implements MapMarker {

    public static final Image img = new ImageIcon(Paths.RESSOURCE_PATH + "flying_plane_icon.png").getImage();
    private final int heading;

    /**
     * constructor for CustomMapMarker
     * @param coord is the Map Marker coord,
     */
    public DefaultMapMarker(Coordinate coord, int heading) {
        super(coord);
        this.heading = heading;
    }

    // TODO move to MarkerPainter (DefaultPainter)
    @Override
    public void paint(Graphics g, Point position, int radius) {
        // custom painting
        var g2d = (Graphics2D) g;
        /*
        g2d.clearRect(0, 0, 100, 100);
        g2d.drawImage(img, position.x+5, position.y+5, null);
        var transform = g2d.getTransform();
        double theta = Math.toRadians(this.heading);
        g2d.rotate(theta);
        g2d.setTransform(transform);*/
        // default painting
        super.paint(g2d, position, radius);
    }

    public static class HeatMapMarker extends MapMarkerCircle {

        private final int level;
        private final Color color;
        private final MarkerPainter painter;

        /**
         * constructor for CustomMapMarker
         *
         * @param coord is the Map Marker coord,
         * @param coord
         * @param
         */
        public HeatMapMarker(@NotNull Coordinate coord, int level) {
            super(coord, 1);
            if (level < 0) {
                throw new OutOfRangeException("level may not be negative!");
            }
            this.level = level;
            this.color = Utilities.colorByLevel(this.level);
            this.painter = new MarkerPainter.HeatMarkerPainter(this.color);
            //this.setBackColor(this.color);
        }

        @Override
        public void paint(Graphics g, Point position, int radius) {
            if (this.painter == null) {
                super.paint(g, position, radius);
            } else {
                this.painter.paint(g, position, radius);
            }

        }
    }
}








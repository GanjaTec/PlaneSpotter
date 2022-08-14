package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.constants.Images;
import planespotter.display.MarkerPainter;
import planespotter.throwables.OutOfRangeException;
import planespotter.util.Utilities;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @name CustomMapMarker
 * @author jml04
 * @version 1.0
 *
 * class CustomMapMarker is a custom map marker which extends from a normal MapMarkerDot
 */
@TestOnly
public class DefaultMapMarker extends MapMarkerDot implements MapMarker {

    public static final Image PLANE_ICON;

    static {
        PLANE_ICON = Images.AIRPLANE_ICON_8x.get().getImage();
    }

    public static DefaultMapMarker fromDataPoint(@NotNull DataPoint dataPoint) {
        return fromPosition(dataPoint.pos(), dataPoint.heading());
    }

    public static DefaultMapMarker fromDataPoint(@NotNull DataPoint dataPoint, @NotNull Color color) {
        return fromPosition(dataPoint.pos(), color, dataPoint.heading());
    }

    public static DefaultMapMarker fromPosition(@NotNull Position pos, int heading) {
        return new DefaultMapMarker(Position.toCoordinate(pos), heading);
    }

    public static DefaultMapMarker fromPosition(@NotNull Position pos, @NotNull Color color, int heading) {
        DefaultMapMarker marker = fromPosition(pos, heading);
        marker.setBackColor(color);
        return marker;
    }


    private final MarkerPainter painter;

    private final int heading;

    /**
     * constructor for CustomMapMarker
     * @param coord is the Map Marker coord,
     */
    public DefaultMapMarker(@NotNull Coordinate coord, int heading) {
        super(coord);
        this.heading = heading;
        this.painter = (g, pos, radius) -> g.drawImage(Utilities.rotate(PLANE_ICON, heading), pos.x, pos.y, null);
    }

    @Override
    public void paint(@NotNull Graphics g, @NotNull Point position, int radius) {
        this.painter.paint(g, position, radius);
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








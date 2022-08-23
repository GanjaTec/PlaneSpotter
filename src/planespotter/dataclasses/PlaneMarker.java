package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.constants.Images;
import planespotter.display.MarkerPainter;
import planespotter.throwables.OutOfRangeException;
import planespotter.util.Utilities;

import java.awt.*;

import static planespotter.constants.DefaultColor.DEFAULT_MAP_ICON_COLOR;

/**
 * @name PlaneMarker
 * @author jml04
 * @version 1.1
 *
 * @description
 * class PlaneMarker is a custom map marker which extends from a normal MapMarkerDot
 * it contains an additional 16x16 airplane icon which can be enabled with a flag,
 * also contains some static methods to convert dataclasses like
 * {@link Flight}s, {@link DataPoint}s or {@link Position}s into {@link PlaneMarker}s
 * @see planespotter.display.TreasureMap
 * @see planespotter.display.MapManager
 * @see org.openstreetmap.gui.jmapviewer.MapMarkerDot
 */
public class PlaneMarker extends MapMarkerDot implements MapMarker {

    // the default plane icon - not selected
    @NotNull protected static final Image DEFAULT_PLANE_ICON;

    // the selected plane icon
    @NotNull protected static final Image SELECTED_PLANE_ICON;

    // initializing plane icons
    static {
        DEFAULT_PLANE_ICON = Images.DEFAULT_AIRPLANE_ICON_16x.get().getImage();
        SELECTED_PLANE_ICON = Images.SELECTED_AIRPLANE_ICON_16x.get().getImage();
    }

    // the MarkerPainter, contains paint() method, may be null
    @Nullable private final MarkerPainter painter;

    // current heading of the plane / marker (in degrees)
    private final int heading;

    // indicates if the marker is selected
    private final boolean selected;

    /**
     * constructor for CustomMapMarker
     * @param coord is the Map Marker coord
     * @param heading is the marker/plane heading in degrees
     * @param showIcon indicates if the plane-icon should be shown instead of a simple dot
     * @param selected indicates if the marker is selected
     */
    public PlaneMarker(@NotNull Coordinate coord, int heading, boolean showIcon, boolean selected) {
        super(coord);
        this.heading = heading;
        this.selected = selected;
        this.painter = getDefaultPainter(heading, showIcon, selected);
    }

    /**
     * creates a {@link PlaneMarker} from a {@link Flight}, checks with the last selected ICAO,
     * if the marker should be selected or not (if the last ICAO is not null)
     *
     * @param flight is the {@link Flight} to be converted
     * @param lastIcao is the last selected ICAO, may be null
     * @param showIcon indicates if the plane icon should be shown
     * @return a new {@link PlaneMarker}, converted from {@link Flight}
     */
    @NotNull
    public static PlaneMarker fromFlight(@NotNull Flight flight, @Nullable String lastIcao, boolean showIcon) {
        final DataPoint dataPoint = flight.dataPoints().get(0);
        final String icao = flight.plane().icao();
        if (lastIcao != null && lastIcao.equalsIgnoreCase(icao)) {
            return PlaneMarker.fromDataPoint(dataPoint, DEFAULT_MAP_ICON_COLOR.get(), showIcon, true);
        }
        return PlaneMarker.fromDataPoint(dataPoint, DEFAULT_MAP_ICON_COLOR.get(), showIcon, false);

    }

    @NotNull
    public static PlaneMarker fromDataPoint(@NotNull DataPoint dataPoint, boolean showIcon, boolean selected) {
        return fromPosition(dataPoint.pos(), dataPoint.heading(), showIcon, selected);
    }

    @NotNull
    public static PlaneMarker fromDataPoint(@NotNull DataPoint dataPoint, @NotNull Color color, boolean showIcon, boolean selected) {
        return fromPosition(dataPoint.pos(), color, dataPoint.heading(), showIcon, selected);
    }

    @NotNull
    public static PlaneMarker fromPosition(@NotNull Position pos, int heading, boolean showIcon, boolean selected) {
        return new PlaneMarker(Position.toCoordinate(pos), heading, showIcon, selected);
    }

    @NotNull
    public static PlaneMarker fromPosition(@NotNull Position pos, @NotNull Color color, int heading, boolean showIcon, boolean selected) {
        PlaneMarker marker = fromPosition(pos, heading, showIcon, selected);
        marker.setBackColor(color);
        return marker;
    }

    @Nullable
    private static MarkerPainter getDefaultPainter(int heading, boolean showIcon, boolean selected) {
        if (!showIcon) {
            return null;
        }
        return selected
                ? (g, pos, radius) -> g.drawImage(Utilities.rotate(SELECTED_PLANE_ICON, heading), pos.x-SELECTED_PLANE_ICON.getWidth(null)/2, pos.y-SELECTED_PLANE_ICON.getHeight(null)/2, null)
                : (g, pos, radius) -> g.drawImage(Utilities.rotate(DEFAULT_PLANE_ICON, heading), pos.x- DEFAULT_PLANE_ICON.getWidth(null)/2, pos.y- DEFAULT_PLANE_ICON.getHeight(null)/2, null);
    }

    public int getHeading() {
        return this.heading;
    }

    public boolean isSelected() {
        return this.selected;
    }

    @Override
    public void paint(@NotNull Graphics g, @NotNull Point position, int radius) {
        if (this.painter == null) {
            super.paint(g, position, radius);
            return;
        }
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








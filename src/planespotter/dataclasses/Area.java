package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import planespotter.constants.Areas;
import planespotter.display.TreasureMap;

/**
 * @name Area
 * @author jml04
 * @version 1.0
 *
 * @description
 * The {@link Area} class represents an area on the map,
 * more simple than the {@link planespotter.constants.Areas} strings
 * @see planespotter.constants.Areas
 */
public class Area {

    // value separator string
    public static final String SEPARATOR = "%2C";

    //
    private final float latTopLeft, latBottomRight, lonTopLeft, lonBottomRight;

    public Area(@Range(from = 90, to = -90)   final float latTopLeft,
                @Range(from = 90, to = -90)   final float latBottomRight,
                @Range(from = -180, to = 180) final float lonTopLeft,
                @Range(from = -180, to = 180) final float lonBottomRight) {
        this.latTopLeft     = latTopLeft;
        this.latBottomRight = latBottomRight;
        this.lonTopLeft     = lonTopLeft;
        this.lonBottomRight = lonBottomRight;
    }

    /**
     * constructs an Area by Positions
     *
     * @param topLeft is the bottom-left-position
     * @param bottomRight is the top-right-position
     */
    public Area(final Position topLeft, final Position bottomRight) {
        this((float) topLeft.lat(), (float) bottomRight.lat(), (float) topLeft.lon(), (float) bottomRight.lon());
    }

    /**
     * constructs an Area by ICoordinates / Coordinates
     *
     * @param topLeft is the bottom-left-coordinate
     * @param bottomRight is the top-right-coordinate
     */
    public Area(final ICoordinate topLeft, final ICoordinate bottomRight) {
        this((float) topLeft.getLat(), (float) bottomRight.getLat(), (float) topLeft.getLon(), (float) bottomRight.getLon());
    }

    /**
     * locates the current lat-lon-rectangle (area) of the map by
     * getting map-positions with getPos. method
     *
     * @param map is the {@link TreasureMap} where the current area is located on
     * @return the current map area, packed in a {@link String} array
     */
    public static Area currentArea(@NotNull final TreasureMap map) {
        // area with map size
        final ICoordinate topLeft = map.getPosition(0, 0);
        final ICoordinate bottomRight = map.getPosition(map.getWidth(), map.getHeight());
        return new Area(topLeft, bottomRight);
    }

    public Position getTopLeft() {
        return new Position(latTopLeft, lonTopLeft);
    }

    public Position getBottomRight() {
        return new Position(latBottomRight, lonBottomRight);
    }

    @Override
    public String toString() {
        return  latTopLeft     + SEPARATOR +
                latBottomRight + SEPARATOR +
                lonTopLeft     + SEPARATOR +
                lonBottomRight;
    }

    public String[] toStringArray() {
        return new String[] {toString()};
    }
}

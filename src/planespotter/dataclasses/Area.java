package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import planespotter.constants.Areas;
import planespotter.display.TreasureMap;
import planespotter.throwables.InvalidDataException;
import planespotter.throwables.MalformedAreaException;

import java.util.Arrays;
import java.util.regex.Pattern;

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
    private static final Pattern AREA_PATTERN = Pattern.compile("(-?[0-9]*\\.-?[0-9]+(%2C-?[0-9]*\\.-?[0-9]+)+)");

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
    public Area(@NotNull final Position topLeft, @NotNull final Position bottomRight) {
        this((float) topLeft.lat(), (float) bottomRight.lat(), (float) topLeft.lon(), (float) bottomRight.lon());
    }

    /**
     * private Area constructor for Area.fromString()
     *
     * @param coords is the coordinate array with a length of 4
     */
    private Area(final double[] coords) {
        this((float) coords[0], (float) coords[1], (float) coords[2], (float) coords[3]);
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

    public static Area fromString(@NotNull String area) throws MalformedAreaException {
        checkAreaString(area);
        double[] coords = Arrays.stream(area.split(SEPARATOR))
                .mapToDouble(Double::parseDouble)
                .toArray();

        return new Area(coords);

    }

    private static void checkAreaString(@NotNull String area) throws MalformedAreaException {
        // TODO: 22.11.2022 check area string with regex
        if (!AREA_PATTERN.matcher(area).matches()) {
            throw new MalformedAreaException();
        }
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

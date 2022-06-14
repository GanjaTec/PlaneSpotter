package planespotter.model;

import org.jetbrains.annotations.Range;
import planespotter.dataclasses.Position;

/**
 * @name AreaFactory
 * @author jml04
 * @version 1.0
 *
 * abstract class AreaFactory represents a Factory for Areas,
 * if one needs more than pre-build Areas, one can create a certain Area by itself
 * @see planespotter.constants.Areas
 */
public abstract class AreaFactory {
    /**
     * Area-Separator-String, is used to separate the doubles in an Area-String
     */
    private static final String SEPARATOR = "%2C";

    /**
     * creates an Area directly by single Coordinates
     *
     * @param latBottomLeft is the bottom-right-latitude
     * @param latTopRight is the top-left-latitude
     * @param lonBottomLeft is the bottom-left-longitude
     * @param lonTopRight is the top-right-longitude
     * @return new Area String, composed of the given Coordinates
     */
    public static String createArea(@Range(from = 89, to = -89) final double latBottomLeft,
                                    @Range(from = 89, to = -89) final double latTopRight,
                                    @Range(from = -179, to = 179) final double lonBottomLeft,
                                    @Range(from = -179, to = 179) final double lonTopRight) {
        return  latBottomLeft + SEPARATOR +
                latTopRight   + SEPARATOR +
                lonBottomLeft + SEPARATOR +
                lonTopRight;
    }

    /**
     * creates an Area by Positions
     *
     * @param bottomLeft is the bottom-left-position
     * @param topRight is the top-right-position
     * @return new Area String, composed of the given Positions
     */
    public static String createArea(final Position bottomLeft, final Position topRight) {
        return createArea(bottomLeft.lat(), topRight.lat(), bottomLeft.lon(), topRight.lon());

    }

}

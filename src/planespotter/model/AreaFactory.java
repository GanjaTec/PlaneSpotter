package planespotter.model;

import org.jetbrains.annotations.Range;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import planespotter.dataclasses.Position;
import planespotter.throwables.InvalidCoordinatesException;

import java.awt.*;

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
     * @param latTopLeft is the bottom-right-latitude
     * @param latBottomRight is the top-left-latitude
     * @param lonTopLeft is the bottom-left-longitude
     * @param lonBottomRight is the top-right-longitude
     * @return new Area String, composed of the given Coordinates
     */
    public static String newArea(@Range(from = 90, to = -90) final double latTopLeft,
                                 @Range(from = 90, to = -90) final double latBottomRight,
                                 @Range(from = -180, to = 180) final double lonTopLeft,
                                 @Range(from = -180, to = 180) final double lonBottomRight) {
        return  latTopLeft     + SEPARATOR +
                latBottomRight + SEPARATOR +
                lonTopLeft     + SEPARATOR +
                lonBottomRight;
    }

    /**
     * creates an Area by Positions
     *
     * @param topLeft is the bottom-left-position
     * @param bottomRight is the top-right-position
     * @return new Area String, composed of the given Positions
     */
    public static String newArea(final Position topLeft, final Position bottomRight) {
        return newArea(topLeft.lat(), bottomRight.lat(), topLeft.lon(), bottomRight.lon());
    }

    /**
     * creates an Area by ICoordinates / Coordinates
     *
     * @param topLeft is the bottom-left-coordinate
     * @param bottomRight is the top-right-coordniate
     * @return new Area String, composed of the given ICoordniates / Coordniates
     */
    public static String newArea(final ICoordinate topLeft, final ICoordinate bottomRight) {
        return newArea(topLeft.getLat(), bottomRight.getLat(), topLeft.getLon(), bottomRight.getLon());
    }

}

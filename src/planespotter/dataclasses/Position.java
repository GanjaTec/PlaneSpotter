package planespotter.dataclasses;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import java.io.Serializable;

/**
 * @author Janne Matti
 * @author Lukas
 *
 * This Class is used to represent a set of Coordinates
 */
public record Position(double lat,
					   double lon)
		implements Serializable {

/**
 * @param coord is the Coordinate to parse
 * @return Position, parsed from Coordinate
 */
	public static Position parsePosition (Coordinate coord) {
		return new Position(coord.getLat(), coord.getLon());
	}

	/**
 * @param position is the Position to parse
 * @return Coordinate, parsed from Position
 */
	public static Coordinate toCoordinate (Position position) {
		return new Coordinate(position.lat(), position.lon());
	}
}

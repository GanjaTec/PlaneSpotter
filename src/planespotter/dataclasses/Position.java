package planespotter.dataclasses;

import org.jetbrains.annotations.Range;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

import java.io.Serializable;

/**
 * @author Janne Matti
 * @author Lukas
 *
 * This Class is used to represent a set of Coordinates
 */
public record Position(@Range(from = 90, to = -90) double lat,
					   @Range(from = -180, to = 180) double lon)
		implements Serializable {

	/**
	 * @param coord is the Coordinate to parse
	 * @return Position, parsed from Coordinate
	 */
	public static Position parsePosition (Coordinate coord) {
		return new Position(coord.getLat(), coord.getLon());
	}

	/**
	 * @param coord is the Coordinate to parse
	 * @return Position, parsed from Coordinate
	 */
	public static Position parsePosition (ICoordinate coord) {
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

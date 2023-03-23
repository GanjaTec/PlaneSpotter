package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

import java.io.Serializable;

/**
 * @name Position
 * @author jml04
 * @author Lukas
 * @version 1.0
 *
 * @description
 * This Class is used to represent a Coordinate, with latitude and longitude
 */
public record Position(@Range(from = 90, to = -90) double lat,
					   @Range(from = -180, to = 180) double lon)
		implements Serializable {

	/**
	 * parses a {@link Coordinate} into {@link Position}
	 *
	 * @param coord is the Coordinate to parse
	 * @return Position, parsed from Coordinate
	 */
	@NotNull
	public static Position parsePosition(@NotNull Coordinate coord) {
		return new Position(coord.getLat(), coord.getLon());
	}

	/**
	 * parses a {@link Coordinate} into {@link Position}
	 *
	 * @param coord is the Coordinate to parse
	 * @return Position, parsed from Coordinate
	 */
	@NotNull
	public static Position parsePosition(@NotNull ICoordinate coord) {
		return new Position(coord.getLat(), coord.getLon());
	}

	/**
	 * parses this {@link Position} object into a {@link Coordinate} object
	 *
	 * @return {@link Coordinate} object from this {@link Position}
	 */
	@NotNull
	public Coordinate toCoordinate() {
		return new Coordinate(this.lat(), this.lon());
	}
}

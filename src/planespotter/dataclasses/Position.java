package planespotter.dataclasses;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import java.io.Serializable;

/**
 * @author Janne Matti
 * @author Lukas
 *
 * This Class is used to represent a set of Coordinates
 */
public class Position implements Data, Serializable {
	private double lat;
	private double lon;

	/**
	 * Constructor
	 * 
	 * @param lat double Latitude
	 * @param lon double Longitude
	 */
	public Position (double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	//Getter
	/**
	 * @return double Latitude
	 */
	public double getLat() {
		return this.lat;
	}

	/**
	 * @return double Longitude
	 */
	public double getLon() {
		return this.lon;
	}

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
		return new Coordinate(position.getLat(), position.getLon());
	}

	/**
	 * @return id from the position
	 */
	@Override
	public int getID() {
		return -9999;
	}
}

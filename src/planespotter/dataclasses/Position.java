package planespotter.dataclasses;

/**
 * @author Janne Matti
 * @author Lukas
 *
 * This Class is used to represent a set of Coordinates
 */
public class Position {
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

}

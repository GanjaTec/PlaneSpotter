package planespotter.dataclasses;

import java.io.Serializable;

/**
 * @author Janne Matti
 * @author Lukas
 *
 * This Class is used to represent a single DB Entry from the 'Tracking'-Table
 */
public record DataPoint(int id,
						int flightID,
						Position pos,
						long timestamp,
						int squawk,
						int speed,
						int heading,
						int altitude)
	implements Serializable {
}

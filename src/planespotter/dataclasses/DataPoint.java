package planespotter.dataclasses;

import java.io.Serializable;

/**
 * @author Janne Matti
 * @author Lukas
 *
 * This Class is used to represent a single DB Entry from the 'Tracking'-Table
 */
public record DataPoint(int id, // data point id
						int flightID, // flight id
						Position pos, // current position
						long timestamp, // current timestamp
						int squawk, // current squawk code
						int speed, // current speed in knots
						int heading, // current heading in degrees
						int altitude) // current altitude in feet
	implements Data, Serializable {
}

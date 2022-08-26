package planespotter.dataclasses;

import java.io.Serializable;

/**
 * @author Janne Matti
 * @author Lukas
 *
 * @description
 * Airline class: represents an airline
 */
public record Airline (int id,
					   String iataTag,
					   String name,
					   String country)
		implements Serializable {
}

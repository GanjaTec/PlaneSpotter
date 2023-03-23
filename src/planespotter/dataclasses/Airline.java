package planespotter.dataclasses;

import java.io.Serializable;

/**
 * @name Airline
 * @author jml04
 * @author Lukas
 * @version 1.0
 *
 * @description
 * Airline class: represents an airline
 */
public record Airline(int id,
					  String iataTag,
					  String name,
					  String country)
		implements Serializable {
}

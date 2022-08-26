package planespotter.dataclasses;

import java.io.Serializable;

/**
 * @author Janne Matti
 * @author Lukas
 *
 * @description
 * Data Structure used to represent a DB entry from the 'Airports'-Table
 */
public record Airport(int id,
                      String iataTag,
                      String name,
                      Position pos)
        implements Serializable {
}
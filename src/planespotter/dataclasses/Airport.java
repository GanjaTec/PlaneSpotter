package planespotter.dataclasses;

import java.io.Serializable;

/**
 * @name Airport
 * @author jml04
 * @author Lukas
 * @version 1.0
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
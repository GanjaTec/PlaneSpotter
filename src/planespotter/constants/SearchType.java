package planespotter.constants;

import org.jetbrains.annotations.NotNull;

/**
 * @name SearchType
 * @author jml04
 * @version 1.0
 *
 * @description
 * enum SearchType contains all search types, with string-names
 * has a static method byItemString() to get an item by string
 */
public enum SearchType {
    AIRLINE("Airline"),
    AIRPORT("Airport"),
    FLIGHT("Flight"),
    PLANE("Plane"),
    AREA("Area");

    // item string
    @NotNull private final String item;

    // private enum constructor
    SearchType(@NotNull String item) {
        this.item = item;
    }

    /**
     * returns a specific SearchType by string, if there is one
     *
     * @param item is the input item string
     * @return enum constant by item if one exists, else null
     */
    @NotNull
    public static SearchType byItemString(String item) {
        return switch (item) {
            case "Flight" -> FLIGHT;
            case "Plane" -> PLANE;
            case "Airline" -> AIRLINE;
            case "Airport" -> AIRPORT;
            case "Area" -> AREA;
            default -> throw new NullPointerException("SearchType not found!");
        };
    }
}

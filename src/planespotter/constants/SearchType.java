package planespotter.constants;

/**
 * @name SearchType
 * @author jml04
 * @version 1.0
 *
 * enum SearchType contains all search types, with string-names
 * has a static method byItem() to get an item by string
 */
public enum SearchType {
    AIRLINE("Airline"),
    AIRPORT("Airport"),
    FLIGHT("Flight"),
    PLANE("Plane"),
    AREA("Area");
    // item string
    private final String item;
    // private enum constructor
    SearchType(String item) {
        this.item = item;
    }

    /**
     * returns a specific SearchType by string, if there is one
     *
     * @param item is the input item string
     * @return enum constant by item if one exists, else null
     */
    public static SearchType byItemString(String item) {
        return switch (item) {
            case "Flight" -> FLIGHT;
            case "Plane" -> PLANE;
            case "Airline" -> AIRLINE;
            case "Airport" -> AIRPORT;
            case "Area" -> AREA;
            default -> null;
        };
    }
}

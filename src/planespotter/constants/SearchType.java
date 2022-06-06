package planespotter.constants;

/**
 *
 */
public enum SearchType {
    AIRLINE("Airline"),
    AIRPORT("Airport"),
    FLIGHT("Flight"),
    PLANE("Plane"),
    AREA("Area");
    // item string
    private final String item;
    // constructor
    SearchType(String item) {
        this.item = item;
    }

    /**
     * returns a specific SearchType by string, if there is one
     *
     * @param item
     * @return
     */
    public static SearchType byItem(String item) {
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

package planespotter.constants.props;

public class UserProperties {

    private final int dataLimit, gridSizeLat, gridSizeLon;
    private final String currentMapSource;

    public UserProperties(int dataLimit, String currentMapSource, int gridSizeLat, int gridSizeLon) {
        this.dataLimit = dataLimit;
        this.currentMapSource = currentMapSource;
        this.gridSizeLat = gridSizeLat;
        this.gridSizeLon = gridSizeLon;
    }

    UserProperties(Property dataLimit, Property mapSource, Property lat, Property lon) {
        this((int) dataLimit.val, (String) mapSource.val, (int) lat.val, (int) lon.val);
    }

    public int dataLimit() {
        return dataLimit;
    }

    public int gridSizeLat() {
        return gridSizeLat;
    }

    public int gridSizeLon() {
        return gridSizeLon;
    }

    public String mapSource() {
        return currentMapSource;
    }

    public Property[] toArray() {
        return new Property[] {
                new Property("dataLimit", dataLimit),
                new Property("currentMapSource", currentMapSource),
                new Property("gridSizeLat", gridSizeLat),
                new Property("gridSizeLon", gridSizeLon)
        };
    }
}

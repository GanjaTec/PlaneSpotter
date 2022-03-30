package planespotter.dataclasses;

public class Airport {
    private int id;
    private String name;
    private AirportType type;

    public Airport (int id, String name, AirportType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

}

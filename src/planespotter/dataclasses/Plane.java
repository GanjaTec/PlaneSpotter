package planespotter.dataclasses;

/**
 * Plane class represents Plane Object
 */
public class Plane {

    private int id;
    private String tailnr;
    private String planetype;
    private String registration;
    private Airline airline;

    public Plane (int id, String tailnr, String planetype, String registration, Airline airline) {
        this.id = id;
        this.tailnr = tailnr;
        this.planetype = planetype;
        this.registration = registration;
        this.airline = airline;
    }

}

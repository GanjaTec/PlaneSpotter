package planespotter.dataclasses;

/**
 * Plane class represents Plane Object
 */
public class Plane {

    private int id;
    private String icao;
    private String tailnr;
    private String planetype;
    private String registration;
    private Airline airline;

    public Plane (int id, String icao, String tailnr, String planetype, String registration, Airline airline) {
        this.id = id;
        this.icao = icao;
        this.tailnr = tailnr;
        this.planetype = planetype;
        this.registration = registration;
        this.airline = airline;
    }

    /**
     * getter
     * TODO: return the Plane attributes
     */
    public int getID () { return id; }

    public String getIcao () { return icao; }

    public String getTailnr () { return tailnr; }

    public String getPlanetype () { return planetype; }

    public String getRegistration () { return registration; }

    public Airline getAirline () { return airline; }

}

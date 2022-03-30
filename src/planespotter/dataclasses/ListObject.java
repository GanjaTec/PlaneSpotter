package planespotter.dataclasses;


import planespotter.Controller;

public class ListObject {

    private final DataPoint data;
    private final String title;

    public ListObject (DataPoint p) {
        data = p;
        int fid = data.getFlightID();
        title = "FlightID.: " + fid + ", Airline: " + "// airline";
    }

    /**
     * getter
     */
    public String getTitle () { return title; }

}

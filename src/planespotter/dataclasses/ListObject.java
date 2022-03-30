package planespotter.dataclasses;

import planespotter.dataclasses.DataPoint;

import javax.swing.*;

public class ListObject {

    private final DataPoint data;
    private final String title;

    public ListObject (DataPoint p) {
        data = p;
        title = "FlightID.: " + data.getFlightID() + ", Airline: " + "//DBOut.getFlight(data.getFlightID()).getPlane().getAirline()";
    }

    /**
     * getter
     */
    public String getTitle () { return title; }

}

package planespotter.model;

import planespotter.dataclasses.Flight;
import planespotter.throwables.DataNotFoundException;

import java.util.List;

/**
 * @name Search
 * @author jml04
 * @version 1.0
 *
 * class search contains search methods for flights, planes, airports, airlines and areas
 */
public class Search {

    /**
     * constructor
     */
    public Search () {
    }

    /**
     * verifies a flight from the flight search
     *
     * @param inputs are the input strings
     */
    public Flight verifyFlight (String[] inputs) throws DataNotFoundException {
        String id = inputs[0];
        String callsign = inputs[1];
        try {
            if (!id.isBlank()) {
                int fid = Integer.parseInt(id);
                return new DBOut().getFlightByID(fid);
            } else if (!callsign.isBlank()) {
                return new DBOut().getFlightsByCallsign(callsign).get(0);
            }
        } catch (DataNotFoundException e) {
        }
        throw new DataNotFoundException("please enter input!");
    }

    /**
     * verifies a plane from the plane search
     *
     * @param inputs are the input strings
     */
    public Flight verifyPlane (String[] inputs) throws DataNotFoundException {
        String id = inputs[0];
        String callsign = inputs[1];
        try {
            if (!id.isBlank()) {
                int fid = Integer.parseInt(id);
                return new DBOut().getFlightByID(fid);
            } else if (!callsign.isBlank()) {
                return new DBOut().getFlightsByCallsign(callsign).get(0);
            }
        } catch (DataNotFoundException e) {
        }
        throw new DataNotFoundException("please enter input!");
    }

}

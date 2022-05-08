package planespotter.model;

import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.throwables.DataNotFoundException;

import java.util.*;

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
    public List<DataPoint> verifyFlight (String[] inputs) throws DataNotFoundException {
        var id = inputs[0];
        var callsign = inputs[1];
        try {
            var out = new DBOut();
            if (!id.isBlank()) {
                int fid = Integer.parseInt(id);
                var list = out.getTrackingByFlight(fid);
                return list;
            } else if (!callsign.isBlank()) {
                var signs = this.findCallsigns(callsign);
                Controller.loadedData = new ArrayList<>();
                var fids = new ArrayDeque<Integer>();
                while (!signs.isEmpty()) {
                    var ids = out.getFlightIDsByCallsign(signs.poll());
                    fids.addAll(ids);
                }
                int counter = 0;
                for (int i : fids) {
                    Controller.loadedData.addAll(out.getTrackingByFlight(i));
                    if (counter++ > 10) break; // MAX 10 FLIGHTS
                }
                if (!Controller.loadedData.isEmpty()) {
                    return Controller.loadedData;
                } else {
                    throw new DataNotFoundException("No flight found for callsign " + callsign + "!");
                }
            }
        } catch (DataNotFoundException ignored) {
            ignored.printStackTrace();
        }
        throw new DataNotFoundException("no data found! - loadedData is empty");
    }

    /**
     * verifies a plane from the plane search
     *
     * @param inputs are the input strings
     */
    public List<DataPoint> verifyPlane (String[] inputs) throws DataNotFoundException {
        var id = inputs[0];
        var planetypes = this.findPlanetypes(inputs[1]); // FIXME: 06.05.2022 ES WERDEN RANDOM planetypes zurückgegeben
        var icao = inputs[2]; // find ICAOs
        var tailNr = inputs[3]; // find TailNr
        // TODO registration
        var out = new DBOut();
        var fids = new ArrayDeque<Integer>();
        if (!id.isBlank()) {
            fids.add(Integer.parseInt(id));
        } else if (!planetypes.isEmpty()) {
            fids = out.getFlightIDsByPlaneTypes(planetypes);
        } else if (!icao.isBlank()) {
            fids.addAll(out.getPlaneIDsByICAO(icao)); // dont add plane ids but flight ids
        } else if (!tailNr.isBlank()) {
            fids.addAll(out.getPlaneIDsByTailNr(tailNr)); // dont add plane ids but flight ids
        }
        if (fids.isEmpty()) {
            throw new DataNotFoundException("no data found! / no input!");
        }
        // TODO ist noch sehr langsam
        var list = new ArrayList<DataPoint>();
        list.addAll(out.getLastTrackingsByFlightIDs(fids)); // working???
        return list;
    }

    /**
     * finds a planetype
     *
     * @param input is the input planetype, must not be complete
     * @return complete planetype, if one found, else the input one
     */
    private ArrayDeque<String> findPlanetypes (String input) throws DataNotFoundException {
        var allPlanetypes = new DBOut().getAllPlanetypesLike(input);
        if (allPlanetypes.isEmpty()) {
            throw new DataNotFoundException("no existing planetype found for " + input + "!");
        }
        return allPlanetypes;
    }

    private ArrayDeque<String> findCallsigns (String input) throws DataNotFoundException {
        var allCallsigns = new DBOut().getAllCallsignsLike(input);
        if (allCallsigns.isEmpty()) {
            throw new DataNotFoundException("no existing callsign found for " + input + "!");
        }
        return allCallsigns;
    }

}

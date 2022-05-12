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
                var controller = Controller.getInstance();
                var signs = this.findCallsigns(callsign);
                controller.loadedData = new ArrayList<>();
                var fids = new ArrayDeque<Integer>();
                while (!signs.isEmpty()) {
                    fids.addAll(out.getFlightIDsByCallsign(signs.poll()));
                }
                int counter = 0;
                for (int i : fids) {
                    controller.loadedData.addAll(out.getTrackingByFlight(i));
                    if (counter++ > 10) break; // MAX 10 FLIGHTS
                }
                if (!controller.loadedData.isEmpty()) {
                    return controller.loadedData;
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
        var id = inputs[0]; // FIXME: 11.05.2022 ID ist 5 statt 56
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
        return new ArrayList<DataPoint>(out.getLastTrackingsByFlightIDs(fids)); // working???
    }

    public List<DataPoint> verifyAirport (String[] inputs) throws DataNotFoundException {
        var id = inputs[0];
        var tag = inputs[1];
        var name = inputs[2];
        var out = new DBOut();
        var dps = new ArrayList<DataPoint>();
        ArrayDeque<Integer> aids;
        if (!id.isBlank()) {

        } else if (!tag.isBlank() || !name.isBlank()) {
            aids = this.findAirports(Objects.requireNonNullElse(tag, name));
            dps.addAll(out.getLastTrackingsByFlightIDs(aids));
        }
        if (dps.isEmpty()) {
            throw new DataNotFoundException("No airports found for these inputs!");
        }
        return dps;
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

    private ArrayDeque<String> findCallsigns (String input) throws DataNotFoundException { // TODO evtl. hier catchen!! in DBout Werfen
        var allCallsigns = new DBOut().getAllCallsignsLike(input);
        if (allCallsigns.isEmpty()) {
            throw new DataNotFoundException("no existing callsign found for " + input + "!");
        }
        return allCallsigns;
    }

    private ArrayDeque<Integer> findAirports (String airport) throws DataNotFoundException {
        var aids = new DBOut().getAirportIDsLike(airport);
        if (aids.isEmpty()) {
            throw new DataNotFoundException("No existing airport found for " + airport + "!");
        }
        return aids;
    }

}

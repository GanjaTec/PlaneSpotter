package planespotter.model;

import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.model.io.DBOut;
import planespotter.throwables.DataNotFoundException;

import java.util.*;

/**
 * @name Search
 * @author jml04
 * @version 1.0
 *
 * class search contains search methods for flights, planes, airports, airlines and areas
 * it gets its parameters from other classes like Controller and tries to return search results.
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
    public Vector<DataPoint> verifyFlight(String[] inputs)
            throws DataNotFoundException {

        var id = inputs[0];
        var callsign = inputs[1];
        try {
            var out = new DBOut();
            var ctrl = Controller.getInstance();
            if (!id.isBlank()) {
                int fid = Integer.parseInt(id);
                ctrl.loadedData = out.getTrackingByFlight(fid);
                return ctrl.loadedData;
            } else if (!callsign.isBlank()) {
                var signs = this.findCallsigns(callsign);
                var fids = new ArrayDeque<Integer>();
                while (!signs.isEmpty()) {
                    fids.addAll(out.getFlightIDsByCallsign(signs.poll()));
                }
                int counter = 0;
                var key = "tracking" + fids;
                ctrl.loadedData = (Vector<DataPoint>) Controller.cache.get(key);
                if (ctrl.loadedData == null) {
                    ctrl.loadedData = new Vector<>();
                    for (int i : fids) {
                        ctrl.loadedData.addAll(out.getTrackingByFlight(i)); // TODO trackingByFlightIDs
                        if (counter++ > 20) break; // MAX 10 FLIGHTS
                    }
                    Controller.cache.put(key, ctrl.loadedData);
                }
                if (!ctrl.loadedData.isEmpty()) {
                    return ctrl.loadedData;
                } else {
                    throw new DataNotFoundException("No flight found for callsign " + callsign + "!");
                }
            }
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        throw new DataNotFoundException("no data found! - loadedData is empty");
    }

    /**
     * verifies a plane from the plane search
     *
     * @param inputs are the input strings
     */
    public Vector<DataPoint> verifyPlane(String[] inputs)
            throws DataNotFoundException {

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
            throw new DataNotFoundException("no data found / no input at Search::verifyPlane!");
        }
        // TODO ist noch sehr langsam
        return new Vector<>(out.getLastTrackingsByFlightIDs(fids)); // working???
    }

    /**
     * verifies an airport by input strings
     *
     * @param inputs are the input strings [id,tag,name]
     * @return Vector of all DataPoints containing a flight with the input airport
     * @throws DataNotFoundException if no airport or no flights where found
     */
    // TODO: 25.05.2022 Airport suche mit Start und Ziel Airport
    public Vector<DataPoint> verifyAirport(String[] inputs)
            throws DataNotFoundException {

        var id = inputs[0];
        var tag = inputs[1];
        var name = inputs[2];
        var out = new DBOut();
        var ctrl = Controller.getInstance();
        if (!id.isBlank()) {
            // trackings with airport id (-> airport join)
        } else if (!tag.isBlank()) {
            var key = "airport" + tag.toUpperCase();
            ctrl.loadedData = (Vector<DataPoint>) Controller.cache.get(key);
            if (ctrl.loadedData == null) {
                ctrl.loadedData = new Vector<>(out.getTrackingsWithAirportTag(tag));
                Controller.cache.put(key, ctrl.loadedData);
            }
        } else if (!name.isBlank()) {
            //ctrl.loadedData.addAll( ); (-> airport join)
        }
        if (ctrl.loadedData.isEmpty()) {
            throw new DataNotFoundException("No airports found for these inputs!");
        }
        return ctrl.loadedData;
    }

    /**
     * finds a planetype
     *
     * @param input is the input planetype, must not be complete
     * @return complete planetype, if one found, else the input one
     */
    private ArrayDeque<String> findPlanetypes(String input)
            throws DataNotFoundException {

        var allPlanetypes = new DBOut().getAllPlanetypesLike(input);
        if (allPlanetypes.isEmpty()) {
            throw new DataNotFoundException("no existing planetype found for " + input + "!");
        }
        return allPlanetypes;
    }

    /**
     * finds a callsign
     *
     * @param input is the callsign to search for
     * @return Deque of all callsigns found for the input string
     * @throws DataNotFoundException if no callsign was found
     */
    private ArrayDeque<String> findCallsigns(String input)
            throws DataNotFoundException { // TODO evtl. hier catchen!! in DBout Werfen

        var allCallsigns = new DBOut().getAllCallsignsLike(input);
        if (allCallsigns.isEmpty()) {
            throw new DataNotFoundException("no existing callsign found for " + input + "!");
        }
        return allCallsigns;
    }

    /**
     * finds a airport
     *
     * @param airport is the airport to search for
     * @return id of the found airport, if one is found
     * @throws DataNotFoundException if no airport was found
     */
    private int findAirport(String airport)
            throws DataNotFoundException {

        var aids = new DBOut().getAirportIDsLike(airport); // TODO evtl ohne LIKE für performance
        assert aids != null;
        if (aids.isEmpty()) {
            throw new DataNotFoundException("No existing airport found for " + airport + "!");
        }
        return aids.getFirst();
    }

}

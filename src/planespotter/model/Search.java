package planespotter.model;

import org.jetbrains.annotations.NotNull;
import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.model.io.DBOut;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.InvalidArrayException;
import planespotter.util.LRUCache;
import planespotter.util.Utilities;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @name Search
 * @author jml04
 * @version 1.0
 *
 * class search contains search methods for flights, planes, airports, airlines and areas
 * it gets its parameters from other classes like Controller and tries to return search results.
 */
public class Search {

    // search cache for data recycling, but still in development
    private final LRUCache<@NotNull String, Vector<DataPoint>> searchCache;

    /**
     * constructor, equivalent to 'new Search(100)'
     */
    public Search() {
        this(100);
    }

    /**
     * constructor
     *
     * @param cacheSize is the cache size
     */
    public Search(int cacheSize) {
        this.searchCache = new LRUCache<>(cacheSize, false);
    }

    /**
     * verifies a flight from the flight search
     *
     * @param inputs are the input strings
     */
    public Vector<DataPoint> forFlight(String[] inputs)
            throws DataNotFoundException {

        DataNotFoundException ex = new DataNotFoundException("No flights found!");
        String id = inputs[0];
        String callsign = inputs[1];
        try {
            Controller ctrl = Controller.getInstance();
            DBOut out = DBOut.getDBOut();
            if (!id.isBlank()) {
                int fid = Integer.parseInt(id);
                ctrl.setDataList(out.getTrackingByFlight(fid));
                return ctrl.getDataList();
            } else if (!callsign.isBlank()) {
                Deque<String> signs = this.findCallsigns(callsign);
                Deque<Integer> fids = new ArrayDeque<>();
                while (!signs.isEmpty()) {
                    fids.addAll(out.getFlightIDsByCallsign(signs.poll()));
                }
                int[] ids = Utilities.parseIntArray(fids);
                String key = "tracking" + Arrays.toString(ids);
                ctrl.setDataList(this.searchCache.get(key));
                if (ctrl.getDataList() == null) {
                    ctrl.setDataList(out.getTrackingsByFlightIDs(ids));
                    this.searchCache.put(key, ctrl.getDataList());
                }
                if (ctrl.getDataList().isEmpty()) {
                    ex = new DataNotFoundException("No flight found for callsign " + callsign + "!");
                } else {
                    return ctrl.getDataList();
                }
            }
        } catch (DataNotFoundException ignored) {
        }
        throw ex;
    }

    /**
     * verifies a plane from the plane search
     *
     * @param inputs are the input strings
     */
    public Vector<DataPoint> forPlane(String[] inputs)
            throws DataNotFoundException {

        var id = inputs[0]; // FIXME: 11.05.2022 ID ist 5 statt 56
        Deque<String> planetypes = this.findPlanetypes(inputs[1]); // FIXME: 06.05.2022 ES WERDEN RANDOM planetypes zurückgegeben
        var icao = inputs[2]; // find ICAOs
        var tailNr = inputs[3]; // find TailNr
        // TODO registration
        var out = DBOut.getDBOut();
        Deque<Integer> fids = new ArrayDeque<>();
        if (!id.isBlank()) {
            fids.add(Integer.parseInt(id));
        } else if (!planetypes.isEmpty()) {
            fids = out.getFlightIDsByPlaneTypes(planetypes);
        } else if (!icao.isBlank()) {
            fids.addAll(out.getPlaneIDsByICAO(icao)); // TODO dont add plane ids but flight ids
        } else if (!tailNr.isBlank()) {
            fids.addAll(out.getPlaneIDsByTailNr(tailNr)); // TODO dont add plane ids but flight ids
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
    public Vector<DataPoint> forAirport(String[] inputs)
            throws DataNotFoundException {

        var id = inputs[0];
        var tag = inputs[1];
        var name = inputs[2];
        var out = DBOut.getDBOut();
        var ctrl = Controller.getInstance();
        if (!id.isBlank()) {
            // trackings with airport id (-> airport join)
        } else if (!tag.isBlank()) {
            var key = "airport" + tag.toUpperCase();
            ctrl.setDataList(this.searchCache.get(key));
            if (ctrl.getDataList() == null) {
                int[] fids = out.getFlightIDsByAirportTag(tag);
                ctrl.setDataList(new Vector<>(out.getTrackingsByFlightIDs(fids)));
                this.searchCache.put(key, ctrl.getDataList());
            }
        } else if (!name.isBlank()) {
            var key = "airport" + name.toUpperCase();
            ctrl.setDataList(this.searchCache.get(key));
            if (ctrl.getDataList() == null) {
                // FIXME too slow
                int[] fids = out.getFlightIDsByAirportName(name);
                ctrl.setDataList(new Vector<>(out.getTrackingsByFlightIDs(fids)));
                this.searchCache.put(key, ctrl.getDataList());
            }
        }
        if (ctrl.getDataList() == null || ctrl.getDataList().isEmpty()) {
            throw new DataNotFoundException("No airports found for these inputs!");
        }
        return ctrl.getDataList();
    }

    public Vector<DataPoint> forAirline(@NotNull String[] inputs)
            throws DataNotFoundException {

        if (inputs.length != 4) {
            throw new InvalidArrayException("Array length must be 4!");
        }
        String id = inputs[0],
               tag = inputs[1],
               name = inputs[2],
               country = inputs[3];
        DBOut dbOut = DBOut.getDBOut();
        Vector<DataPoint> data;
        int idInt = -1;
        int[] fids = new int[0];
        try {
            idInt = Integer.parseInt(id);
        } catch (NumberFormatException ignored) {
        }

        if (idInt != -1) {
            fids = dbOut.getFlightIDsByAirlineID(idInt);
        } else if (!tag.isBlank()) {
            fids = dbOut.getFlightIDsByAirlineTag(tag);
        } else if (!name.isBlank()) {
            fids = dbOut.getFlightIDsByAirlineName(name);
        } else if (!country.isBlank()) {
            fids = dbOut.getFlightIDsByAirlineCountry(country);
        }
        data = dbOut.getTrackingsByFlightIDs(fids);

        if (data.isEmpty()) {
            throw new DataNotFoundException("No airports found for these inputs!");
        }
        return data;
    }

    /**
     * finds a planetype
     *
     * @param input is the input planetype, must not be complete
     * @return complete planetype, if one found, else the input one
     */
    @NotNull
    private Deque<String> findPlanetypes(@NotNull String input)
            throws DataNotFoundException {

        Deque<String> allPlanetypes = DBOut.getDBOut().getAllPlanetypesLike(input);
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
    @NotNull
    private Deque<String> findCallsigns(@NotNull String input)
            throws DataNotFoundException { // TODO evtl. hier catchen!! in DBout Werfen

        Deque<String> allCallsigns = DBOut.getDBOut().getAllCallsignsLike(input);
        if (allCallsigns.isEmpty()) {
            throw new DataNotFoundException("no existing callsign found for " + input + "!");
        }
        return allCallsigns;
    }

}

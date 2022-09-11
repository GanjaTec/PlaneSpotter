package planespotter.model;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.DataPoint;
import planespotter.model.io.DBOut;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.InvalidArrayException;
import planespotter.util.LRUCache;
import planespotter.util.Utilities;

import java.util.*;

/**
 * @name Search
 * @author jml04
 * @version 1.0
 *
 * @description
 * class search contains search methods for flights, planes, airports, airlines and areas
 * it gets its parameters from other classes like Controller and tries to return search results.
 */
public class Search {

    // search cache for data recycling, but still in development
    private final LRUCache<@NotNull String, Vector<DataPoint>> searchCache;

    // flight count from current search query
    private int currentFlightCount;

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
        this.currentFlightCount = 0;
    }

    /**
     * verifies a flight from the flight search
     *
     * @param inputs are the input strings
     */
    public Vector<DataPoint> forFlight(String[] inputs) throws DataNotFoundException {

        DataNotFoundException ex = new DataNotFoundException("No flights found!");
        String id = inputs[0];
        String callsign = inputs[1];
        try {
            DBOut out = DBOut.getDBOut();
            if (!id.isBlank()) {
                int fid = Integer.parseInt(id);
                currentFlightCount = 1;
                return out.getTrackingByFlight(fid);
            } else if (!callsign.isBlank()) {
                Deque<String> signs = findCallsigns(callsign);
                Deque<Integer> fids = new ArrayDeque<>();
                while (!signs.isEmpty()) {
                    fids.addAll(out.getFlightIDsByCallsign(signs.poll()));
                }
                int[] ids = Utilities.parseIntArray(fids);
                currentFlightCount = ids.length;
                String key = "flight." + callsign;
                Vector<DataPoint> data = searchCache.get(key);
                if (data == null) {
                    data = out.getTrackingsByFlightIDs(ids);
                    searchCache.put(key, data);
                }
                if (data.isEmpty()) {
                    ex = new DataNotFoundException("No flight found for callsign " + callsign + "!");
                } else {
                    return data;
                }
            }
        } catch (DataNotFoundException dnf) {
            ex = dnf;
        }
        throw ex;
    }

    /**
     * starts a DB-search for a specific {@link planespotter.dataclasses.Plane}
     *
     * @param inputs are the input strings
     */
    public Vector<DataPoint> forPlane(String[] inputs) throws DataNotFoundException {

        String id = inputs[0];
        String type = inputs[1];
        String icao = inputs[2]; // find ICAOs
        String tailNr = inputs[3]; // find TailNr
        // TODO registration
        DBOut dbOut = DBOut.getDBOut();
        int[] fids = null;
        if (id != null && !id.isBlank()) {
            fids = new int[] { Integer.parseInt(id) };
        } else if (type != null && !type.isBlank()) {
            Deque<String> planetypes = dbOut.getAllPlanetypesLike(inputs[1]);
            fids = dbOut.getFlightIDsByPlaneTypes(planetypes);
        } else if (!icao.isBlank()) {
            fids = dbOut.getFlightIDsByICAOLike(icao);
        } else if (!tailNr.isBlank()) {
            fids = dbOut.getFlightIDsByTailNrLike(tailNr);
        }
        if (fids == null || fids.length == 0) {
            throw new DataNotFoundException("no data found / no input at Search::verifyPlane!");
        }
        return dbOut.getTrackingsByFlightIDs(fids);
    }

    /**
     * starts a DB-search for an {@link planespotter.dataclasses.Airport}
     *
     * @param inputs are the input strings [id,tag,name]
     * @return Vector of all DataPoints containing a flight with the input airport
     * @throws DataNotFoundException if no airport or no flights where found
     */
    // TODO: 25.05.2022 Airport suche mit Start und Ziel Airport
    @NotNull
    public Vector<DataPoint> forAirport(String[] inputs) throws DataNotFoundException {

        String id = inputs[0];
        String tag = inputs[1];
        String name = inputs[2];
        DBOut out = DBOut.getDBOut();
        Vector<DataPoint> data = null;
        String key;
        if (!id.isBlank()) {
            // trackings with airport id (-> airport join)
        } else if (!tag.isBlank()) {
            key = "airport" + tag.toUpperCase();
            data = searchCache.get(key);
            if (data == null) {
                int[] fids = out.getFlightIDsByAirportTag(tag);
                data = out.getTrackingsByFlightIDs(fids);
                searchCache.put(key, data);
            }
        } else if (!name.isBlank()) {
            key = "airport" + name.toUpperCase();
            data = searchCache.get(key);
            if (data == null) {
                // FIXME too slow
                int[] fids = out.getFlightIDsByAirportName(name);
                data = out.getTrackingsByFlightIDs(fids);
                searchCache.put(key, data);
            }
        }
        if (data == null || data.isEmpty()) {
            throw new DataNotFoundException("No airports found for these inputs!");
        }
        return data;
    }

    /**
     * starts a DB-search for an {@link planespotter.dataclasses.Airline}
     *
     * @param inputs are the input {@link String}s to search for
     * @return {@link Vector} of {@link DataPoint}s, all tracking points with that airlines
     * @throws DataNotFoundException if no {@link planespotter.dataclasses.Airline} or
     *                               {@link planespotter.dataclasses.Flight} was found
     */
    @NotNull
    public Vector<DataPoint> forAirline(@NotNull String[] inputs) throws DataNotFoundException {

        if (inputs.length != 4) {
            throw new InvalidArrayException("Array length must be 4!");
        }
        String      id = inputs[0],
                   tag = inputs[1],
                  name = inputs[2],
               country = inputs[3];
        DBOut dbOut = DBOut.getDBOut();
        int idInt = -1;
        int[] fids = new int[0];
        try {
            idInt = Integer.parseInt(id);
        } catch (NumberFormatException ignored) {
        }

        // TODO: 09.09.2022 LIKE bei allen

        if (idInt != -1) {
            fids = dbOut.getFlightIDsByAirlineID(idInt);
        } else if (!tag.isBlank()) {
            fids = dbOut.getFlightIDsByAirlineTag(tag);
        } else if (!name.isBlank()) {
            fids = dbOut.getFlightIDsByAirlineName(name);
        } else if (!country.isBlank()) {
            fids = dbOut.getFlightIDsByAirlineCountry(country);
        }

        if (fids.length == 0) {
            throw new DataNotFoundException("No airports found for these inputs!");
        }
        return dbOut.getTrackingsByFlightIDs(fids);
    }

    /**
     * finds a callsign with a LIKE statement
     *
     * @param input is the callsign to search for
     * @return Deque of all callsigns found for the input string
     * @throws DataNotFoundException if no callsign was found
     */
    @NotNull
    private Deque<String> findCallsigns(@NotNull String input)
            throws DataNotFoundException {

        Deque<String> allCallsigns = DBOut.getDBOut().getAllCallsignsLike(input);
        if (allCallsigns.isEmpty()) {
            throw new DataNotFoundException("no existing callsign found for " + input + "!");
        }
        return allCallsigns;
    }

    /**
     * getter for current {@link planespotter.dataclasses.Flight} count
     *
     * @return flight count from current search query
     */
    public int getCurrentFlightCount() {
        return currentFlightCount;
    }
}

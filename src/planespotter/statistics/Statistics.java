package planespotter.statistics;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Airport;
import planespotter.dataclasses.Position;
import planespotter.throwables.DataNotFoundException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @name Statistics
 * @author jml04
 * @version 1.0
 *
 * class Statistics is responsible for creating of statistics
 */
public class Statistics {

    /**
     *
     */
    public Statistics() {
    }

    /**
     * creates a heat map for airport significance,
     * one entry has an airport and a level (significance)
     *
     * @param airports are the input flights, more flights-> better results
     * @return HashMap with <String tag = airport tag, Integer level = significance>
     */
    public final Map<Airport, Integer> airportSignificance(@NotNull Deque<Airport> airports) {
        var asignf = new HashMap<Airport, Integer>();
        var val = new AtomicInteger();
        airports.forEach(a -> {
            if (asignf.containsKey(a)) {
                val.set(asignf.get(a));
                asignf.replace(a, val.get() + 1);
            } else {
                asignf.put(a, 1);
            }
        });
        return asignf;
    }

    // TODO change name
    public final HashMap<Position, Integer> positionHeatMap(@NotNull final Vector<Position> positions) {
        int size = (int) positions.stream()
                            .distinct()
                            .count();
        final var heatMap = new HashMap<Position, Integer>(size);
        Set<Position> keySet;
        int currentLvl;
        for (var pos : positions) {
            for (int i = 0; i < size; i++) {
                keySet = heatMap.keySet();
                var nearOf = this.containsKeyNearOf(keySet, pos);
                if (nearOf == null) {
                    heatMap.put(pos, 1);
                } else {
                    currentLvl = heatMap.get(nearOf);
                    heatMap.replace(nearOf, currentLvl + 1);
                }
            }
        }
        if (heatMap.isEmpty()) {
            try {
                throw new DataNotFoundException("heat map is empty, check the inputs!", true);
            } catch (DataNotFoundException e) {
                e.printStackTrace();
            }
        }
        return heatMap;
    }

    private Position containsKeyNearOf(@NotNull Set<Position> positionSet, @NotNull Position key) {
        for (var pos : positionSet) {
            double tolerance = 0.03;
            if (       (key.lat() < pos.lat() + tolerance)
                    && (key.lat() > pos.lat() - tolerance)
                    && (key.lon() < pos.lon() + tolerance)
                    && (key.lon() > pos.lon() - tolerance)) {
                return pos;
            }
        }
        return null;
    }

}

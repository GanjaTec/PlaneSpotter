package planespotter.model;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Airport;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {

    public Statistics () {
    }

    /**
     *
     * @param airports are the input flights, more flights-> better results
     * @return HashMap with <String tag = airport tag, Integer level = significance>
     */
    public HashMap<Airport, Integer> airportSignificance (@NotNull ArrayDeque<Airport> airports) {
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

}

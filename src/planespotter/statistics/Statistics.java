package planespotter.statistics;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import planespotter.controller.Controller;
import planespotter.dataclasses.Airport;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Position;
import planespotter.throwables.InvalidDataException;
import planespotter.util.math.Vector2D;
import planespotter.model.io.DBOut;
import planespotter.throwables.DataNotFoundException;
import planespotter.util.Utilities;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static planespotter.util.math.MathUtils.*;

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

    public static <K, V extends Number> CategoryDataset createMapBarDataset(@NotNull Map<String, Map<K, V>> stats) {
        var dataset = new DefaultCategoryDataset();
        // TODO: 16.06.2022 dataset has incrementValue()
        stats.keySet().forEach(rowKey -> {
            var map = (Map<K, V>) stats.get(rowKey);
            map.keySet()
                    .forEach(key -> dataset.addValue(map.get(key), rowKey, String.valueOf(key)));
        });
        return dataset;
    }

    public static <K, V extends Number> CategoryDataset createBarDataset(@NotNull Map<K, V> stats) {
        var dataset = new DefaultCategoryDataset();
        // TODO: 16.06.2022 dataset has incrementValue()
        stats.keySet()
                .forEach(key -> dataset.addValue(stats.get(key), "Row-Key", String.valueOf(key)));

        return dataset;
    }

    @SuppressWarnings(value = "Wrong output!")
    public JFreeChart topAirports(int limit) {
        var dbOut = DBOut.getDBOut();
        Deque<String> airportTags;
        try {
            airportTags = dbOut.getAllAirportTags();
        } catch (DataNotFoundException e) {
            Controller.getInstance().handleException(e);
            return null;
        }
        var apStats = this.onlySignificant(this.tagCount(airportTags), 10);
        // filtering top airports
        var sortedMap = new HashMap<String, Integer>();
        apStats.entrySet()
                .stream()
                .sorted((a, b) -> Math.max(a.getValue(), b.getValue()))
                .limit(limit)
                .forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        var dataset = Statistics.createBarDataset(sortedMap);
        return ChartFactory.createBarChart("Top-Airports", "Airports", "Flight-Count",
                                            dataset, PlotOrientation.HORIZONTAL, true, true, false);
    }

    public Map<Position, double[]> flightHeadwind(int flightID) {
        var dbOut = DBOut.getDBOut();
        try {
            var tracking = dbOut.getTrackingByFlight(flightID);
            var map = new HashMap<Position, double[]>();

            Position current, last;
            Vector2D vector;
            double[] values;
            long tsCurrent, tsLast;
            double km, tDiffHrs;
            int counter = 0;

            var firstDP = tracking.get(0);
            last = firstDP.pos();
            tsLast = firstDP.timestamp();
            values = new double[] { firstDP.speed(), -1. };
            map.put(last, values);

            for (var dp : tracking) {
                current = dp.pos();
                tsCurrent = dp.timestamp();
                if (counter > 0) {
                    vector = Vector2D.ofDegrees(last, current);
                    values[0] = dp.speed();
                    km = abs(vector);
                    tDiffHrs = timeDiff(tsCurrent, tsLast, TimeUnit.SECONDS, TimeUnit.HOURS);
                    if (tDiffHrs != 0.) {
                        values[1] = divide(km, tDiffHrs) * divide(1., tDiffHrs);
                        map.put(current, values);
                    }
                }
                tsLast = tsCurrent;
                last = current;
                counter++;
            }
            return map;

        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        throw new InvalidDataException("Couldn't calculate headwind values, check input!");
    }

    public Map<String, Integer> onlySignificant(@NotNull final Map<String, Integer> inputMap, final int minValue) {
        var map = new HashMap<String, Integer>();
        var keys = inputMap.keySet();
        keys.forEach(key -> {
            var val = inputMap.get(key);
            if (val > minValue) {
                map.put(key, val);
            }
        });
        return map;
    }

    public Map<String, Integer> tagCount(@NotNull final Deque<String> tags) {
        var map = new HashMap<String, Integer>();
        tags.forEach(tag -> {
            if (map.containsKey(tag)) {
                map.replace(tag, map.get(tag) + 1);
            } else if (!tag.isBlank()) {
                map.put(tag, 1);
            }
        });
        return map;
    }

    @SafeVarargs
    public final Map<String, Map<Long, Integer>> windSpeed(final Position topLeft, final Position bottomRight, Deque<DataPoint>... dataPoints) {
        var maps = new HashMap<String, Map<Long, Integer>>();
        var counter = new AtomicInteger(0);
        var dbOut = DBOut.getDBOut();
        for (int i = 0; i < dataPoints.length; i++) {
            var dpArr = Utilities.parseDataPointArray(dataPoints[counter.getAndIncrement()]);
            var fid = dpArr[0].flightID();
            try {
                var key = dbOut.getFlightByID(fid).callsign();
                maps.put(key, new HashMap<>());
                Arrays.stream(dpArr)
                        .filter(dp -> Utilities.fitArea(dp.pos(), topLeft, bottomRight))
                        .forEach(dp -> maps.get(key).put(dp.timestamp(), Utilities.knToKmh(dp.speed())));
            } catch (DataNotFoundException e) {
                e.printStackTrace();
            }
        }
        // TODO: 17.06.2022 returnt bisher nur timestamp mit speed
        return maps;
    }

    public JFreeChart airlineSignificance(int minCount) {
        var dbOut = DBOut.getDBOut();
        Deque<String> airlineTags = null;
        try {
            airlineTags = dbOut.getAllAirlineTags();
        } catch (DataNotFoundException e) {
            Controller.getInstance().handleException(e);
        }
        assert airlineTags != null;
        var airlStats = this.onlySignificant(this.tagCount(airlineTags), minCount);
        var dataset = Statistics.createBarDataset(airlStats);
        return ChartFactory.createBarChart("Airline-Significance", "Airlines", "Flight-Count",
                                            dataset, PlotOrientation.HORIZONTAL, true, true, false);
    }

    public JFreeChart airportSignificance(int minCount) {
        var dbOut = DBOut.getDBOut();
        Deque<String> airportTags = null;
        try {
            airportTags = dbOut.getAllAirportTags();
        } catch (DataNotFoundException e) {
            Controller.getInstance().handleException(e);
        }
        assert airportTags != null;
        var apStats = this.onlySignificant(this.tagCount(airportTags), minCount);
        var dataset = Statistics.createBarDataset(apStats);
        return ChartFactory.createBarChart("Airport-Significance", "Airports", "Flight-Count",
                                            dataset, PlotOrientation.HORIZONTAL, true, true, false);
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
        int size = Utilities.asInt(positions.stream()
                            .distinct()
                            .count());
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
                throw new DataNotFoundException("heat map is empty, check the inputs!");
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

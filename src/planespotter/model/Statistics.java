package planespotter.model;

import de.gtec.util.math.Maths;
import de.gtec.util.math.vector.Vector2D;
import de.gtec.util.math.vector.Vectors;
import de.gtec.util.threading.ConcurrentCollections;
import de.gtec.util.threading.Threading;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import planespotter.controller.Controller;
import planespotter.dataclasses.*;
import planespotter.model.io.DBOut;
import planespotter.model.nio.client.http.FrameSender;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.InvalidDataException;
import planespotter.util.Bitmap;
import planespotter.util.Time;
import planespotter.util.Utilities;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static planespotter.util.math.MathUtils.abs;

/**
 * @name Statistics
 * @author jml04
 * @version 1.0
 *
 * @description
 * class Statistics is responsible for creating statistics,
 * most of its methods are a bit complicated and use difficult data structures
 * to save data efficiently, but some methods should be improved to work faster
 */
public class Statistics {

    private class Triple<A, B, C> {
        final A a; final B b; final C c;

        Triple(A a, B b, C c) {
            this.a = a; this.b = b; this.c = c;
        }
    };

    /**
     * {@link Statistics} constructor,
     * creates a new, empty {@link Statistics} object
     */
    public Statistics() {
    }

    /**
     * creates a {@link CategoryDataset} by a {@link Map} of {@link Map}s,
     * generated by another statistic-method
     *
     * @param stats is the stats-{@link Map} to convert into {@link CategoryDataset}
     * @param <K> is the key class of the inner {@link Map}
     * @param <V> is the value class of the inner {@link Map}
     * @return {@link CategoryDataset} with the values given by the input {@link Map}
     */
    @NotNull
    public static <K, V extends Number> CategoryDataset createMapBarDataset(@NotNull Map<String, Map<K, V>> stats) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // TODO: 16.06.2022 dataset has incrementValue()
        stats.keySet().forEach(rowKey -> {
            Map<K, V> map = stats.get(rowKey);
            map.keySet().forEach(key -> dataset.addValue(map.get(key), rowKey, String.valueOf(key)));
        });
        return dataset;
    }

    /**
     * creates a {@link CategoryDataset} by a {@link Map},
     * generated by another statistic-method
     *
     * @param stats is the {@link Map} containing the statistics
     * @param <K> is the key class of the {@link Map}
     * @param <V> is the value class of the {@link Map}
     * @return {@link CategoryDataset} with the values given by the input {@link Map}
     */
    @NotNull
    public static <K, V extends Number> CategoryDataset createBarDataset(@NotNull Map<K, V> stats) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // TODO: 16.06.2022 dataset has incrementValue()
        stats.keySet()
                .forEach(key -> dataset.addValue(stats.get(key), "Row-Key", String.valueOf(key)));

        return dataset;
    }

    /**
     * this method creates a {@link Bitmap} containing all positions from the tracking-table (DB),
     * so all existing {@link Position}s from all {@link DataPoint}s,
     * the higher a level on a {@link Bitmap}-field, the more {@link Position}s are there
     *
     * @param gridSize is the multiplier for one grid-rectangle
     *                 (gridSize = 1 : 360x180,
     *                  gridSize = 2 : 180x90,
     *                  gridSize = 0.5 : 720x360,
     *                  ...... )
     * @return {@link Bitmap} from all tracking-{@link Position}s
     * @throws DataNotFoundException if no {@link Position}s were found in the DB
     */
    @NotNull
    public Bitmap globalPositionBitmap(float gridSize)
            throws DataNotFoundException {

        DBOut dbOut = DBOut.getDBOut();
        Vector<Position> allPositions = dbOut.getAllTrackingPositions();
        return Bitmap.fromPosVector(allPositions, gridSize);
    }

    /**
     * calculates the flights with the longest tracked
     * flight-route, might be unaccurate when a flight
     * has a too long time period between data points
     *
     * @return the most tracked flights (longest routes), key is the callsign, value is the route length in KM
     */
    public Map<String, Integer> mostTrackedFlights(int minCount) throws DataNotFoundException {
        // helper
        DataOutputManager out = DataOutputManager.getOutputManager();
        DataOutput currentOut = out.getCurrentOut();
        Controller ctrl = Controller.getInstance();

        Map<Object, Triple<Vector2D<Double>, Integer, Long>> mostTracked = ConcurrentCollections.map();

        if (currentOut instanceof DBOut dbOut) {

            int limit = (int) ctrl.getConfig().getProperty("dataLimit").val;
            mostTracked = localMostTracked(mostTracked, dbOut, limit);

        } else if (currentOut instanceof FrameSender frameSender) {

            String host = ctrl.getRestUploader().getHost();
            URI uri = URI.create(host + "/data/api/download/all");
            mostTracked = onlineMostTracked(mostTracked, frameSender, uri);
        }
        return filteredAndSorted(mostTracked, minCount);
    }

    @NotNull
    private Map<Object, Triple<Vector2D<Double>, Integer, Long>> localMostTracked(Map<Object, Triple<Vector2D<Double>, Integer, Long>> mostTracked, DBOut dbOut, int dataLimit) throws DataNotFoundException {
        Queue<DataPoint> tracking = dbOut.allTrackingData(dataLimit);
        DataPoint[] arr = tracking.stream().toArray(DataPoint[]::new);

        Arrays.sort(arr, Comparator.comparingLong(DataPoint::timestamp));
        tracking = Threading.concurrentQueue(Arrays.asList(arr));

        while (!tracking.isEmpty()) {
            Vector2D<Double> now;
            DataPoint dp = tracking.poll();
            int fid = dp.flightID();
            if (fid == 998) {
                // skipping invalid entry (could be N/A or private flight)
                continue;
            }
            // TODO: 29.03.2023 add timestamp to tuple , always re-calc avg timestamp and check variation
            long timestamp = dp.timestamp();
            double xn = dp.pos().lat();
            double yn = dp.pos().lon();
            now = new Vector2D<>(xn, yn);

            if (!mostTracked.containsKey(fid)) {
                mostTracked.put(fid, new Triple<>(now, 0, dp.timestamp()));

            } else {
                Triple<Vector2D<Double>, Integer, Long> entry = mostTracked.get(fid);
                long time = entry.c;
                int distance = entry.b;

                if (Math.abs(time - timestamp) > 360000) { // ~ 1 hour
                    // timestamps too far from eachother
                    mostTracked.put(fid, new Triple<>(now, distance, timestamp));
                    continue;
                }
                Vector2D last = entry.a;
                de.gtec.util.math.vector.Vector<Double> result = Vectors.subtract(now, last);
                distance += Maths.abs(result);

                mostTracked.put(fid, new Triple<>(now, distance, timestamp));
            }
        }
        return mostTracked.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue()));
    }


    @NotNull
    private Map<Object, Triple<Vector2D<Double>, Integer, Long>> onlineMostTracked(Map<Object, Triple<Vector2D<Double>, Integer, Long>> mostTracked, FrameSender frameSender, URI uri) throws DataNotFoundException {
        Queue<UniFrame> frames;
        try {
            frames = Threading.concurrentQueue(frameSender.getData(uri));
        } catch (IOException e) {
            throw new DataNotFoundException("Could not load data");
        }
        UniFrame[] arr = frames.stream().toArray(UniFrame[]::new);

        Arrays.sort(arr, Comparator.comparingLong(Frame::getTimestamp));
        frames = Threading.concurrentQueue(Arrays.asList(arr));

        while (!frames.isEmpty()) {
            Vector2D<Double> now;
            UniFrame frame = frames.poll();
            String key = frame.getCallsign();
            long timestamp = frame.getTimestamp();
            double xn = Maths.latDegreesToKm(frame.getLat());
            double yn = Maths.lonDegreesToKm(frame.getLat(), frame.getLon());
            now = new Vector2D<>(xn, yn);

            if (!mostTracked.containsKey(key)) {
                mostTracked.put(key, new Triple<>(now, 0, timestamp));

            } else {
                Triple<Vector2D<Double>, Integer, Long> entry = mostTracked.get(key);
                long time = entry.c;
                int distance = entry.b;

                if (Math.abs(time - timestamp) > 360000) { // ~ 1 hour
                    // timestamps too far from eachother
                    mostTracked.put(key, new Triple<>(now, distance, timestamp));
                    continue;
                }
                Vector2D last = entry.a;
                de.gtec.util.math.vector.Vector result = Vectors.subtract(now, last);
                distance += Maths.abs(result);

                mostTracked.put(key, new Triple<>(now, distance, timestamp));
            }
        }
        return mostTracked.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue()));
    }

    @NotNull
    private Map<String, Integer> filteredAndSorted(Map<Object, Triple<Vector2D<Double>, Integer, Long>> mostTracked, int minValue) {
        return mostTracked.entrySet()
                .parallelStream()
                .filter(e -> e.getValue().b >= minValue)
                .sorted(Comparator.comparingInt(e -> e.getValue().b))
                .limit(50)
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().b));
    }

    /**
     * creates a {@link Map} of {@link Airport} tags and {@link Airline} IDs by counting the most-present
     * {@link Airline}s for each {@link Airport}
     *
     * @param maxAirlinesPerAirport is the max. {@link Airline} count displayed for one {@link Airport}
     * @return {@link Map} of {@link Airport}-tag (String) and {@link Airline}-IDs (int[])
     * @throws DataNotFoundException if no data was found in the database
     */
    // FIXME: 23.08.2022 will probably take too much time
    @NotNull
    public Map<String, int[]> topAirlinesPerAirport(int maxAirlinesPerAirport, int dataLimit)
            throws DataNotFoundException {

        DBOut dbOut = DBOut.getDBOut();
        Map<String, Map<Integer, Integer>> airportMap = new HashMap<>();
        // TODO replace allFLights with extra method with a smaller (faster) query
        List<Flight> allFlights = dbOut.getAllFlights(dataLimit);
        Map<String, int[]> apTopAirlines = new HashMap<>();
        String src, dest;
        int airline;
        for (Flight flight : allFlights) {
            src = flight.src().iataTag();
            dest = flight.dest().iataTag();
            airline = flight.plane().airline().id();

            putSorted(airportMap, airline, src, dest);
        }

        Set<Map.Entry<String, Map<Integer, Integer>>> entries = airportMap.entrySet();
        int[] airlineIDs;
        Set<Integer> keys;
        for (Map.Entry<String, Map<Integer, Integer>> entry : entries) {
            keys = entry.getValue().keySet();
            airlineIDs = keys.stream()
                    .mapToInt(i -> i)
                    .limit(maxAirlinesPerAirport)
                    .toArray();
            apTopAirlines.put(entry.getKey(), airlineIDs);
        }
        return apTopAirlines;
    }

    /**
     * this method will probably take too much time
     *
     * @param airportMap
     * @param airline
     * @param airports
     */
    // TODO: 24.08.2022 put directly in the right order, do not sort after putting
    private void putSorted(@NotNull Map<String, Map<Integer, Integer>> airportMap, int airline, @NotNull String... airports) {
        Map<Integer, Integer> airlineMap;
        for (String airport : airports) {
            if (airportMap.containsKey(airport)) {
                airlineMap = airportMap.get(airport);
                if (airlineMap.containsKey(airline)) {
                    airlineMap.replace(airline, airlineMap.get(airline) + 1);
                }
            } else {
                airlineMap = new HashMap<>(1);
                airlineMap.put(airline, 1);
                airportMap.put(airport, airlineMap);
            }
            airportMap.replace(airport, sortMapByValues(airlineMap));
        }
    }

    @NotNull
    private Map<Integer, Integer> sortMapByValues(@NotNull Map<Integer, Integer> airlineMap) {
        Set<Map.Entry<Integer, Integer>> entries = airlineMap.entrySet();
        Map.Entry[] sorted = entries.stream()
                .sorted((a, b) -> a.getValue() < b.getValue() ? a.getKey() : b.getKey())
                .toArray(Map.Entry[]::new);
        return Map.ofEntries(sorted);
    }

    /**
     * This method should create a {@link Map} of {@link Position}s,
     * paired with a double array that contains the ground speed and the 'real speed' of a plane.
     *
     * This technique will not work because the ground speed is already the 'real speed' (with headwind).
     * Another opportunity for this method would be a double array that contains the
     * ground speed (so the 'real speed') and the speed without headwind,
     * which should be a bit higher than the 'real speed'.
     *
     * @param flightID is the flight ID to calculate the speed {@link Map}
     * @return
     */
    public Map<Position, double[]> flightSpeedComparison(int flightID) {
        DBOut dbOut = DBOut.getDBOut();
        try {
            Vector<DataPoint> tracking = dbOut.getTrackingByFlight(flightID);
            HashMap<Position, double[]> map = new HashMap<>();

            Position current, last;
            planespotter.util.math.Vector2D<Double> vector;
            double[] values;
            long tsCurrent, tsLast;
            double km, tDiffHrs;
            int counter = 0;

            DataPoint firstDP = tracking.get(0);
            last = firstDP.pos();
            tsLast = firstDP.timestamp();
            values = new double[] { firstDP.speed(), -1. };
            map.put(last, values);

            for (DataPoint dp : tracking) {
                current = dp.pos();
                tsCurrent = dp.timestamp();
                if (counter > 0) {
                    vector = planespotter.util.math.Vector2D.ofDegrees(last, current);
                    values[0] = dp.speed();
                    km = abs(vector);
                    tDiffHrs = Time.timeDiff(tsCurrent, tsLast, TimeUnit.SECONDS, TimeUnit.HOURS);
                    if (tDiffHrs != 0.) {
                        values[1] = (km / tDiffHrs) * (1.0 / tDiffHrs);
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

    /**
     * filters a {@link Map} by only significant values (values that are higher or equals minValue)
     *
     * @param map is the input {@link Map} to be filtered
     * @param minValue is the smallest allowed value
     * @return the input {@link Map} but without entries with a value lower than the minimum
     */
    @NotNull
    public Map<String, Integer> onlySignificant(@NotNull final Map<String, Integer> map, final int minValue) {
        Deque<String> removeKeys = new ArrayDeque<>();
        map.forEach((key, val) -> {
            if (val < minValue) {
                removeKeys.add(key);
            }
        });
        String key;
        while (!removeKeys.isEmpty()) {
            key = removeKeys.poll();
            map.remove(key);
        }
        return map;
    }

    /**
     *
     *
     * @param tags
     * @return
     */
    public Map<String, Integer> tagCount(@NotNull final Deque<String> tags) {
        Map<String, Integer> map = new HashMap<>();
        tags.forEach(tag -> {
            if (map.containsKey(tag)) {
                map.replace(tag, map.get(tag) + 1);
            } else if (!tag.isBlank()) {
                map.put(tag, 1);
            }
        });
        return map;
    }

    /**
     *
     *
     * @param topLeft
     * @param bottomRight
     * @param dataPoints
     * @return
     */
    @Deprecated(since = "?", forRemoval = true)
    @SafeVarargs
    public final Map<String, Map<Long, Integer>> windSpeed(final Position topLeft, final Position bottomRight, Deque<DataPoint>... dataPoints) {
        HashMap<String, Map<Long, Integer>> maps = new HashMap<>();
        AtomicInteger counter = new AtomicInteger(0);
        DBOut dbOut = DBOut.getDBOut();
        DataPoint[] dpArr;
        int fid;
        for (int i = 0; i < dataPoints.length; i++) {
            dpArr = Utilities.parseDataPointArray(dataPoints[counter.getAndIncrement()]);
            fid = dpArr[0].flightID();
            try {
                String key = dbOut.getFlightByID(fid).callsign();
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

    /**
     *
     *
     * @param minCount
     * @return
     * @throws DataNotFoundException
     */
    public JFreeChart airlineSignificance(int minCount)
            throws DataNotFoundException {

        DBOut dbOut = DBOut.getDBOut();
        Deque<String> airlineTags = dbOut.getAllAirlineTags();
        Map<String, Integer> airlStats = this.onlySignificant(this.tagCount(airlineTags), minCount);
        CategoryDataset dataset = Statistics.createBarDataset(airlStats);
        return ChartFactory.createBarChart("Airline-Significance", "Airlines", "Flight-Count",
                                            dataset, PlotOrientation.HORIZONTAL, true, true, false);
    }

    /**
     *
     *
     * @param minCount
     * @return
     * @throws DataNotFoundException
     */
    public JFreeChart airportSignificance(int minCount)
            throws DataNotFoundException {

        DBOut dbOut = DBOut.getDBOut();
        Deque<String> airportTags = dbOut.getAllAirportTagsNotDistinct();
        Map<String, Integer> apStats = this.onlySignificant(this.tagCount(airportTags), minCount);
        CategoryDataset dataset = Statistics.createBarDataset(apStats);
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
    @Deprecated(since = "new airportSignificance with JFreeChart")
    @NotNull
    public final Map<Airport, Integer> airportSignificance(@NotNull Deque<Airport> airports) {
        HashMap<Airport, Integer> asignf = new HashMap<>();
        AtomicInteger val = new AtomicInteger();
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

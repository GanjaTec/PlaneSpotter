package planespotter.constants;

import org.jetbrains.annotations.NotNull;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSourceInfo;

import planespotter.model.nio.Filters;
import planespotter.throwables.ExtensionException;
import planespotter.throwables.InvalidDataException;

import java.io.*;

/**
 * @name UserSettings
 * @author jml04
 * @version 1.0
 *
 * @description
 * class UserSettings contains the user settings which can be edited in the settings menu
 */
// TODO: 26.08.2022 not static
// TODO: 24.08.2022 MERGE WITH Configuration class
public class UserSettings {
    // TODO: 07.08.2022 maybe save all non-final values in one HashMap
    // max loaded data
    private static int maxLoadedData;

    // current tile source
    private static TileSource currentMapSource;

    private static int GRIDSIZE_LAT;
    private static int GRIDSIZE_LON;

    // default map base url
    private static final String BASE_URL;

    // map types
    public static final TileSource BING_MAP, TRANSPORT_MAP, DEFAULT_MAP;

    // collector filter strings
    private static final Filters collectorFilters;

    // static initializer
    static {
        // setting default map ('osm') base url
        BASE_URL = "https://a.tile.openstreetmap.de";
        // setting tile sources
        BING_MAP = new BingAerialTileSource();
        TRANSPORT_MAP = new OsmTileSource.TransportMap();
        DEFAULT_MAP = new TMSTileSource(new TileSourceInfo("OSM", BASE_URL, "0"));
        // initializing non-final fields
        try {
            // initialization with saved config file
            Object[] settingsValues = read(Configuration.CONFIG_FILENAME);
            initialize(settingsValues);
        } catch (Exception e) {
            e.printStackTrace();
            // initialization with default values
            Object[] defaultValues = {50000, DEFAULT_MAP, 6, 12};
            initialize(defaultValues);
        }
        collectorFilters = null/*Filters.read(Configuration.FILTERS_FILENAME)*/;
    }

    /**
     *
     *
     * @param values
     */
    private static void initialize(@NotNull Object[] values) {
        if (values.length != 4) {
            return;
        }
        maxLoadedData = (int) values[0];
        currentMapSource = (TileSource) values[1];
        GRIDSIZE_LAT = (int) values[2];
        GRIDSIZE_LON = (int) values[3];
    }

    // TODO: 07.08.2022 maybe change to HashMap
    @NotNull
    private static Object[] values() {
        return new Object[] {
                maxLoadedData, currentMapSource, GRIDSIZE_LAT, GRIDSIZE_LON
        };
    }

    /**
     * @return maxLoadedFlights, the limit of loaded flights
     */
    public static int getMaxLoadedData() {
        return maxLoadedData;
    }

    /**
     * sets the max loaded flights variable
     *
     * @param newMax, the new flight limit
     */
    public static void setMaxLoadedData(int newMax) {
        maxLoadedData = newMax;
    }

    /**
     * @return the current map tile source
     */
    @NotNull
    public static TileSource getCurrentMapSource() {
        return currentMapSource;
    }

    /**
     * sets the current map tile source
     *
     * @param mapSource is the tile source to set (bingMap, tmstMap, transportMap)
     */
    public static void setCurrentMapSource(@NotNull TileSource mapSource) {
        currentMapSource = mapSource;
    }

    /**
     *
     *
     * @param filename
     * @return
     * @throws ExtensionException
     * @throws FileNotFoundException
     */
    @NotNull
    private static Object[] read(@NotNull String filename)
            throws ExtensionException, FileNotFoundException, InvalidDataException {

        System.out.println("Reading configuration file...");
        if (!filename.endsWith(".psc")) {
            throw new ExtensionException("File must end with '.psc'");
        }
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException("Configuration file not found!");
        }
        try (Reader fileReader = new FileReader(file);
             BufferedReader buf = new BufferedReader(fileReader)) {
            return buf.lines()
                    .filter(line -> !line.startsWith("#") && line.contains(":"))
                    .map(line -> line.split(": "))
                    .filter(vals -> vals.length > 1)
                    .map(vals -> {
                        String key = vals[0],
                               value = vals[1];
                        switch (key) {
                            case "maxLoadedData", "gridSizeLat", "gridSizeLon" -> {
                                try {
                                    return Integer.parseInt(value);
                                } catch (NumberFormatException nfe) {
                                    throw new InvalidDataException("Couldn't parse String to Int!");
                                }
                            }
                            case "currentMapSource" -> {
                                return translateSource(value);
                            }
                            default -> throw new InvalidDataException("Couldn't read settings data!");
                        }
                    })
                    .toArray(Object[]::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new InvalidDataException("Couldn't read settings data!");
    }

    /**
     *
     *
     * @param name
     * @return
     */
    private static Object translateSource(@NotNull String name) {
        return switch (name) {
            case "OSM" -> DEFAULT_MAP;
            case "Public Transport" -> TRANSPORT_MAP;
            case "Bing" -> BING_MAP;
            default -> throw new InvalidDataException("Couldn't read map source data!");
        };
    }

    /**
     *
     *
     * @param filename
     * @return
     */
    public static boolean write(@NotNull String filename) {

        System.out.println("Writing configuration file...");
        File file = new File(filename);
        try (FileWriter fWriter = new FileWriter(file);
             BufferedWriter buf = new BufferedWriter(fWriter)) {

            String[] strings = new String[] {
                    "maxLoadedData: " + maxLoadedData,
                    "\ncurrentMapSource: " + currentMapSource,
                    "\ngridSizeLat: " + GRIDSIZE_LAT,
                    "\ngridSizeLon: " + GRIDSIZE_LON
            };
            for (String s : strings) {
                buf.write(s);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     *
     * @return
     */
    public static int getGridsizeLat() {
        return GRIDSIZE_LAT;
    }

    /**
     *
     *
     * @param gridsizeLat
     */
    public static void setGridsizeLat(int gridsizeLat) {
        GRIDSIZE_LAT = gridsizeLat;
    }

    /**
     *
     *
     * @return
     */
    public static int getGridsizeLon() {
        return GRIDSIZE_LON;
    }

    /**
     *
     *
     * @param gridsizeLon
     */
    public static void setGridsizeLon(int gridsizeLon) {
        GRIDSIZE_LON = gridsizeLon;
    }

    @NotNull
    public static Filters getCollectorFilters() {
        return collectorFilters;
    }
}

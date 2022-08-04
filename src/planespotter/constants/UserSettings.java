package planespotter.constants;

import jnr.ffi.annotations.In;
import org.jetbrains.annotations.NotNull;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSourceInfo;
import planespotter.throwables.ExtensionException;
import planespotter.throwables.InvalidDataException;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.stream.Stream;

/**
 * @name UserSettings
 * @author jml04
 * @version 1.0
 * @description
 * class UserSettings contains the user settings which can be edited in the settings menu
 */
public class UserSettings {
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

    // static initializer
    static {
        // setting default map ('osm') base url
        BASE_URL = "https://a.tile.openstreetmap.de";
        // setting tile sources
        BING_MAP = new BingAerialTileSource();
        TRANSPORT_MAP = new OsmTileSource.TransportMap();
        DEFAULT_MAP = new TMSTileSource(new TileSourceInfo("OSM", BASE_URL, "0"));
        // setting current max-load and map-source
        GRIDSIZE_LAT = 3;
        GRIDSIZE_LON = 3;

        try {
            // initialization with saved config file
            Object[] settingsValues = read(Paths.RESOURCE_PATH + "config.psc");
            initialize(settingsValues);
        } catch (Exception e) {
            // initialization with default values
            Object[] defaultValues = {50000, DEFAULT_MAP};
            initialize(defaultValues);
        }
    }

    private static void initialize(@NotNull Object[] values) {
        for (Object val : values) {
            if (val instanceof Integer i) {
                maxLoadedData = i;
            } else if (val instanceof TileSource mapSrc) {
                currentMapSource = mapSrc;
            }
        }

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

    @NotNull
    private static Object[] read(@NotNull String filename) throws ExtensionException, FileNotFoundException {
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
                            case "maxLoadedData" -> {
                                try {
                                    return Integer.parseInt(value);
                                } catch (NumberFormatException nfe) {
                                    throw new InvalidDataException("Couldn't parse String to Int!");
                                }
                            }
                            case "currentMapSource" -> {
                                return readMapSource(value);
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

    private static Object readMapSource(@NotNull String name) {
        return switch (name) {
            case "DEFAULT_MAP" -> DEFAULT_MAP;
            case "TRANSPORT_MAP" -> TRANSPORT_MAP;
            case "BING_MAP" -> BING_MAP;
            default -> throw new InvalidDataException("Couldn't read map source data!");
        };
    }

    public static void write() {

    }

    public static int getGridsizeLat() {
        return GRIDSIZE_LAT;
    }

    public static void setGridsizeLat(int gridsizeLat) {
        GRIDSIZE_LAT = gridsizeLat;
    }

    public static int getGridsizeLon() {
        return GRIDSIZE_LON;
    }

    public static void setGridsizeLon(int gridsizeLon) {
        GRIDSIZE_LON = gridsizeLon;
    }
}

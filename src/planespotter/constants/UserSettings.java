package planespotter.constants;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSourceInfo;

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

    // initializer
    static {
        // setting default map ('osm') base url
        BASE_URL = "https://a.tile.openstreetmap.de";
        // setting tile sources
        BING_MAP = new BingAerialTileSource();
        TRANSPORT_MAP = new OsmTileSource.TransportMap();
        DEFAULT_MAP = new TMSTileSource(new TileSourceInfo("neu", BASE_URL, "0"));
        // setting current max-load and map-source
        maxLoadedData = 50000;
        currentMapSource = DEFAULT_MAP;
        GRIDSIZE_LAT = 3;
        GRIDSIZE_LON = 3;
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
     * @param currentMapSource is the tile source to set (bingMap, tmstMap, transportMap)
     */
    public static void setCurrentMapSource(TileSource currentMapSource) {
        UserSettings.currentMapSource = currentMapSource;
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

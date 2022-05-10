package planespotter.display;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSourceInfo;

/**
 * @name UserSettings
 * @author jml04
 * @version 1.0
 *
 * class UserSettings contains the user settings which can be edited in the settings menu
 * TODO may be saved through another class like ConfigManager
 */
public class UserSettings {
    // settings variables
    // max loaded data
    private static int maxLoadedData = 20000;

    private static TileSource currentMapSource = new UserSettings().tmstMap;
    // map types
    private final String baseUrl = "https://c.tile.openstreetmap.de";
    private final TileSource    bingMap = new BingAerialTileSource(),
                                transportMap = new OsmTileSource.TransportMap(),
                                tmstMap = new TMSTileSource(new TileSourceInfo("neu", baseUrl, "0"));


    /**
     * @return maxLoadedFlights, the limit of loaded flights
     */
    public static int getMaxLoadedData() {
        return maxLoadedData;
    }

    /**
     * @set the max loaded flights variable
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

}

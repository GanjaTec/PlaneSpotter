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
 *
 * class UserSettings contains the user settings which can be edited in the settings menu
 */
public class UserSettings {
    // max loaded data
    private static int maxLoadedData = 20000;
    // current tile source
    private static TileSource currentMapSource = new UserSettings().tmstMap;
    // default map base url
    private final String baseUrl = "https://a.tile.openstreetmap.de";
    // map types
    public final TileSource    bingMap = new BingAerialTileSource(),
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
    public void setMaxLoadedData(int newMax) {
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
    public void setCurrentMapSource(TileSource currentMapSource) {
        UserSettings.currentMapSource = currentMapSource;
    }

}

package planespotter.display;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSourceInfo;
import planespotter.controller.Controller;

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
    private final String baseUrl = "https://a.tile.openstreetmap.de";
    public final TileSource    bingMap = new BingAerialTileSource(),
                               transportMap = new OsmTileSource.TransportMap(),
                               tmstMap = new TMSTileSource(new TileSourceInfo("neu", baseUrl, "0"));


    /**
     * @return maxLoadedFlights, the limit of loaded flights
     */
    public int getMaxLoadedData() {
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
    public TileSource getCurrentMapSource() {
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

    /**
     *
     * @param data [0] and [1] must be filled
     */
    public void confirm (String... data) {
        if (data[0] == null || data[1] == null) {
            throw new IllegalArgumentException("Please fill all fields! (with the right params)");
        }
        this.setMaxLoadedData(Integer.parseInt(data[0]));
        var map = this.getCurrentMapSource();
        switch (data[1]) {
            case "Bing Map" -> map = this.bingMap;
            case "Default Map" -> map = this.tmstMap;
            case "Transport Map" -> map = this.transportMap;
        }
        this.setCurrentMapSource(map);
        new GUISlave().mapViewer().setTileSource(map);
    }

}

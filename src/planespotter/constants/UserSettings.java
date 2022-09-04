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

}

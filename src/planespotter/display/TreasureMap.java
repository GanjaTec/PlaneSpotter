package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSourceInfo;
import planespotter.constants.Configuration;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @name TreasureMap
 * @author jml04
 * @version 1.0
 *
 * class TreasureMap represents a Map which extends from JMapViewer
 */
public class TreasureMap extends JMapViewer {

    @NotNull public static final Object PAINT_LOCK = new Object();

    @NotNull public static final TileSource OPEN_STREET_MAP, BING_MAP, TRANSPORT_MAP;

    static {
        // setting tile sources
        OPEN_STREET_MAP = new TMSTileSource(new TileSourceInfo("OSM", "https://a.tile.openstreetmap.de", "0"));
        BING_MAP = new BingAerialTileSource();
        TRANSPORT_MAP = new OsmTileSource.TransportMap();
    }

    // heat map image
    private BufferedImage heatMap;

    /**
     * treasure map constructor
     */
    public TreasureMap() {
        super(new MemoryTileCache());

        heatMap = null;
    }

    /**
     * paints the map, if a this.heatMap is not null,
     * the heat map is also painted
     *
     * @param g is the given graphics object, i don't know where it comes from
     */
    @Override
    protected void paintComponent(Graphics g) {
        synchronized (PAINT_LOCK) {
            super.paintComponent(g);
        }
        /*if (this.heatMap != null) {
            var g2d = (Graphics2D) g;
            g2d.drawImage(this.heatMap, 0, 0, super.getWidth(), super.getHeight(), null);
        }*/
    }

    public final BufferedImage getHeatMap() {
        return this.heatMap;
    }

    public void setHeatMap(BufferedImage heatMap) {
        this.heatMap = heatMap;
    }

}

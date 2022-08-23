package planespotter.display;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;

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

    public static final Object PAINT_LOCK = new Object();
    // heat map image
    private BufferedImage heatMap = null;

    /**
     * treasure map constructor
     */
    public TreasureMap() {
        super(new MemoryTileCache());
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

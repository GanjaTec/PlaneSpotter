package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.openstreetmap.gui.jmapviewer.MapRectangleImpl;

import java.awt.*;
import java.awt.image.BufferedImage;

public class HeatMapRectangle extends MapRectangleImpl {

    private final BufferedImage heatMapImg;
    private final TreasureMap viewer;

    public HeatMapRectangle(@NotNull final BufferedImage heatMapImg, @NotNull final TreasureMap viewer) {
        super(null, null);
        this.heatMapImg = heatMapImg;
        this.viewer = viewer;
    }

    @Override
    public void paint(Graphics g, Point topLeft, Point bottomRight) {
        //var point = this.viewer.getMapPosition(coord.getLat(), coord.getLon(), true);
        /*var point = this.viewer.getMapPosition(180., 90., true);
        if (point != null) {
            g.drawImage(this.heatMapImg, point.x, point.y, Color.BLACK, null);
        }*/
        g.drawImage(this.heatMapImg, topLeft.x, topLeft.y, null);
    }
}











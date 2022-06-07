package planespotter.display;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.BitSet;

/**
 * @name MarkerPainter
 * @author jml04
 * @version 1.0
 * @since "MapMarker implementation update"
 *
 * abstract class MarkerPainter represents an abstract painter for a MapMarker,
 * contains abstract paint() method, which has to be overwritten
 */
public abstract class MarkerPainter {

    public abstract void paint(Graphics g, Point position, int radius);

    /**
     * @name HeatMapPainter
     * @version 1.0
     *
     * inner class HeatMapPainter represents a painter for a heat map,
     * contains the overwritten method void paint()
     */
    public static class HeatMarkerPainter extends MarkerPainter {

        private final Color color;

        public HeatMarkerPainter(Color color) {
            this.color = color;
        }

        @Override
        public void paint(Graphics g, Point position, int radius) {
            var g2d = (Graphics2D) g;
            g2d.setColor(this.color);
            g2d.fillRect(position.x, position.y, 10, 10);
        }
    }

    public static class HeatRectPainter extends MarkerPainter {

        private final Color color;

        public HeatRectPainter(@NotNull Color color) {
            this.color = color;
        }

        @Override
        public void paint(Graphics g, Point position, int radius) {
            var g2d = (Graphics2D) g;
            g2d.setColor(this.color);
            //g2d.fillRect(position.x, position.y, 30, 30);
            g2d.fill3DRect(position.x, position.y, 10, 10, true);
        }
    }

}

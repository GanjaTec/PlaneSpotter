package planespotter.display;

import java.awt.*;

/**
 * @name MarkerPainter
 * @author jml04
 * @version 1.0
 * @since "MapMarker implementation update"
 *
 * abstract class MarkerPainter represents an abstract painter for a MapMarker,
 * contains abstract paint() method, which has to be overwritten
 */
@FunctionalInterface
public interface MarkerPainter {

    void paint(Graphics g, Point position, int radius);

}

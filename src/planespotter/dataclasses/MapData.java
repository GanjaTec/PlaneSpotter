package planespotter.dataclasses;

import planespotter.constants.ViewType;

import java.awt.*;
import java.io.Serializable;
import java.util.Vector;

/**
 * @name MapData
 * @author jml04
 * @version 1.0
 *
 * record MapData represents a map within its data points,
 * view type and current visible area
 */
public record MapData(Vector<DataPoint> data,
                      ViewType viewType,
                      Rectangle visibleRect)
        implements Serializable {
}

package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;

import java.awt.*;

public class MapLine extends MapPolygonImpl {

    public MapLine(@NotNull Position a, @NotNull Position b, @NotNull Color color) {
        super(a.toCoordinate(), b.toCoordinate(), a.toCoordinate());
        setColor(color);
    }



}

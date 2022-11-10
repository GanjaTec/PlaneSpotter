package planespotter.util.math;


import planespotter.dataclasses.Position;

import static planespotter.util.math.MathUtils.latDegreesToKm;
import static planespotter.util.math.MathUtils.lonDegreesToKm;

public class Vector2D<N extends Number> {

    public final N x, y;

    public Vector2D(N x, N y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2D<Double> ofDegrees(Position x) {
        return new Vector2D<>(latDegreesToKm(x.lat()), lonDegreesToKm(x.lat(), x.lon()));
    }

    public static Vector2D<Double> ofDegrees(Position from, Position to) {
        double toLat = to.lat(), fromLat = from.lat();
        return new Vector2D<>(latDegreesToKm(toLat) - latDegreesToKm(fromLat),
                            lonDegreesToKm(toLat, to.lon()) - lonDegreesToKm(fromLat, from.lon()));
    }

}

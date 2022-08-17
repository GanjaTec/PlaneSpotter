package planespotter.util.math;


import planespotter.dataclasses.Position;

import static planespotter.util.math.MathUtils.latDegreeToKm;
import static planespotter.util.math.MathUtils.lonDegreeToKm;

public class Vector2D<N extends Number> {

    public final N x, y;

    public Vector2D(N x, N y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2D<Double> ofDegrees(Position x) {
        return new Vector2D<>(latDegreeToKm(x.lat()), lonDegreeToKm(x.lat(), x.lon()));
    }

    public static Vector2D<Double> ofDegrees(Position from, Position to) {
        double toLat = to.lat(), fromLat = from.lat();
        return new Vector2D<>(latDegreeToKm(toLat) - latDegreeToKm(fromLat),
                            lonDegreeToKm(toLat, to.lon()) - lonDegreeToKm(fromLat, from.lon()));
    }

}

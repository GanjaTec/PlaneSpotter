package planespotter.util;


import planespotter.dataclasses.Position;

import static planespotter.util.MathUtils.latDegreeToKm;
import static planespotter.util.MathUtils.lonDegreeToKm;

public class Vector2D {

    public final double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2D ofDegrees(Position x) {
        return new Vector2D(latDegreeToKm(x.lat()), lonDegreeToKm(x.lat(), x.lon()));
    }

    public static Vector2D ofDegrees(Position from, Position to) {
        double toLat = to.lat(), fromLat = from.lat();
        return new Vector2D(latDegreeToKm(toLat) - latDegreeToKm(fromLat),
                            lonDegreeToKm(toLat, to.lon()) - lonDegreeToKm(fromLat, from.lon()));
    }

}

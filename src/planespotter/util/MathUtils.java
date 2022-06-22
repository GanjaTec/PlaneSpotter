package planespotter.util;

import org.jetbrains.annotations.Range;

import java.util.concurrent.TimeUnit;

import static java.lang.StrictMath.*;

/**
 * @name
 * @author jml04
 * @version 1.0
 *
 * Class MathUtils contains different help functions for maths
 */
public abstract class MathUtils {

    public static final double LAT_TO_KM_MULTIPLIER = 111.3;

    public static double abs(Vector2D vector) {
        return (vector instanceof Vector3D v3d)
                ? sqrt(x2(v3d.x) + x2(v3d.y) + x2(v3d.z))
                : sqrt(x2(vector.x) + x2(vector.y));
    }

    public static double x2(double number) {
        return pow(number, 2);
    }

    public static double latDegreeToKm(double lat) {
        return lat * LAT_TO_KM_MULTIPLIER;
    }

    public static double lonDegreeToKm(double lat, double lon) {
        double oneDegree = LAT_TO_KM_MULTIPLIER * cos(toRadians(lat));
        return lon * oneDegree;
    }

    public static long timeDiff(long now, long last, TimeUnit inputTimeUnit, TimeUnit outputTimeUnit) {
        long tDiff = now - last;
        return switch (outputTimeUnit) {
            case NANOSECONDS -> inputTimeUnit.toNanos(tDiff);
            case MICROSECONDS -> inputTimeUnit.toMicros(tDiff);
            case MILLISECONDS -> inputTimeUnit.toMillis(tDiff);
            case SECONDS -> inputTimeUnit.toSeconds(tDiff);
            case MINUTES -> inputTimeUnit.toMinutes(tDiff);
            case HOURS -> inputTimeUnit.toHours(tDiff);
            case DAYS -> inputTimeUnit.toDays(tDiff);
        };
    }

    public static int divide(int a, int divisor) {
        if (divisor == 0.) {
            throw new ArithmeticException("Divisor may not be null!");
        }
        return a / divisor;
    }

    public static double divide(double a, double divisor) {
        if (divisor == 0.) {
            throw new ArithmeticException("Divisor may not be null!");
        }
        return a / divisor;
    }

    /**
     * converts a boolean to byte (0 or 1)
     *
     * @param bool is the give boolean
     * @return 0 if true, else 1
     */
    public static @Range(from = 0, to = 1) byte toBinary(boolean bool) {
        return (byte) (bool ? 0 : 1);
    }
}

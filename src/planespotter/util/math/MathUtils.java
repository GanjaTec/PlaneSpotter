package planespotter.util.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import planespotter.util.Utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public static double abs(Vector2D<Double> v2d) {
        return (v2d instanceof Vector3D<Double> v3d)
                ? sqrt(x2(v3d.x) + x2(v3d.y) + x2(v3d.z))
                : sqrt(x2(v2d.x) + x2(v2d.y));
    }

    public static double x2(double number) {
        return pow(number, 2);
    }

    public static double x3(double number) {
        return pow(number, 3);
    }

    public static double latDegreeToKm(double lat) {
        return lat * LAT_TO_KM_MULTIPLIER;
    }

    public static double lonDegreeToKm(double lat, double lon) {
        double oneDegree = LAT_TO_KM_MULTIPLIER * cos(toRadians(lat));
        return lon * oneDegree;
    }

    public static int divide(int a, int divisor) {
        zeroCheck(divisor);
        return StrictMath.floorDiv(a, divisor);
    }

    public static long divide(long a, long divisor) {
        zeroCheck(divisor);
        return StrictMath.floorDiv(a, divisor);
    }

    public static double divide(double a, double divisor) {
        zeroCheck(divisor);
        return new BigDecimal(a)
                .divide(new BigDecimal(divisor), RoundingMode.DOWN)
                .doubleValue();
    }

    private static void zeroCheck(@NotNull Number num) {
        ArithmeticException ex = null;
        if ((num instanceof Integer || num instanceof Long) && Utilities.asInt(num) == 0) {
            ex = new ArithmeticException("0 not allowed as divisor!");
        } else if (num.doubleValue() == 0.0) {
            ex = new ArithmeticException("0 not allowed as divisor!");
        }
        if (ex != null) {
            throw ex;
        }
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

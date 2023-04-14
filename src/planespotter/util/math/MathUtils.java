package planespotter.util.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import planespotter.throwables.InvalidArrayException;
import planespotter.throwables.OutOfRangeException;
import planespotter.util.Bytes;
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

    public static final double LAT_TO_KM_MULTIPLIER = 110.574;
    public static final double LON_TO_KM_MULTIPLIER = 111.320;

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

    public static double latDegreesToKm(double lat) {
        return lat * LAT_TO_KM_MULTIPLIER;
    }

    public static double lonDegreesToKm(double lat, double lon) {
        double oneDegree = LAT_TO_KM_MULTIPLIER * cos(toRadians(lat));
        return lon * oneDegree;
    }

    public static double kmToLatDegrees(double km) {
        return km / LAT_TO_KM_MULTIPLIER;
    }

    public static double kmToLonDegrees(double km, double lat) {
        return km / (LON_TO_KM_MULTIPLIER * cos(toRadians(lat)));
    }

    /**
     * Note: this method is slower than a simple a / b
     *
     * @param a
     * @param divisor
     * @return
     */
    public static int divide(int a, int divisor) {
        zeroCheck(divisor);
        return StrictMath.floorDiv(a, divisor);
    }

    /**
     * Note: this method is slower than a simple a / b
     *
     * @param a
     * @param divisor
     * @return
     */
    public static long divide(long a, long divisor) {
        zeroCheck(divisor);
        return StrictMath.floorDiv(a, divisor);
    }

    /**
     * Note: this method is very slow, uses BigDecimal instead of primitive double
     *
     *  -> Testing this method against the "default division" with two arrays with length of 1000000:
     *          [Benchmark] Benchmark 'Default Division' successfully done!
     *          [Benchmark] Elapsed Time: 6 ms / 0 s
     *          [Benchmark] Benchmark 'MathUtils Division' successfully done!
     *          [Benchmark] Elapsed Time: 2968 ms / 2 s
     *     @see planespotter.a_test.Test for this benchmark
     *
     * @param a
     * @param divisor
     * @return
     */
    @Deprecated(since = "too slow", forRemoval = true)
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

    public static byte[] arrayMean(byte[][] arrays) {
        int len = arrays.length;
        if (len == 0) {
            throw new InvalidArrayException("Too few arrays given, must be at least 2");
        }
        if (len == 1) {
            return arrays[0];
        }
        int arrayLen = arrays[0].length;
        checkArraySizes(arrays, len, arrayLen);

        byte[] result = new byte[arrayLen];
        for (int i = 0, mean; i < arrayLen; i++) {
            mean = 0;
            for (byte[] bts : arrays) {
                mean += Bytes.toUnsignedInt(bts[i]);
            }
            result[i] = Bytes.fromUnsignedInt(mean / len);
        }
        return result;
    }

    public static byte[] arrayMax(byte[][] arrays) {
        int len = arrays.length;
        if (len < 2) {
            throw new InvalidArrayException("Too few arrays given, must be at least 2");
        }
        int arrayLen = arrays[0].length;
        checkArraySizes(arrays, len, arrayLen);

        byte[] result = new byte[arrayLen];
        for (int i = 0; i < arrayLen; i++) {
            byte max = -128;
            for (int j = 0; j < len; j++) {
                max = (byte) Math.max(max, arrays[j][i]);
            }
            result[i] = max;
        }
        return result;
    }

    public static byte[] arrayBinOr(byte[][] arrays) {
        int len = arrays.length;
        if (len < 2) {
            throw new InvalidArrayException("Too few arrays given, must be at least 2");
        }
        int arrayLen = arrays[0].length;
        checkArraySizes(arrays, len, arrayLen);

        byte[] result = new byte[arrayLen];
        for (int i = 0, or; i < arrayLen; i++) {
            or = 0;
            for (int j = 0; j < len; j++) {
                or |= Bytes.toUnsignedInt(arrays[j][i]);
            }
            result[i] = Bytes.fromUnsignedInt(or);
        }
        return result;
    }

    private static void checkArraySizes(byte[][] arrays, int len, int arrayLen) {
        for (int i = 1; i < len; i++) {
            if (arrayLen != arrays[i].length) {
                throw new OutOfRangeException("Array sizes do not match");
            }
        }
    }

}

package planespotter.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import planespotter.dataclasses.*;
import planespotter.throwables.IllegalInputException;
import planespotter.throwables.InvalidDataException;
import planespotter.throwables.OutOfRangeException;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static planespotter.util.math.MathUtils.divide;

/**
 * @name Utilities
 * @author @all
 * @version 1.0
 *
 * class Utilities contains different utilities
 */
public abstract class Utilities {

    /**
     * plays a sound from the default toolkit
     *
     * @param sound is the sound to be played
     * @see planespotter.constants.Sound
     */
    public static boolean playSound(@NotNull final String sound) {
        var sound2 = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty(sound);
        if (sound2 != null) {
            sound2.run();
            return true;
        }
        return false;
    }

    public static long decToHex(int dec) {
        return Long.parseLong(Integer.toHexString(dec), 16);
    }

    public static final Map<Character, Integer> charIntValues = new HashMap<>(16);
    static {
        charIntValues.put('0', 0x000000);
        charIntValues.put('1', 0x000001);
        charIntValues.put('2', 0x000002);
        charIntValues.put('3', 0x000003);
        charIntValues.put('4', 0x000004);
        charIntValues.put('5', 0x000005);
        charIntValues.put('6', 0x000006);
        charIntValues.put('7', 0x000007);
        charIntValues.put('8', 0x000008);
        charIntValues.put('9', 0x000009);
        charIntValues.put('a', 0x00000A);
        charIntValues.put('b', 0x00000B);
        charIntValues.put('c', 0x00000C);
        charIntValues.put('d', 0x00000D);
        charIntValues.put('e', 0x00000E);
        charIntValues.put('f', 0x00000F);
    }

    public static int hexStrToInt(String hexStr) {
        char[] chars = hexStr.toCharArray();
        int pow = 0x000000,
            hex = 0x000000,
            num;
        for (char c : chars) {
            num = charIntValues.get(c);
            hex += num * StrictMath.pow(0x000010, pow);
            pow += 0x000001;
        }
        return hex;
    }

    public static byte toByteLevel(int lvl, int max) {
        if (lvl == 0 || max == 0) {
            return 0;
        }
        float lvlPercentage = (float) divide((float) lvl, max);
        return (byte) ((255 * lvlPercentage) - 128);
    }

    /**
     * @param feet is the input, in feet (ft)
     * @return a feet value in meters
     */
    public static int feetToMeters(@Range(from = 0, to = Integer.MAX_VALUE) int feet) {
        return asInt(feet / 3.2808);
    }

    /**
     * @param kn is the input, in knots (kn)
     * @return the knots in km per hour
     */
    public static int knToKmh(@Range(from = 0,to = Integer.MAX_VALUE) int kn) {
        return asInt(Math.round(kn * 1.852));
    }

    /**
     * packs a string in the format 'myText'
     *
     * @param input is the string to pack
     * @return packed input string with 's
     */
    public static String packString(@NotNull String input) {
        return "'" + input + "'";
    }

    /**
     * strips a string to the right format
     * Example: from ' "Hello" ' to ' Hello '
     *
     * @param in is the string to strip
     * @return input-string, but without the "s
     */
    public static String stripString(@NotNull String in) {
        return in.replaceAll("\"", "");
    }

    /**
     * checks a string for illegal characters or expressions
     *
     * @param check is the (sql) string to check
     * @return string, without illegal characters/expressions
     */
    public static String checkString(@NotNull String check)
        throws IllegalInputException {

        if (       check.contains("*")      || check.contains(";")
                || check.contains("SELECT") || check.contains("select")
                || check.contains("JOIN")   || check.contains("join")
                || check.contains("DROP")   || check.contains("drop")
                || check.contains("INSERT") || check.contains("insert")
                || check.contains("FROM")   || check.contains("from")
                || check.contains("TABLE")) {
            throw new IllegalInputException();
        }
        return check.replaceAll("%", "");
    }

    /**
     * @param number is the number to be casted to an int
     * @param <N> is an instance of Number
     * @return number cast as int
     */
    public static <N extends Number> int asInt(N number) {
        return number.intValue();
    }

    /**
     * parses a vector of data points to a vector of positions
     *
     * @param toParse is the Data Vector to parse
     * @return Vector of Positions
     */
    public static <T extends Data> Vector<Position> parsePositionVector(@NotNull final Vector<T> toParse) {
        if (toParse.isEmpty()) {
            throw new IllegalArgumentException("input may not be empty!");
        }
        var firstElement = toParse.get(0);
        if (firstElement instanceof DataPoint) {
            return toParse.stream()
                    .map(data -> ((DataPoint) data).pos())
                    .collect(Collectors.toCollection(Vector::new));
        } else if (firstElement instanceof Flight) {
            return toParse.stream()
                    .map(flight -> {
                        var dps = ((Flight) flight).dataPoints();
                        return dps.get(dps.size() - 1).pos();
                    })
                    .collect(Collectors.toCollection(Vector::new));
        }
        throw new InvalidDataException("Invalid input data, Must be a Vector of DataPoints or Flights!");
    }

    /**
     * parses a flight vector to a data point vector
     *
     * @param flights are the flights to parse
     * @return vector of data points
     */
    public static Vector<DataPoint> parseDataPointVector(@NotNull final Vector<Flight> flights) {
        if (flights.isEmpty()) {
            throw new IllegalArgumentException("input may not be empty!");
        }
        var dataPoints = new Vector<DataPoint>();
        flights.parallelStream()
                .forEach(flight -> dataPoints.addAll(flight.dataPoints().values()));
        return dataPoints;
    }

    /**
     *
     *
     * @param arrayOrCollection
     * @param <T>
     * @return
     */
    public static <T, R> Deque<R> parseDeque(T arrayOrCollection) {
        return (arrayOrCollection instanceof Collection<?> collection)
                ? new ArrayDeque<>((Collection<R>) collection)
                : new ArrayDeque<>((Collection<R>) List.of(arrayOrCollection));
    }

    public static DataPoint[] parseArray(Deque<DataPoint> deque) {
        return deque.toArray(DataPoint[]::new);
    }

    /**
     *
     *
     * @param toCheck
     * @param topLeft
     * @param bottomRight
     * @return
     */
    public static boolean fitArea(final Position toCheck, final Position topLeft, final Position bottomRight) {
        double checkLat = toCheck.lat(),
               checkLon = toCheck.lon();
        return     checkLat < topLeft.lat()
                && checkLat > bottomRight.lat()
                && checkLon > topLeft.lon()
                && checkLon < bottomRight.lon();
    }

    /**
     * @param altitude is the altitude from the given DataPoint
     * @return a specific color, depending on the altitude
     */
    public static Color colorByAltitude(int altitude) {
        if (altitude < 0) {
            throw new IllegalArgumentException("altitude out of range! (0-765)");
        }
        long meters = Utilities.feetToMeters(altitude);
        int maxHeight = 15000;
        int r = 255,
                g = 0,
                b = 0;
        int factor = (255 / 50);
        for (long i = 0; i < maxHeight;) {
            if (meters <= i) {
                return new Color(r, g, b);
            } else {
                if (i < 7000) {
                    g += factor * 2;
                } else {
                    if (r > 0) {
                        r -= factor * 4;
                        if (r < 0) { // gebraucht?
                            r = 0;
                        }
                    } else {
                        b += factor * 4;
                    }
                }
                i += 300;
            }
        }
        return new Color(r, g, 255);
    }

    // TODO: 24.06.2022 auf BYTES umstellen
    public static Color colorByLevel(final int level) { // max level: 255 TODO change to 127
        if (level < 0 || level > 255) {
            throw new OutOfRangeException("level out of range! (0-255)");
        }
        int r = 255,
            g = 255,
            b = 255,
            a = 255; // TODO: 0 oder 255 ???
        if (level == 0) {
            return new Color(r, g, b, a);
        }
        a = 0;
        r = 100;
        g = 100;
        for (int i = 0; i < 255 && i < level; i++) {
            if (i < 150 && g < 255) {
                g += 5;
            }
            if (i > 180) {
                if (r < 255) r += 5;
                if (g > 0) g -= 5;
            }
            if (b > 0) {
                b -= 5;
            }
        }
        return new Color(r, g, b);
    }

    public static ImageIcon scaledImage(ImageIcon input, int width, int height) {
        var scaled = input.getImage().getScaledInstance(width, height, 4);
        return new ImageIcon(scaled);
    }

    public static int linesCode(@NotNull final String rootPath, @NotNull String... extensions) {
        var linesCode = new AtomicInteger(0);
        var allFiles = new ArrayDeque<Deque<File>>();

        for (var ext : extensions) {
            allFiles.add(allFilesWithExtension(rootPath, ext));
        }

        while (!allFiles.isEmpty()) {
            var files = allFiles.poll();
            while (!files.isEmpty()) {
                try {
                    var fr = new FileReader(files.poll());
                    var br = new BufferedReader(fr);
                    br.lines().forEach(l -> {
                        if (!l.isBlank()) {
                            linesCode.getAndIncrement();
                        }
                    });
                    br.close();
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return linesCode.get();
    }

    private static Deque<File> allFilesWithExtension(final String path, String extension) {
        if (!extension.startsWith(".")) {
            throw new InvalidDataException("extension must begin with '.'");
        }
        var files = new ArrayDeque<File>();
        try (var paths = Files.walk(java.nio.file.Paths.get(path))) {
            paths.forEach(p -> {
                var file = p.toFile();
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    files.add(file);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

}

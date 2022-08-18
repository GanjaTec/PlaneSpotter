package planespotter.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import org.jetbrains.annotations.TestOnly;
import planespotter.constants.UnicodeChar;
import planespotter.controller.Controller;
import planespotter.dataclasses.*;
import planespotter.throwables.*;
import planespotter.util.math.MathUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @name Utilities
 * @author jml04
 * @author Lukas
 * @author Bennet
 * @version 1.0
 * @description
 * class Utilities contains different utility-methods for different usages
 */
public abstract class Utilities {

    // char-values connected to hex-int-values
    public static final Map<Character, Integer> charIntValues = new HashMap<>(16);

    // initialing the char-int map
    static {
        initCharInts();
    }

    /**
     * initializes the charIntValues-Map which contains char-values connected to hex-int-values
     */
    private static void initCharInts() {
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

    /**
     * getter for the project root-directory,
     * should only be invoked while starting the program
     *
     * @return absolute root directory name as String
     */
    @NotNull
    public static String getAbsoluteRootPath() {
        return (Controller.ROOT_PATH == null) ? System.getProperty("user.dir") : Controller.ROOT_PATH;
    }

    /**
     * plays a sound from the default toolkit
     *
     * @param sound is the sound to be played
     * @see planespotter.constants.Sound
     */
    public static boolean playSound(@NotNull final String sound) {
        Runnable sound2 = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty(sound);
        if (sound2 != null) {
            sound2.run();
            return true;
        }
        return false;
    }

    /**
     * @indev
     * rotates an image by certain degrees
     * the rotating technique is a bit tricky, because we cannot
     * just do something like img.rotate(...), we have to create
     * a new, rotated Graphics first and then draw the image on it.
     *
     * @param img is the {@link Image} that should be rotated
     * @param degrees is the degree of the rotation, from 0 to 360
     * @return {@link BufferedImage} with specific rotation
     */
    @NotNull
    public static BufferedImage rotate(@NotNull Image img, @Range(from = 0, to = 360) int degrees) {
        int width = img.getWidth(null);
        int height = img.getHeight(null);

        BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = buf.createGraphics();

        graphics.rotate(Math.toRadians(degrees), MathUtils.divide(width, 2), MathUtils.divide(height, 2));
        graphics.drawImage(img, 0, 0, null);

        return buf;
    }

    /**
     * finds the highest value of an 2D int array
     *
     * @param of is the 2D int array to search in
     * @return the highest value found in the input array
     */
    public static int maxValue(int[][] of) {
        if (of == null || of.length == 0) {
            throw new InvalidArrayException("Invalid input, no empty arrays allowed!");
        }
        return findMax2D(of);
    }

    /**
     * finds the highest value in a 2D-array in O(n)
     *
     * @param in is the input-2D-array to search in
     * @return max value of the input int-array
     */
    private static int findMax2D(int[][] in) {
        int width = in.length;
        if (width == 0) {
            return 0;
        }
        int height = in[0].length;
        if (height == 0) {
            return 0;
        }
        int max = 0;
        // TODO maybe this could be done parallel for huge 2d arrays
        for (int[] line : in) {
            for (int curr : line) {
                if (curr > max) {
                    max = curr;
                }
            }
        }
        return max;
    }

    /**
     * converts a decimal-int to hex-int,
     * does not work yet
     *
     * @param dec is the decimal
     * @return hexadecimal of the decimal-input
     */
    public static long decToHex(int dec) {
        return Long.parseLong(Integer.toHexString(dec), 16);
    }

    /**
     * converts a hex-string (e.g. "0xF62BA5" or "0xffa5b3") to an int
     *
     * @param hexStr is the hexadecimal input value as String
     * @return hex string as int
     */
    public static int hexStrToInt(@NotNull String hexStr) {
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

    /**
     * converts an int-level to a byte-level depending on
     * the max-int-level (calculated before)
     *
     * @param lvl is the level to convert
     * @param max is the max level, which must be calculated before this method
     * @return input level as byte level (-128 - 127)
     */
    public static byte toByteLevel(int lvl, int max) {
        if (lvl < 0 || max <= 0) {
            throw new OutOfRangeException("level or max-level is out of range (0-" + UnicodeChar.INFINITY + ")");
        } else if (lvl == 0) {
            return -128;
        }
        float lvlPercentage = (float) MathUtils.divide((float) lvl, max);
        return (byte) ((255 * lvlPercentage) - 128);
    }

    /**
     * converts a feet-value to a meters-value
     *
     * @param feet is the input, in feet (ft)
     * @return a feet value in meters
     */
    public static int feetToMeters(@Range(from = 0, to = Integer.MAX_VALUE) int feet) {
        return asInt(MathUtils.divide(feet, 3.2808));
    }

    /**
     * converts a knots-value to a km/h-value
     *
     * @param kn is the input, in knots (kn)
     * @return the knots in km per hour
     */
    public static int knToKmh(@Range(from = 0,to = Integer.MAX_VALUE) int kn) {
        return asInt(Math.round(kn * 1.852));
    }

    /**
     * packs a string in the format 'myText'
     *
     * @param str is the string to pack
     * @return packed input string with 's
     */
    @NotNull
    public static String packString(@NotNull String str) {
        return "'" + str + "'";
    }

    /**
     * strips a string to the right format
     * Example: from ' "Hello" ' to ' Hello '
     *
     * @param in is the string to strip
     * @return input-string, but without the "s
     */
    @NotNull
    public static String stripString(@NotNull String in) {
        return in.replaceAll("\"", "");
    }

    /**
     * checks a string for illegal characters or expressions
     *
     * @param check is the (sql) string to check
     * @return string, without illegal characters/expressions
     */
    @NotNull
    public static String checkString(@NotNull String check)
        throws IllegalInputException {

        if (       check.contains("*")      || check.contains(";")
                || check.contains("SELECT") || check.contains("select")
                || check.contains("JOIN")   || check.contains("join")
                || check.contains("DROP")   || check.contains("drop")
                || check.contains("INSERT") || check.contains("insert")
                || check.contains("FROM")   || check.contains("from")
                || check.contains("TABLE")  || check.contains("--")) {
            // throwing new exception because of illegal data in the input string
            throw new IllegalInputException("Input expressions or characters not allowed!");
        }
        // replacing all '%', to prevent inputs like '%.....%', which take too much time to search for
        // '%' is a SQL-placeholder for everything with any length
        return check.replaceAll("%", "");
    }

    /**
     * checks an array of input strings for illegal
     * characters or expressions
     *
     * @param inputs is the input string-array which is going to be checked
     * @return the checked array, without illegal expressions
     * @throws IllegalInputException if an illegal expression was found
     */
    @NotNull
    public static String[] checkInputs(@NotNull String... inputs)
            throws IllegalInputException {

        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = checkString(inputs[i]);
        }
        return inputs;
    }

    /**
     * converts a {@link Number} to an {@link Integer}, not directly by casting,
     * but by just calling the intValue() method from the Number class
     *
     * @param number is the number to be cast to an int
     * @param <N> is an instance of Number
     * @return number cast as int
     */
    public static <N extends Number> int asInt(@NotNull final N number) {
        return number.intValue();
    }

    /**
     * parses a vector of data points to a vector of positions
     *
     * @param toParse is the Data Vector to parse
     * @return Vector of Positions
     */
    @NotNull
    public static <T extends Serializable> Vector<Position> parsePositionVector(@NotNull final Vector<T> toParse) {
        if (toParse.isEmpty()) {
            throw new IllegalArgumentException("input may not be empty!");
        }

        T firstElement = toParse.get(0);

        if (firstElement instanceof DataPoint) {
            return toParse.stream()
                    .map(data -> ((DataPoint) data).pos())
                    .collect(Collectors.toCollection(Vector::new));

        } else if (firstElement instanceof Flight) {
            return toParse.stream()
                    .map(flight -> {
                        Map<Integer, DataPoint> dps = ((Flight) flight).dataPoints();
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
    @NotNull
    public static Vector<DataPoint> parseDataPointVector(@NotNull final Vector<Flight> flights) {
        if (flights.isEmpty()) {
            throw new IllegalArgumentException("input may not be empty!");
        }
        Vector<DataPoint> dataPoints = new Vector<>();
        flights.parallelStream()
                .forEach(flight -> dataPoints.addAll(flight.dataPoints().values()));
        return dataPoints;
    }

    /**
     * converts an array or a collection to Deque
     *
     * @param arrayOrCollection is the input value, any array or collection
     * @param <T> is the input type, stands for array or collection
     * @return Deque consisting of the input values
     */
    @NotNull
    public static <T> Deque<?> parseDeque(@NotNull T arrayOrCollection) {
        if (arrayOrCollection instanceof Collection<?> collection) {
            return new ArrayDeque<>(collection);
        } else try {
            return new ArrayDeque<>(List.of(arrayOrCollection));
        } catch (final Throwable thr) {
            throw new InvalidDataException("incorrect input, array or collection expected");
        }
    }

    /**
     * parses a {@link Deque} of {@link DataPoint}s to an array of {@link DataPoint}s
     *
     * @param deque is the DataPoint deque to convert
     * @return array of DataPoints
     */
    @NotNull
    public static DataPoint[] parseDataPointArray(@NotNull Deque<DataPoint> deque) {
        return deque.toArray(DataPoint[]::new);
    }

    /**
     * converts a {@link Deque} of {@link Integer}s to an int-array
     *
     * @param deque is the Integer-Deque to parse
     * @return converted int-array
     */
    public static int[] parseIntArray(@NotNull Deque<Integer> deque) {
        return Arrays.stream(deque.toArray(Integer[]::new))
                .mapToInt(Integer::intValue)
                .toArray();
    }

    /**
     * checks if a given position fits in the given area, spanned by topLeft and bottomRight position
     *
     * @param toCheck is the position to check
     * @param topLeft is the top left position of the area
     * @param bottomRight is the bottom right position of the area
     * @return true if toCheck fits in the given area, else false
     */
    @SuppressWarnings(value = "is this needed?")
    public static boolean fitArea(@NotNull final Position toCheck, @NotNull final Position topLeft, @NotNull final Position bottomRight) {
        double checkLat = toCheck.lat(),
               checkLon = toCheck.lon();
        return     checkLat < topLeft.lat()
                && checkLat > bottomRight.lat()
                && checkLon > topLeft.lon()
                && checkLon < bottomRight.lon();
    }

    /**
     * creates a {@link Color} depending on the given altitude,
     * starts with red, goes over yellow to green and light-blue (highest)
     *
     * @param altitude is the altitude from the given DataPoint
     * @return a specific color, depending on the altitude
     */
    @NotNull
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
                        if (r < 0) {
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

    /**
     * creates a certain color, depending on input level
     *
     * @param level is the input level for certain a color,
     *              must be between 0 and 255
     * @return color by level
     */
    // TODO: 24.06.2022 change to byte input
    @SuppressWarnings(value = "does not work yet")
    @NotNull
    public static Color colorByLevel(final int level) {
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

    /**
     * brings an image in a certain scale
     *
     * @param img is the input image that should be scaled
     * @param width is the image output width
     * @param height is the image output height
     * @return a scaled instance of the input image
     */
    @NotNull
    public static ImageIcon scaledImage(@NotNull ImageIcon img, int width, int height) {
        Image scaled = img.getImage().getScaledInstance(width, height, 4);
        return new ImageIcon(scaled);
    }

    /**
     * checks a {@link java.net.http.HttpResponse} status code,
     * does nothing if it is 200 (default),
     * if it is not 200, an exception is thrown
     *
     * @param status is the status code, given by the {@link java.net.http.HttpResponse}
     * @throws Fr24Exception when the status code is invalid
     */
    public static void checkStatusCode(int status) {
        if (status != 200) {
            throw new Fr24Exception("CheckStatus: Status code" + status + " is invalid!");
        }
    }

    /**
     * counts the lines of code with given file extensions
     *
     * @param rootPath is the root path to start counting
     * @param extensions are the file extensions
     * @return count of lines code (with the given extension)
     */
    public static int linesCode(@NotNull final String rootPath, @NotNull String... extensions) {
        if (extensions.length == 0) {
            throw new InvalidDataException("No given extensions!");
        }
        AtomicInteger linesCode = new AtomicInteger(0);
        Deque<Deque<File>> allFiles = new ArrayDeque<>();

        for (String ext : extensions) {
            if (!ext.startsWith(".")) {
                ext = "." + ext;
            }
            allFiles.add(allFilesWithExtension(rootPath, ext));
        }

        Deque<File> currentFiles;
        while (!allFiles.isEmpty()) {
            currentFiles = allFiles.poll();
            while (!currentFiles.isEmpty()) {
                try (   Reader fr = new FileReader(currentFiles.poll());
                        BufferedReader br = new BufferedReader(fr)) {

                    br.lines().forEach(l -> {
                        if (!l.isBlank()) {
                            linesCode.getAndIncrement();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return linesCode.get();
    }

    /**
     * returns all files, which have the given extension
     *
     * @param path is the root path to search in
     * @param extension is the file extension to search for (e.g. '.java')
     * @return Deque of all files with the given extension
     */
    @NotNull
    private static Deque<File> allFilesWithExtension(@NotNull final String path, @NotNull String extension) {
        if (!extension.startsWith(".")) {
            throw new InvalidDataException("extension must begin with '.'");
        }
        Deque<File> files = new ArrayDeque<>();
        Path start = Paths.get(path); // java.nio.file.Paths is not equal to our Paths-class
        // try with resource
        try (Stream<Path> paths = Files.walk(start)) {

            paths.forEach(p -> {
                File file = p.toFile();
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    files.add(file);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    @SuppressWarnings(value = "not working yet")
    @TestOnly
    public static <E> void printClassValues(E o) {
        Class<E> classOfO = (Class<E>) o.getClass();
        try {
            Field[] fields = classOfO.getDeclaredFields();
            Arrays.stream(fields).forEach(field -> {
                try {
                    field.setAccessible(true);
                    String name = field.getName();
                    System.out.println(name);
                    Object value = null;
                    if (field.getType() == String.class) {
                        value = field.get(name);
                    } else if (field.getType() == int.class) {
                        value = field.getInt(name);
                    } else if (field.getType() == double.class) {
                        value = field.getDouble(name);
                    }
                    System.out.println(name + ": " + value);
                } catch (InaccessibleObjectException | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

}

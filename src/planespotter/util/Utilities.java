package planespotter.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.TestOnly;
import planespotter.constants.Areas;
import planespotter.constants.UnicodeChar;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.Position;
import planespotter.model.Scheduler;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Fr24Supplier;
import planespotter.throwables.*;
import planespotter.util.math.MathUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @name Utilities
 * @author jml04
 * @author Lukas
 * @author Bennet
 * @version 1.0
 *
 * @description
 * class Utilities contains different utility-methods for different usages
 */
public abstract class Utilities {

    /**
     * char-values connected to hex-int-values
     */
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
        return System.getProperty("user.dir") + "\\";
    }

    /**
     * does a connection pre-check for all given {@link URL}s
     *
     * @param timeoutMillis is the maximum request time
     * @param urls are the {@link URL}s to check
     * @throws Fr24Exception if a connection check failed
     */
    public static void connectionPreCheck(int timeoutMillis, @NotNull URL... urls) throws Fr24Exception {
        URLConnection conn;
        InetAddress address = null;
        String hostName;
        for (URL url : urls) {
            try {
                conn = url.openConnection();
                conn.setConnectTimeout(timeoutMillis);
                conn.connect();
                address = InetAddress.getByName(url.getHost());
                if (!address.isReachable(timeoutMillis)) {
                    throw new IOException();
                }
            } catch (IOException e) {
                hostName = address == null ? "N/A" : address.getHostName();
                throw new Fr24Exception("address " + hostName + "is not reachable!");
            }
        }
    }

    /**
     * creates an URI-{@link String} by its parts
     *
     * @param scheme is the URI scheme (e.g. http or https)
     * @param host is the URI hostname
     * @param port os the URI port
     * @param path is the URI path
     * @param query is the URI query
     * @param fragment is the URI fragment
     * @return a new URI-{@link String}, composed of these parts
     */
    @NotNull
    public static String createURI(@Nullable String scheme, @NotNull String host, @Nullable String port,
                                @Nullable String path, @Nullable String query, @Nullable String fragment) {

        if (host.isBlank()) {
            throw new InvalidDataException("Host may not be blank");
        }
        return  (scheme == null ? "" : scheme + "://") + host +
                (port == null ? "" : ":" + port + "/") +
                (path == null ? "" : path) +
                (query == null ? "" : "?" + query) +
                (fragment == null ? "" : "#" + fragment);


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
     * finds the highest value of an 2D int array,
     * 0 is the minimum value
     *
     * @param of is the 2D int array to search in
     * @return the highest value found in the input array
     */
    public static int maxValue(final int[][] of) {
        if (of == null) {
            throw new InvalidArrayException("Invalid input, array must not be null!");
        }
        int max = 0;
        for (int[] line : of) {
            max = findMax(line, max);
        }
        return max;
    }

    /**
     * finds the highest value in an array in O(n)
     *
     * @param in is the input-2D-array to search in
     * @param startValue is the start max-value
     * @return max value of the input int-array
     */
    private static int findMax(final int[] in, final int startValue) {
        int max = startValue;

        for (int curr : in) {
            if (curr > max) {
                max = curr;
            }
        }
        return max;
    }

    /**
     * parses a float to byte array
     *
     * @param f is the float to parse
     * @return byte array (length of 4) containing the float value
     */
    public static byte[] floatToBytes(float f) {
        return ByteBuffer.allocate(4).putFloat(f).array();
    }

    /**
     * parses a byte array to float
     *
     * @param bytes is the byte array to parse, should be with length 4
     * @return float, parsed from byte array
     */
    public static float bytesToFloat(byte[] bytes) {
        if (bytes.length < 4) {
            throw new InvalidDataException("byte array must at least contain 4 bytes");
        }
        return ByteBuffer.wrap(bytes).getFloat();
    }

    /**
     *
     * IMPORTANT: only use Bitmaps of gridSize 1.0 here
     *
     * @param bmp
     * @param minLvl
     * @return
     */
    public static Queue<String> calculateInterestingAreas1(@NotNull Bitmap bmp, byte minLvl) {
        if (bmp.width > 361) {
            throw new InvalidDataException("Bitmap is too huge, please use gridSize 1.0f here");
        }
        Queue<String> areas = new ArrayDeque<>();
        byte[][] bytes = bmp.getBitmap();
        for (int x = 0; x < bmp.width; x++) {
            for (int y = 0; y < bmp.height; y++) {
                if (bytes[x][y] < minLvl) {
                    continue;
                }
                areas.add(Areas.newArea(y, y + 1, x, x + 1));
            }
        }
        return areas;
    }

    @TestOnly
    public static Queue<String> calculateInterestingAreas2(double latGridSize, double lonGridSize, int interestingCount) {
        Queue<String> interesting = new ArrayDeque<>();
        String[] worldRaster = Areas.getWorldAreaRaster1D(latGridSize, lonGridSize);
        HttpResponse<String> response;
        Fr24Deserializer deserializer = new Fr24Deserializer();
        int count, reqCount = 0;
        for (String area : worldRaster) {
            System.out.println("Sending request " + reqCount++ + "...");
            try {
                response = new Fr24Supplier(area).sendRequest(5);
                count = (int) deserializer.deserialize(response).count();
                if (count >= interestingCount) {
                    interesting.add(area);
                }
                Scheduler.sleep(300L); // limit request rate
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Requests sent: " + reqCount);
        System.out.println("Interesting areas: " + interesting);
        return interesting;
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
        // got this calculation from internet, easier than the one before and more pretty numbers
        int rest = lvl % 256;
        return (byte) (rest - 256);
    }

    /**
     * converts a feet-value to a meters-value
     *
     * @param feet is the input, in feet (ft)
     * @return a feet value in meters
     */
    public static int feetToMeters(@Range(from = 0, to = Integer.MAX_VALUE) int feet) {
        return asInt(feet / 3.2808);
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
     * converts a {@link Number} to an {@link Integer}, not directly by casting,
     * but by just calling the intValue() method from the Number class
     *
     * @param number is the number to be cast to an int
     * @param <N> is an instance of Number
     * @return number cast as int
     */
    public static <N extends Number> int asInt(@NotNull final N number) {
        boolean primitive = number.getClass().isPrimitive();
        return primitive ? (int) number : number.intValue();
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
        } else if (arrayOrCollection instanceof Object[] arr) {
            return new ArrayDeque<>(List.of(arr));
        } else if (arrayOrCollection.getClass().isArray()) {
            int length = Array.getLength(arrayOrCollection);
            Deque<Object> dq = new ArrayDeque<>();
            for (int i = 0; i < length; i++) {
                dq.add(Array.get(arrayOrCollection, i));
            }
            return dq;
        }
        throw new InvalidDataException("incorrect input, array or collection expected!");
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
     * adds a custom icon to the {@link SystemTray}
     *
     * @param icon is the added icon ({@link Image})
     * @param onClick is the ({@link FunctionalInterface}) {@link ActionListener} which is executed on icon click
     * @return true if the icon was added to the {@link SystemTray}, else false
     */
    public static boolean addTrayIcon(@NotNull Image icon, @NotNull ActionListener onClick) {
        if (!SystemTray.isSupported()) {
            return false;
        }
        TrayIcon trayIcon = new TrayIcon(icon);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(onClick);
        try {
            SystemTray.getSystemTray().add(trayIcon);
            return true;
        } catch (AWTException e) {
            e.printStackTrace();
            return false;
        }
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
     * checks a {@link java.net.http.HttpResponse} status code,
     * does nothing if it is 200 (default),
     * if it is not 200, an exception is thrown
     *
     * @param status is the status code, given by the {@link java.net.http.HttpResponse}
     * @throws Fr24Exception when the status code is invalid
     */
    public static void checkStatusCode(int status) {
        String invalidMsg = "CheckStatus: Status code '" + status + "' is invalid!";
        StatusException stex = switch (status) {
            case 200, 201 -> null; // status code is OK
            case 403 -> new StatusException(status, invalidMsg + "\nError 403, Forbidden");
            case 451 -> new StatusException(status, invalidMsg + "\nSeems like there is a problem with the Http-header (User-Agent)!");
            default -> new StatusException(status, invalidMsg + "\nUnknown error!");
        };
        if (stex != null) {
            throw stex;
        }
    }


    // string utilities

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
     *
     *
     * @param filename
     * @param expectedFormat
     * @return
     */
    @NotNull
    public static String checkFileName(@NotNull String filename, @NotNull String expectedFormat) {
        if (filename.isBlank() || expectedFormat.isBlank()) {
            throw new InvalidDataException("No file name or format!");
        }
        expectedFormat = (expectedFormat.startsWith(".") ? "" : ".") + expectedFormat.toLowerCase();
        return filename.endsWith(expectedFormat) ? filename : filename + expectedFormat;
    }

    /**
     * checks an {@link URI} for illegal expressions
     *
     * @param uri is the {@link URI} to check
     * @throws IllegalInputException if an illegal expression was found
     */
    public static void checkUri(@NotNull URI uri) throws IllegalInputException {
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
            throw new IllegalInputException("Wrong scheme, URI must begin with 'http' or 'https'!");
        }
        // TODO: 08.09.2022 weitere checks
    }


    // image utilities

    /**
     * brings an image in a certain scale
     *
     * @param img is the input image that should be scaled
     * @param width is the image output width
     * @param height is the image output height
     * @return a scaled instance of the input image
     */
    @NotNull
    public static ImageIcon scale(@NotNull ImageIcon img, int width, int height) {
        Image scaled = img.getImage().getScaledInstance(width, height, 4);
        return new ImageIcon(scaled);
    }

    /**
     * rotates an image by certain degrees
     * the rotating technique is a bit tricky, because we cannot
     * just do something like img.rotate(...), we have to create
     * a new, rotated Graphics first and then draw the image on it.
     *
     * @param img is the {@link Image} that should be rotated
     * @param degrees is the degree of the rotation, from 0 to 360
     * @param imageType is the image type constant from {@link BufferedImage}
     * @param flipHorizontally indicates if the image should be flipped horizontally
     * @return {@link BufferedImage} with specific rotation
     */
    @NotNull
    public static BufferedImage rotate(@NotNull Image img, @Range(from = 0, to = 360) final int degrees, int imageType, boolean flipHorizontally) {
        final int width = img.getWidth(null);
        final int height = img.getHeight(null);

        BufferedImage buf = new BufferedImage(width, height, imageType);
        Graphics2D graphics = buf.createGraphics();

        graphics.rotate(Math.toRadians(degrees), (double) width / 2, (double) height / 2);
        if (flipHorizontally) {
            graphics.drawImage(img, width, 0, -width, height, null);
        } else {
            graphics.drawImage(img, 0, 0, null);
        }

        return buf;
    }

    /**
     * converts an {@link Image} of any type (mostly ToolkitImages) to {@link BufferedImage}
     *
     * @param img is the {@link Image} to be converted
     * @param imgType is the image type constant from {@link BufferedImage}.'...'
     * @return new {@link BufferedImage}, converted from any {@link Image} type
     */
    @NotNull
    public static BufferedImage createBufferedImage(Image img, int imgType) {
        BufferedImage buf = new BufferedImage(img.getWidth(null), img.getHeight(null), imgType);
        Graphics2D g = buf.createGraphics();
        g.drawImage(img, 0, 0, null);
        return buf;
    }


    // file utilities

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

    /**
     * checks a {@link File} for the right extension
     *
     * @param file is the checked file
     * @param expectedExtension is the expected file extension with the form '.abc'
     * @throws FileNotFoundException is thrown if the file is null, does not
     *                               exist or if the file name is blank
     * @throws ExtensionException if the given extension is invalid or if the given
     *                            file does not have the expected extension
     */
    public static void checkFile(@Nullable File file, @NotNull String expectedExtension)
            throws FileNotFoundException, ExtensionException {

        if (!expectedExtension.startsWith(".")) {
            throw new ExtensionException("File extension must begin with '.'");
        }
        if (file == null || !file.exists() || file.getName().isBlank()) {
            throw new FileNotFoundException("File check failed, check input file!");
        }
        if (!file.getName().endsWith(expectedExtension)) {
            throw new ExtensionException("File does not have the expected extension " + expectedExtension);
        }
    }


    // reflection utilities

    /**
     * finds the {@link Class} who called this method
     * equal to Reflection.getCallerClass()
     *
     * @return the caller class of this method as a {@link Class} object
     */
    public static Class<?> getCallerClass() {
        StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        return stackWalker.getCallerClass();
    }

    /**
     * this method prints all current field values
     * of the given object of any class
     *
     * @param o an object, containing the printed values
     * @param <E> is the object type (class)
     */
    @TestOnly
    public static <E> void printCurrentFields(@NotNull E o) {
        Class<?> classOfO = o.getClass();
        try {
            Field[] fields = classOfO.getDeclaredFields();
            Arrays.stream(fields).forEach(field -> {
                String name; Object value;
                try {
                    field.setAccessible(true);
                    name = field.getName();
                    value = field.get(o);
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

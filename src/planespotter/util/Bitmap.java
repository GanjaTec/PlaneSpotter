package planespotter.util;

import de.gtec.util.SimpleBenchmark;
import de.gtec.util.bmp.Filler;
import de.gtec.util.math.HighestValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.TestOnly;
import planespotter.dataclasses.Area;
import planespotter.dataclasses.Position;
import planespotter.throwables.InvalidArrayException;
import planespotter.throwables.InvalidDataException;
import planespotter.throwables.OutOfRangeException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Vector;

/**
 * @name Bitmap
 * @author jml04
 * @version 1.1
 *
 * @description
 * the Bitmap class represents a 2D-bitmap,
 * each value stands for a certain level (e.g. the flight count on this position).
 * The 2D-array represents a 2D-map with lat and lon values
 */
public class Bitmap {

    // minimum grid size to prevent OutOfMemoryErrors
    private static final float MIN_GRID_SIZE = 0.01f;

    // bitmap as 2D-byte array (memory-efficient),
    // it is not possible to use short- or int-arrays here
    // because they use too much memory
    private final byte[][] bitmap;

    // bitmap width and height
    public final int width, height;

    /**
     * constructor for Bitmap,
     * needs a filled 2D-bitmap-array which must be created before.
     * not easy to use, maybe look at static methods
     * Bitmap.fromInt2d(...) or Bitmap.fromPosVector(...)
     *
     * @param bitmap is the bitmap 2D-array (pre-filled)
     */
    public Bitmap(byte[][] bitmap) {
        this.bitmap = bitmap;
        this.width = bitmap.length;
        if (this.width == 0) {
            throw new InvalidArrayException("Array width and length must be higher or equals one!");
        }
        this.height = bitmap[0].length;
        if (this.height == 0) {
            throw new InvalidArrayException("Array width and length must be higher or equals one!");
        }
    }

    /**
     * creates a Bitmap from position-vector,
     * the higher a field value, the more positions in this field
     *
     * @param positions is the position vector, where each bitmap field
     *                  represents specific coordinates and each value stands
     *                  for the number of positions in a certain field
     * @param gridSize is the bitmap grid size, 1 is normal (360x180), 0.5 is the double (720x360)
     *                 (should be 0.05 or higher to prevent memory problems)
     * @return Bitmap instance, created by pos-vector under a certain grid size
     */
    @HighMemory(msg = "Huge 2D-arrays (with gridSize about 0.025 and lower) can cause OutOfMemoryError")
    @NotNull
    public static Bitmap fromPosVector(@NotNull Vector<Position> positions, @Range(from = 0, to = 2) float gridSize) {
        checkGridSize(gridSize);

        int width = (int) (360.0 / gridSize) + 1;
        int height = (int) (180.0 / gridSize) + 1;
        int[][] ints2d = new int[width][height];

        // nicht nötig
        //fillZeros(ints2d);

        // the parallel stream performs much better than
        // a normal for-loop, 3s against 12s, we can use
        // it because positions is a Vector (thread safe)
        positions.parallelStream()
                .forEach(pos -> {
                    int posX, posY;
                    posX = (int) ((pos.lon() + 180) / gridSize);
                    posY = (int) ((pos.lat() + 90) / gridSize);
                    ints2d[posX][posY]++;
                });

        writeGridSize(ints2d, gridSize, width, height);
        return Bitmap.fromInt2d(ints2d);
    }

    /**
     * creates a Bitmap from position-vector,
     * the higher a field value, the more positions in this field
     *
     * @param positions is the position vector, where each bitmap field
     *                  represents specific coordinates and each value stands
     *                  for the number of positions in a certain field
     * @param gridSize is the bitmap grid size, 1 is normal (360x180), 0.5 is the double (720x360)
     *                 (should be 0.05 or higher to prevent memory problems)
     * @param area is the {@link Area} or: the Bitmap region on the map
     * @return Bitmap instance, created by pos-vector under a certain grid size, but only a certain {@link Area}
     */
    public static Bitmap fromPosVector(@NotNull Vector<Position> positions, @Range(from = 0, to = 2) float gridSize, @NotNull Area area) {
        checkGridSize(gridSize);

        int width = (int) (360.0 / gridSize) + 1;
        int height = (int) (180.0 / gridSize) + 1;
        int[][] ints2d = new int[width][height];

        fillZeros(ints2d);

        double topLeftLat = area.getTopLeft().lat(),
               topLeftLon = area.getTopLeft().lon(),
               botRightLat = area.getBottomRight().lat(),
               botRightLon = area.getBottomRight().lon();

        positions.parallelStream()
                .forEach(pos -> {
                    int posX, posY;
                    double lat = pos.lat(), lon = pos.lon();
                    if (lat > topLeftLat || lat < botRightLat || lon < topLeftLon || lon > botRightLon) {
                        return;
                    }
                    posX = (int) ((lon + 180) / gridSize);
                    posY = (int) ((lat + 90) / gridSize);
                    ints2d[posX][posY]++;
                });

        // TODO: 27.11.2022 das ganze könnte man auch in der fromInt2d machen,
        //  dann direkt nur den area bereich in ein neues byte array kopieren

        // FIXME: 26.11.2022 FIX Bitmap Ausrichtung, NegativeArraySizeException,

        int sx = (int) ((topLeftLon + 180) / gridSize),
            sy = (int) ((topLeftLat + 90) / gridSize), // FIXME ey is greater than sy
            ex = (int) ((botRightLon + 180) / gridSize),
            ey = (int) ((botRightLat + 90) / gridSize),
            nw = ex - sx,
            nh = sy - ey;
        int[][] i2dNew = new int[nw][nh];

        // FIXME: 26.11.2022 ArrayIndexOutOfBounds
        for (int x_new = 0, x_old = sx; x_new < nw; x_new++, x_old++) {
            for (int y_new = 0, y_old = sy; y_new < nh; y_new++, y_old++) {
                i2dNew[x_new][x_old] = ints2d[x_old][y_old];
            }
        }

        writeGridSize(i2dNew, gridSize, nw, nh);
        return fromInt2d(i2dNew);
    }

    /**
     *
     *
     * @param ints2d
     * @param gridSize
     * @param width
     * @param height
     */
    private static void writeGridSize(int[][] ints2d, float gridSize, int width, int height) {
        // writing gridSize to the last 4 bytes
        int last = width - 1;
        byte[] gridSizeBytes = Utilities.floatToBytes(gridSize);
        for (int y = height - 4, i = 0; y < height; y++, i++) {
            ints2d[last][y] = gridSizeBytes[i] + 128;
        }
    }

    /**
     * fills a 2D-array with zeros
     *
     * @param ints2d is the 2D-array to fill
     */
    private static void fillZeros(int[][] ints2d) {
        for (int[] arr : ints2d) {
            Arrays.fill(arr, 0);
        }
    }

    private static void checkGridSize(float gridSize) {
        if (gridSize < MIN_GRID_SIZE) {
            throw new OutOfRangeException("grid size must be 0.02 or higher!");
        }
    }


    /**
     * creates a Bitmap from pre-filled 2D-int-array
     * (much easier than creating a Bitmap per constructor)
     *
     * @param ints2d is the input 2D-int array, which is automatically converted to byte-array
     * @return Bitmap from 2D-int array
     */
    @HighMemory(msg = "Huge 2D-arrays (with gridSize about 0.02 and lower) can cause OutOfMemoryErrors")
    @NotNull
    public static Bitmap fromInt2d(int[][] ints2d) {

        int width = ints2d.length;
        if (width == 0) {
            throw new InvalidArrayException("input array is empty, width out of range!");
        }
        int height = ints2d[0].length;
        if (height == 0) {
            throw new InvalidArrayException("input array is empty, height out of range!");
        }
        long start = Time.nowMillis();

        int max = de.gtec.util.Utilities.highestValue(ints2d);
        byte[][] bytes = new byte[width][height];

        System.out.println("Elapsed: " + Time.elapsedMillis(start) + " ms");

        Filler.fill(bytes, ints2d, width, height, max);

        return new Bitmap(bytes);
    }

    /**
     * creates a {@link Bitmap} from {@link Image} with filename
     *
     * @param filename is the image file name
     * @return {@link Bitmap} from image
     * @throws FileNotFoundException if the file was not found
     * @throws InvalidDataException if the image data is invalid
     */
    @NotNull
    public static Bitmap fromImage(@NotNull String filename)
            throws FileNotFoundException {

        BufferedImage img;
        File imgFile = new File(filename);
        if (!imgFile.exists()) {
            throw new FileNotFoundException("No Bitmap found for filename " + filename + ", file must end with '.bmp'");
        }
        try {
            img = ImageIO.read(imgFile);
        } catch (IOException e) {
            throw new InvalidDataException("Couldn't read bitmap image! Be sure you have a valid file type!");
        }
        return Bitmap.fromImage(img);
    }

    /**
     * creates a {@link Bitmap} from {@link BufferedImage}
     *
     * @param img is the {@link BufferedImage} to convert into {@link Bitmap}
     * @return {@link Bitmap}, converted from {@link BufferedImage}
     */
    @NotNull
    public static Bitmap fromImage(@NotNull BufferedImage img) {
        int width = img.getWidth(),
            height = img.getHeight();
        byte[][] bmpBytes = new byte[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmpBytes[x][y] = (byte) img.getRGB(x, y);
            }
        }
        return new Bitmap(bmpBytes);
    }

    /**
     * writes a {@link Bitmap} to '.bmp' file with {@link ImageIO}
     *
     * @param bitmap is the {@link Bitmap} to write
     * @param filename is the filename of the {@link Bitmap} file
     * @return the written {@link File}
     * @throws IOException if an error occurs during the write operation
     */
    public static File write(Bitmap bitmap, String filename)
            throws IOException {

        // using write method with file parameter
        return write(bitmap, new File(filename));
    }

    /**
     * writes a {@link Bitmap} to a specific {@link File}
     *
     * @param bitmap is the {@link Bitmap} to write
     * @param file is the {@link File} to write the {@link Bitmap} into
     * @return the written {@link File}
     * @throws IOException if an error occurs during the write operation
     */
    public static File write(Bitmap bitmap, File file)
            throws IOException {

        String filename = file.getName();
        if (filename.endsWith(".bmp")) {
            file = new File(filename + ".bmp");
        }
        ImageIO.write(bitmap.toImage(true), "BMP", file);
        return file;
    }

    /**
     *
     *
     * @return
     */
    @NotNull
    public static File writeToCSV(@NotNull Bitmap bitmap, @NotNull String filename) throws IOException {
        File file = new File(Utilities.checkFileName(filename, "csv"));
        try (Writer fw = new FileWriter(file)) {

            int c, len;
            for (byte[] arr : bitmap.getBitmap()) {
                c = 0;
                len = arr.length - 1;
                for (byte lvl : arr) {
                    fw.write(lvl + (c++ == len ? "\n" : ","));
                }
            }
        }
        return file;
    }

    /**
     * reads a {@link Bitmap} from a specific {@link File}
     *
     * @param filename is the name of the {@link File} to read
     * @return the read {@link Bitmap}
     * @throws FileNotFoundException if the file does not exist
     */
    public static Bitmap read(String filename)
            throws IOException {

        return Bitmap.fromImage(filename);
    }

    /**
     * converts this {@link Bitmap} to a {@link BufferedImage}
     *
     * @return {@link BufferedImage} displaying the {@link Bitmap}
     */
    public BufferedImage toImage(boolean rotateAndFlip) {
        var img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_GRAY);
        short lvl; Color color;
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                // this should be an unsigned byte,
                //  but there is no unsigned byte in Java,
                //  so we use short here
                lvl = (short) (255 - (this.bitmap[x][y] + 128));
                color = new Color(lvl, lvl, lvl);
                img.setRGB(x, y, color.getRGB());
            }
        }
        return rotateAndFlip
                ? Utilities.rotate(img, 180, BufferedImage.TYPE_BYTE_GRAY, true)
                : img;
    }

    /**
     * creates a 1D-byte-array from the 2D-{@link Bitmap}-array
     *
     * @return 1D-array of this {@link Bitmap}
     */
    public byte[] toByteArray() {
        byte[] bytes = new byte[this.width * this.height];
        for (int i = 0, x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++, i++) {
                bytes[i] = this.bitmap[x][y];
            }
        }
        return bytes;
    }

    @TestOnly
    public float getGridSize() {
        int last = width - 1;
        byte[] fBytes = new byte[] {
                bitmap[last][height - 1], bitmap[last][height - 2], bitmap[last][height - 3], bitmap[last][height - 4]
        };
        return Utilities.bytesToFloat(fBytes);
    }

    /**
     * getter for the bitmap 2D-array
     *
     * @return the 2D-bitmap-array
     */
    public byte[][] getBitmap() {
        return this.bitmap;
    }

    /**
     * overwritten equals() method, compares two object first,
     * then comparing the bitmap values
     *
     * @param obj is another {@link Bitmap} to compare
     * @return true if the given {@link Bitmap} is equals this {@link Bitmap}, else false
     */
    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof Bitmap bmp && Arrays.deepEquals(this.bitmap, bmp.bitmap));
    }

    /**
     * overwritten toString() method returns this {@link Bitmap} as a {@link String}
     *
     * @return {@link String} of this {@link Bitmap} object
     */
    @Override
    public String toString() {
        return "Bitmap[" + width + "x" + height + "]";
    }

}

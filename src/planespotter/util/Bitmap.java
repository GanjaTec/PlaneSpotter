package planespotter.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import planespotter.dataclasses.Position;
import planespotter.throwables.InvalidArrayException;
import planespotter.throwables.InvalidDataException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import static planespotter.util.math.MathUtils.divide;

/**
 * @name Bitmap
 * @author jml04
 * @version 1.0
 * @description
 * the Bitmap class represents a 2D-bitmap,
 * each value stands e.g. for a certain
 */
public class Bitmap {

    // static components

    /**
     * creates a Bitmap from pre-filled 2D-int-array
     * (much easier than creating a Bitmap per constructor)
     *
     * @param ints2d is the input 2D-int array, which is automatically converted to byte-array
     * @return Bitmap from 2D-int array
     */
    public static Bitmap fromInt2d(int[][] ints2d) {

        int width = ints2d.length;
        if (width == 0) {
            throw new InvalidArrayException("input array is empty, width out of range!");
        }
        int height = ints2d[0].length;
        if (height == 0) {
            throw new InvalidArrayException("input array is empty, height out of range!");
        }

        int max = Utilities.maxValue(ints2d);
        byte[][] bytes = new byte[width][height];
        int level;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                level = ints2d[x][y];
                bytes[x][y] = Utilities.toByteLevel(level, max);
            }
        }
        return new Bitmap(bytes);
    }

    /**
     * creates a Bitmap from position-vector,
     * the higher a field value, the more positions in this field
     *
     * @param positions is the position vector, where each bitmap field
     *                  represents specific coordinates and each value stands
     *                  for the number of positions in a certain field
     * @param gridSize is the bitmap grid size, 1 is normal (360x180), 0.5 is the double (720x360)
     * @return Bitmap instance, created by pos-vector under a certain grid size
     */
    public static Bitmap fromPosVector(Vector<Position> positions, @Range(from = 0, to = 2) float gridSize) {

        int width = (int) divide(360., gridSize) + 1;
        int height = (int) divide(180., gridSize) + 1;
        int[][] ints2d = new int[width][height];

        Arrays.stream(ints2d)
                .forEach(arr -> Arrays.fill(arr, 0));

        int posX, posY;
        for (Position pos : positions) {
            posX = (int) divide(pos.lon() + 180, gridSize); // FIXME: 26.06.2022
            posY = (int) divide(pos.lat() + 90, gridSize); // FIXME: 26.06.2022
            // if ? TODO if necessary
            //if (posX < width && posY < height) {
                ints2d[posX][posY]++;
            /*} else {
                ints2d[posX - 1][posY - 1]++;
            }*/
        }
        return Bitmap.fromInt2d(ints2d);
    }

    @NotNull
    public static Bitmap fromImage(@NotNull String filename) {
        BufferedImage img;
        File imgFile = new File(filename);
        if (!imgFile.exists()) {
            throw new InvalidDataException("No Bitmap found for filename " + filename + ", file must end with '.bmp'");
        }
        try {
            img = Bitmap.readImage(imgFile);
        } catch (IOException e) {
            throw new InvalidDataException("Couldn't read bitmap image! Be sure you have a valid file type!");
        }
        return Bitmap.fromImage(img);
    }

    /**
     *
     *
     * @param img
     * @return
     */
    @NotNull
    public static Bitmap fromImage(@NotNull BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        byte[][] bmpBytes = new byte[width][height];
        byte rgb;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                rgb = (byte) img.getRGB(x, y);
                System.out.println(rgb);
                bmpBytes[x][y] = rgb;
            }
        }
        return new Bitmap(bmpBytes);
    }

    /**
     *
     *
     * @param bitmap
     * @param file
     * @return
     */
    @Deprecated(since = ".bmp with ImageIO")
    public static File write0000(Bitmap bitmap, File file) {

        byte[] bytes;
        ImageOutputStream outputStream;
        long startTime = Time.nowMillis();
        try {
            bytes = bitmap.getByteArray();
            outputStream = new FileImageOutputStream(file);
            outputStream.writeInt(bitmap.width);
            outputStream.writeInt(bitmap.heigth);
            outputStream.write(bytes);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            long elapsed = Time.elapsedMillis(startTime);
            System.out.println("Wrote Bitmap file '" + file.getName() + "' in " + elapsed + " milliseconds!");
        }
        return file;
    }

    /**
     *
     *
     * @param bitmap
     * @param filename
     * @return
     */
    @Deprecated(since = ".bmp with ImageIO")
    public static File write(Bitmap bitmap, String filename)
            throws IOException {

        // using write method with file parameter
        return write(bitmap, new File(filename));
    }

    /**
     *
     *
     * @param bitmap
     * @param file
     * @return
     * @throws IOException
     */
    public static File write(Bitmap bitmap, File file)
            throws IOException {

        ImageIO.write(bitmap.toImage(), "BMP", file);
        return file;
    }

    /**
     *
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    @Deprecated(since = ".bmp with ImageIO")
    public static Bitmap read0000(File file)
            throws IOException {

        if (!file.exists()) {
            throw new FileNotFoundException("File not found!");
        }
        FileImageInputStream inputStream;
        int width, heigth, length;
        byte[] bytes;
        byte[][] bitmap;
        inputStream = new FileImageInputStream(file);
        width = inputStream.readInt();
        heigth = inputStream.readInt();
        length = width * heigth;
        bytes = new byte[length];
        inputStream.read(bytes);
        inputStream.close();

        bitmap = new byte[width][heigth];
        int i = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < heigth; y++, i++) {
                bitmap[x][y] = bytes[i];
            }
        }
        return new Bitmap(bitmap);
    }

    @NotNull
    public static BufferedImage readImage(@NotNull File file)
        throws IOException {

        return ImageIO.read(file);
    }

    /**
     *
     *
     * @param filename
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap read(String filename)
            throws IOException {

        return Bitmap.fromImage(Bitmap.readImage(new File(filename)));
    }

    // instance fields

    // bitmap as 2D-byte array (memory-efficient)
    private final byte[][] bitmap;
    // bitmap width and height
    public final int width, heigth;

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
        this.heigth = bitmap[0].length;
    }

    /**
     *
     *
     * @return
     */
    public BufferedImage toImage() {
        var img = new BufferedImage(this.width, this.heigth, BufferedImage.TYPE_BYTE_GRAY);
        short lvl;
        Color color;
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.heigth; y++) {
                // this should be an unsigned byte,
                //  but there is no unsigned byte in Java,
                //  so we use short here
                lvl = (short) (255 - (this.bitmap[x][y] + 128));
                color = new Color(lvl, lvl, lvl);
                img.setRGB(x, y, color.getRGB());
                // TODO: 27.06.2022
 /*               var graphics = img.createGraphics();
                graphics.rotate(StrictMath.toRadians(180));
*/
            }
        }
        return img;
    }

    /**
     *
     *
     * @return
     */
    public byte[] getByteArray() {
        byte[] bytes = new byte[this.width * this.heigth];
        int i = 0;
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.heigth; y++, i++) {
                bytes[i] = this.bitmap[x][y];
            }
        }
        return bytes;
    }

    /**
     *
     *
     * @return
     */
    public byte[][] getBitmap() {
        return this.bitmap;
    }

    /**
     *
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Bitmap that = (Bitmap) obj;
        return Arrays.deepEquals(this.bitmap, that.bitmap);
    }

    /**
     *
     *
     * @return
     */
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     *
     *
     * @return
     */
    @Override
    public String toString() {
        return String.valueOf(this);
    }

}

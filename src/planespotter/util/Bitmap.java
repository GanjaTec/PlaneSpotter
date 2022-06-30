package planespotter.util;

import planespotter.dataclasses.Position;
import planespotter.statistics.HeatMap;
import planespotter.throwables.InvalidArrayException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import static planespotter.util.math.MathUtils.divide;

// TODO: 24.06.2022 MOVE BitmapIO methods to this class
public final class Bitmap {

    private final byte[][] bitmap;
    public final int width, heigth;

    public Bitmap(byte[][] bitmap) {
        this.bitmap = bitmap;
        this.width = bitmap.length;
        if (this.width == 0) {
            throw new InvalidArrayException("Array width and length must be higher or equals one!");
        }
        this.heigth = bitmap[0].length;
    }

    public static Bitmap fromInt2d(int[][] ints2d) {
        int width = ints2d.length;
        if (width == 0) {
            throw new InvalidArrayException("input array is empty, width out of range!");
        }
        int height = ints2d[0].length;
        if (height == 0) {
            throw new InvalidArrayException("input array is empty, height out of range!");
        }

        int max = HeatMap.maxValue(ints2d); // TODO move method to Utilities or MathUtils
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

    public static Bitmap fromPosVector(Vector<Position> positions, float gridSize) {
        //gridSize = (gridSize == 1) ? (float) 0.9972 : gridSize; // FIXME: 25.06.2022

        int width = (int) divide(360., gridSize) + 1;
        int height = (int) divide(180., gridSize) + 1;
        int[][] ints2d = new int[width][height];

        Arrays.stream(ints2d)
                .forEach(arr -> Arrays.fill(arr, 0));

        int posX, posY;
        for (var pos : positions) {
            posX = (int) divide(pos.lon() + 180, gridSize); // FIXME: 26.06.2022
            posY = (int) divide(pos.lat() + 90, gridSize); // FIXME: 26.06.2022
            // if ?
            if (posX < width && posY < height) {
                ints2d[posX][posY]++;
            } else {
                ints2d[posX - 1][posY - 1]++;
            }
        }
        return Bitmap.fromInt2d(ints2d);
    }

    public static Bitmap fromHeatMap(HeatMap heatMap) {
        return new Bitmap(heatMap.getHeatMap());
    }

    /**
     *
     *
     * @param bitmap
     * @param file
     * @return
     */
    public static File write(Bitmap bitmap, File file) {
        long startTime = System.currentTimeMillis();
        try {
            byte[] bytes = bitmap.getByteArray();
            var outputStream = new FileImageOutputStream(file);
            outputStream.writeInt(bitmap.width);
            outputStream.writeInt(bitmap.heigth);
            outputStream.write(bytes);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            float elapsed = (float) (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("Wrote Bitmap file '" + file.getName() + "' in " + elapsed + " seconds!");
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
    public static File write(Bitmap bitmap, String filename) {
        return write(bitmap, new File(filename));
    }

    public static File writeBmp(Bitmap bitmap, File file)
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
    public static Bitmap read(File file)
            throws IOException {

        if (!file.exists()) {
            throw new FileNotFoundException("File not found!");
        }
        FileImageInputStream inputStream;
        int width, heigth;
        byte[] bytes;
        byte[][] bitmap;
        inputStream = new FileImageInputStream(file);
        width = inputStream.readInt();
        heigth = inputStream.readInt();
        int length = width * heigth;
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

    /**
     *
     *
     * @param filename
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap read(String filename)
            throws IOException {

        return read(new File(filename));
    }

    public BufferedImage toImage() {
        var img = new BufferedImage(this.width, this.heigth, BufferedImage.TYPE_INT_RGB);
        short lvl;
        //var hexStr = new StringBuilder();
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.heigth; y++) {
                lvl = (short) (255 - (this.bitmap[x][y] + 128));
                //hexStr.append(Utilities.decToHex(lvl)).append(Utilities.decToHex(lvl)).append(Utilities.decToHex(lvl));
                //img.setRGB(x, y, (int) (lvl * MathUtils.x3(16))); // FIXME: 25.06.2022 right color code!
                //img.setRGB(x, y, (lvl == 0) ? Utilities.hexStrToInt(Integer.toHexString(Color.WHITE.getRGB())) : Utilities.hexStrToInt(Integer.toHexString(Color.BLACK.getRGB())));
                var color = new Color(lvl, lvl, lvl);
                img.setRGB(x, y, color.getRGB());
                // TODO: 27.06.2022
                var graphics = img.createGraphics();
                graphics.rotate(StrictMath.toRadians(180));
            }
        }
        return img;
    }

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

    public byte[][] getBitmap() {
        return this.bitmap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Bitmap) obj;
        return Arrays.deepEquals(this.bitmap, that.bitmap);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return String.valueOf(this);
    }

}

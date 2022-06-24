package planespotter.dataclasses;

import planespotter.throwables.InvalidArrayException;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

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
            outputStream.write(bitmap.width);
            outputStream.write(bitmap.heigth);
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

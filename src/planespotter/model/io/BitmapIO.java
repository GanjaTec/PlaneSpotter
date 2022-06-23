package planespotter.model.io;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @name BitmapIO
 * @author jml04
 * @version 1.0
 *
 * Class BitmapIO is responsible for bitmap input and output
 * uses IO-Streams to write byte[][] with high performance
 */
public abstract class BitmapIO {
// TODO change bitmap to byte
    /**
     *
     *
     * @param bitmap
     * @param file
     * @return
     */
    public static File write(short[][] bitmap, File file) {
        long startTime = System.currentTimeMillis();
        try {
            var outputStream = new FileImageOutputStream(file);
            for (short[] column : bitmap) {
                outputStream.writeShorts(column, 0, column.length);
            }
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
    public static File write(short[][] bitmap, String filename) {
        return BitmapIO.write(bitmap, new File(filename));
    }

    /**
     *
     *
     * @param file
     * @param target
     * @return
     * @throws FileNotFoundException
     */
    public static short[][] read(File file, short[][] target)
            throws FileNotFoundException {

        if (!file.exists()) {
            throw new FileNotFoundException("File not found!");
        }
        try {
            var inputStream = new FileImageInputStream(file);
            for (short[] column : target) {
                inputStream.readFully(column, 0, column.length);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return target;
    }

    /**
     *
     *
     * @param filename
     * @param bitmap
     * @return
     * @throws FileNotFoundException
     */
    public static short[][] read(String filename, short[][] bitmap)
            throws FileNotFoundException {

        return BitmapIO.read(new File(filename), bitmap);
    }

}

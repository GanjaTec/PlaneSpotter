package planespotter.util.combine;

import org.jetbrains.annotations.Nullable;
import planespotter.util.Bitmap;
import planespotter.util.Utilities;
import planespotter.util.math.MathUtils;
import planespotter.util.math.Size2D;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BitmapCombiner extends Combiner<Bitmap> {

    protected static final int MODE_MEAN = 1,
                               MODE_MAX  = 2,
                               MODE_BIN  = 3;

    protected static final Object C_LOCK = new Object();

    public BitmapCombiner(Bitmap... initElements) {
        super(initElements);
    }

    @Nullable
    protected Bitmap combineBmp(int len, int mode) {
        checkMode(mode);
        if (len < 2) {
            System.err.println("At least 2 Bitmaps are needed for combine-operations");
            return null;
        }
        // TODO: 19.10.2022 maybe replace with "Größe anpassen / skalieren"
        Size2D size = Utilities.checkBmpSize(getElements()); // represents the size (x = width, y = height)

        int width = size.width();
        int height = size.height();

        byte[][] result = new byte[width][height],
                params = new byte[len][height];
        Queue<CompletableFuture<Void>> futures = new ConcurrentLinkedQueue<>();

        switch (mode) {
            case MODE_MEAN -> {
                for (int x = 0, j = 0; x < width; x++, j++) {
                    futures.add(asyncMeanTask(len, result, params, x, j));
                }
            }
            case MODE_MAX -> {
                for (int x = 0, j = 0; x < width; x++, j++) {
                    futures.add(asyncMaxTask(len, result, params, x, j));
                }
            }
            case MODE_BIN -> {
                for (int x = 0, j = 0; x < width; x++, j++) {
                    futures.add(asyncBinTask(len, result, params, x, j));
                }
            }
        }
        while (!futures.isEmpty()) {
            futures.poll().join();
        }
        return new Bitmap(result);
    }

    private void checkMode(int mode) {
        if (mode < MODE_MEAN || mode > MODE_BIN) {
            throw new IllegalArgumentException("Mode not known, use BitmapCombiner mode constants instead");
        }
    }

    private CompletableFuture<Void> asyncMeanTask(int len, byte[][] result, byte[][] params, final int x, final int j) {
        return CompletableFuture.runAsync(() -> {
            for (int i = 0; i < len; i++) {
                byte[] arr = getElements()[i].getBitmap()[x];
                params[i] = arr;
            }
            byte[] mean = MathUtils.arrayMean(params);
            //synchronized (C_LOCK) {
            result[j] = mean;
            //}
        });
    }

    private CompletableFuture<Void> asyncMaxTask(int len, byte[][] result, byte[][] params, final int x, final int j) {
        return CompletableFuture.runAsync(() -> {
            for (int i = 0; i < len; i++) {
                byte[] arr = getElements()[i].getBitmap()[x];
                params[i] = arr;
            }
            byte[] max = MathUtils.arrayMax(params);
            //synchronized (C_LOCK) {
            result[j] = max;
            //}
        });
    }

    private CompletableFuture<Void> asyncBinTask(int len, byte[][] result, byte[][] params, final int x, final int j) {
        return CompletableFuture.runAsync(() -> {
            for (int i = 0; i < len; i++) {
                byte[] arr = getElements()[i].getBitmap()[x];
                params[i] = arr;
            }
            byte[] or = MathUtils.arrayBinOr(params);
            //synchronized (C_LOCK) {
            result[j] = or;
            //}
        });
    }

}

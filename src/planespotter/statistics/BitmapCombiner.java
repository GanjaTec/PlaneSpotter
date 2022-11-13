package planespotter.statistics;

import org.jetbrains.annotations.NotNull;
import planespotter.throwables.InvalidArrayException;
import planespotter.util.Bitmap;
import planespotter.util.math.Vector2D;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class BitmapCombiner extends Combiner<Bitmap> {

    private static final Object CLOCK = new Object();

    public BitmapCombiner(Bitmap... initElements) {
        super(initElements);
    }

    @Override
    @NotNull
    public BitmapCombiner combineAll() {
        if (elements() < 2) {
            System.err.println("2 Bitmaps are needed for combine-operations");
            return this;
        }
        Bitmap combined = combine(elements.toArray(Bitmap[]::new));
        result.set(combined);
        return this;
    }

    @NotNull
    public static Bitmap combine(@NotNull Bitmap... bmps) throws InvalidArrayException { // must not be caught
        int len = bmps.length;
        if (len < 2) {
            throw new InvalidArrayException("combine needs at least two arrays!");
        }
        // TODO: 19.10.2022 maybe replace with "Größe anpassen / skalieren"
        Vector2D<Integer> size = checkBmpSize(bmps); // represents the size (x = width, y = height)

        byte[][] newBytes = new byte[size.x][size.y];
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                newBytes[x][y] = -128;
            }
        }
        Queue<CompletableFuture<Void>> futures = new ArrayDeque<>();
        Arrays.stream(bmps)
                .map(Bitmap::getBitmap)
                .forEach(bitmap -> {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        for (int x = 0; x < size.x; x++) {
                            for (int y = 0; y < size.y; y++) {
                                synchronized (CLOCK) {
                                    newBytes[x][y] = (byte) ((newBytes[x][y] + bitmap[x][y]) / 2);
                                }
                            }
                        }
                    });
                    futures.add(future);
                });
        while (!futures.isEmpty()) {
            CompletableFuture<Void> future = futures.poll();
            if (!future.isDone()) {
                future.join();
            }
        }
        return new Bitmap(newBytes);
    }

    public BitmapCombiner addReCalc(@NotNull Bitmap... bmps) {
        addNoCalc(bmps);
        return combineAll();
    }

    public BitmapCombiner addNoCalc(@NotNull Bitmap... bmps) {
        elements.addAll(Arrays.asList(bmps));
        return this;
    }

    private static Vector2D<Integer> checkBmpSize(@NotNull Bitmap[] bmps) {
        int c = 0, w = 0, h = 0;
        for (Bitmap bmp : bmps) {
            if (c++ == 0) {
                w = bmp.getBitmap().length;
                h = bmp.getBitmap()[0].length;
            } else if (bmp.getBitmap().length != w || bmp.getBitmap()[0].length != h) {
                throw new InvalidArrayException("Array sizes do not match!");
            }
        }
        return new Vector2D<>(w, h);
    }

}

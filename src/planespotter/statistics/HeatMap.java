package planespotter.statistics;

import org.jetbrains.annotations.Nullable;
import planespotter.throwables.InvalidArrayException;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public abstract class HeatMap {

    protected byte[][] heatMap;
    protected final float gridSize;
    private int max;

    /**
     *
     * @param data is the data to heat
     * @return
     */
    protected abstract <D> HeatMap heat(D data);

    /**
     *
     * @return
     */
    protected abstract BufferedImage createImage();

    /**
     *
     * @return
     */
    protected abstract BufferedImage createBitmap();

    /**
     *
     * @param gridSize
     */
    protected HeatMap(final float gridSize, boolean create) {
        this.gridSize = gridSize;
        if (create) {
            this.createHeatMap();
        }
    }

    public static HeatMap nullHeatMap() {
        return new HeatMap(3, false) {
            @Override protected <D> HeatMap heat(D data) {
                return null;
            }

            @Override protected BufferedImage createImage() {
                return null;
            }

            @Override protected BufferedImage createBitmap() {
                return null;
            }
        };
    }

    /**
     *
     */
    protected void createHeatMap() {
        final int x = this.getWidth(),
                  y = this.getDepth();
        this.heatMap = new byte[x][y];
        for (byte[] bytes : this.heatMap) {
            Arrays.fill(bytes, (byte) -128);
        }
    }

    /**
     *
     * @return
     * @param maxHeatMap
     */
    protected final HeatMap transformLevels(int[][] maxHeatMap) {
        int width = this.getWidth();
        int heigth = this.getDepth();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < heigth; y++) {
                this.heatMap[x][y] = this.toByteLevel(maxHeatMap[x][y]);
            }
        }
        return this;
    }

    /**
     *
     * @param level
     * @return
     */
    private byte toByteLevel(int level) {
        // int prozent = wie viel prozent ist level von max
        // return prozent von 255
        final float lvlPercentage = (float) (level / this.max);
        final float lvl = (255 * lvlPercentage);
        if (lvl > 0 && lvl < 1) {
            return 1;
        } else {
            return (byte) ((int) lvl - 128);
        }

    }

    public static int maxValue(int[][] of) {
        return nullHeatMap().findMax(of).max;
    }

    /**
     *
     * @return
     */
    protected <B> HeatMap findMax(@Nullable B in) {
        this.max = -1;
        int width = this.getWidth();
        int heigth = this.getDepth();
        int current;
        int[][] arr2d = (in instanceof int[][] ints) ? ints : null;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < heigth; y++) {
                current = (arr2d == null) ? this.heatMap[x][y] : arr2d[x][y];
                if (current > this.max) {
                    this.max = current;
                }
            }
        }
        if (this.max == -1) {
            throw new InvalidArrayException();
        }
        return this;
    }

    /**
     *
     * @return
     */
    public byte[][] getHeatMap() {
        return this.heatMap;
    }

    /**
     *
     * @return
     */
    public int getWidth() {
        return (int) (180/this.gridSize);
    }

    /**
     *
     * @return
     */
    public int getDepth() {
        return (int) (360/this.gridSize);
    }

}

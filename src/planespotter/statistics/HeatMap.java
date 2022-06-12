package planespotter.statistics;

import planespotter.throwables.InvalidArrayException;
import planespotter.util.TaskWatchDog;

import java.awt.image.BufferedImage;

public abstract class HeatMap {

    protected short[][] heatMap;
    protected final float gridSize;
    private int max;

    protected final TaskWatchDog watchDog;

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
    protected HeatMap(final float gridSize) {
        this.watchDog = new TaskWatchDog();
        this.gridSize = gridSize;
        this.createHeatMap();
        this.watchDog.allocation(2);
    }

    /**
     *
     */
    protected void createHeatMap() {
        final int x = this.getWidth(),
                y = this.getDepth();
        this.heatMap = new short[x][y];
        for (int lat = 0; lat < x; lat++) {
            for (int lon = 0; lon < y; lon++) {
                this.heatMap[lat][lon] = 0;
                this.watchDog.allocation(2).comparison();
            }
            this.watchDog.allocation().comparison().array();
        }
        this.watchDog.allocation(3).array();
    }

    /**
     *
     * @return
     */
    protected final HeatMap transformLevels() {
        int width = this.getWidth();
        int heigth = this.getDepth();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < heigth; y++) {
                this.heatMap[x][y] = this.transformToLvl(this.heatMap[x][y]);
            }
        }
        return this;
    }

    /**
     *
     * @param level
     * @return
     */
    private short transformToLvl(int level) {
        // int prozent = wie viel prozent ist level von max
        // return prozent von 255
        final float lvlPercentage = (float) (level / this.max);
        float lvl = (255 * lvlPercentage);
        if (lvl > 0 && lvl < 1) {
            return 1;
        } else return (short) lvl;

    }

    /**
     *
     * @return
     */
    protected HeatMap findMax() {
        this.max = -1;
        int width = this.getWidth();
        int heigth = this.getDepth();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < heigth; y++) {
                int current = this.heatMap[x][y];
                if (current > this.max) {
                    this.max = current;
                }
            }
        }
        if (max == -1) {
            throw new InvalidArrayException();
        }
        return this;
    }

    /**
     *
     * @return
     */
    public short[][] getHeatMap() {
        return this.heatMap;
    }

    /**
     *
     * @return
     */
    public int getWidth() {
        return (int) (360/this.gridSize);
    }

    /**
     *
     * @return
     */
    public int getDepth() {
        return (int) (180/this.gridSize);
    }

}

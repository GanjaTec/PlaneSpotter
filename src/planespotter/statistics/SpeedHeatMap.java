package planespotter.statistics;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Position;
import planespotter.throwables.OutOfRangeException;
import planespotter.util.Utilities;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class SpeedHeatMap extends HeatMap {

    private final Position topLeft, bottomRight;

    /**
     * @param gridSize
     */
    public SpeedHeatMap(float gridSize, Position topLeft, Position bottomRight) {
        super(gridSize);
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        super.watchDog.allocation(2);
    }

    // TODO: 31.05.2022 datenstrukturen verbessern, Hash Map ist langsam, am besten array
    @Override
    public  <D> HeatMap heat(D data) {
        if (data instanceof HashMap<?,?> speedMap) {
            // für jede pos -> bereich checken und dort 1 erhöhen
            var adds = new HashMap<Position, Integer>();
            super.watchDog.allocation().comparison();

            speedMap.forEach((k, v) -> {
                if (   k instanceof Position pos
                    && v instanceof Integer speed) {

                    int x = Utilities.asInt(pos.lat() + 180),
                        y = Utilities.asInt(pos.lon() + 90);
                    var key = new Position(x, y);
                    if (adds.containsKey(key)) {
                        adds.replace(key, adds.get(key) + 1);
                    } else {
                        adds.put(key, 1);
                    }
                    this.heatMap[x][y] += speed;
                    super.watchDog.allocation(10).comparison();
                }
            });
            adds.forEach((pos, addCount) -> {
                int x = (int) pos.lat(),
                    y = (int) pos.lon();
                short speed = this.heatMap[x][y];
                this.heatMap[x][y] = (short) (speed / addCount);
                super.watchDog.allocation(5);
            });
            super.watchDog.array().array().print();
        }
        return this;
    }

    @Override
    public BufferedImage createImage() {
        return null;
    }

    @Override
    public BufferedImage createBitmap() {
        return null;
    }


    private static class SpeedMapEntry {
        final Position pos;
        int speed;
        int addCount;

        private SpeedMapEntry(@NotNull Position pos, int speed) {
            if (speed < 0) {
                throw new OutOfRangeException("Speed may not be negative!");
            }
            final int lat = (int) pos.lat(),
                      lon = (int) pos.lon();
            this.pos = new Position(lat, lon);
            this.speed = speed;
            this.addCount = 1;
        }

        private void addSpeed(final int speed) {
            this.speed += speed;
            this.addCount++;
        }

        private int avgSpeed() {
            return this.speed / this.addCount;
        }
    }

}

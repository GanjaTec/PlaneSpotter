package planespotter.statistics;

import org.jetbrains.annotations.NotNull;
import planespotter.controller.Controller;
import planespotter.dataclasses.Position;
import planespotter.display.TreasureMap;
import planespotter.throwables.InvalidCoordinatesException;
import planespotter.throwables.InvalidDataException;
import planespotter.util.Utilities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Vector;

/**
 * @name RectHeatMap
 * @author jml04
 * @version 1.0
 *
 * class RectHeatMap contains a 2D-HeatMap-Array from the whole world map
 */
public class RasterHeatMap extends HeatMap {

    public RasterHeatMap(float gridSize) {
        super(gridSize);
    }

    /**
     *
     * @param data are the positions, must be instance of Vector<Position>
     */
    @Override
    public <D> RasterHeatMap heat(D data) {
        if (data instanceof Vector<?> positions) {
            positions.parallelStream().forEach(pos -> {
                try {
                    this.assign(((Position) pos).lat() + 180, ((Position) pos).lon() + 90, (Position) pos);
                    super.watchDog.allocation(3).comparison();
                } catch (InvalidCoordinatesException e) {
                    e.printStackTrace();
                }
            });
            super.watchDog.print();
            return (RasterHeatMap) this.findMax().transformLevels();
        }
        throw new InvalidDataException("data must be instance of Vector<Position>!");
    }

    /**
     *
     * @param atX
     * @param atY
     * @param position
     * @throws InvalidCoordinatesException
     */
    // TODO einfacher machen
    private void assign(double atX, double atY, @NotNull Position position)
            throws InvalidCoordinatesException {
        // length and with
        final int x = super.getWidth(),
                y = super.getDepth();
        if (atX < 0 || atX > x || atY < 0 || atY > y) {
            throw new IllegalArgumentException("coordinates out of range!");
        }
        var posLat = position.lat()+180;
        var posLon = position.lon()+90;
        int assignLat = Integer.MAX_VALUE, assignLon = Integer.MAX_VALUE;
        super.watchDog.allocation(8).comparison(4);
        for (int lat = 0; lat < x; lat++) {
            if (posLat > lat) {
                if (posLat < lat+1) {
                    assignLat = lat+1;
                    super.watchDog.allocation(3).comparison(2);
                    break;
                }
            } else { // needed?
                assignLat = lat;
                super.watchDog.allocation().comparison();
                break;
            }
            super.watchDog.allocation().comparison();
        }
        super.watchDog.array();
        for (int lon = 0; lon < y; lon++) {
            if (posLon > lon) {
                if (posLon < lon+1) {
                    assignLon = lon+1;
                    super.watchDog.allocation(3).comparison(2);
                    break;
                }
            } else { // needed?
                assignLon = lon;
                super.watchDog.allocation().comparison(2);
                break;
            }
            super.watchDog.comparison().allocation();
        }
        super.watchDog.array();
        if (assignLat == Integer.MAX_VALUE || assignLon == Integer.MAX_VALUE) {
            throw new InvalidCoordinatesException();
        }
        super.heatMap[assignLat][assignLon] = (short) (super.heatMap[assignLat][assignLon] + 1);
        super.watchDog.allocation().comparison(2);
    }

    public BufferedImage createImage() {
        assert this.heatMap != null;
        var gui = Controller.getGUI();
        var map = gui.getMap();
        var mapSize = new Rectangle(gui.getMap().getWidth(), gui.getMap().getHeight());
        return this.fitCurrentRect(map, super.heatMap);

        /*final int htmpX = this.getX(),
                  htmpY = this.getY();
        var img = new BufferedImage(htmpX, htmpY, BufferedImage.TYPE_INT_RGB);
        int lvl;
        for (int x = 0; x < htmpX; x++) {
            for (int y = 0; y < htmpY; y++) {
                lvl = this.heatMap[x][y];
                img.setRGB(x, y, Utilities.colorByLevel(lvl).getRGB());
            }
        }*/
    }

    private BufferedImage fitCurrentRect(TreasureMap viewer, short[][] heatMap) {
        final int width = viewer.getWidth(),
                  height = viewer.getHeight();
        // TODO: 31.05.2022 MULTIPLIKATOR ausrechnen, schwierig (bessere alternative?)
        final var startPos = viewer.getPosition(0, 0);
        final var endPos = viewer.getPosition(width, height);
        final int startLat = (int) (startPos.getLat() + 180),
                  startLon = (int) (startPos.getLon() + 90),
                  endLat = (int) (endPos.getLat() + 180),
                  endLon = (int) (endPos.getLon() + 90);
        final var img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // TODO abstand zwischen start und end lat / lon (mit gridSize verrechnen) evtl multi
        Color color;
        for (int x = startLat; x < endLat; x++) {
            for (int y = startLon; y < endLon; y++) {
                color = Utilities.colorByLevel(heatMap[x][y]);
                img.setRGB(x, y, color.getRGB());
            }
        }
        return img;
    }

    @Override
    public final BufferedImage createBitmap() {
        assert this.heatMap != null;
        int width = this.getWidth(),
                height = this.getDepth();
        var img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        int lvl;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                lvl = this.heatMap[x][y];
                img.setRGB(x, y, new Color(lvl, lvl, lvl).getRGB());
            }
        }
        //img.getRaster().setPixels(0, 0, 100, 100, flattenedData);
        return img;
    }

}

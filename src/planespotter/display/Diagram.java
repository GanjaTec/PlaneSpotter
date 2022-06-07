package planespotter.display;

import planespotter.constants.DefaultColor;
import planespotter.constants.GUIConstants;

import javax.swing.*;
import java.awt.*;

public class Diagram extends JPanel {

    public static final int TYPE_BAR_CHART = 0,
                            TYPE_CAKE_CHART = 1,
                            TYPE_GRAPH_CHART = 2,
                            TYPE_HEATMAP_CHART = 3;

    // diagram type
    private final int type;
    private final short[][] heatMap;

    // TODO eventuell Klasse DiagramBuilder mit static create..Diagram methoden
    public Diagram(final int diagramType,
                   int width,
                   int heigth,
                   final short[]... heatMap) {
        this.type = diagramType;
        super.setBorder(GUIConstants.LINE_BORDER);
        if (heatMap != null) {
            width = heatMap.length * 2;
            heigth = heatMap[0].length * 2;
            this.heatMap = heatMap;
        } else {
            this.heatMap = null;
        }
        super.setSize(width, heigth);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2d = (Graphics2D) g;
        switch (this.type) {
            case TYPE_BAR_CHART -> {
                var color = DefaultColor.DEFAULT_ACCENT_COLOR.get();
                g2d.setColor(color);
            }
            case TYPE_CAKE_CHART -> this.paintTestCake(g2d);
            case TYPE_GRAPH_CHART -> {}
            case TYPE_HEATMAP_CHART -> {
                if (this.heatMap != null) {
                    this.paintHeatMap(g2d, this.heatMap);
                }
            }
        }
    }

    private void paintHeatMap(Graphics2D g2d, final short[][] heatMap) {
        int width = super.getWidth(),
            heigth = super.getHeight();
        var color = Color.WHITE;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < heigth; y++) {
                if (x % 2 == 0 && y % 2 == 0) {
                    //color = Utilities.colorByLevel(heatMap[x/2][y/2]);
                    if (heatMap[x/2][y/2] > 0) {
                        color = Color.BLACK;
                    } else {
                        color = Color.WHITE;
                    }
                }
                g2d.setColor(color);
                g2d.fillRect(x, y, 1, 1);
            }
        }
    }

    private void paintTestCake(Graphics2D g2d) {
        // TEST //
        g2d.setColor(Color.BLACK);
        g2d.drawArc(250, 80, 200, 200,  0, 360);
        int red = 0, green = 0, blue = 240;
        for (int a = 0; a < 360; a++) {
            if (a < 120) {
                blue -= 2;
                green += 2;
            } else if (a < 240) {
                green -= 2;
                red += 2;
            } else {
                red -= 2;
                blue += 2;
            }
            g2d.setColor(new Color(red, green, blue));
            g2d.fillArc(250, 80, 200, 200, a, 1);
        }
        red = 240; green = 0; blue = 0;
        for (int a = 0; a < 360; a++) {
            if (a < 120) {
                red -= 2;
                blue += 2;
            } else if (a < 240) {
                blue -= 2;
                green += 2;
            } else {
                green -= 2;
                red += 2;
            }
            g2d.setColor(new Color(red, green, blue));
            g2d.fillArc(280, 110, 140, 140, a, 1);
        }
        red = 0; green = 240; blue = 0;
        for (int a = 0; a < 360; a++) {
            if (a < 120) {
                green -= 2;
                red += 2;
            } else if (a < 240) {
                red -= 2;
                blue += 2;
            } else {
                blue -= 2;
                green += 2;
            }
            g2d.setColor(new Color(red, green, blue));
            g2d.fillArc(310, 140, 80, 80, a, 1);
        }
    }

}

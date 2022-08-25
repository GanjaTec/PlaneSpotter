package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import planespotter.constants.UnicodeChar;
import planespotter.constants.Warning;
import planespotter.display.models.LayerPane;
import planespotter.statistics.Statistics;
import planespotter.util.Bitmap;
import planespotter.util.Utilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.jfree.chart.ChartFactory.createBarChart;

/* probably not needed */
public class Diagrams {

    public static void showPosHeatMap(@NotNull UserInterface ui, @NotNull Bitmap bitmap) {
        showPosHeatMap(ui, bitmap.toImage());
    }

    public static void showPosHeatMap(@NotNull UserInterface ui, @NotNull BufferedImage bitmapImg) {
        LayerPane layerPane = ui.getLayerPane();
        layerPane.replaceBottom(heatMapPanel(layerPane, bitmapImg));
    }

    public static void showTopAirlines(UserInterface ui, Statistics stats) {
        var input = JOptionPane.showInputDialog("Please enter a minimum significance (0-" + UnicodeChar.INFINITY.get() + ")", 250);
        if (input == null || input.isBlank()) {
            return;
        }
        LayerPane layerPane = ui.getLayerPane();
        try {
            int minCount = Integer.parseInt(input);
            layerPane.replaceBottom(Diagrams.barChartPanel(layerPane, stats.airlineSignificance(minCount)));
        } catch (NumberFormatException nfe) {
            ui.showWarning(Warning.NUMBER_EXPECTED);
        }
    }

    public static void showTopAirports(UserInterface ui, Statistics stats) {
        var input = JOptionPane.showInputDialog("Please enter a minimum significance (0-" + UnicodeChar.INFINITY.get() + ")", 250);
        if (input == null || input.isBlank()) {
            return;
        }
        LayerPane layerPane = ui.getLayerPane();
        try {
            int minCount = Integer.parseInt(input);
            layerPane.replaceBottom(Diagrams.barChartPanel(layerPane, stats.airportSignificance(minCount)));
        } catch (NumberFormatException nfe) {
            ui.showWarning(Warning.NUMBER_EXPECTED);
        }
    }

    @NotNull
    private static JPanel heatMapPanel(@NotNull Component parent, @NotNull Bitmap bitmap) {
        return heatMapPanel(parent, bitmap.toImage());
    }

    @NotNull
    private static JPanel heatMapPanel(@NotNull Component parent, @NotNull File bitmapFile)
            throws FileNotFoundException {

        BufferedImage bitmapImg;
        try {
            bitmapImg = ImageIO.read(bitmapFile);
        } catch (IOException e) {
            throw new FileNotFoundException("Bitmap file not found or invalid!");
        }
        return heatMapPanel(parent, bitmapImg);
    }

    @NotNull
    private static JPanel heatMapPanel(@NotNull Component parent, @NotNull BufferedImage heatMapImg) {
        int width = parent.getWidth(),
            height = parent.getHeight();
        ImageIcon imgIcon = new ImageIcon(heatMapImg);
        JLabel imgLabel = new JLabel(Utilities.scale(imgIcon, width, height));
        JPanel panel = new JPanel(null);
        panel.setBounds(0, 0, width, height);
        imgLabel.setBounds(panel.getBounds());
        panel.add(imgLabel);
        return panel;
    }

    private static ChartPanel barChartPanel(Component parent, JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setBounds(parent.getBounds());
        return panel;
    }

    private static ChartPanel barChartPanel(Component parent, String title, String xName, String yName, CategoryDataset dataset) {
        JFreeChart barChart = createBarChart(title, xName, yName, dataset);
        //BufferedImage chartImage = barChart.createBufferedImage(width, height);
        ChartPanel panel = new ChartPanel(barChart);
        panel.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
        return panel;
        //return new Diagram(Diagram.TYPE_IMAGE, width, height, chartImage);
    }

}

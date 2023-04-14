package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import planespotter.constants.UnicodeChar;
import planespotter.constants.Warning;
import planespotter.display.models.LayerPane;
import planespotter.display.models.ZoomPane;
import planespotter.model.Statistics;
import planespotter.throwables.DataNotFoundException;
import planespotter.util.Bitmap;
import planespotter.util.Utilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static org.jfree.chart.ChartFactory.createBarChart;

/**
 * @name StatsView
 * @author jml04
 * @version 1.0
 *
 * @description
 * The {@link StatsView} class contains different utility methods for
 * showing statistics like {@link Bitmap}s or charts
 */
public class StatsView {

    /**
     * shows the position
     *
     * @param ui
     * @param bitmap
     */
    public static void showPosHeatMap(@NotNull UserInterface ui, @NotNull Bitmap bitmap) {
        showPosHeatMap(ui, bitmap.toImage(true));
    }

    public static void showPosHeatMap(@NotNull UserInterface ui, @NotNull BufferedImage bitmapImg) {
        LayerPane layerPane = ui.getLayerPane();
        layerPane.replaceBottom(heatMapPanel(layerPane, bitmapImg));
    }

    public static void showTopAirlines(UserInterface ui, Statistics stats)
            throws DataNotFoundException {

        String input = ui.getUserInput("Please enter a minimum significance (0-" + UnicodeChar.INFINITY.get() + ")", 250);
        if (input.isBlank()) {
            return;
        }
        LayerPane layerPane = ui.getLayerPane();
        try {
            int minCount = Integer.parseInt(input);
            layerPane.replaceBottom(StatsView.barChartPanel(layerPane, stats.airlineSignificance(minCount)));
        } catch (NumberFormatException nfe) {
            ui.showWarning(Warning.NUMBER_EXPECTED);
        }
    }

    public static void showTopAirports(UserInterface ui, Statistics stats)
            throws DataNotFoundException {

        String input = ui.getUserInput("Please enter a minimum significance (0-" + UnicodeChar.INFINITY.get() + ")", 250);
        if (input.isBlank()) {
            return;
        }
        LayerPane layerPane = ui.getLayerPane();
        try {
            int minCount = Integer.parseInt(input);
            layerPane.replaceBottom(StatsView.barChartPanel(layerPane, stats.airportSignificance(minCount)));
        } catch (NumberFormatException nfe) {
            ui.showWarning(Warning.NUMBER_EXPECTED);
        }
    }

    public static void showMostTracked(UserInterface ui, Statistics stats)
            throws DataNotFoundException {

        String input = ui.getUserInput("Please enter a minimum significance (0-" + UnicodeChar.INFINITY.get() + ")", 250);
        if (input.isBlank()) {
            return;
        }
        LayerPane layerPane = ui.getLayerPane();
        int minCount;
        try {
            minCount = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            ui.showWarning(Warning.NUMBER_EXPECTED);
            return;
        }
        Map<String, Integer> mostTracked = stats.mostTrackedFlights(minCount);
        CategoryDataset dataset = Statistics.createBarDataset(mostTracked);
        ChartPanel chart = StatsView.barChartPanel(layerPane, "Most tracked Flights", "Flight", "Distance (km)", dataset);
        layerPane.replaceBottom(chart);
    }

    @NotNull
    private static JPanel heatMapPanel(@NotNull Component parent, @NotNull Bitmap bitmap) {
        return heatMapPanel(parent, bitmap.toImage(true));
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
    private static ZoomPane heatMapPanel(@NotNull Component parent, @NotNull Image heatMapImg) {
        int width = parent.getWidth(),
            height = parent.getHeight();
        Image scaled = Utilities.scale(new ImageIcon(heatMapImg), width, height).getImage();
        ZoomPane panel = new ZoomPane(scaled);
        panel.setBounds(0, 0, width, height);
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

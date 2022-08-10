package planespotter.display;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import planespotter.constants.UnicodeChar;
import planespotter.constants.Warning;
import planespotter.display.models.LayerPane;
import planespotter.statistics.Statistics;

import javax.swing.*;
import java.awt.*;

import static org.jfree.chart.ChartFactory.createBarChart;

/* probably not needed */
public class Diagrams {

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
            ui.showWarning(Warning.INT_EXPECTED);
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
            ui.showWarning(Warning.INT_EXPECTED);
        }
    }

    public static ChartPanel barChartPanel(Component parent, JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setBounds(parent.getBounds());
        return panel;
    }

    public static ChartPanel barChartPanel(Component parent, String title, String xName, String yName, CategoryDataset dataset) {
        JFreeChart barChart = createBarChart(title, xName, yName, dataset);
        //BufferedImage chartImage = barChart.createBufferedImage(width, height);
        ChartPanel panel = new ChartPanel(barChart);
        panel.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
        return panel;
        //return new Diagram(Diagram.TYPE_IMAGE, width, height, chartImage);
    }

}

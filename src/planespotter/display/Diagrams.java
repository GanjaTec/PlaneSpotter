package planespotter.display;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;

import java.awt.*;

import static org.jfree.chart.ChartFactory.createBarChart;

/* probably not needed */
public class Diagrams {

    public static ChartPanel barChartPanel(Component parent, JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
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

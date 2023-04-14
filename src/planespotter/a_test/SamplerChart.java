package planespotter.a_test;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.util.ArrayList;

public class SamplerChart extends JPanel {

    private Sampler sampler;
    private JFreeChart chart;
    private DefaultCategoryDataset dataset;

    public SamplerChart(@NotNull Sampler sampler, @NotNull String title, @NotNull String xLabel, @NotNull String yLabel, int width, int height) {
        this.sampler = new Sampler(sampler.getPeriod(), sampler.getMaxSamples()) {
            @Override
            public void sample() {
                sampler.sample();
            }

            @Override
            public void draw() {
                SamplerChart.this.draw();
            }
        };
        this.dataset = new DefaultCategoryDataset();
        this.chart = ChartFactory.createAreaChart(title, xLabel, yLabel, dataset);
        setSize(width, height);
        setLayout(null);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBounds(0, 0, chartPanel.getWidth(), chartPanel.getHeight());
        add(chartPanel);
    }

    public void draw() {
        int rcount = dataset.getRowCount();
        int scount = sampler.getSampleCount();
        if (rcount >= scount) {
            return;
        }
        ArrayList<Sampler.Sample> samples = sampler.getSamples();
        for (int i = rcount; i < scount; i++) {
            dataset.addValue((Number) samples.get(i).value(), i, samples.get(i).timestamp());
        }
        chart.getCategoryPlot().setDataset(dataset);
        chart.fireChartChanged();
    }

    public Sampler getSampler() {
        return sampler;
    }

    public void setSampler(@NotNull Sampler sampler) {
        this.sampler = sampler;
    }
}

package planespotter.a_test;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SamplerPane extends JPanel {

    private final BufferedImage img;
    private final Sampler sampler;
    private int maxY;

    public SamplerPane(int width, int height, int maxY, @NotNull Sampler sampler) {
        this(0, 0, width, height, maxY, sampler);
    }

    public SamplerPane(int x, int y, int width, int height, int maxY, @NotNull Sampler sampler) {
        if (width < 50 || height < 50) {
            throw new IllegalArgumentException("Panel too small");
        }
        this.img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.maxY = maxY;
        setLayout(null);
        setBounds(x, y, width, height);
        this.sampler = new Sampler(sampler.getPeriod(), 100) {
            @Override
            public void sample() {
                sampler.sample();
            }

            @Override
            public void draw() {
                SamplerPane.this.draw();
            }
        };
    }

    private void testDraw(Graphics g) {
        g.drawRect(20, 20, getWidth() - 20, getHeight() - 20);
    }

    private void draw() {
        //Graphics2D g2d = img.createGraphics();
        Graphics g2d = getGraphics();
        testDraw(g2d);
        drawCoordSys(g2d);
        drawLastSamples(g2d);
        //paintAll(g2d);
        paintComponent(g2d);
        /*removeAll();
        JLabel lbl = new JLabel(new ImageIcon(img));
        lbl.setSize(getSize());
        add(lbl);*/
    }

    private void drawCoordSys(Graphics g) {
        g.setColor(Color.BLACK);
        int bottom = getHeight() - 20;
        g.drawLine(20, 20, 20, bottom);
        g.drawLine(20, bottom, getWidth() - 20, bottom);
        g.drawString(String.valueOf(maxY), 14, 20);
        g.drawString("now", bottom + 4, getWidth() - 24);
    }

    private void drawLastSamples(Graphics g) {
        g.setColor(Color.RED);
        ArrayList<Sampler.Sample> samples = sampler.getSamples();
        int skip = samples.size() - 100;
        if (skip < 0) {
            skip = 0;
        }
        AtomicInteger count = new AtomicInteger(0);
        samples.stream()
                .skip(skip)
                .peek(s -> {
                    if (s.value() > maxY) {
                        maxY = s.value();
                    }
                })
                .forEach(s -> {
                    int x = 20 + count.incrementAndGet();
                    drawSample(g, s, x, maxY);
                });
    }

    private void drawSample(@NotNull Graphics g, @NotNull Sampler.Sample sample, int x, int max) {
        // TODO: 16.12.2022 fit pixels
        int val = sample.value();
        int bottom = getHeight() - 20;
        g.drawLine(x, bottom, x, bottom - val);
    }

}

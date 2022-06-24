package planespotter.a_test;

import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.TestOnly;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import planespotter.constants.ANSIColor;
import planespotter.constants.Images;
import planespotter.constants.Paths;
import planespotter.dataclasses.Bitmap;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Position;
import planespotter.display.models.Diagram;
import planespotter.statistics.RasterHeatMap;
import planespotter.statistics.Statistics;
import planespotter.model.io.DBOut;
import planespotter.throwables.DataNotFoundException;
import planespotter.util.Utilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@TestOnly
public class Test {
    // TEST-MAIN
    public static void main(String[] args) throws Exception {

        var test = new Test();

        var stats = new Statistics();

        var hm = new RasterHeatMap(1f).heat(test.testPosVector());
        test.createTestJFrame(hm.createImage());

        //test.bitmapWriteTest(new File(Paths.RESOURCE_PATH + "testBitmap.bmp"));

        //test.testSpeedChartByFlightID(1363);
/*
        var wind = stats.flightHeadwind(7326);
        var dataset = new DefaultCategoryDataset();
        for (var pos : wind.keySet()) {
            double[] values = wind.get(pos);
            var posString = String.valueOf(pos);
            dataset.addValue(values[0], "Groundspeed", posString);
            dataset.addValue(values[1], "VectorSpeed", posString);
        }
        var chart = ChartFactory.createLineChart("Speed-Diagram", "Positions", "Speed (km/h & direction)", dataset);
        var frame = new ChartFrame("Speed-Statistic", chart);
        frame.pack();
        frame.setVisible(true);
*/

    }

    private void speedChartTest() throws DataNotFoundException {
        var ptps = new DBOut().getAllPlanetypesLike("c20");
        var fids = new DBOut().getFlightIDsByPlaneTypes(ptps);
        var arr = fids.toArray(new Integer[0]);

        this.testSpeedChartByFlightID(Stream.of(arr).mapToInt(i -> i).toArray());
    }

    private void testSpeedChartByFlightID(@Range(from = 0, to = Integer.MAX_VALUE) int... flightIDs) {
        var dbOut = new DBOut();
        var inputDeques = new ArrayDeque<?>[0];
        for (int fid : flightIDs) {
            try {
                int length = inputDeques.length;
                inputDeques = Arrays.copyOf(inputDeques, length + 1);
                var dps = new Vector<>(dbOut.getTrackingByFlight(fid));
                inputDeques[length] = (ArrayDeque<?>) Utilities.parseDeque(dps);
            } catch (DataNotFoundException e) {
                e.printStackTrace();
            }
        };
        var stats = new Statistics();
        var speedMap = stats.windSpeed(new Position(90., -180.), new Position(-90., 180.), (Deque<DataPoint>[]) inputDeques);
        var dataset = Statistics.createMapBarDataset(speedMap);
        var chart = ChartFactory.createLineChart("Flight-Groundspeed", "Time", "Speed (km/h)", dataset);
        var frame = new ChartFrame("Speed", chart);
        frame.pack();
        frame.setVisible(true);
    }

    private void testChart1() throws DataNotFoundException {
        var stats = new Statistics();
        var airportTags = new DBOut().getAllAirportTags();
        var apStats = stats.onlySignificant(stats.tagCount(airportTags), 900);
        var dataset = Statistics.createBarDataset(apStats);
        DefaultCategoryDataset defaultCategoryDataset = new DefaultCategoryDataset();
        defaultCategoryDataset.addValue(3., "Speed", "t0");
        defaultCategoryDataset.addValue(9., "Speed", "t1");
        defaultCategoryDataset.addValue(16., "Speed", "t2");
        defaultCategoryDataset.addValue(7., "Speed", "t3");
        defaultCategoryDataset.addValue(45., "Altitude", "t0");
        defaultCategoryDataset.addValue(26., "Altitude", "t1");
        defaultCategoryDataset.addValue(51., "Altitude", "t2");
        defaultCategoryDataset.addValue(33., "Altitude", "t3");
        var barChart = ChartFactory.createLineChart("Test-LineChart", "Attributes", "Time", defaultCategoryDataset, PlotOrientation.VERTICAL, true, true, false);
        var frame = new ChartFrame("Test-LineChart", barChart);
        frame.pack();
        frame.setVisible(true);
    }

    private void testAirlineChart() throws DataNotFoundException {
        var stats = new Statistics();
        var airlineTags = new DBOut().getAllAirlineTags();
        var airlineStats = stats.onlySignificant(stats.tagCount(airlineTags), 150);
        var dataset = Statistics.createBarDataset(airlineStats);
        var barChart = ChartFactory.createBarChart("Airline Counter", "Airlines", "Count/Significance", dataset, PlotOrientation.HORIZONTAL, true, true, false);
        newChartFrame("Airline Counter", barChart);
    }

    private static void newChartFrame(String title, JFreeChart chart) {
        var frame = new ChartFrame(title, chart);
        frame.pack();
        frame.setVisible(true);
    }

    private void topAirports(int limit) throws DataNotFoundException {
        var stats = new Statistics();
        var airportTags = new DBOut().getAllAirportTags();
        var apStats = stats.onlySignificant(stats.tagCount(airportTags), 500);
        // filtering top airports
        var sortedMap = new HashMap<String, Integer>();
        apStats.entrySet()
                .stream()
                .sorted((a, b) -> Math.max(a.getValue(), b.getValue()))
                .limit(limit)
                .forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        var dataset = Statistics.createBarDataset(sortedMap);
        var barChart = ChartFactory.createBarChart("Airport-Significance", "Airports", "Flight-Count", dataset, PlotOrientation.HORIZONTAL, true, true, false);
        newChartFrame("Airport Significance", barChart);
    }

    private void testAirportChart(int minCount) throws DataNotFoundException {
        var stats = new Statistics();
        var airportTags = new DBOut().getAllAirportTags();
        var apStats = stats.onlySignificant(stats.tagCount(airportTags), minCount);
        var dataset = Statistics.createBarDataset(apStats);
        var barChart = ChartFactory.createBarChart("Airport-Significance", "Airports", "Flight-Count", dataset, PlotOrientation.HORIZONTAL, true, true, false);
        newChartFrame("Airport Significance", barChart);
    }

    private void testAnimation(Test test) {
        var size = new Dimension(800, 500);
        ImageIcon bground = Images.BGROUND_IMG.get(); // doesn't work with every image (???)
        ImageIcon enemy = Images.PAPER_PLANE_ICON.get();

        AtomicInteger x = new AtomicInteger(10), y = new AtomicInteger(size.height-20);
        final int[] xVelocity = {2};
        final int[] yVelocity = {-1};

        var myLabel = new JLabel() { // bground image?
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                var g2d = (Graphics2D) g;
                g2d.drawImage(enemy.getImage(), x.get(), y.get(), null);
            }
        };
        myLabel.setSize(size);
        var myPanel = new JPanel();
        myPanel.add(myLabel);
        myPanel.setSize(size);

        Timer timer = new Timer(10, e -> {
            if (x.get() >= size.width || x.get() <= 0) {
                xVelocity[0] = xVelocity[0] * -1;
            }
            if (y.get() <= 0 || y.get() >= size.height) {
                yVelocity[0] = yVelocity[0] * -1;
            }
            x.addAndGet(xVelocity[0]);
            y.addAndGet(yVelocity[0]);

            myLabel.repaint();
        });

        test.createTestJFrame(myPanel);
        timer.start();
    }

    private void processHandleTest() {
        ProcessHandle.allProcesses()
                .forEach(p -> System.out.println(
                        p.pid() + ": " +
                        p.info().user().orElse(null) + ", " +
                        p.info().command().orElse(null)
                ));
    }

    private void bitmapWriteTest(File file) throws DataNotFoundException {
        var positions = new DBOut().getAllTrackingPositions();
        var heat = new RasterHeatMap(0.1f)
                .heat(positions);

        Bitmap.write(new Bitmap(heat.getHeatMap()), file);
    }

    private void bitmapReadTest(File file) throws FileNotFoundException {
        var heatMap = new RasterHeatMap(0.01f).getHeatMap();
        Bitmap input = null;
        try {
            input = Bitmap.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int counter = 0;
        String str;
        assert input != null;
        for (var arr : input.getBitmap()) {
            for (var by : arr) {
                if (by != 0) {
                    str = (counter == 0) ? ("" + by) : (", " + by);
                    System.out.print(str);
                    counter++;
                }
            }
        }
        System.out.println();
    }

    public void bufferedImageTest() {
        var img = new BufferedImage(1920, 1200, BufferedImage.TYPE_INT_RGB);
        Color color;
        int r=1, g=255, b=100;
        var rnd = new Random();
        for (int i = 0; i < 1920; i++) {
            for (int j = 0; j < 1200; j++) {
                if (j < 255 && j > 1) {
                    r = rnd.nextInt(j);
                    g = rnd.nextInt(j);
                    b = rnd.nextInt(j);
                } else {
                    r = rnd.nextInt(255);
                    g = rnd.nextInt(255);
                    b = rnd.nextInt(255);
                }

                color = new Color(r, g, b);
                img.setRGB(i, j, color.getRGB());
            }
        }

        this.createTestJFrame(img);
    }

    private <T> void createTestJFrame(T source) {
        var panel = new JPanel();
        if (source instanceof BufferedImage img) {
            var label = new JLabel(new ImageIcon(img));
            panel.setSize(img.getWidth() + 100, img.getHeight() + 100);
            panel.add(label);
        } else if (source instanceof Diagram dia) {
            panel.setLayout(null);
            panel.setSize(dia.getSize());
            panel.add(dia);
        } else if (source instanceof Component cmp){
            panel.setSize(cmp.getSize());
            panel.add(cmp);
        }
        var frame = new JFrame();
        frame.setSize(panel.getSize());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setVisible(true);
    }

    public final Vector<Position> testPosVector() {
        try {
            return new DBOut().getAllTrackingPositions();
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void printRectHeatMap() throws DataNotFoundException {
        long startTime = System.currentTimeMillis();
        System.out.println("Loading heat map...");
        System.out.println();

        DBOut dbOut = new DBOut();
        //var tracking = dbOut.getLiveTrackingBetween(10000, 20000);
        var posVector = dbOut.getAllTrackingPositions();
        var rectHeatMap = new RasterHeatMap(1f)
                .heat(posVector);

        Arrays.stream(rectHeatMap.getHeatMap())
                //.map(lv -> (Arrays.toString(lv).replaceAll("0, ", "")))
                .forEach(s -> System.out.println(Arrays.toString(s)));

        var img = rectHeatMap.createImage();
        var file = new File(Paths.RESOURCE_PATH + "testHeatMap.bmp");
        try {
            ImageIO.write(img, "bmp", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JLabel jLabel = new JLabel(new ImageIcon(img));
        JPanel jPanel = new JPanel();
        jPanel.setSize(img.getWidth() + 100, img.getHeight()+ 100);
        jPanel.add(jLabel);
        JFrame r = new JFrame();
        r.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        r.setSize(jPanel.getSize());
        r.add(jPanel);
        r.setVisible(true);

        System.out.println();
        long elapsedMillis = System.currentTimeMillis() - startTime;
        System.out.println(ANSIColor.ORANGE.get() + "elapsed time: " +
                elapsedMillis/1000 + "s" + ANSIColor.RESET.get());
    }


    private void printPositionHeatMap() {
        long startTime = System.currentTimeMillis();
        System.out.println("Loading heat map...");
        System.out.println();

        var statistics = new Statistics();
        Vector<DataPoint> liveTrackingBetween = null;
        try {
            liveTrackingBetween = new DBOut().getLiveTrackingBetween(0, 25000);
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        assert liveTrackingBetween != null;
        var positions = Utilities.parsePositionVector(liveTrackingBetween);
        var positionHeatMap = statistics.positionHeatMap(positions);

        Arrays.stream(positionHeatMap.toString().split(",")).forEach(System.out::println);

        System.out.println();
        long elapsedMillis = System.currentTimeMillis() - startTime;
        System.out.println(ANSIColor.ORANGE.get() + "elapsed time: " +
                           elapsedMillis/1000 + "s" + ANSIColor.RESET.get());
    }

    private void printSystemEnvironment() {
        var sysEnv = System.getenv();
        var envStr = sysEnv.toString();
        var envSubStr = envStr.substring(1, envStr.length() - 1);
        var split = envSubStr.split(",");
        Arrays.stream(split)
                .forEach(System.out::println);
    }

    private void printSystemProperties() {
        var split = System.getProperties()
                .toString()
                .split(",");
        Arrays.stream(split)
                .forEach(System.out::println);
    }

}

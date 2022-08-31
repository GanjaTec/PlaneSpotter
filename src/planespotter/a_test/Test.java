package planespotter.a_test;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import planespotter.constants.*;
import planespotter.dataclasses.Flight;
import planespotter.unused.ANSIColor;
import planespotter.util.Bitmap;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Position;
import planespotter.statistics.Statistics;
import planespotter.model.io.DBOut;
import planespotter.throwables.DataNotFoundException;
import planespotter.util.Time;
import planespotter.util.Utilities;
import sun.misc.Unsafe;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.WebSocket;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@TestOnly
public class Test {

    private static final String bitmapPath = Paths.RESOURCE_PATH + "newTestBitMap.bmp";

    // TEST-MAIN
    public static void main(String[] args) throws Exception {

        // testing python scripts and functions in java
/*
        int a = 99, b = 111;
        var func = "result = " + a + "+" + b;
        var result = PyAdapter.runFunction(func);
        System.out.println(result);
        result = PyAdapter.runScript(Paths.PY_RUNTIME_HELPER + "testprint.py");
        System.out.println(result);
*/
        DBOut dbOut = DBOut.getDBOut();
        long start = Time.nowMillis();
        @NotNull List<Flight> allFlights = dbOut.getAllFlightsBetween(330000, 340000);
        System.out.println("Elapsed: " + Time.elapsedSeconds(start) + "s, " + Time.elapsedMillis(start) + "ms");

    }

    /**
     * something like this could be used when starting the program,
     * as a connection 'pre-check' (also for the map tile URLs),
     * maybe with param 'URL... urls'
     *
     * @throws IOException
     */
    public static void connectTest() throws IOException {
        String spec = "https://data-live.flightradar24.com/";
        URL testUrl = new URL(spec);
        URLConnection conn = testUrl.openConnection();
        conn.setConnectTimeout(5000);
        try {
            conn.connect();
            InetAddress address = InetAddress.getByName(testUrl.getHost());
            System.out.println(address.getHostName() + " reachable: " + address.isReachable(10));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Unsafe getUnsafe() {
        if (Utilities.getCallerClass() != Test.class) {
            throw new IllegalCallerException("This method may only be used in the Test class!");
        }
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("couldn't load the Unsafe class!");
    }

    /**
     * unsafe class is able to do 'JVM-actions' like allocating memory or sth. like that
     */
    private static void unsafeTest() {
        Unsafe unsafe = getUnsafe();
        System.out.println(unsafe.addressSize());
        unsafe.allocateMemory(4);

        // this throws a JVM error because of illegal access // or maybe wrong address
        long address = 0x00000001L;
        unsafe.setMemory(address, 1L, (byte) 5);
        System.out.println(unsafe.getByte(address));
    }

    private void testBitmapWrite() throws IOException {
        var positions = new Test().TEST_POS_VECTOR;/*new DBOut()
                .getTrackingsWithAirportTag("FRA")
                .stream()
                .map(DataPoint::pos)
                .collect(Collectors.toCollection(Vector::new));*/
        assert positions != null;
        var bmp = Bitmap.fromPosVector(positions, 0.5f);
        //test.createTestJFrame(bmp.toImage());
        // bitmap write & read funktioniert
        Bitmap.write(bmp, new File(Paths.RESOURCE_PATH + "bmpBitmap.bmp"));
    }

    /*private void speedChartTest() throws DataNotFoundException {
        var ptps = DBOut.getDBOut().getAllPlanetypesLike("c20");
        var fids = DBOut.getDBOut().getFlightIDsByPlaneTypes(ptps);
        var arr = fids.toArray(new Integer[0]);

        this.testSpeedChartByFlightID(Stream.of(arr).mapToInt(i -> i).toArray());
    }

    private void testSpeedChartByFlightID(@Range(from = 0, to = Integer.MAX_VALUE) int... flightIDs) {
        var dbOut = DBOut.getDBOut();
        var inputDeques = new ArrayDeque<?>[0];
        for (int fid : flightIDs) {
            try {
                int length = inputDeques.length;
                inputDeques = Arrays.copyOf(inputDeques, length + 1);
                var dps = new Vector<>(dbOut.getTrackingByFlight(fid)); // TODO not in for
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
    }*/

    private void testChart1() throws DataNotFoundException {
        var stats = new Statistics();
        var airportTags = DBOut.getDBOut().getAllAirportTagsNotDistinct();
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
        var airlineTags = DBOut.getDBOut().getAllAirlineTags();
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
        var airportTags = DBOut.getDBOut().getAllAirportTagsNotDistinct();
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
        var airportTags = DBOut.getDBOut().getAllAirportTagsNotDistinct();
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
       /* } else if (source instanceof Diagram dia) {
            panel.setLayout(null);
            panel.setSize(dia.getSize());
            panel.add(dia);*/
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

    public final Vector<Position> TEST_POS_VECTOR;
    {
        Vector<Position> trackingPositions;
        try {
            trackingPositions = DBOut.getDBOut().getAllTrackingPositions();
        } catch (DataNotFoundException e) {
            trackingPositions = null;
        }
        TEST_POS_VECTOR = trackingPositions;
    }

    private void printPositionHeatMap() {
        long startTime = System.currentTimeMillis();
        System.out.println("Loading heat map...");
        System.out.println();

        var statistics = new Statistics();
        Vector<DataPoint> liveTrackingBetween = null;
        try {
            liveTrackingBetween = DBOut.getDBOut().getLiveTrackingBetween(0, 25000);
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

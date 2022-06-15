package planespotter.a_test;

import org.jetbrains.annotations.TestOnly;
import planespotter.constants.ANSIColor;
import planespotter.constants.Images;
import planespotter.constants.Paths;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Position;
import planespotter.display.*;
import planespotter.statistics.RasterHeatMap;
import planespotter.statistics.Statistics;
import planespotter.model.io.DBOut;
import planespotter.model.io.BitmapIO;
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

@TestOnly
public class Test {
    // TEST-MAIN
    public static void main(String[] args) throws Exception {
        //final long startTime =  nowMillis();

        var test = new Test();
        //System.out.println(linesCode(Paths.CODE_PATH));
        System.out.println();

/*
        var scheduler = new Scheduler();
        var supplier = new ProtoSupplier(new ProtoDeserializer(), new ProtoKeeper(1200L)); // TODO best threshold time?
        scheduler.schedule(supplier, 0, (int) TimeUnit.MINUTES.toSeconds(3));
*/


        //System.out.println("linesCode = " + linesCode(Paths.CODE_PATH));


       /* byte b1= Integer.valueOf(5).byteValue();
        byte b2 = Integer.valueOf(100480).byteValue();
        System.out.print(b1);
        System.out.print(", ");
        System.out.println(b2);*/
/*
        var speedMap = new OutputWizard(null).loadSpeedMap(0, 300000);
        var heatMap = new SpeedHeatMap(0.1f, null, null).heat(speedMap);
*/
        /*var file = new File(Paths.RESSOURCE_PATH + "bitmap.bmp");

        var positions = new DBOut().getAllTrackingPositions();
        var heat = new RasterHeatMap(1f)
                .heat(positions);
        var dia = new Diagram(Diagram.TYPE_CAKE_CHART, 600, 400, heat.getHeatMap());
        test.createTestJFrame(dia);*/

        /*var bitmap = heat.createBitmap();
        var img = heat.createImage();
        //test.createTestJFrame(bitmap);
        test.createTestJFrame(img);*/

        //System.out.println();
        //System.out.println("Task finished in " + elapsedMillis(startTime) + " milliseconds!");
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
        var heat = new RasterHeatMap(0.01f)
                .heat(positions);

        BitmapIO.write(heat.getHeatMap(), file);
    }

    private void bitmapReadTest(File file) throws FileNotFoundException {
        var heatMap = new RasterHeatMap(0.01f).getHeatMap();
        short[][] input = BitmapIO.read(file, heatMap);
        int counter = 0;
        String str;
        for (var a : input) {
            for (var b : a) {
                if (b != 0) {
                    str = (counter == 0) ? ("" + b) : (", " + b);
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
        var file = new File(Paths.RESSOURCE_PATH + "testHeatMap.bmp");
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

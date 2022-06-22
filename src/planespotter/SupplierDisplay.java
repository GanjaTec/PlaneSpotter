package planespotter;

import libs.UWPButton;
import planespotter.constants.Images;
import planespotter.model.io.DBWriter;
import planespotter.util.MathUtils;
import planespotter.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static planespotter.constants.DefaultColor.*;
import static planespotter.constants.GUIConstants.MENU_BORDER;
import static planespotter.constants.Images.FLYING_PLANE_ICON;

/**
 * @version 1.0
 * <p>
 * inner class SupplierDisplay is a little Display for the SupplierMain
 * @name SupplierDisplay
 * @see planespotter.SupplierMain
 */
public class SupplierDisplay {

    private static final Runtime runtime = Runtime.getRuntime();
    private static boolean paused = false,
                           enabled = true;
    private static final String STATUS_TXT = "Status: ";
    // inserted values indexes:   0 = allFrames,   1 = newPlanes,   2 = newFlights
    private final int[] inserted = {0, 0, 0};
    private int totalMemory = 13212;
    private final UWPButton pauseButton = new UWPButton(),
            startStopButton = new UWPButton();
    private final JProgressBar progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, totalMemory);
    private final JLabel insertedLabel = new JLabel(),
            memoryLabel = new JLabel(),
            newPlanesLabel = new JLabel(),
            newFlightsLabel = new JLabel(),
            statusLabel = new JLabel();
    private final JLabel[] labels = {insertedLabel, newPlanesLabel, newFlightsLabel, memoryLabel, statusLabel};
    private final JFrame frame;

    public SupplierDisplay() {
        this.frame = this.frame();
    }

    public void start() {
        this.frame.setVisible(true);
        this.tryAddTrayIcon();
    }

    private JFrame frame() {
        var size = new Dimension(300, 400);
        int compWidth = size.width - 20;
        int y = 10;
        for (var lbl : this.labels) {
            lbl.setBounds(10, y, compWidth - 20, 20);
            y += 30;
        }
        this.progressBar.setForeground(DEFAULT_MAP_ICON_COLOR.get());
        this.progressBar.setBounds(10, y, compWidth - 20, 20);
        this.progressBar.setString("Memory Usage");
        this.progressBar.setStringPainted(true);
        this.progressBar.setBorder(MENU_BORDER);

        var seps = new JSeparator[] {
                new JSeparator(), new JSeparator(), new JSeparator(),
                new JSeparator(), new JSeparator(), new JSeparator()
        };
        y = 30;
        for (var sep : seps) {
            sep.setOpaque(true);
            sep.setForeground(Color.DARK_GRAY);
            sep.setBounds(10, y + 5, compWidth - 20, 2);
            y += 30;
        }

        this.startStopButton.setBounds(10, size.height - 70, compWidth - 20, 20);
        this.startStopButton.setSelectedColor(DEFAULT_SEARCH_ACCENT_COLOR.get());
        this.startStopButton.setEffectColor(DEFAULT_FONT_COLOR.get());
        this.startStopButton.setForeground(DEFAULT_FONT_COLOR.get());
        this.startStopButton.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        this.startStopButton.setText("Start / Stop");
        this.startStopButton.addActionListener(e -> {
            DBWriter.setEnabled(enabled = !enabled);
            paused = !enabled;
            switch (MathUtils.toBinary(enabled)) {
                case 0 -> SupplierMain.startCollecting();
                case 1 -> System.out.println(SupplierMain.stopCollecting() ? "Interrupted successfully!" : "Couldn't stop the Collector!");
            }
            this.setStatus((enabled ? "enabled, " : "disabled, ") + (paused ? "paused" : "running"));
        });

        this.pauseButton.setBounds(10, size.height - 100, compWidth - 20, 20);
        this.pauseButton.setSelectedColor(DEFAULT_SEARCH_ACCENT_COLOR.get());
        this.pauseButton.setEffectColor(DEFAULT_FONT_COLOR.get());
        this.pauseButton.setForeground(DEFAULT_FONT_COLOR.get());
        this.pauseButton.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        this.pauseButton.setText("Pause");
        this.pauseButton.addActionListener(e -> {
            DBWriter.setEnabled(paused = !paused);
            this.setStatus((enabled ? "enabled, " : "disabled, ") + (paused ? "paused" : "running"));
        });

        this.statusLabel.setForeground(DEFAULT_ACCENT_COLOR.get());
        this.setStatus("enabled, running");

        var panel = new JPanel();
        panel.setLayout(null);
        panel.setSize(size);
        Arrays.stream(this.labels).forEach(panel::add);
        Arrays.stream(seps).forEach(panel::add);
        panel.add(this.progressBar);
        panel.add(this.pauseButton);
        panel.add(this.startStopButton);

        JFrame.setDefaultLookAndFeelDecorated(false);
        var frame = new JFrame("Fr24-Collector");
        frame.setIconImage(FLYING_PLANE_ICON.get().getImage());
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.setSize(size);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(panel);

        return frame;
    }

    private void setStatus(String text) {
        this.statusLabel.setText(STATUS_TXT + text);
    }

    public synchronized void update(final int insertedNow, final int newPlanesNow, final int newFlightsNow) {
        this.inserted[0] += insertedNow;
        this.inserted[1] += newPlanesNow;
        this.inserted[2] += newFlightsNow;
        this.totalMemory = (int) (runtime.totalMemory() / 10_000);
        int freeMemory = (int) (runtime.freeMemory() / 10_000);
        int memoryUsage = (this.totalMemory - freeMemory);

        this.insertedLabel.setText("Inserted Frames: " + this.inserted[0] + ", " + insertedNow + " per Sec.");
        this.memoryLabel.setText("Memory: free: " + freeMemory + " MB, total: " + this.totalMemory + " MB");
        this.newPlanesLabel.setText("New Planes: " + this.inserted[1] + ", " + newPlanesNow + " per Sec.");
        this.newFlightsLabel.setText("New Flights: " + this.inserted[2] + ", " + newFlightsNow + " per Sec.");
        this.progressBar.setValue(memoryUsage);
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        freeMemory = (int) (runtime.freeMemory() / 10_000);
        memoryUsage = (this.totalMemory - freeMemory);
        this.progressBar.setValue(memoryUsage);
        this.memoryLabel.setText("Memory: free: " + freeMemory + " MB, total: " + this.totalMemory + " MB");
    }

    private void tryAddTrayIcon() {
        if (SystemTray.isSupported()) {
            var trayIcon = new TrayIcon(Images.FLYING_PLANE_ICON.get().getImage());
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> this.frame.setVisible(this.frame.isVisible()));
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }
}

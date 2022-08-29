package planespotter.display.models;

import libs.UWPButton;
import org.jetbrains.annotations.Nullable;
import planespotter.model.Fr24Collector;
import planespotter.constants.Images;
import planespotter.controller.Controller;
import planespotter.model.Collector;
import planespotter.model.io.DBIn;
import planespotter.model.nio.Supplier;
import planespotter.util.math.MathUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static planespotter.constants.DefaultColor.*;
import static planespotter.constants.Images.FLYING_PLANE_ICON;
import static planespotter.util.math.MathUtils.divide;

/**
 * @name SupplierDisplay
 * @author jml04
 * @version 1.0
 *
 * inner class SupplierDisplay is a little Display for the SupplierMain
 * @see Fr24Collector
 */
public class SupplierDisplay implements WindowListener {

    private static final String STATUS_TXT = "Status: ",
                                LAST_FRAME_TXT = "Last Frame: ",
                                QUEUE_SIZE_TXT = "Queued Frames: ";
    // inserted values indexes:   0 = allFrames,   1 = newPlanes,   2 = newFlights
    private final int[] inserted = {0, 0, 0};
    private final Collector<? extends Supplier> collector;
    private int totalMemory = divide((int) Controller.RUNTIME.totalMemory(), 10_000);
    // swing components
    private final UWPButton pauseButton = new UWPButton(),
                            startStopButton = new UWPButton();
    private final JProgressBar progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, totalMemory);
    private final JLabel insertedLabel = new JLabel(),
                         memoryLabel = new JLabel(),
                         newPlanesLabel = new JLabel(),
                         newFlightsLabel = new JLabel(),
                         statusLabel = new JLabel(),
                         lastFrameLabel = new JLabel(),
                         queueSizeLabel = new JLabel(),
                         errorLabel = new JLabel();
    private final JLabel[] labels = {insertedLabel, newPlanesLabel, newFlightsLabel, memoryLabel, statusLabel, lastFrameLabel, queueSizeLabel, errorLabel};
    private final JFrame frame;

    public SupplierDisplay(int defaultCloseOperation, Collector<? extends Supplier> collector) {
        this.frame = this.frame(defaultCloseOperation);
        this.collector = collector;
    }

    public void start() {
        this.frame.setVisible(true);
        this.tryAddTrayIcon();
    }

    private JFrame frame(int defaultCloseOperation) {
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
        this.progressBar.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));

        var seps = new JSeparator[] {
                new JSeparator(), new JSeparator(), new JSeparator(),
                new JSeparator(), new JSeparator(), new JSeparator(),
                new JSeparator(), new JSeparator()
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
            DBIn.setEnabled(this.collector.setEnabled(!this.collector.isEnabled()));
            this.collector.setPaused(this.collector.isEnabled());
            switch (MathUtils.toBinary(this.collector.isEnabled())) {
                case 0 -> this.collector.startCollecting();
                case 1 -> System.out.println(this.collector.stopCollecting() ? "Interrupted successfully!" : "Couldn't stop the Collector!");
            }
            this.setStatus( (this.collector.isEnabled() ? "enabled, " : "disabled, ") +
                            (this.collector.isPaused() ? "paused" : "running"));
        });

        this.pauseButton.setBounds(10, size.height - 100, compWidth - 20, 20);
        this.pauseButton.setSelectedColor(DEFAULT_SEARCH_ACCENT_COLOR.get());
        this.pauseButton.setEffectColor(DEFAULT_FONT_COLOR.get());
        this.pauseButton.setForeground(DEFAULT_FONT_COLOR.get());
        this.pauseButton.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        this.pauseButton.setText("Pause");
        this.pauseButton.addActionListener(e -> {
            DBIn.setEnabled(this.collector.setPaused(!this.collector.isPaused()));
            this.setStatus( (this.collector.isEnabled() ? "enabled, " : "disabled, ") +
                            (this.collector.isPaused() ? "paused" : "running"));
        });

        this.statusLabel.setForeground(DEFAULT_ACCENT_COLOR.get());
        this.setStatus("enabled, running");
        this.setQueueSize(0);

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
        frame.setDefaultCloseOperation(defaultCloseOperation);
        frame.setIconImage(FLYING_PLANE_ICON.get().getImage());
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.setSize(size);
        frame.setResizable(false);
        frame.add(panel);

        return frame;
    }

    private void setQueueSize(int size) {
        this.queueSizeLabel.setText(QUEUE_SIZE_TXT + size);
    }

    private void setNextFrame(String frame) {
        this.lastFrameLabel.setText(LAST_FRAME_TXT + frame);
    }

    private void setStatus(String text) {
        this.statusLabel.setText(STATUS_TXT + text);
    }

    public synchronized void update(final int insertedNow, final int newPlanesNow, final int newFlightsNow, String lastFrame, int queueSize, @Nullable Throwable error) {
        this.inserted[0] += insertedNow;
        this.inserted[1] += newPlanesNow;
        this.inserted[2] += newFlightsNow;
        this.totalMemory = divide((int) Controller.RUNTIME.totalMemory(), 10_000);
        int freeMemory = divide((int) Controller.RUNTIME.freeMemory(), 10_000);
        int memoryUsage = this.totalMemory - freeMemory;

        this.insertedLabel.setText("Inserted Frames: " + this.inserted[0] + ", " + insertedNow + " per Sec.");
        this.memoryLabel.setText("Memory: free: " + freeMemory + " MB, total: " + this.totalMemory + " MB");
        this.newPlanesLabel.setText("New Planes: " + this.inserted[1] + ", " + newPlanesNow + " per Sec.");
        this.newFlightsLabel.setText("New Flights: " + this.inserted[2] + ", " + newFlightsNow + " per Sec.");
        this.progressBar.setValue(memoryUsage);
        this.setNextFrame(lastFrame);
        this.setQueueSize(queueSize);
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException ignored) {
        }
        freeMemory = (int) (Controller.RUNTIME.freeMemory() / 10_000);
        memoryUsage = (this.totalMemory - freeMemory);
        this.progressBar.setValue(memoryUsage);
        this.memoryLabel.setText("Memory: free: " + freeMemory + " MB, total: " + this.totalMemory + " MB");
        if (error != null) {
            this.errorLabel.setText("Error: " + error.getMessage());
        }
    }

    private void tryAddTrayIcon() {
        if (SystemTray.isSupported()) {
            var trayIcon = new TrayIcon(Images.FLYING_PLANE_ICON.get().getImage());
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> this.frame.setVisible(!this.frame.isVisible()));
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // add supplier somewhere
    }

    @Override
    public void windowClosing(WindowEvent e) {
        // remove supplier somewhere
    }

    @Override public void windowClosed(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}
}

package planespotter.display.models;

import libs.UWPButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.constants.Images;
import planespotter.controller.Controller;
import planespotter.model.Collector;
import planespotter.model.Fr24Collector;
import planespotter.model.nio.Supplier;
import planespotter.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
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
    private final JLabel[] labels = {insertedLabel, newPlanesLabel, newFlightsLabel, memoryLabel, lastFrameLabel, queueSizeLabel, errorLabel, statusLabel};
    private final JFrame frame;

    public SupplierDisplay(int defaultCloseOperation, Collector<? extends Supplier> collector, @NotNull ActionListener onPauseClick, @NotNull ActionListener onStartStopClick) {
        this.frame = this.frame(defaultCloseOperation, onPauseClick, onStartStopClick);
        this.collector = collector;

    }

    public void start() {
        frame.setVisible(true);
        Utilities.addTrayIcon(Images.FLYING_PLANE_ICON.get().getImage(), e -> this.frame.setVisible(!this.frame.isVisible()));
    }

    private JFrame frame(int defaultCloseOperation, @NotNull ActionListener onPauseClick, @NotNull ActionListener onStartStopClick) {
        Dimension size = new Dimension(300, 400);
        int compWidth = size.width - 20;
        int y = 10;
        for (JLabel lbl : labels) {
            lbl.setBounds(10, y, compWidth - 20, 20);
            y += 30;
        }
        progressBar.setForeground(DEFAULT_MAP_ICON_COLOR.get());
        progressBar.setBounds(10, y, compWidth - 20, 20);
        progressBar.setString("Memory Usage");
        progressBar.setStringPainted(true);
        progressBar.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));

        JSeparator[] seps = new JSeparator[] {
                new JSeparator(), new JSeparator(), new JSeparator(),
                new JSeparator(), new JSeparator(), new JSeparator(),
                new JSeparator(), new JSeparator()
        };
        y = 30;
        for (JSeparator sep : seps) {
            sep.setOpaque(true);
            sep.setForeground(Color.DARK_GRAY);
            sep.setBounds(10, y + 5, compWidth - 20, 2);
            y += 30;
        }

        startStopButton.setBounds(10, size.height - 70, compWidth - 20, 20);
        startStopButton.setSelectedColor(DEFAULT_SEARCH_ACCENT_COLOR.get());
        startStopButton.setEffectColor(DEFAULT_FONT_COLOR.get());
        startStopButton.setForeground(DEFAULT_FONT_COLOR.get());
        startStopButton.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        startStopButton.setText("Start / Stop");
        startStopButton.addActionListener(onStartStopClick);

        pauseButton.setBounds(10, size.height - 100, compWidth - 20, 20);
        pauseButton.setSelectedColor(DEFAULT_SEARCH_ACCENT_COLOR.get());
        pauseButton.setEffectColor(DEFAULT_FONT_COLOR.get());
        pauseButton.setForeground(DEFAULT_FONT_COLOR.get());
        pauseButton.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        pauseButton.setText("Pause");
        pauseButton.addActionListener(onPauseClick);

        statusLabel.setForeground(DEFAULT_ACCENT_COLOR.get());
        setStatus("enabled, running");
        setQueueSize(0);
        setError("");

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setSize(size);
        Arrays.stream(labels).forEach(panel::add);
        Arrays.stream(seps).forEach(panel::add);
        panel.add(progressBar);
        panel.add(pauseButton);
        panel.add(startStopButton);

        JFrame.setDefaultLookAndFeelDecorated(false);
        JFrame frame = new JFrame("Fr24-Collector");
        frame.setDefaultCloseOperation(defaultCloseOperation);
        frame.setIconImage(FLYING_PLANE_ICON.get().getImage());
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.setSize(size);
        frame.setResizable(false);
        frame.add(panel);

        return frame;
    }

    private void setError(@NotNull String error) {
        errorLabel.setText("Last Error: " + error);
    }

    private void setQueueSize(int size) {
        queueSizeLabel.setText(QUEUE_SIZE_TXT + size);
    }

    private void setLastFrame(String frame) {
        lastFrameLabel.setText(LAST_FRAME_TXT + frame);
    }

    public void setStatus(String text) {
        statusLabel.setText(STATUS_TXT + text);
    }

    public synchronized void update(final int insertedNow, final int newPlanesNow, final int newFlightsNow, String lastFrame, int queueSize, @Nullable Throwable error) {
        inserted[0] += insertedNow;
        inserted[1] += newPlanesNow;
        inserted[2] += newFlightsNow;
        totalMemory = divide((int) Controller.RUNTIME.totalMemory(), 10_000);
        int freeMemory = divide((int) Controller.RUNTIME.freeMemory(), 10_000);
        int memoryUsage = totalMemory - freeMemory;

        insertedLabel.setText("Inserted Frames: " + inserted[0] + ", " + insertedNow + " per Sec.");
        memoryLabel.setText("Memory: free: " + freeMemory + " MB, total: " + totalMemory + " MB");
        newPlanesLabel.setText("New Planes: " + inserted[1] + ", " + newPlanesNow + " per Sec.");
        newFlightsLabel.setText("New Flights: " + inserted[2] + ", " + newFlightsNow + " per Sec.");
        progressBar.setValue(memoryUsage);
        setLastFrame(lastFrame);
        setQueueSize(queueSize);
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException ignored) {
        }
        freeMemory = (int) (Controller.RUNTIME.freeMemory() / 10_000);
        memoryUsage = (totalMemory - freeMemory);
        progressBar.setValue(memoryUsage);
        memoryLabel.setText("Memory: free: " + freeMemory + " MB, total: " + totalMemory + " MB");
        if (error != null) {
            setError(error.getMessage());
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

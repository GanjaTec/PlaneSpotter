package planespotter.display.models;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.DefaultColor;
import planespotter.display.UserInterface;
import planespotter.model.simulation.Simulator;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;

public class SimulationAddons extends JPanel {

    private static final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 11);

    private JPanel info, buttons;
    private JLabel status, remTime;
    private UWPButton start, stop, close;

    public SimulationAddons(JComponent parent) {
        this(parent, null, null, null);
    }

    public SimulationAddons(@NotNull JComponent parent, ActionListener onStart,
                            ActionListener onStop, ActionListener onClose) {
        super(null);
        setOpaque(false);
        setBorder(BorderFactory.createLineBorder(Color.RED));
        add(buttons = buttonPanel(onStart, onStop, onClose));
        add(info = createInfoPanel());
        resize(0, 0, parent.getWidth(), parent.getHeight());

    }

    private JPanel buttonPanel(ActionListener onStart, ActionListener onStop, ActionListener onClose) {
        start = createButton(20, 10, 50, 20, "Start", Color.GREEN);
        stop = createButton(90, 10, 50, 20, "Stop", DefaultColor.DEFAULT_MAP_ICON_COLOR.get());
        close = createButton(160, 10, 50, 20, "Close", Color.RED);

        if (onStart != null) {
            setStartAction(onStart);
        }
        if (onStop != null) {
            setStopAction(onStop);
        }
        if (onClose != null) {
            setCloseAction(onClose);
        }

        JPanel panel = new JPanel(null);
        panel.setBackground(DefaultColor.DEFAULT_FONT_COLOR.get());
        panel.setBorder(BorderFactory.createLineBorder(Color.RED));
        panel.add(start);
        panel.add(stop);
        panel.add(close);
        panel.setBounds(0, 0, 230, 40);
        return panel;
    }

    private static UWPButton createButton(int x, int y, int w, int h, String text, Color selected) {
        UWPButton bt = new UWPButton(text);
        bt.setBounds(x, y, w, h);
        bt.setEffectColor(DefaultColor.DEFAULT_FONT_COLOR.get());
        bt.setSelectedColor(selected);
        bt.setBackground(DefaultColor.DEFAULT_ACCENT_COLOR.get());
        bt.setBorder(BorderFactory.createLineBorder(Color.RED));
        bt.setFont(DEFAULT_FONT);
        return bt;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(DefaultColor.DEFAULT_FONT_COLOR.get());
        panel.setBounds(0, 0, 230, 40);
        panel.setBorder(BorderFactory.createLineBorder(Color.RED));

        status = new JLabel("Stopped");
        status.setFont(DEFAULT_FONT);
        status.setForeground(DefaultColor.DEFAULT_MAP_ICON_COLOR.get());
        status.setBounds(20, 10, 50, 20);

        remTime = new JLabel("Remaining: ");
        remTime.setFont(DEFAULT_FONT);
        remTime.setBounds(90, 10, 120, 20);

        panel.add(status);
        panel.add(remTime);

        return panel;
    }

    public void resize(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
        info.setLocation(getWidth() - 250, getHeight() - 120);
        buttons.setLocation(getWidth() - 250, getHeight() - 60);
    }

    public void setStartAction(@NotNull ActionListener onStart) {
        start.addActionListener(onStart);
    }

    public void setStopAction(@NotNull ActionListener onStop) {
        stop.addActionListener(onStop);
    }

    public void setCloseAction(@NotNull ActionListener onClose) {
        close.addActionListener(onClose);
    }

    public void setStatus(int status) {
        final JLabel sts = this.status;
        switch (status) {
            case Simulator.RUNNING -> {
                sts.setText("Running");
                sts.setForeground(new Color(0, 170, 60));
            }
            case Simulator.STOPPED -> {
                sts.setText("Stopped");
                sts.setForeground(DefaultColor.DEFAULT_MAP_ICON_COLOR.get());
            }
            case Simulator.CLOSED -> {
                sts.setText("Closed");
                sts.setForeground(Color.RED);
            }
            default -> System.err.println("Status must be between -1 (closed) and 1 (running)");
        }
    }

    public void setRemaining(int ms) {
        int vc = ms / 1000;
        int m1000 = ms % 1000;
        int ac = m1000 == 0 ? 0 : 1000 / m1000;
        remTime.setText("Remaining: " + vc + "," + ac + "s");
    }

}

package planespotter.display.models;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.Configuration;
import planespotter.controller.ActionHandler;
import planespotter.model.Scheduler;
import planespotter.util.Utilities;
import planespotter.util.math.MathUtils;

import javax.swing.*;

import java.awt.*;
import java.util.concurrent.TimeUnit;

import static planespotter.constants.DefaultColor.*;
import static planespotter.constants.Images.*;

/**
 * @name PanelModels
 * @author jml04
 * @version 1.0
 *
 * contains panel models for GUI
 */
public final class PaneModels {

    @NotNull
    public static JFrame windowFrame(@NotNull ActionHandler listener) {
        // getting main window object
        JFrame window = new JFrame(Configuration.TITLE);
        // setting window start size and preferred size
        Dimension size = new Dimension(1280, 720);
        window.setSize(size);
        window.setPreferredSize(size);
        // setting default close operation, do nothing for external exit action
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // setting window location relative to null
        window.setLocationRelativeTo(null);
        // component listener for component resize
        window.addComponentListener(listener);
        // window listener for window actions like open/close
        window.addWindowListener(listener);
        // setting plane icon as window-icon
        window.setIconImage(FLYING_PLANE_ICON.get().getImage());
        // first setting to not-visible
        window.setVisible(false);
        // returning window
        window.setJMenuBar(MenuModels.topMenuBar(listener));

        return window;
    }

    public static synchronized void startScreenAnimation(int sec) {
        ImageIcon img = Utilities.scale(START_SCREEN.get(), 800, 100);
        JLabel label = new JLabel(img);
        label.setSize(img.getIconWidth(), img.getIconHeight());
        label.setOpaque(false);
        label.setLayout(null);

        JDialog dialog = new JDialog();
        dialog.add(label);
        dialog.setSize(label.getSize());
        dialog.setLocationRelativeTo(null);
        dialog.setUndecorated(true);
        dialog.setOpacity(0.0f);
        dialog.setVisible(true);
        // easy animation
        long millis = TimeUnit.SECONDS.toMillis(sec);
        long vel = MathUtils.divide(millis, 100L);
        float opc;
        for (int s = 0; s < millis; s += vel) {
            opc = dialog.getOpacity();
            dialog.setOpacity(opc + 0.01f);
            Scheduler.sleep(vel);
        }
        dialog.setVisible(false);
    }

    @NotNull
    public static JLabel loadingScreen() {
        ImageIcon img = LOADING_CYCLE_GIF.get();
        JLabel label = new JLabel(img);
        label.setSize(img.getIconWidth(), img.getIconHeight());
        label.setOpaque(false);
        label.setLayout(null);

        return label;
    }

    public static <D> JPanel statsPanel(D data, JDesktopPane parent) {
        // setting up stats panel
        var stats = new JPanel();
        stats.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
        stats.setLayout(null);
        stats.setBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR.get()));
        //stats.setOpaque(false);
        if (data instanceof Image img) {
            var label = new JLabel(new ImageIcon(img));
            label.setLocation(0, 0);
            label.setSize(stats.getSize());
            stats.add(label);
        }

        return stats;
    }

    /**
     * creates a JScrollPane with the given Component and a specific layout
     * @param inside is the JTree or whatever, which is displayed in the JScrollPane
     * @return sp, the JScrollPane
     */
    public JScrollPane listScrollPane(JTree inside, JPanel parent) {
        var sp = new JScrollPane(inside);
        sp.setViewportView(inside);
        sp.setBackground(DEFAULT_BG_COLOR.get());
        sp.setForeground(DEFAULT_BORDER_COLOR.get());
        sp.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        sp.setBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR.get()));
        var verticalScrollBar = sp.getVerticalScrollBar();
        verticalScrollBar.setBackground(DEFAULT_BG_COLOR.get());
        verticalScrollBar.setForeground(DEFAULT_ACCENT_COLOR.get());
        verticalScrollBar.setBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR.get()));
        sp.setVerticalScrollBar(verticalScrollBar);
        sp.setOpaque(false);
        return sp;
    }

}

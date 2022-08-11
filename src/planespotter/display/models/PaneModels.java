package planespotter.display.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.constants.Configuration;
import planespotter.constants.Images;
import planespotter.constants.Paths;
import planespotter.controller.ActionHandler;

import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;

import static planespotter.constants.GUIConstants.*;
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
        var window = new JFrame(Configuration.TITLE);
        // setting window start size
        window.setSize(1280, 720);
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

    public static  <D> JPanel statsPanel(D data, JDesktopPane parent) {
        // setting up stats panel
        var stats = new JPanel();
        stats.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
        stats.setLayout(null);
        stats.setBorder(LINE_BORDER);
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
        sp.setBorder(LINE_BORDER);
        var verticalScrollBar = sp.getVerticalScrollBar();
        verticalScrollBar.setBackground(DEFAULT_BG_COLOR.get());
        verticalScrollBar.setForeground(DEFAULT_ACCENT_COLOR.get());
        verticalScrollBar.setBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR.get()));
        sp.setVerticalScrollBar(verticalScrollBar);
        sp.setOpaque(false);
        return sp;
    }

    /**
     * loading screen method, creates a loading screen
     *
     * @return loading screen JFrame
     */
    public JFrame loadingScreen() {
        var loadingScreen = new JFrame();
        loadingScreen.setSize(333, 243);
        loadingScreen.setLocationRelativeTo(null);
        loadingScreen.setLayout(null);
        loadingScreen.setOpacity(1f);
        loadingScreen.setUndecorated(true);
        var lblLoading = new JLabel(Images.LOADING_GIF.get());
        lblLoading.setBounds(0, 0, 333, 243);
        loadingScreen.add(lblLoading);
        return loadingScreen;
    }

}

package planespotter.display.models;

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

    public JFrame windowFrame(ActionHandler listener) {
        var window = new JFrame("PlaneSpotter v0.1");
        window.setSize(1280, 720);
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.addComponentListener(listener);
        window.addWindowListener(listener);
        window.setIconImage(FLYING_PLANE_ICON.get().getImage());
        window.setVisible(false);
        return window;
    }

    /**
     * main-panel
     */
    public JPanel mainPanel(JFrame parent) {
        // TODO: setting up mainpanel
        var mainpanel = new JPanel();
        mainpanel.setBounds(0, 0, parent.getWidth(), parent.getHeight()); // mainpanel width: 1260
        mainpanel.setLayout(null);
        mainpanel.setBackground(DEFAULT_BG_COLOR.get());
        return mainpanel;
    }

    public JDesktopPane[] desktopPanes(JPanel parent) {
        // setting up left desktop pane
        var dpleft = new JDesktopPane();
        dpleft.setBorder(LINE_BORDER);
        dpleft.setBackground(DEFAULT_BG_COLOR.get());
        dpleft.setDesktopManager(new DefaultDesktopManager());
        dpleft.setBounds(0, 70, 280, parent.getHeight() - 70);
        dpleft.setOpaque(false);
        // setting up right desktop pane
        var dpright = new JDesktopPane();
        dpright.setBorder(LINE_BORDER);
        dpright.setBackground(DEFAULT_BG_COLOR.get());
        dpright.setDesktopManager(new DefaultDesktopManager());
        dpright.setBounds(280, 70, parent.getWidth() - 280, parent.getHeight() - 70);
        dpright.setOpaque(false);
        return new JDesktopPane[] {
                dpleft, dpright
        };
    }

    /**
     * title panel with bground and title
     */
    public JPanel titlePanel(JPanel parent) {
        // setting up title panel
        var title = new JPanel();
        title.setBounds(0, 0, parent.getWidth(), 70);
        title.setOpaque(true);
        title.setBackground(DEFAULT_ACCENT_COLOR.get());
        title.setLayout(null);
        title.setBorder(LINE_BORDER);
        return title;
    }

    /**
     * @return head panel for view
     */
    public JPanel viewHeadPanel(JDesktopPane parent) {
        // setting up view head panel
        var viewHead = new JPanel();
        viewHead.setBounds(0, 0, parent.getWidth(), 24);
        viewHead.setLayout(null);
        var border = BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR.get());
        viewHead.setBorder(border);
        viewHead.setBackground(DEFAULT_BG_COLOR.get());
        viewHead.setOpaque(false);

        return viewHead;
    }

        /**
         * @return head label, which is part of viewHeadPanel
         */
        public JLabel headTxtLabel() {
            // setting up head label
            var head = new JLabel("PlaneSpotter > ");
            head.setBounds(5, 0, 600, 20);
            head.setFont(FONT_MENU.deriveFont(18));
            head.setForeground(DEFAULT_FONT_COLOR.get());
            head.setOpaque(false);

            return head;
        }

    /**
     * @return list panel
     */
    public JPanel listPanel(JDesktopPane parent) {
        // setting up list panel
        var list = new JPanel();
        list.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
        list.setBackground(DEFAULT_BG_COLOR.get());
        list.setLayout(null);
        list.setBorder(LINE_BORDER);
        list.setOpaque(false);

        return list;
    }

    /**
     * map panel
     */
    public JPanel mapPanel(JDesktopPane parent) {
        // setting up map panel
        var map = new JPanel();
        map.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
        map.setLayout(null);
        map.setBorder(LINE_BORDER);
        map.setOpaque(false);

        return map;
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
     * menu panel
     */
    public JPanel menuPanel(JDesktopPane parent) {
        // setting up menu panel
        var menu = new JPanel();
        menu.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        menu.setBackground(DEFAULT_BG_COLOR.get());
        menu.setLayout(null);
        menu.setOpaque(false);

        return menu;
    }

    /**
     * info panel
     */
    public JPanel infoPanel(JDesktopPane parent) {
        // setting up info panel
        var info = new JPanel();
        info.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        info.setBackground(DEFAULT_ACCENT_COLOR.get());
        info.setLayout(null);
        info.setOpaque(false);

        return info;
    }

    /**
     * search panel // TODO radioButtons, TxtField, 2 Buttons, JLabels -> aber extern
     */
    public JPanel searchPanel(JPanel parent) {
        // TODO: setting up search panel
        var search = new JPanel();
        search.setBounds(10, 175, parent.getWidth()-20, parent.getHeight()-265);
        search.setBackground(DEFAULT_ACCENT_COLOR.get());
        var border = BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get(), 1);
        search.setBorder(border);
        search.setLayout(null);
        search.setVisible(false);

        return search;
    }

    /**
     * src panel
     */
    public JPanel startPanel(JDesktopPane parent) {
        // setting up src screen panel
        var start = new JPanel();
        start.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
        start.setBackground(DEFAULT_BG_COLOR.get());
        start.setLayout(null);
        start.setBorder(LINE_BORDER);
        start.setOpaque(false);

        return start;
    }

    /**
     * background label
     */
    public JLabel backgroundLabel(JDesktopPane parent) {
        // setting up background image
        var bground = new JLabel(BGROUND_IMG.get());
        bground.setSize(parent.getWidth(), parent.getHeight());

        return bground;
    }

    /**
     * background label
     */
    public JLabel menuBgLabel(JDesktopPane parent) {
        // setting up background image
        var bground = new JLabel(MENU_BGROUND_IMG.get());
        bground.setSize(parent.getWidth(), parent.getHeight());

        return bground;
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

    public JLabel titleBackgroundLabel(JPanel parent) {
        var titleBackground = new JLabel();
        titleBackground.setBounds(parent.getBounds());
        titleBackground.setBorder(LINE_BORDER);
        var img = Images.TITLE.get();
        int width = titleBackground.getWidth();
        int height = titleBackground.getHeight();
        var bgroundImg = new ImageIcon(img.getImage().getScaledInstance(width, height, 4));
        titleBackground.setIcon(bgroundImg);
        return titleBackground;
    }

    public JLabel startScreenLabel(JPanel parent, ImageIcon image) {
        var lblStartScreen = new JLabel(image);
        lblStartScreen.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        lblStartScreen.setBorder(LINE_BORDER);
        lblStartScreen.setOpaque(false);
        return lblStartScreen;
    }


}

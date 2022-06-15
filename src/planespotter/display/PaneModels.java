package planespotter.display;

import planespotter.constants.DefaultColor;
import planespotter.constants.GUIConstants;
import planespotter.constants.Images;
import planespotter.controller.ActionHandler;

import javax.swing.*;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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

    JFrame windowFrame(ActionHandler listener) {
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
    JPanel mainPanel (JFrame parent) {
        // TODO: setting up mainpanel
        var mainpanel = new JPanel();
        mainpanel.setBounds(0, 0, parent.getWidth(), parent.getHeight()); // mainpanel width: 1260
        mainpanel.setLayout(null);
        mainpanel.setBackground(DEFAULT_BG_COLOR.get());
        return mainpanel;
    }

    JDesktopPane[] desktopPanes(JPanel parent) {
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
    JPanel titlePanel (JPanel parent) {
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
     * title text label
     */
    JLabel titleTxtLabel (JPanel parent) {
        // setting up title label
        var title_text = new JLabel("P l a n e S p o t t e r");
        title_text.setFont(TITLE_FONT);
        title_text.setForeground(DEFAULT_FG_COLOR.get());
        title_text.setFocusable(false);
        title_text.setBounds(parent.getWidth()/2-200, 0, 400, 70); // bounds in Bounds Klasse (?)

        return title_text;
    }

    /**
     * @return head panel for view
     */
    JPanel viewHeadPanel (JDesktopPane parent) {
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
        JLabel headTxtLabel() {
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
    JPanel listPanel (JDesktopPane parent) {
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
    JPanel mapPanel (JDesktopPane parent) {
        // setting up map panel
        var map = new JPanel();
        map.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
        map.setBackground(DEFAULT_BG_COLOR.get());
        map.setLayout(null);
        map.setBorder(LINE_BORDER);
        map.setOpaque(false);

        return map;
    }

    /**
     * menu panel
     */
    JPanel menuPanel (JDesktopPane parent) {
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
    JPanel infoPanel (JDesktopPane parent) {
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
    JPanel searchPanel (JPanel parent) {
        // TODO: setting up search panel
        var search = new JPanel();
        search.setBounds(10, 150, parent.getWidth()-20, parent.getHeight()-240);
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
    JPanel startPanel (JDesktopPane parent) {
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
    JLabel backgroundLabel (JDesktopPane parent) {
        // setting up background image
        var bground = new JLabel(BGROUND_IMG.get());
        bground.setSize(parent.getWidth(), parent.getHeight());

        return bground;
    }

    /**
     * background label
     */
    JLabel menuBgLabel (JDesktopPane parent) {
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
    JScrollPane listScrollPane(JTree inside, JPanel parent) {
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
    JFrame loadingScreen() {
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

    JLabel titleBackgroundLabel(JPanel parent) {
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

    JLabel startScreenLabel(JPanel parent, ImageIcon image) {
        var lblStartScreen = new JLabel(image);
        lblStartScreen.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        lblStartScreen.setBorder(LINE_BORDER);
        lblStartScreen.setOpaque(false);
        return lblStartScreen;
    }

    /**
     * @name SupplierDisplay
     * @version 1.0
     *
     * inner class SupplierDisplay is a little Display for the SupplierMain
     * @see planespotter.SupplierMain
     */
    public static class SupplierDisplay {

        private static final Runtime runtime = Runtime.getRuntime();
        // inserted values indexes:   0 = allFrames,   1 = newPlanes,   2 = newFlights
        private final int[] inserted = {0, 0, 0};
        private int totalMemory = 13212;
        private final JProgressBar progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, totalMemory);
        private final JLabel insertedLabel = new JLabel(),
                             memoryLabel = new JLabel(),
                             newPlanesLabel = new JLabel(),
                             newFlightsLabel = new JLabel();
        private final JLabel[] labels = { insertedLabel, newPlanesLabel, newFlightsLabel, memoryLabel };
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
                lbl.setBounds(10, y, compWidth-20, 20);
                y += 30;
            }
            this.progressBar.setForeground(DEFAULT_MAP_ICON_COLOR.get());
            this.progressBar.setBounds(10, y, compWidth-20, 20);
            this.progressBar.setString("Memory Usage");
            this.progressBar.setStringPainted(true);
            this.progressBar.setBorder(MENU_BORDER);

            var seps = new JSeparator[] {
                    new JSeparator(),new JSeparator(),new JSeparator(),
                    new JSeparator(),new JSeparator()
            };
            y = 30;
            for (var sep : seps) {
                sep.setOpaque(true);
                sep.setForeground(Color.DARK_GRAY);
                sep.setBounds(10, y+5, compWidth-20, 2);
                y += 30;
            }

            var panel = new JPanel();
            //panel.setBackground(Color.DARK_GRAY);
            panel.setLayout(null);
            panel.setSize(size);
            Arrays.stream(this.labels).forEach(panel::add);
            Arrays.stream(seps).forEach(panel::add);
            panel.add(this.progressBar);

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






}

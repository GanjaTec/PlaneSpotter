package planespotter.display.models;

import libs.UWPButton;
import org.jetbrains.annotations.NotNull;
import planespotter.constants.Images;
import planespotter.controller.ActionHandler;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.DefaultColor.*;

/**
 * @name MenuModels
 * @author jml04
 * @version 1.0
 *
 * MenuModels class contains different menu component models
 */
public final class MenuModels {

    @NotNull
    public static JMenuBar topMenuBar(@NotNull final ActionHandler actionHandler) {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File"),
              liveMapMenu = new JMenu("Live-Map"),
              searchMenu = new JMenu("Search"),
              statsMenu = new JMenu("Statistics"),
              supplierMenu = new JMenu("Supplier"),
              settingsMenu = new JMenu("Settings"),
              closeMenu = new JMenu("Close View"),
              helpMenu = new JMenu("Help");
        liveMapMenu.addMouseListener(actionHandler);
        searchMenu.addMouseListener(actionHandler);
        settingsMenu.addMouseListener(actionHandler);
        closeMenu.addMouseListener(actionHandler);

        JMenuItem[] fileItems = new JMenuItem[] {
                new JMenuItem("Open", Images.OPEN_FILE_ICON_16x.get()),
                new JMenuItem("Save As", Images.SAVE_FILE_ICON_16x.get()),
                new JMenuItem("Exit", Images.EXIT_ICON_16x.get())
        };
        JMenu heatMapMenu = new JMenu("Heat-Map");
        heatMapMenu.setIcon(Images.HEATMAP_ICON_16x.get());
        JMenuItem[] statsItems = new JMenuItem[] {
                new JMenuItem("Top-Airports", Images.STATS_ICON_16x.get()),
                new JMenuItem("Top-Airlines", Images.STATS_ICON_16x.get()),
                heatMapMenu
        };
        JMenuItem[] heatMapItems = new JMenuItem[] {
                new JMenuItem("Position-HeatMap"),
                new JMenuItem("coming soon...")
        };
        JMenuItem[] supplierItems = new JMenuItem[] {
                new JMenuItem("Fr24-Supplier", Images.PLANE_ICON_16x.get()),
                new JMenuItem("ADSB-Supplier", Images.PLANE_ICON_16x.get()),
                new JMenuItem("Antenna", Images.ANTENNA_ICON_16x.get())
        };
        Font font = FONT_MENU.deriveFont(13f);

        Arrays.stream(fileItems).forEach(item -> {
            item.addMouseListener(actionHandler);
            item.setFont(font);
            fileMenu.add(item);
            fileMenu.addSeparator();
        });
        Arrays.stream(statsItems).forEach(item -> {
            if (item instanceof JMenu menu) {
                Arrays.stream(heatMapItems).forEach(i -> {
                    i.addMouseListener(actionHandler);
                    i.setFont(font);
                    menu.add(i);
                    menu.addSeparator();
                });
            } else {
                item.addMouseListener(actionHandler);
            }
            item.setFont(font);
            statsMenu.add(item);
            statsMenu.addSeparator();

        });
        Arrays.stream(supplierItems).forEach(item -> {
            item.addMouseListener(actionHandler);
            item.setFont(font);
            supplierMenu.add(item);
            supplierMenu.addSeparator();
        });
        JMenu[] menus = new JMenu[] {
                fileMenu, liveMapMenu, searchMenu, statsMenu, supplierMenu, settingsMenu, closeMenu, helpMenu
        };
        Arrays.stream(menus).forEach(m -> {
            m.setFont(font);
            menuBar.add(m);
        });
        return menuBar;
    }

    /**
     *
     */
    public JProgressBar progressBar(JPanel parent) {
        // seting up progress bar
        var progressbar = new JProgressBar();
        progressbar.setBorder(MENU_BORDER);
        progressbar.setBackground(DEFAULT_FONT_COLOR.get());
        progressbar.setBorderPainted(true);
        progressbar.setForeground(new Color(92, 214, 92));
        progressbar.setBounds(parent.getWidth() - 386, 4, 200, 16);
        progressbar.setIndeterminate(true);
        progressbar.setVisible(false);

        return progressbar;
    }

    // TODO: 01.07.2022 man könnte die kompletten Settings mit einer
    //  JTable machen, linke spalte keys, rechte spalte values

    // TODO: 01.07.2022 Settings class oder inner class

    /**
     * @return settings option pane (which pops up)
     */
    public JDialog settingsDialog(@NotNull JFrame parent, @NotNull ActionHandler actionHandler) {
            var maxLoadLbl = new JLabel("Max. loaded Data:");
                maxLoadLbl.setBounds(20, 10, 300, 25);
                maxLoadLbl.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
                maxLoadLbl.setFont(FONT_MENU);
                maxLoadLbl.setOpaque(false);
            var mapType = new JLabel("Map Type:");
                mapType.setBounds(20, 50, 300, 25);
                mapType.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
                mapType.setFont(FONT_MENU);
                mapType.setOpaque(false);
            var livePeriod = new JLabel("Live Data Period (sec):");
                livePeriod.setBounds(20, 90, 300, 25);
                livePeriod.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
                livePeriod.setFont(FONT_MENU);
                livePeriod.setOpaque(false);
            var liveMapFilters = new JLabel("Live Map Filters: ");
                liveMapFilters.setBounds(20, 130, 300, 25);
                liveMapFilters.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
                liveMapFilters.setFont(FONT_MENU);
                liveMapFilters.setOpaque(false);

        var settings = new JDialog(parent);
        settings.setBounds(parent.getWidth()/2-250, parent.getHeight()/2-200, 540, 400);
        settings.setLayout(null);
        settings.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        settings.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        settings.setType(Window.Type.POPUP);
        settings.setResizable(false);
        settings.setFocusable(false);
        // adding labels
        JSeparator[] separators = this.separators(4, 40, 40);
        JComboBox<String> mapTypeCmbBox = this.settings_mapTypeCmbBox(actionHandler);
        JTextField maxLoadTxtField = this.settings_maxLoadTxtField(actionHandler);
        JButton[] settingsButtons = this.settingsButtons(settings, actionHandler);
        UWPButton filterButton = this.settingsFilterButton(actionHandler);
        JSlider livePeriodSlider = this.settingsLivePeriodSlider();

        Arrays.stream(separators).forEach(settings::add);
        Arrays.stream(settingsButtons).forEach(settings::add);
        settings.add(filterButton);
        settings.add(livePeriodSlider);
        settings.add(maxLoadTxtField);
        settings.add(mapTypeCmbBox);
        settings.add(maxLoadLbl);
        settings.add(mapType);
        settings.add(livePeriod);
        settings.add(liveMapFilters);

        settings.setVisible(false);

        return settings;
    }

    /**
     * settings opt. pane max-load text field
     */
    public JTextField settings_maxLoadTxtField(KeyListener listener) {
        var maxLoadTxtfield = new JTextField();
        maxLoadTxtfield.setBounds(350, 10, 150, 25);
        maxLoadTxtfield.setBorder(BorderFactory.createLineBorder(DEFAULT_FONT_COLOR.get()));
        maxLoadTxtfield.setBackground(DEFAULT_FONT_COLOR.get());
        maxLoadTxtfield.setForeground(DEFAULT_ACCENT_COLOR.get());
        maxLoadTxtfield.setFont(FONT_MENU);
        maxLoadTxtfield.addKeyListener(listener);

        return maxLoadTxtfield;
    }

    public JButton[] settingsButtons(JDialog parent, ActionListener listener) {
        int mid = parent.getWidth() / 2;
        int height = parent.getHeight() - 80;
        var cancel = new UWPButton("Cancel");
            cancel.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            cancel.setForeground(DEFAULT_FONT_COLOR.get());
            cancel.setFont(FONT_MENU);
            cancel.setBounds(mid - 140, height, 120, 25);
            cancel.addActionListener(listener);
        var confirm = new UWPButton("Confirm");
            confirm.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            confirm.setForeground(DEFAULT_FONT_COLOR.get());
            confirm.setFont(FONT_MENU);
            confirm.setBounds(mid + 20, height, 120, 25);
            confirm.addActionListener(listener);
        return new UWPButton[] {
                cancel, confirm
        };
    }

    // TODO: 05.07.2022 Settings class

    public JComboBox<String> settings_mapTypeCmbBox(ItemListener listener) {
        var mapTypeCmbBox = new JComboBox<>(new String[] {
                "Default Map",
                "Bing Map",
                "Transport Map"
        });
        mapTypeCmbBox.setBounds(350, 50, 150,  25);
        mapTypeCmbBox.setBorder(BorderFactory.createLineBorder(DEFAULT_FONT_COLOR.get()));
        mapTypeCmbBox.setBackground(DEFAULT_FONT_COLOR.get());
        mapTypeCmbBox.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        mapTypeCmbBox.setFont(FONT_MENU);
        mapTypeCmbBox.addItemListener(listener);

        return mapTypeCmbBox;
    }

    public JSlider settingsLivePeriodSlider() {
        var slider = new JSlider(JSlider.HORIZONTAL, 1, 10, 2);
        slider.setBounds(350, 90, 150, 25);
        slider.setToolTipText("Live-Data loading period in seconds (1-10)");

        return slider;
    }

    public UWPButton settingsFilterButton(ActionListener listener) {
        var button = new UWPButton("Filters");
        button.setBounds(350, 130, 150, 25);
        button.setEffectColor(DEFAULT_FONT_COLOR.get());
        button.setSelectedColor(DEFAULT_MAP_ICON_COLOR.get());
        button.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        button.setFont(FONT_MENU);
        button.addActionListener(listener);

        return button;
    }

    public JSeparator[] separators(int count, int startY, int plus) {
        var seps = new JSeparator[count];
        for (int i = 0; i < count; i++) {
            var s = new JSeparator();
            s.setBounds(20, startY, 480, 2);
            s.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            seps[i] = s;
            startY += plus;
        }
        return seps;
    }

    /**
     * @return file chooser for file dialog
     */
    public static JFileChooser fileSaver(JFrame parent) {
        var home = FileSystemView.getFileSystemView().getHomeDirectory();
        var fileChooser = new JFileChooser(home);
        fileChooser.setAcceptAllFileFilterUsed(false);
        var pls = new FileNameExtensionFilter("nur .pls-Dateien", "pls"); // TODO constant?
        fileChooser.setFileFilter(pls);
        fileChooser.showSaveDialog(parent);

        return fileChooser;
    }

    /**
     * @return file chooser for file dialog
     */
    public static JFileChooser fileLoader(JFrame parent) {
        var home = FileSystemView.getFileSystemView().getHomeDirectory();
        var fileChooser = new JFileChooser(home);
        fileChooser.setAcceptAllFileFilterUsed(false);
        var pls = new FileNameExtensionFilter("only .pls-Files", "pls", ".pls"); // which is the right one?
        fileChooser.setFileFilter(pls);
        fileChooser.showOpenDialog(parent);

        return fileChooser;
    }

    // TODO SETTINGS
    // reload data button
    // other settings
    // evtl. Theme oder so -> Farbe
}

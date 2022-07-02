package planespotter.display.models;

import libs.UWPButton;

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

    /**
     * menubar (contains the other menu components)
     */
    public JMenuBar menuBar(JPanel parent) {
        // setting up menubar
        var menubar = new JMenuBar();
        menubar.setBackground(DEFAULT_BG_COLOR.get());
        menubar.setForeground(DEFAULT_FG_COLOR.get());
        menubar.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        menubar.setBorder(LINE_BORDER);
        menubar.setLayout(null);
        menubar.setOpaque(false);

        return menubar;
    }

    /**
     * list button
     */
    public JButton listButton(JMenuBar parent, ActionListener listener) {
        // setting up list button
        var list = new UWPButton("List-View");
        list.setBackground(DEFAULT_ACCENT_COLOR.get());
        list.setForeground(DEFAULT_FONT_COLOR.get());
        list.setBounds(10, 15, ((parent.getWidth()-20)/2)-5, 25);
        list.setFont(FONT_MENU);
        list.addActionListener(listener);

        return list;
    }

    /**
     * map button
     */
    public JButton mapButton(JMenuBar parent, ActionListener listener) {
        // setting up list button
        var map = new UWPButton("Live-Map");
        map.setBackground(DEFAULT_ACCENT_COLOR.get());
        map.setForeground(DEFAULT_FONT_COLOR.get());
        map.setBounds(145, 15, ((parent.getWidth()-20)/2)-5, 25);
        map.setFont(FONT_MENU);
        map.addActionListener(listener);

        return map;
    }

    /**
     * settings button
     */
    public JButton statisticsButton(JMenuBar parent, ActionListener listener) {
        // setting up settings menu
        var settings = new UWPButton("Statistics");
        settings.setBackground(DEFAULT_ACCENT_COLOR.get());
        settings.setForeground(DEFAULT_FONT_COLOR.get());
        settings.setFont(FONT_MENU);
        settings.setBounds(10, 55, parent.getWidth()-20, 25);
        settings.addActionListener(listener);

        return settings;
    }

    /**
     * settings button
     */
    public JButton supplierButton(JMenuBar parent, ActionListener listener) {
        // setting up settings menu
        var settings = new UWPButton("Supplier");
        settings.setBackground(DEFAULT_ACCENT_COLOR.get());
        settings.setForeground(DEFAULT_FONT_COLOR.get());
        settings.setFont(FONT_MENU);
        settings.setBounds(10, 95, parent.getWidth()-20, 25);
        settings.addActionListener(listener);

        return settings;
    }

    /**
     * settings button
     */
    public JButton settingsButton(JMenuBar parent, ActionListener listener) {
        // setting up settings menu
        var settings = new UWPButton("Settings");
        settings.setBackground(DEFAULT_ACCENT_COLOR.get());
        settings.setForeground(DEFAULT_FONT_COLOR.get());
        settings.setFont(FONT_MENU);
        settings.setBounds(10, 135, parent.getWidth()-20, 25);
        settings.addActionListener(listener);

        return settings;
    }

    /**
     * search-filter button
     */
    public JButton searchButton(JMenuBar parent, ActionListener listener) {
        // setting up search-settings menu
        var search_settings = new UWPButton("Search");
        search_settings.setBackground(DEFAULT_ACCENT_COLOR.get());
        search_settings.setForeground(DEFAULT_FONT_COLOR.get());
        search_settings.setFont(FONT_MENU);
        search_settings.setBounds(10, parent.getHeight()-15, parent.getWidth()-20, 25);
        search_settings.addActionListener(listener);

        return search_settings;
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

    /**
     * close view button
     */
    public JButton fileButton(JDesktopPane parent, ActionListener listener) {
        // setting up view close button
        var file = new UWPButton();
        file.setText("File");
        file.setBackground(Color.DARK_GRAY);
        file.setForeground(DEFAULT_FONT_COLOR.get());
        file.setBounds(parent.getWidth()-184, 4, 80, 16);
        file.setFont(new Font("DialogInput", Font.PLAIN, 14));
        file.addActionListener(listener);

        return file;
    }

    /**
     * close view button
     */
    public JButton closeViewButton(JDesktopPane parent, ActionListener listener) {
        // setting up view close button
        var closeView = new UWPButton();
        closeView.setText("Close");
        closeView.setBackground(Color.DARK_GRAY);
        closeView.setForeground(DEFAULT_FONT_COLOR.get());
        closeView.setBounds(parent.getWidth()-85, 4, 80, 16);
        closeView.setFont(new Font("DialogInput", Font.PLAIN, 14));
        closeView.addActionListener(listener);

        return closeView;
    }

    // TODO: 01.07.2022 man könnte die kompletten Settings mit einer
    //  JTable machen, linke spalte keys, rechte spalte values

    // TODO: 01.07.2022 Settings class oder inner class

    /**
     * @return settings option pane (which pops up)
     */
    public JDialog settingsDialog(JFrame parent) {
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
        var settings = new JDialog(parent);
        settings.setBounds(parent.getWidth()/2-250, parent.getHeight()/2-200, 540, 400);
        settings.setLayout(null);
        settings.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        settings.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        settings.setType(Window.Type.POPUP);
        settings.setResizable(false);
        settings.setFocusable(false);
        // adding labels
        var seps = this.separators(3, 40, 40);
        Arrays.stream(seps).forEach(settings::add);
        settings.add(maxLoadLbl);
        settings.add(mapType);
        settings.add(livePeriod);


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
        var pls = new FileNameExtensionFilter("only .pls-Files", "pls");
        fileChooser.setFileFilter(pls);
        fileChooser.showOpenDialog(parent);

        return fileChooser;
    }

    public JButton[] fileMenu(JPanel parent, ActionListener listener) {
        var components = new JButton[] {
                new UWPButton("Back"),
                new UWPButton("Save"),
                new UWPButton("Open")
        };
        int minus = 84;
        for (var comp : components) {
            comp.setBounds(parent.getWidth() - minus, 4, 80, 16);
            comp.setBackground(Color.DARK_GRAY);
            comp.setForeground(DEFAULT_FONT_COLOR.get());
            comp.setFont(new Font("DialogInput", Font.PLAIN, 14));
            comp.setVisible(false);
            comp.addActionListener(listener);
            minus += 84;
        }
        return components;
    }

    // TODO SETTINGS
    // reload data button
    // confirm button
    // other settings
    // evtl. Theme oder so -> Farbe

    //TODO evtl. methode getAllAsList()
}

package planespotter.display;

import libs.UWPButton;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;

import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.GUIConstants.DefaultColor.*;

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
    JMenuBar menuBar (JPanel parent) {
        // setting up menubar
        var menubar = new JMenuBar();
        menubar.setBackground(DEFAULT_BG_COLOR.get());
        menubar.setForeground(DEFAULT_FG_COLOR.get());
        menubar.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        menubar.setBorder(LINE_BORDER);
        menubar.setLayout(null);

        return menubar;
    }

    /**
     * list button
     */
    JButton listButton (JMenuBar parent) {
        // setting up list button
        var list = new UWPButton();
        list.setText("List-View");
        list.setBackground(DEFAULT_ACCENT_COLOR.get());
        list.setForeground(DEFAULT_FONT_COLOR.get());
        list.setBounds(10, 15, ((parent.getWidth()-20)/2)-5, 25);
        list.setFont(FONT_MENU);

        return list;
    }

    /**
     * map button
     */
    JButton mapButton (JMenuBar parent) {
        // setting up list button
        var map = new UWPButton();
        map.setText("Live-Map");
        map.setBackground(DEFAULT_ACCENT_COLOR.get());
        map.setForeground(DEFAULT_FONT_COLOR.get());
        map.setBounds(145, 15, ((parent.getWidth()-20)/2)-5, 25);
        map.setFont(FONT_MENU);

        return map;
    }

    /**
     * settings button
     */
    JButton settingsButton (JMenuBar parent) {
        // setting up settings menu
        var settings = new UWPButton();
        settings.setText("Settings");
        settings.setBackground(DEFAULT_ACCENT_COLOR.get());
        settings.setForeground(DEFAULT_FONT_COLOR.get());
        settings.setFont(FONT_MENU);
        settings.setBounds(10, 55, parent.getWidth()-20, 25);

        return settings;
    }

    /**
     * search-filter button
     */
    JButton searchButton (JMenuBar parent) {
        // setting up search-settings menu
        var search_settings = new UWPButton();
        search_settings.setText("Search");
        search_settings.setBackground(DEFAULT_ACCENT_COLOR.get());
        search_settings.setForeground(DEFAULT_FONT_COLOR.get());
        search_settings.setFont(FONT_MENU);
        search_settings.setBounds(10, parent.getHeight()-15, parent.getWidth()-20, 25);

        return search_settings;
    }

    /**
     *
     */
    JProgressBar progressBar (JMenuBar parent) {
        // seting up progress bar
        var progressbar = new JProgressBar();
        progressbar.setBorder(LINE_BORDER);
        progressbar.setBackground(DEFAULT_FONT_COLOR.get());
        progressbar.setBorderPainted(true);
        progressbar.setForeground(new Color(92, 214, 92));
        progressbar.setBounds(10, 90, parent.getWidth()-20, 15);
        progressbar.setIndeterminate(true);
        progressbar.setVisible(false);

        return progressbar;
    }

    /**
     * search text field
     */
    JTextField searchTextField (JMenuBar parent) {
        // setting up search text field
        var search = new JTextField();
        search.setToolTipText("Search");
        search.setBounds(10, parent.getHeight()-60, parent.getWidth()-20, 25);
        search.setBackground(Color.WHITE);
        search.setFont(FONT_MENU);
        search.setBorder(LINE_BORDER);

        return search;
    }

    /**
     * close view button
     */
    JButton fileButton (JDesktopPane parent) {
        // setting up view close button
        var file = new UWPButton();
        file.setText("File");
        file.setBackground(Color.DARK_GRAY);
        file.setForeground(DEFAULT_FONT_COLOR.get());
        file.setBounds(parent.getWidth()-184, 4, 80, 16);
        file.setFont(new Font("DialogInput", Font.PLAIN, 14));

        return file;
    }

    /**
     * close view button
     */
    JButton closeViewButton (JDesktopPane parent) {
        // setting up view close button
        var closeView = new UWPButton();
        closeView.setText("Close");
        closeView.setBackground(Color.DARK_GRAY);
        closeView.setForeground(DEFAULT_FONT_COLOR.get());
        closeView.setBounds(parent.getWidth()-85, 4, 80, 16);
        closeView.setFont(new Font("DialogInput", Font.PLAIN, 14));

        return closeView;
    }

    /**
     * @return settings option pane (which pops up)
     */
    JDialog settingsDialog (JFrame parent) {
            var maxLoadLbl = new JLabel("Max. loaded Data:");
            maxLoadLbl.setBounds(20, 20, 180, 30);
            maxLoadLbl.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            maxLoadLbl.setFont(FONT_MENU);
            maxLoadLbl.setOpaque(false);
            var mapType = new JLabel("Map Type:");
            mapType.setBounds(20, 70, 300, 30);
            mapType.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            mapType.setFont(FONT_MENU);
            mapType.setOpaque(false);
        var settings = new JDialog(parent);
        settings.setBounds(parent.getWidth()/2-250, parent.getHeight()/2-200, 500, 400);
        settings.setLayout(null);
        settings.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        settings.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        settings.setType(Window.Type.POPUP);
        settings.setResizable(false);
        settings.setFocusable(false);
        settings.add(maxLoadLbl);
        settings.add(mapType);
        settings.setVisible(false);

        return settings;
    }

    /**
     * settings opt. pane max-load text field
     */
    JTextField settings_maxLoadTxtField () {
        var maxLoadTxtfield = new JTextField();
        maxLoadTxtfield.setBounds(200, 20, 50, 30);
        maxLoadTxtfield.setBorder(BorderFactory.createLineBorder(DEFAULT_FONT_COLOR.get()));
        maxLoadTxtfield.setBackground(DEFAULT_BG_COLOR.get());
        maxLoadTxtfield.setForeground(DEFAULT_MAP_ICON_COLOR.get());
        maxLoadTxtfield.setFont(FONT_MENU);

        return maxLoadTxtfield;
    }

    JButton[] settingsButtons (JDialog parent) {
        int mid = parent.getWidth() / 2;
        int height = parent.getHeight() - 80;
        var cancel = new UWPButton("Cancel");
            cancel.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            cancel.setForeground(DEFAULT_FONT_COLOR.get());
            cancel.setFont(FONT_MENU);
        cancel.setBounds(mid - 140, height, 120, 30);
        var confirm = new UWPButton("Confirm");
        confirm.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        confirm.setForeground(DEFAULT_FONT_COLOR.get());
        confirm.setFont(FONT_MENU);
        confirm.setBounds(mid + 20, height, 120, 30);
        return new UWPButton[] {
                cancel, confirm
        };
    }

    JComboBox<String> settings_mapTypeCmbBox () {
        var mapTypeCmbBox = new JComboBox<>(new String[]{
                "Bing Map",
                "Default Map",
                "Transport Map"
        });
        mapTypeCmbBox.setBounds(200, 70, 100,  30);
        mapTypeCmbBox.setBorder(BorderFactory.createLineBorder(DEFAULT_FONT_COLOR.get()));
        mapTypeCmbBox.setBackground(DEFAULT_BG_COLOR.get());
        mapTypeCmbBox.setForeground(DEFAULT_MAP_ICON_COLOR.get());
        mapTypeCmbBox.setFont(FONT_MENU);

        return mapTypeCmbBox;
    }

    /**
     * @return file chooser for file dialog
     */
    public JFileChooser fileSaver (JFrame parent) {
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
    public JFileChooser fileLoader (JFrame parent) {
        var home = FileSystemView.getFileSystemView().getHomeDirectory();
        var fileChooser = new JFileChooser(home);
        fileChooser.setAcceptAllFileFilterUsed(false);
        var pls = new FileNameExtensionFilter("only .pls-Files", "pls");
        fileChooser.setFileFilter(pls);
        fileChooser.showOpenDialog(parent);

        return fileChooser;
    }

    JButton[] fileMenu (JPanel parent) {
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

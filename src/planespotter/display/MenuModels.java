package planespotter.display;

import libs.UWPButton;

import planespotter.constants.ViewType;
import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.model.FileMaster;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.util.HashMap;

import static planespotter.constants.GUIConstants.*;

/**
 * @name MenuModels
 * @author jml04
 * @version 1.0
 *
 * MenuModels class contains different menu component models
 */
final class MenuModels {

    // TODO AUSWAHL: selectAll, selectByID, selectByICAO, selectByName, selectByIATA, selectByPlaneType; //...

    /**
     * menubar (contains the other menu components)
     */
    JMenuBar menuBar (JPanel parent) {
        // TODO: setting up menubar
        var menubar = new JMenuBar();
        menubar.setBackground(DEFAULT_BG_COLOR);
        menubar.setForeground(DEFAULT_FG_COLOR);
        menubar.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        menubar.setBorder(LINE_BORDER);
        menubar.setLayout(null);

        return menubar;
    }

    /**
     * list button
     */
    JButton listButton (JMenuBar parent) {
        // TODO setting up list button
        var list = new UWPButton();
        list.setText("List-View");
        list.setBackground(DEFAULT_ACCENT_COLOR);
        list.setForeground(DEFAULT_FONT_COLOR);
        list.setBounds(10, 15, ((parent.getWidth()-20)/2)-5, 25);
        list.setFont(FONT_MENU);

        return list;
    }

    /**
     * map button
     */
    JButton mapButton (JMenuBar parent) {
        // TODO setting up list button
        var map = new UWPButton();
        map.setText("Live-Map");
        map.setBackground(DEFAULT_ACCENT_COLOR);
        map.setForeground(DEFAULT_FONT_COLOR);
        map.setBounds(145, 15, ((parent.getWidth()-20)/2)-5, 25);
        map.setFont(FONT_MENU);

        return map;
    }

    /**
     * settings button
     */
    JButton settingsButton (JMenuBar parent) {
        // TODO: setting up settings menu
        var settings = new UWPButton();
        settings.setText("Settings");
        settings.setBackground(DEFAULT_ACCENT_COLOR);
        settings.setForeground(DEFAULT_FONT_COLOR);
        settings.setFont(FONT_MENU);
        settings.setBounds(10, 55, parent.getWidth()-20, 25);

        return settings;
    }

    /**
     * search-filter button
     */
    JButton searchButton (JMenuBar parent) {
        // TODO: setting up search-settings menu
        var search_settings = new UWPButton();
        search_settings.setText("Search");
        search_settings.setBackground(DEFAULT_ACCENT_COLOR);
        search_settings.setForeground(DEFAULT_FONT_COLOR);
        search_settings.setFont(FONT_MENU);
        search_settings.setBounds(10, parent.getHeight()-15, parent.getWidth()-20, 25);

        return search_settings;
    }

    /**
     *
     */
    JProgressBar progressBar (JMenuBar parent) {
        // TODO: seting up progress bar
        var progressbar = new JProgressBar();
        progressbar.setBorder(LINE_BORDER);
        progressbar.setBackground(DEFAULT_FONT_COLOR);
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
        // TODO: setting up search text field
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
        // TODO: setting up view close button
        var file = new UWPButton();
        file.setText("File");
        file.setBackground(Color.DARK_GRAY);
        file.setForeground(DEFAULT_FONT_COLOR);
        file.setBounds(parent.getWidth()-184, 4, 80, 16);
        file.setFont(new Font("DialogInput", Font.PLAIN, 14));

        return file;
    }

    /**
     * close view button
     */
    JButton closeViewButton (JDesktopPane parent) {
        // TODO: setting up view close button
        var closeView = new UWPButton();
        closeView.setText("Close");
        closeView.setBackground(Color.DARK_GRAY);
        closeView.setForeground(DEFAULT_FONT_COLOR);
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
            maxLoadLbl.setForeground(DEFAULT_FONT_COLOR);
            maxLoadLbl.setFont(FONT_MENU);
            maxLoadLbl.setOpaque(false);
            var mapType = new JLabel("Map Type:");
            mapType.setBounds(20, 70, 300, 30);
            mapType.setForeground(DEFAULT_FONT_COLOR);
            mapType.setFont(FONT_MENU);
            mapType.setOpaque(false);
        var settings = new JDialog(parent);
        settings.setBounds(parent.getWidth()/2-250, parent.getHeight()/2-200, 500, 400);
        settings.setLayout(null);
        settings.setBackground(DEFAULT_SEARCH_ACCENT_COLOR);
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
        maxLoadTxtfield.setBorder(BorderFactory.createLineBorder(DEFAULT_FONT_COLOR));
        maxLoadTxtfield.setBackground(DEFAULT_BG_COLOR);
        maxLoadTxtfield.setForeground(DEFAULT_MAP_ICON_COLOR);
        maxLoadTxtfield.setFont(FONT_MENU);

        return maxLoadTxtfield;
    }

    JButton[] settingsButtons (JDialog parent) {
        int mid = parent.getWidth() / 2;
        int height = parent.getHeight() - 80;
        var cancel = new UWPButton("Cancel");
            cancel.setBackground(DEFAULT_SEARCH_ACCENT_COLOR);
            cancel.setForeground(DEFAULT_FONT_COLOR);
            cancel.setFont(FONT_MENU);
        cancel.setBounds(mid - 140, height, 120, 30);
        var confirm = new UWPButton("Confirm");
        confirm.setBackground(DEFAULT_SEARCH_ACCENT_COLOR);
        confirm.setForeground(DEFAULT_FONT_COLOR);
        confirm.setFont(FONT_MENU);
        confirm.setBounds(mid + 20, height, 120, 30);
        return new UWPButton[] {
                cancel, confirm
        };
    }

    JComboBox<String> settings_mapTypeCmbBox () {
        var mapTypeCmbBox = new JComboBox<String>(new String[] {
                "Bing Map",
                "Default Map",
                "Transport Map"
        });
        mapTypeCmbBox.setBounds(200, 70, 100,  30);
        mapTypeCmbBox.setBorder(BorderFactory.createLineBorder(DEFAULT_FONT_COLOR));
        mapTypeCmbBox.setBackground(DEFAULT_BG_COLOR);
        mapTypeCmbBox.setForeground(DEFAULT_MAP_ICON_COLOR);
        mapTypeCmbBox.setFont(FONT_MENU);

        return mapTypeCmbBox;
    }

    /**
     * @return file chooser for file dialog
     */
    JFileChooser fileSaver (JFrame parent) {
        var home = FileSystemView.getFileSystemView().getHomeDirectory();
        var fileChooser = new JFileChooser(home);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("nur .pls-Dateien", "pls"));
        fileChooser.showSaveDialog(parent);
        new FileMaster().savePlsFile(fileChooser);

        return fileChooser;
    }

    /**
     * @return file chooser for file dialog
     */
    JFileChooser fileLoader (JFrame parent) throws DataNotFoundException {
        var home = FileSystemView.getFileSystemView().getHomeDirectory();
        var fileChooser = new JFileChooser(home);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("only .pls-Files", "pls"));
        fileChooser.showOpenDialog(parent);
        HashMap<Integer, DataPoint> route = null;
        try {
            route = new FileMaster().loadPlsFile(fileChooser);
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        int flightID;
        if (route != null) {
            flightID = route.get(0).getFlightID();
        } else {
            throw new DataNotFoundException("No route to save, select a route first!");
        }
        Controller.getInstance().show(ViewType.MAP_TRACKING, flightID + "");
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
            comp.setForeground(DEFAULT_FONT_COLOR);
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

package planespotter.display;

import planespotter.constants.ViewType;
import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.model.FileMaster;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.Paths.SRC_PATH;

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
    static JMenuBar menuBar (JPanel parent) {
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
     *
     */
    static JButton fileButton (JMenuBar parent) {
        // TODO: setting up file button
        var file = new JButton("File");
        file.setFont(FONT_MENU);
        file.setBorder(MENU_BORDER);
        file.setBackground(DEFAULT_ACCENT_COLOR);
        file.setForeground(DEFAULT_FONT_COLOR);
        file.setBounds(10, 15, parent.getWidth()-20, 25);
        //file.addActionListener(new Listener());

        return file;
    }

    /**
     * list button
     */
    static JButton listButton (JMenuBar parent) {
        // TODO setting up list button
        var list = new JButton("List-View");
        list.setBackground(DEFAULT_ACCENT_COLOR);
        list.setForeground(DEFAULT_FONT_COLOR);
        list.setBorder(MENU_BORDER);
        list.setBounds(10, 55, ((parent.getWidth()-20)/2)-5, 25);
        list.setFont(FONT_MENU);
        //list.addActionListener(new Listener());

        return list;
    }

    /**
     * map button
     */
    static JButton mapButton (JMenuBar parent) {
        // TODO setting up list button
        var map = new JButton("Live-Map");
        map.setBackground(DEFAULT_ACCENT_COLOR);
        map.setForeground(DEFAULT_FONT_COLOR);
        map.setBorder(MENU_BORDER);
        map.setBounds(145, 55, ((parent.getWidth()-20)/2)-5, 25);
        map.setFont(FONT_MENU);
        //map.addActionListener(new Listener());

        return map;
    }

    /**
     * settings button
     */
    static JButton settingsButton (JMenuBar parent) {
        // TODO: setting up settings menu
        var settings = new JButton("Settings");
        settings.setFont(FONT_MENU);
        settings.setBorder(MENU_BORDER);
        settings.setBackground(DEFAULT_ACCENT_COLOR);
        settings.setForeground(DEFAULT_FONT_COLOR);
        settings.setBounds(10, 95, parent.getWidth()-20, 25);
        //settings.addActionListener(new Listener());

        return settings;
    }

    /**
     * search-filter button
     */
    static JButton searchButton (JMenuBar parent) {
        // TODO: setting up search-settings menu
        var search_settings = new JButton("Search");
        search_settings.setFont(FONT_MENU);
        search_settings.setBorder(MENU_BORDER);
        search_settings.setBackground(DEFAULT_ACCENT_COLOR);
        search_settings.setForeground(DEFAULT_FONT_COLOR);
        search_settings.setBounds(10, parent.getHeight()-15, parent.getWidth()-20, 25);
        //search_settings.addActionListener(new Listener());

        return search_settings;
    }

    /**
     *
     */
    static JProgressBar progressBar (JMenuBar parent) {
        // TODO: seting up progress bar
        var progressbar = new JProgressBar(0, 100);
        progressbar.setBorder(LINE_BORDER);
        progressbar.setBackground(DEFAULT_FONT_COLOR);
        progressbar.setBorderPainted(true);
        progressbar.setForeground(new Color(92, 214, 92));
        progressbar.setBounds(10, 135, parent.getWidth()-20, 15);
        progressbar.setVisible(false);
        progressbar.setValue(0);

        return progressbar;
    }

    /**
     * search text field
     */
    static JTextField searchTextField (JMenuBar parent) {
        // TODO: setting up search text field
        var search = new JTextField();
        search.setToolTipText("Search");
        search.setBounds(10, parent.getHeight()-60, parent.getWidth()-20, 25);
        search.setBackground(Color.WHITE);
        search.setFont(FONT_MENU);
        search.setBorder(LINE_BORDER);
        //search.addKeyListener(new Listener());

        return search;
    }

    /**
     * close view button
     */
    static JButton closeViewButton (JDesktopPane parent) {
        // TODO: setting up view close button
        var closeView = new JButton("Close");
        closeView.setBounds(parent.getWidth()-85, 4, 80, 16);
        closeView.setBackground(Color.DARK_GRAY);
        closeView.setForeground(DEFAULT_FONT_COLOR);
        closeView.setFont(new Font("DialogInput", 0, 14));
        closeView.setBorder(MENU_BORDER);
        //closeView.addActionListener(new Listener());

        return closeView;
    }

    /**
     * @return settings option pane (which pops up)
     */
    static JInternalFrame settings_intlFrame (JPanel parent) {
            var maxLoadLbl = new JLabel("Max. loaded Data:");
            maxLoadLbl.setBounds(20, 20, 180, 30);
            maxLoadLbl.setForeground(DEFAULT_FONT_COLOR);
            maxLoadLbl.setFont(FONT_MENU);
        var settings = new JInternalFrame();
        settings.setBounds(parent.getWidth()/2-250, parent.getHeight()/2-200, 500, 400);
        settings.setLayout(null);
        settings.setBackground(DEFAULT_BORDER_COLOR);
        settings.setClosable(true);
        settings.setResizable(false);
        settings.setFocusable(false);
        settings.add(maxLoadLbl);
        settings.hide();

        return settings;
    }

    /**
     * settings opt. pane max-load text field
     */
    static JTextField settingsOP_maxLoadTxtField () {
        var maxLoadTxtfield = new JTextField();
        maxLoadTxtfield.setBounds(200, 20, 50, 30);
        maxLoadTxtfield.setBorder(BorderFactory.createLineBorder(DEFAULT_FONT_COLOR));
        maxLoadTxtfield.setBackground(DEFAULT_BG_COLOR);
        maxLoadTxtfield.setForeground(DEFAULT_MAP_ICON_COLOR);
        maxLoadTxtfield.setFont(FONT_MENU);
        return maxLoadTxtfield;
    }

    /**
     * @return file chooser for file dialog
     */
    static JFileChooser fileSaver (JFrame parent) {
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
    static JFileChooser fileLoader (JFrame parent) {
        var home = FileSystemView.getFileSystemView().getHomeDirectory();
        var fileChooser = new JFileChooser(home);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("nur .pls-Dateien", "pls"));
        fileChooser.showOpenDialog(parent);
        HashMap<Integer, DataPoint> route = null;
        try {
            route = new FileMaster().loadPlsFile(fileChooser);
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        int flightID = route.get(0).getFlightID();
        Controller.getInstance().createDataView(ViewType.MAP_TRACKING, flightID + "");
        return fileChooser;
    }

    // TODO SETTINGS
    // reload data button
    // confirm button
    // other settings
    // evtl. Theme oder so -> Farbe

    //TODO evtl. methode getAllAsList()
}

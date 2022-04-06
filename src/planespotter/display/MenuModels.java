package planespotter.display;

import javax.swing.*;

import java.awt.*;

import static planespotter.constants.GUIConstants.*;

/**
 * @name MenuModels
 * @author jml04
 * @version 1.0
 *
 * MenuModels class contains different menu component models
 */
public final class MenuModels extends GUI {

    /**
     *
     */
    // evtl. falsche reihenfolge
    public static JRadioButton rb_flights, rb_airlines, rb_airports, rb_planes;
    public static JMenuItem selectAll, selectByID, selectByICAO, selectByName, selectByIATA, selectByPlaneType; //...
    public static JRadioButton rbAirline, rbFlight;

    //default desktop width
    static int WIDTH_RIGHT = 1259-280;
    static int WIDTH_LEFT = 1259-WIDTH_RIGHT; // unnötig (=279)
    // large menu item width
    static int WIDTH_MENUITEM = WIDTH_LEFT-25;

    /**
     * menubar (contains the other menu components)
     */
    public static JMenuBar menuBar () {
        // TODO: setting up menubar
        JMenuBar menubar = new JMenuBar();
        menubar.setBackground(DEFAULT_BG_COLOR);
        menubar.setForeground(DEFAULT_FG_COLOR);
        menubar.setBounds(0, 0, WIDTH_LEFT, 590);
        menubar.setLayout(null);

        return menubar;
    }

    /**
     *
     */
    public static JButton fileButton () {
        // TODO: setting up file button
        JButton file = new JButton("File");
        file.setFont(FONT_MENU);
        file.setBorder(MENU_BORDER);
        file.setBackground(DEFAULT_ACCENT_COLOR);
        file.setForeground(DEFAULT_FONT_COLOR);
        file.setBounds(10, 15, WIDTH_MENUITEM, 25);
        //file.addActionListener(new Listener());

        return file;
    }

    /**
     * list button
     */
    public static JButton listButton () {
        // TODO setting up list button
        JButton list = new JButton("List-View");
        list.setBackground(DEFAULT_ACCENT_COLOR);
        list.setForeground(DEFAULT_FONT_COLOR);
        list.setBorder(MENU_BORDER);
        list.setBounds(10, 55, 120, 25);
        list.setFont(FONT_MENU);
        //list.addActionListener(new Listener());

        return list;
    }

    /**
     * map button
     */
    public static JButton mapButton () {
        // TODO setting up list button
        JButton map = new JButton("Map-View");
        map.setBackground(DEFAULT_ACCENT_COLOR);
        map.setForeground(DEFAULT_FONT_COLOR);
        map.setBorder(MENU_BORDER);
        map.setBounds(145, 55, 120, 25);
        map.setFont(FONT_MENU);
        //map.addActionListener(new Listener());

        return map;
    }

    /**
     * settings button
     */
    public static JButton settingsButton () {
        // TODO: setting up settings menu
        JButton settings = new JButton("Settings");
        settings.setFont(FONT_MENU);
        settings.setBorder(MENU_BORDER);
        settings.setBackground(DEFAULT_ACCENT_COLOR);
        settings.setForeground(DEFAULT_FONT_COLOR);
        settings.setBounds(10, 95, WIDTH_MENUITEM, 25);
        //settings.addActionListener(new Listener());

        return settings;
    }

    /**
     * search-filter button
     */
    public static JButton searchFilterButton () {
        // TODO: setting up search-settings menu
        JButton search_settings = new JButton("Search-Filter");
        search_settings.setFont(FONT_MENU);
        search_settings.setBorder(MENU_BORDER);
        search_settings.setBackground(DEFAULT_ACCENT_COLOR);
        search_settings.setForeground(DEFAULT_FONT_COLOR);
        search_settings.setBounds(10, 515, WIDTH_MENUITEM, 25);
        //search_settings.addActionListener(new Listener());

        return search_settings;
    }

    /**
     *
     */
    public static JProgressBar progressBar () {
        // TODO: seting up progress bar
        JProgressBar progressbar = new JProgressBar(0, 100);
        progressbar.setBorder(MENU_BORDER);
        progressbar.setBackground(DEFAULT_BG_COLOR);
        progressbar.setForeground(new Color(0, 255, 0));
        progressbar.setBounds(10, 555, WIDTH_MENUITEM, 25);
        progressbar.setVisible(false);
        progressbar.setValue(0);

        return progressbar;
    }

    /**
     * search text field
     */
    public static JTextField searchTextField () {
        // TODO: setting up search text field
        JTextField search = new JTextField();
        search.setToolTipText("Search");
        search.setBounds(10, 470, WIDTH_MENUITEM, 25);
        search.setBackground(Color.WHITE);
        search.setFont(FONT_MENU);
        search.setBorder(MENU_BORDER);
        //search.addKeyListener(new Listener());

        return search;
    }

    /**
     * close view button
     */
    public static JButton closeViewButton () {
        // TODO: setting up view close button
        JButton closeView = new JButton("Close");
        closeView.setBounds(955, 605, 30, 20);
        closeView.setBackground(DEFAULT_BG_COLOR);
        closeView.setForeground(DEFAULT_FONT_COLOR);
        closeView.setFont(FONT_MENU);
        closeView.setBorder(MENU_BORDER);
        //closeView.addActionListener(new Listener());

        return closeView;
    }



    public void allesAndere () {

        // TODO: setting up radio button: "search for airline"
        rbAirline = new JRadioButton();
        //rbAirline.addChangeListener(new Listener());

        // TODO: setting up radio button: "search for flight"
        rbFlight = new JRadioButton();
        //rbFlight.addChangeListener(new Listener());
    }

}

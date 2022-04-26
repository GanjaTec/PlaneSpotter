package planespotter.display;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
        closeView.setBackground(DEFAULT_BORDER_COLOR);
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
     * radio buttons
     */
    static JComboBox<String> searchFor_cmbBox (JPanel parent) {
        // TODO: setting up "search for" combo box
        JComboBox<String> searchFor = new JComboBox(MenuModels.searchBoxItems());
        searchFor.setBounds(parent.getWidth()/2, 10, (parent.getWidth()-20)/2, 25);
        searchFor.setBackground(DEFAULT_BORDER_COLOR);
        searchFor.setForeground(DEFAULT_MAP_ICON_COLOR);
        searchFor.setFont(FONT_MENU);

        return searchFor;
    }

    /**
     * @param parent is the panel where the combo-box is in
     * @return menu combobox-text-label
     */
     static JLabel cmbBoxLabel (JPanel parent) {
        var boxLabel = new JLabel("Search for:");
        boxLabel.setBounds(10, 10, (parent.getWidth()-20)/2, 25);
        boxLabel.setBackground(DEFAULT_BG_COLOR);
        boxLabel.setForeground(DEFAULT_MAP_ICON_COLOR);
        boxLabel.setFont(FONT_MENU);

        return boxLabel;
    }
        /**
         * @return search combo-box items (array of Strings)
         */
        private static String[] searchBoxItems () {
            String[] items = {  "Plane",
                                "Flight",
                                "Airline",
                                "Airport",
                                "Area"};
            return items;
        }

    /**
     * @return panel for exact search settings
     */
    static JSeparator searchSeperator (JPanel parent) {
        // TODO: setting up exact search panel
        var seperator = new JSeparator(JSeparator.HORIZONTAL);
        seperator.setBounds(10, 43, parent.getWidth()-20, 2);
        seperator.setBackground(DEFAULT_BORDER_COLOR);

        return seperator;
    }

    /**
     * @param parent is the parent panel where the message label is shown in
     * @return
     */
    static JTextArea searchMessage(JPanel parent) {
        var message = "Es muss mindestens eins der Felder ausgefüllt sein!";
        var headMessage = new JTextArea(message);
        headMessage.setBounds(10, parent.getHeight()-80, parent.getWidth()-20, 35);
        headMessage.setBackground(DEFAULT_BG_COLOR);
        headMessage.setForeground(DEFAULT_FONT_COLOR);
        headMessage.setBorder(null);
        headMessage.setEditable(false);
        headMessage.setLineWrap(true);
        headMessage.setWrapStyleWord(true);
        var font = new Font(FONT_MENU.getFontName(), Font.PLAIN, 12);
        headMessage.setFont(font);

        return headMessage;
    }

    /**
     * @param parent is the parent panel component
     * @return list of JLabels (the search field names)
     */
    static List<JComponent> flightSearch(JPanel parent) {
        List<JComponent> components = new ArrayList<>();
            components.add(new JLabel("ID:"));
            components.add(new JTextField());
            components.add(new JLabel("Flight-Nr.:"));
            components.add(new JTextField());
            components.add(new JButton("Load List"));
            components.add(new JButton("Load Map"));
        int width = (parent.getWidth()-20)/2;
        int y = 55;
        for (JComponent c : components) {
            if (c instanceof JLabel) {
                c.setBounds(10, y, width, 25);
                c.setBackground(DEFAULT_BG_COLOR);
                c.setForeground(DEFAULT_MAP_ICON_COLOR);
            } else if (c instanceof JTextField) {
                c.setBounds(parent.getWidth()/2, y, width, 25);
                c.setBackground(DEFAULT_FONT_COLOR);
                c.setForeground(DEFAULT_FG_COLOR);
                c.setBorder(LINE_BORDER);
                y += 35;
            } else if (c instanceof JButton) {
                var buttonText = ((JButton) c).getText();
                if (buttonText.equals("Load List")) {
                    c.setBounds(10, parent.getHeight()-35, width-5, 25);
                } else if (buttonText.equals("Load Map")) {
                    c.setBounds((parent.getWidth()/2)+5, parent.getHeight()-35, width-5, 25);
                }
                c.setBackground(DEFAULT_ACCENT_COLOR);
                c.setForeground(DEFAULT_FONT_COLOR);
                c.setBorder(MENU_BORDER);
            }
            c.setFont(FONT_MENU);
            c.setVisible(false);
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

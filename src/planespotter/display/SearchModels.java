package planespotter.display;

import libs.UWPButton;

import javax.imageio.spi.ServiceRegistry;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static planespotter.constants.GUIConstants.*;

/**
 * @name SearchModels
 * @author jml04
 * @version 1.0
 *
 * class SearchModels contains different (gui-menu) search models
 */
public final class SearchModels {

    /**
     * radio buttons
     */
    JComboBox<String> searchFor_cmbBox (JPanel parent) {
        // TODO: setting up "search for" combo box
        JComboBox<String> searchFor = new JComboBox(new SearchModels().searchBoxItems());
        searchFor.setBounds(parent.getWidth()/2, 10, (parent.getWidth()-20)/2, 25);
        searchFor.setBackground(DEFAULT_SEARCH_ACCENT_COLOR);
        searchFor.setForeground(DEFAULT_MAP_ICON_COLOR);
        searchFor.setFont(FONT_MENU);

        return searchFor;
    }

    /**
     * @param parent is the panel where the combo-box is in
     * @return menu combobox-text-label
     */
    JLabel cmbBoxLabel (JPanel parent) {
        var boxLabel = new JLabel("Search for:");
        boxLabel.setBounds(10, 10, (parent.getWidth()-20)/2, 25);
        boxLabel.setForeground(DEFAULT_MAP_ICON_COLOR);
        boxLabel.setFont(FONT_MENU);

        return boxLabel;
    }

    /**
     * @return search combo-box items (array of Strings)
     */
    private String[] searchBoxItems () {
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
    JSeparator searchSeperator (JPanel parent) {
        // TODO: setting up exact search panel
        var seperator = new JSeparator(JSeparator.HORIZONTAL);
        seperator.setBounds(10, 43, parent.getWidth()-20, 2);
        seperator.setBackground(DEFAULT_SEARCH_ACCENT_COLOR);

        return seperator;
    }

    /**
     * @param parent is the parent panel where the message label is shown in
     * @return
     */
    JTextArea searchMessage (JPanel parent) {
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
    List<JComponent> flightSearch (JPanel parent, GUI gui) {
        var components = new ArrayList<JComponent>();
        components.add(new JLabel("ID:"));
        var id = new JTextField();
        gui.search_flightID = id;
        components.add(id);
        components.add(new JLabel("Callsign.:"));
        var callsign = new JTextField();
        gui.search_callsign = callsign;
        components.add(callsign);
        var loadList = new UWPButton();
        loadList.setText("Load List");
        components.add(loadList);
        var loadMap = new UWPButton();
        loadMap.setText("Load Map");
        components.add(loadMap);
        int width = (parent.getWidth()-20)/2;
        int y = 55;
        for (var c : components) {
            if (c instanceof JLabel) {
                c.setBounds(10, y, width, 25);
                c.setBackground(DEFAULT_BG_COLOR);
                c.setForeground(DEFAULT_MAP_ICON_COLOR);
            } else if (c instanceof JTextField) {
                c.setBounds(parent.getWidth()/2, y, width, 25);
                c.setBackground(DEFAULT_FONT_COLOR);
                c.setForeground(DEFAULT_FG_COLOR);
                c.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR));
                y += 35;
            } else if (c instanceof JButton) {
                var buttonText = ((JButton) c).getText();
                if (buttonText.equals("Load List")) {
                    c.setBounds(10, parent.getHeight()-35, width-5, 25);
                    c.setName("loadList");
                } else if (buttonText.equals("Load Map")) {
                    c.setBounds((parent.getWidth()/2)+5, parent.getHeight()-35, width-5, 25);
                    c.setName("loadMap");
                }
                c.setBackground(DEFAULT_SEARCH_ACCENT_COLOR);
                c.setForeground(DEFAULT_FONT_COLOR);
                c.setBorder(MENU_BORDER);
            }
            c.setFont(FONT_MENU);
            c.setVisible(false);
        }
        return components;
    }

    /**
     * @param parent is the parent panel component
     * @return list of JLabels (the search field names)
     */
    List<JComponent> planeSearch (JPanel parent, GUI gui) {
        var components = new ArrayList<JComponent>();
        components.add(new JLabel("ID:"));
        var id = new JTextField();
        gui.search_planeID = id;
        components.add(id);
        components.add(new JLabel("Planetype:"));
        var planetype = new JTextField();
        gui.search_planetype = planetype;
        components.add(planetype);
        components.add(new JLabel("ICAO:"));
        var icao = new JTextField();
        gui.search_icao = icao;
        components.add(icao);
        components.add(new JLabel("Tail-Nr.:"));
        var tailNr = new JTextField();
        gui.search_tailNr = tailNr;
        components.add(tailNr);
        var loadList = new UWPButton();
        loadList.setText("Load List");
        components.add(loadList);
        var loadMap = new UWPButton();
        loadMap.setText("Load Map");
        components.add(loadMap);
        int width = (parent.getWidth()-20)/2;
        int y = 55;
        for (var c : components) {
            if (c instanceof JLabel) {
                c.setBounds(10, y, width, 25);
                c.setBackground(DEFAULT_BG_COLOR);
                c.setForeground(DEFAULT_MAP_ICON_COLOR);
            } else if (c instanceof JTextField) {
                c.setBounds(parent.getWidth()/2, y, width, 25);
                c.setBackground(DEFAULT_FONT_COLOR);
                c.setForeground(DEFAULT_FG_COLOR);
                c.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR));
                y += 35;
            } else if (c instanceof JButton) {
                var buttonText = ((JButton) c).getText();
                if (buttonText.equals("Load List")) {
                    c.setBounds(10, parent.getHeight()-35, width-5, 25);
                    c.setName("loadList");
                } else if (buttonText.equals("Load Map")) {
                    c.setBounds((parent.getWidth()/2)+5, parent.getHeight()-35, width-5, 25);
                    c.setName("loadMap");
                }
                c.setBackground(DEFAULT_SEARCH_ACCENT_COLOR);
                c.setForeground(DEFAULT_FONT_COLOR);
                c.setBorder(MENU_BORDER);
            }
            c.setFont(FONT_MENU);
            c.setVisible(false);
        }
        return components;
    }

    public ArrayList<JComponent> airportSearch (JPanel parent, GUI gui) {
        var components = new ArrayList<JComponent>();
        components.add(new JLabel("ID:"));
        var id = new JTextField();
        gui.search_airpID = id;
        components.add(id);
        components.add(new JLabel("Tag:"));
        var tag = new JTextField();
        gui.search_airpTag = tag;
        components.add(tag);
        components.add(new JLabel("Name:"));
        var name = new JTextField();
        gui.search_airpName = name;
        components.add(name);
        var loadList = new UWPButton();
        loadList.setText("Load List");
        components.add(loadList);
        var loadMap = new UWPButton();
        loadMap.setText("Load Map");
        components.add(loadMap);
        int width = (parent.getWidth()-20)/2;
        int y = 55;
        for (var c : components) {
            if (c instanceof JLabel) {
                c.setBounds(10, y, width, 25);
                c.setBackground(DEFAULT_BG_COLOR);
                c.setForeground(DEFAULT_MAP_ICON_COLOR);
            } else if (c instanceof JTextField) {
                c.setBounds(parent.getWidth()/2, y, width, 25);
                c.setBackground(DEFAULT_FONT_COLOR);
                c.setForeground(DEFAULT_FG_COLOR);
                c.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR));
                y += 35;
            } else if (c instanceof JButton) {
                var buttonText = ((JButton) c).getText();
                if (buttonText.equals("Load List")) {
                    c.setBounds(10, parent.getHeight()-35, width-5, 25);
                    c.setName("loadList");
                } else if (buttonText.equals("Load Map")) {
                    c.setBounds((parent.getWidth()/2)+5, parent.getHeight()-35, width-5, 25);
                    c.setName("loadMap");
                }
                c.setBackground(DEFAULT_SEARCH_ACCENT_COLOR);
                c.setForeground(DEFAULT_FONT_COLOR);
                c.setBorder(MENU_BORDER);
            }
            c.setFont(FONT_MENU);
            c.setVisible(false);
        }
        return components;
    }

    /**
     * displays on the screen if there is e.g. an error
     *
     * @param msg is the error message
     * @return option pane with a error message
     */
    public JOptionPane errorMsgPane (String msg) {
        return new JOptionPane(msg, JOptionPane.ERROR_MESSAGE);
    }

}

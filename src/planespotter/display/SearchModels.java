package planespotter.display;

import libs.UWPButton;
import planespotter.controller.ActionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.DefaultColor.*;

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
    JComboBox<String> searchFor_cmbBox(JPanel parent, ItemListener listener) {
        // setting up "search for" combo box
        var searchFor = new JComboBox<>(this.searchBoxItems());
        searchFor.setBounds(parent.getWidth()/2, 10, (parent.getWidth()-20)/2, 25);
        searchFor.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        searchFor.setForeground(DEFAULT_MAP_ICON_COLOR.get());
        searchFor.setFont(FONT_MENU);
        searchFor.addItemListener(listener);

        return searchFor;
    }

    /**
     * @param parent is the panel where the combo-box is in
     * @return menu combobox-text-label
     */
    JLabel cmbBoxLabel(JPanel parent) {
        var boxLabel = new JLabel("Search for:");
        boxLabel.setBounds(10, 10, (parent.getWidth()-20)/2, 25);
        boxLabel.setForeground(DEFAULT_MAP_ICON_COLOR.get());
        boxLabel.setFont(FONT_MENU);
        boxLabel.setOpaque(false);

        return boxLabel;
    }

    /**
     * @return search combo-box items (array of Strings)
     */
    private String[] searchBoxItems() {
        return new String[] {
                "Flight",
                "Plane",
                "Airline",
                "Airport",
                "Area"
        };
    }

    /**
     * @return panel for exact search settings
     */
    JSeparator searchSeperator(JPanel parent) {
        // TODO: setting up exact search panel
        var seperator = new JSeparator(JSeparator.HORIZONTAL);
        seperator.setBounds(10, 43, parent.getWidth()-20, 2);
        seperator.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());

        return seperator;
    }

    /**
     * @param parent is the parent panel where the message label is shown in
     * @return the search message text area
     */
    JTextArea searchMessage(JPanel parent) {
        var message = "Es muss mindestens eins der Felder ausgefüllt sein!";
        var headMessage = new JTextArea(message);
        headMessage.setBounds(10, parent.getHeight()-80, parent.getWidth()-20, 35);
        headMessage.setBackground(DEFAULT_BG_COLOR.get());
        headMessage.setForeground(DEFAULT_FONT_COLOR.get());
        headMessage.setBorder(null);
        headMessage.setEditable(false);
        headMessage.setLineWrap(true);
        headMessage.setWrapStyleWord(true);
        headMessage.setOpaque(false);
        var font = new Font(FONT_MENU.getFontName(), Font.PLAIN, 12);
        headMessage.setFont(font);

        return headMessage;
    }

    /**
     * @param parent is the parent panel component
     * @return list of JLabels (the search field names)
     */
    List<JComponent> flightSearch(JPanel parent, GUI gui, ActionHandler listener) {
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
                c.setBackground(DEFAULT_BG_COLOR.get());
                c.setForeground(DEFAULT_MAP_ICON_COLOR.get());
                c.setOpaque(false);
            } else if (c instanceof JTextField) {
                c.setBounds(parent.getWidth()/2, y, width, 25);
                c.setBackground(DEFAULT_FONT_COLOR.get());
                c.setForeground(DEFAULT_FG_COLOR.get());
                c.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
                c.addKeyListener(listener);
                y += 35;
            } else if (c instanceof JButton bt) {
                var buttonText = bt.getText();
                if (buttonText.equals("Load List")) {
                    bt.setBounds(10, parent.getHeight()-35, width-5, 25);
                    bt.setName("loadList");
                } else if (buttonText.equals("Load Map")) {
                    bt.setBounds((parent.getWidth()/2)+5, parent.getHeight()-35, width-5, 25);
                    bt.setName("loadMap");
                }
                bt.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
                bt.setForeground(DEFAULT_FONT_COLOR.get());
                bt.setBorder(MENU_BORDER);
                bt.addActionListener(listener);
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
    List<JComponent> planeSearch(JPanel parent, GUI gui, ActionHandler listener) {
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
                c.setBackground(DEFAULT_BG_COLOR.get());
                c.setForeground(DEFAULT_MAP_ICON_COLOR.get());
                c.setOpaque(false);
            } else if (c instanceof JTextField) {
                c.setBounds(parent.getWidth()/2, y, width, 25);
                c.setBackground(DEFAULT_FONT_COLOR.get());
                c.setForeground(DEFAULT_FG_COLOR.get());
                c.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
                c.addKeyListener(listener);
                y += 35;
            } else if (c instanceof JButton bt) {
                var buttonText = bt.getText();
                if (buttonText.equals("Load List")) {
                    bt.setBounds(10, parent.getHeight()-35, width-5, 25);
                    bt.setName("loadList");
                } else if (buttonText.equals("Load Map")) {
                    bt.setBounds((parent.getWidth()/2)+5, parent.getHeight()-35, width-5, 25);
                    bt.setName("loadMap");
                }
                bt.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
                bt.setForeground(DEFAULT_FONT_COLOR.get());
                bt.setBorder(MENU_BORDER);
                bt.addActionListener(listener);
            }
            c.setFont(FONT_MENU);
            c.setVisible(false);
        }
        return components;
    }

    public ArrayList<JComponent> airportSearch(JPanel parent, GUI gui, ActionHandler listener) {
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
                c.setBackground(DEFAULT_BG_COLOR.get());
                c.setForeground(DEFAULT_MAP_ICON_COLOR.get());
                c.setOpaque(false);
            } else if (c instanceof JTextField) {
                c.setBounds(parent.getWidth()/2, y, width, 25);
                c.setBackground(DEFAULT_FONT_COLOR.get());
                c.setForeground(DEFAULT_FG_COLOR.get());
                c.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
                c.addKeyListener(listener);
                y += 35;
            } else if (c instanceof JButton bt) {
                var buttonText = bt.getText();
                if (buttonText.equals("Load List")) {
                    bt.setBounds(10, parent.getHeight()-35, width-5, 25);
                    bt.setName("loadList");
                } else if (buttonText.equals("Load Map")) {
                    bt.setBounds((parent.getWidth()/2)+5, parent.getHeight()-35, width-5, 25);
                    bt.setName("loadMap");
                }
                bt.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
                bt.setForeground(DEFAULT_FONT_COLOR.get());
                bt.setBorder(MENU_BORDER);
                bt.addActionListener(listener);
            }
            c.setFont(FONT_MENU);
            c.setVisible(false);
        }
        return components;
    }

}

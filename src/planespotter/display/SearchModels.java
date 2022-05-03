package planespotter.display;

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
    static JComboBox<String> searchFor_cmbBox (JPanel parent) {
        // TODO: setting up "search for" combo box
        JComboBox<String> searchFor = new JComboBox(SearchModels.searchBoxItems());
        searchFor.setBounds(parent.getWidth()/2, 10, (parent.getWidth()-20)/2, 25);
        searchFor.setBackground(DEFAULT_ACCENT_COLOR);
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
        //boxLabel.setBackground(DEFAULT_BG_COLOR);
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
        seperator.setBackground(DEFAULT_ACCENT_COLOR);

        return seperator;
    }

    /**
     * @param parent is the parent panel where the message label is shown in
     * @return
     */
    static JTextArea searchMessage (JPanel parent) {
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
    static List<JComponent> flightSearch (JPanel parent, GUI gui) {
        List<JComponent> components = new ArrayList<>();
        components.add(new JLabel("ID:"));
        JTextField id = new JTextField();
        gui.search_flightID = id;
        components.add(id);
        components.add(new JLabel("Flight-Nr.:"));
        JTextField callsign = new JTextField();
        gui.search_callsign = callsign;
        components.add(callsign);
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
                    c.setName("loadList");
                } else if (buttonText.equals("Load Map")) {
                    c.setBounds((parent.getWidth()/2)+5, parent.getHeight()-35, width-5, 25);
                    c.setName("loadMap");
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

    /**
     * displays on the screen if there is e.g. an error
     *
     * @param msg is the error message
     * @return option pane with a error message
     */
    public static JOptionPane errorMsgPane (String msg) {
        return new JOptionPane(msg, TrayIcon.MessageType.WARNING.ordinal());
    }

}

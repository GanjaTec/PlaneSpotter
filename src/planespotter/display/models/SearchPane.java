package planespotter.display.models;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.SearchType;
import planespotter.controller.ActionHandler;
import planespotter.display.UserInterface;
import planespotter.throwables.NoSuchComponentException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static planespotter.constants.DefaultColor.*;

public class SearchPane extends JPanel {

    @NotNull
    private final Map<SearchType, List<JComponent>> allSearchModels;

    @NotNull
    final Map<String, JTextField> searchFields;

    @NotNull
    private final JComboBox<String> searchCmbBox;

    private SearchType currentSearchType;

    public SearchPane(@NotNull Component parent, @NotNull ActionHandler actionHandler) {
        super();

        super.setBounds(10, 175, 250, parent.getHeight()-265);
        super.setBackground(DEFAULT_ACCENT_COLOR.get());
        super.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
        super.setLayout(null);
        super.setVisible(false);

        super.add(cmbBoxLabel(this));
        this.searchCmbBox = searchFor_cmbBox(this, actionHandler);
        super.add(this.searchCmbBox);
        super.add(searchSeperator(this));
        super.add(searchMessage(this));

        this.searchFields = new HashMap<>();

        this.allSearchModels = allSearches(this, actionHandler);

        for (List<JComponent> comps : this.allSearchModels.values()) {
            if (comps != null) {
                for (JComponent c : comps) {
                    if (c instanceof JLabel) {
                        c.setOpaque(false);
                    }
                    super.add(c);
                }
            }
        }
        this.currentSearchType = SearchType.FLIGHT;

    }

    public void showSearch(@NotNull SearchType type) {
        if (type == SearchType.AREA) {
            return; // area search is not implemented
        }
        List<JComponent> search = allSearchModels.get(type);
        for (List<JComponent> model : allSearchModels.values()) {
            boolean isSearchModel = (model == search);
            for (JComponent comp : model) {
                comp.setVisible(isSearchModel);
            }
        }
        currentSearchType = type;
    }

    @NotNull
    public JTextField getSearchField(@NotNull String key) {
        if (!searchFields.containsKey(key)) {
            throw new NoSuchComponentException("No search field found for key " + key + "!");
        }
        return searchFields.get(key);

    }

    @NotNull
    public String[] searchInput() {
        return switch (currentSearchType) {
            case FLIGHT -> new String[] {
                    getSearchField("flight.id").getText(),
                    getSearchField("flight.callsign").getText()
            };
            case PLANE -> new String[] {
                    getSearchField("plane.id").getText(),
                    getSearchField("plane.type").getText(),
                    getSearchField("plane.icao").getText(),
                    getSearchField("plane.tailnr").getText()
            };
            case AIRLINE -> new String[] {
                    getSearchField("airline.id").getText(),
                    getSearchField("airline.tag").getText(),
                    getSearchField("airline.name").getText(),
                    getSearchField("airline.country").getText()
            };
            case AIRPORT -> new String[] {
                    getSearchField("airport.id").getText(),
                    getSearchField("airport.tag").getText(),
                    getSearchField("airport.name").getText()
            };
            default -> throw new NoSuchComponentException();
        };
    }

    public void clearSearch() {
        final String blank = "";
        allSearchModels.get(currentSearchType)
                .forEach(m -> {
                    if (m instanceof JTextField jtf) {
                        jtf.setText(blank);
                    }
                });

    }

    @NotNull
    public SearchType getCurrentSearchType() {
        return currentSearchType;
    }

    @NotNull
    public JComboBox<String> getSearchCmbBox() {
        return searchCmbBox;
    }

    /**
     * radio buttons
     */
    private JComboBox<String> searchFor_cmbBox(JPanel parent, ItemListener listener) {
        // setting up "search for" combo box
        var searchFor = new JComboBox<>(this.searchBoxItems());
        searchFor.setBounds(parent.getWidth()/2, 10, (parent.getWidth()-20)/2, 25);
        searchFor.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        searchFor.setForeground(DEFAULT_MAP_ICON_COLOR.get());
        searchFor.setFont(UserInterface.DEFAULT_FONT);
        searchFor.addItemListener(listener);

        return searchFor;
    }

    /**
     * @param parent is the panel where the combo-box is in
     * @return menu combobox-text-label
     */
    private JLabel cmbBoxLabel(JPanel parent) {
        var boxLabel = new JLabel("Search for:");
        boxLabel.setBounds(10, 10, (parent.getWidth()-20)/2, 25);
        boxLabel.setForeground(DEFAULT_MAP_ICON_COLOR.get());
        boxLabel.setFont(UserInterface.DEFAULT_FONT);
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
    private JSeparator searchSeperator(JPanel parent) {
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
    private JTextArea searchMessage(JPanel parent) {
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
        var font = new Font(UserInterface.DEFAULT_FONT.getFontName(), Font.PLAIN, 12);
        headMessage.setFont(font);

        return headMessage;
    }

    private Map<SearchType, List<JComponent>> allSearches(@NotNull SearchPane searchPane, @NotNull ActionHandler actionHandler) {
        Map<SearchType, List<JComponent>> searchModels = new HashMap<>(4);
        searchModels.put(SearchType.FLIGHT, flightSearch(searchPane, actionHandler));
        searchModels.put(SearchType.PLANE, planeSearch(searchPane, actionHandler));
        searchModels.put(SearchType.AIRLINE, airlineSearch(searchPane, actionHandler));
        searchModels.put(SearchType.AIRPORT, airportSearch(searchPane, actionHandler));
        return searchModels;
    }



    /**
     * @param pane is the parent panel component
     * @return list of JLabels (the search field names)
     */
    private List<JComponent> flightSearch(SearchPane pane, ActionHandler listener) {
        ArrayList<JComponent> components = new ArrayList<>();
        components.add(new JLabel("ID:"));
        JTextField id = new JTextField();
        pane.searchFields.put("flight.id", id);
        components.add(id);
        components.add(new JLabel("Callsign.:"));
        JTextField callsign = new JTextField();
        pane.searchFields.put("flight.callsign", callsign);
        components.add(callsign);
        // TODO: 09.08.2022 FlightNr

        UWPButton searchBt = new UWPButton();
        searchBt.setText("Search");
        components.add(searchBt);
        int width = (pane.getWidth()-20)/2;
        int y = 55;
        for (JComponent c : components) {
            if (c instanceof JLabel) {
                c.setBounds(10, y, width, 25);
                c.setBackground(DEFAULT_BG_COLOR.get());
                c.setForeground(DEFAULT_MAP_ICON_COLOR.get());
                c.setOpaque(false);
            } else if (c instanceof JTextField) {
                c.setBounds(pane.getWidth()/2, y, width, 25);
                c.setBackground(DEFAULT_FONT_COLOR.get());
                c.setForeground(DEFAULT_FG_COLOR.get());
                c.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
                c.addKeyListener(listener);
                y += 35;
            } else if (c instanceof JButton bt) {
                var buttonText = bt.getText();
                if (buttonText.equals("Search")) {
                    bt.setBounds(10, pane.getHeight()-35, pane.getWidth()-20, 25);
                }
                bt.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
                bt.setForeground(DEFAULT_FONT_COLOR.get());
                bt.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
                bt.addActionListener(listener);
            }
            c.setFont(UserInterface.DEFAULT_FONT);
            c.setVisible(false);
        }
        return components;
    }

    /**
     * @param pane is the parent panel component
     * @return list of JLabels (the search field names)
     */
    private List<JComponent> planeSearch(SearchPane pane, ActionHandler listener) {
        ArrayList<JComponent> components = new ArrayList<>();
        components.add(new JLabel("ID:"));
        JTextField id = new JTextField();
        pane.searchFields.put("plane.id", id);
        components.add(id);
        components.add(new JLabel("Planetype:"));
        JTextField planetype = new JTextField();
        pane.searchFields.put("plane.type", planetype);
        components.add(planetype);
        components.add(new JLabel("ICAO:"));
        JTextField icao = new JTextField();
        pane.searchFields.put("plane.icao", icao);
        components.add(icao);
        components.add(new JLabel("Tail-Nr.:"));
        JTextField tailNr = new JTextField();
        pane.searchFields.put("plane.tailnr", tailNr);
        components.add(tailNr);
        UWPButton searchBt = new UWPButton();
        searchBt.setText("Search");
        components.add(searchBt);
        int width = (pane.getWidth()-20)/2;
        int y = 55;
        for (JComponent c : components) {
            if (c instanceof JLabel) {
                c.setBounds(10, y, width, 25);
                c.setBackground(DEFAULT_BG_COLOR.get());
                c.setForeground(DEFAULT_MAP_ICON_COLOR.get());
                c.setOpaque(false);
            } else if (c instanceof JTextField) {
                c.setBounds(pane.getWidth()/2, y, width, 25);
                c.setBackground(DEFAULT_FONT_COLOR.get());
                c.setForeground(DEFAULT_FG_COLOR.get());
                c.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
                c.addKeyListener(listener);
                y += 35;
            } else if (c instanceof JButton bt) {
                String buttonText = bt.getText();
                if (buttonText.equals("Search")) {
                    bt.setBounds(10, pane.getHeight()-35, pane.getWidth()-20, 25);
                }
                bt.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
                bt.setForeground(DEFAULT_FONT_COLOR.get());
                bt.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
                bt.addActionListener(listener);
            }
            c.setFont(UserInterface.DEFAULT_FONT);
            c.setVisible(false);
        }
        return components;
    }

    private ArrayList<JComponent> airportSearch(SearchPane pane, ActionHandler listener) {
        ArrayList<JComponent> components = new ArrayList<>();
        components.add(new JLabel("ID:"));
        JTextField id = new JTextField();
        pane.searchFields.put("airport.id", id);
        components.add(id);
        components.add(new JLabel("Tag:"));
        JTextField tag = new JTextField();
        pane.searchFields.put("airport.tag", tag);
        components.add(tag);
        components.add(new JLabel("Name:"));
        JTextField name = new JTextField();
        pane.searchFields.put("airport.name", name);
        components.add(name);
        UWPButton searchBt = new UWPButton();
        searchBt.setText("Search");
        components.add(searchBt);

        int width = (pane.getWidth()-20)/2;
        int y = 55;
        for (JComponent c : components) {
            if (c instanceof JLabel) {
                c.setBounds(10, y, width, 25);
                c.setBackground(DEFAULT_BG_COLOR.get());
                c.setForeground(DEFAULT_MAP_ICON_COLOR.get());
                c.setOpaque(false);
            } else if (c instanceof JTextField) {
                c.setBounds(pane.getWidth()/2, y, width, 25);
                c.setBackground(DEFAULT_FONT_COLOR.get());
                c.setForeground(DEFAULT_FG_COLOR.get());
                c.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
                c.addKeyListener(listener);
                y += 35;
            } else if (c instanceof JButton bt) {
                String buttonText = bt.getText();
                if (buttonText.equals("Search")) {
                    bt.setBounds(10, pane.getHeight()-35, pane.getWidth()-20, 25);
                }
                bt.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
                bt.setForeground(DEFAULT_FONT_COLOR.get());
                bt.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
                bt.addActionListener(listener);
            }
            c.setFont(UserInterface.DEFAULT_FONT);
            c.setVisible(false);
        }
        return components;
    }

    private List<JComponent> airlineSearch(@NotNull SearchPane pane, @NotNull ActionHandler listener) {
        ArrayList<JComponent> components = new ArrayList<JComponent>();
        components.add(new JLabel("ID:"));
        JTextField id = new JTextField();
        pane.searchFields.put("airline.id", id);
        components.add(id);
        components.add(new JLabel("Tag:"));
        JTextField tag = new JTextField();
        pane.searchFields.put("airline.tag", tag);
        components.add(tag);
        components.add(new JLabel("Name:"));
        JTextField name = new JTextField();
        pane.searchFields.put("airline.name", name);
        components.add(name);
        components.add(new JLabel("Country:"));
        JTextField country = new JTextField();
        pane.searchFields.put("airline.country", country);
        components.add(country);
        UWPButton searchBt = new UWPButton();
        searchBt.setText("Search");
        components.add(searchBt);
        int width = (pane.getWidth()-20)/2;
        int y = 55;
        for (JComponent c : components) {
            if (c instanceof JLabel) {
                c.setBounds(10, y, width, 25);
                c.setBackground(DEFAULT_BG_COLOR.get());
                c.setForeground(DEFAULT_MAP_ICON_COLOR.get());
                c.setOpaque(false);
            } else if (c instanceof JTextField) {
                c.setBounds(pane.getWidth()/2, y, width, 25);
                c.setBackground(DEFAULT_FONT_COLOR.get());
                c.setForeground(DEFAULT_FG_COLOR.get());
                c.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
                c.addKeyListener(listener);
                y += 35;
            } else if (c instanceof JButton bt) {
                String buttonText = bt.getText();
                if (buttonText.equals("Search")) {
                    bt.setBounds(10, pane.getHeight()-35, pane.getWidth()-20, 25);
                }
                bt.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
                bt.setForeground(DEFAULT_FONT_COLOR.get());
                bt.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
                bt.addActionListener(listener);
            }
            c.setFont(UserInterface.DEFAULT_FONT);
            c.setVisible(false);
        }
        return components;
    }
}

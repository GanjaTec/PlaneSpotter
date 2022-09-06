package planespotter.display.models;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.SearchType;
import planespotter.controller.ActionHandler;
import planespotter.throwables.NoSuchComponentException;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static planespotter.constants.DefaultColor.DEFAULT_ACCENT_COLOR;
import static planespotter.constants.DefaultColor.DEFAULT_SEARCH_ACCENT_COLOR;

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

        SearchModels searchModels = new SearchModels();
        super.add(searchModels.cmbBoxLabel(this));
        this.searchCmbBox = searchModels.searchFor_cmbBox(this, actionHandler);
        super.add(this.searchCmbBox);
        super.add(searchModels.searchSeperator(this));
        super.add(searchModels.searchMessage(this));

        this.searchFields = new HashMap<>();

        this.allSearchModels = searchModels.allSearches(this, actionHandler);

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
}

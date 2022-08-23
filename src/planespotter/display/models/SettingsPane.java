package planespotter.display.models;

import libs.UWPButton;
import org.jetbrains.annotations.NotNull;
import planespotter.constants.UserSettings;
import planespotter.controller.ActionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Objects;

import static planespotter.constants.DefaultColor.*;
import static planespotter.constants.GUIConstants.FONT_MENU;

public class SettingsPane extends JDialog {

    private final JTextField maxLoadTxtField;
    private final JComboBox<String> mapTypeCmbBox;
    private final JSlider livePeriodSlider;

    public SettingsPane(@NotNull JFrame parent, @NotNull ActionHandler actionHandler) {
        var maxLoadLbl = new JLabel("Max. loaded Data:");
        maxLoadLbl.setBounds(20, 10, 300, 25);
        maxLoadLbl.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        maxLoadLbl.setFont(FONT_MENU);
        maxLoadLbl.setOpaque(false);
        var mapType = new JLabel("Map Type:");
        mapType.setBounds(20, 50, 300, 25);
        mapType.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        mapType.setFont(FONT_MENU);
        mapType.setOpaque(false);
        var livePeriod = new JLabel("Live Data Period (sec):");
        livePeriod.setBounds(20, 90, 300, 25);
        livePeriod.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        livePeriod.setFont(FONT_MENU);
        livePeriod.setOpaque(false);
        var liveMapFilters = new JLabel("Live Map Filters: ");
        liveMapFilters.setBounds(20, 130, 300, 25);
        liveMapFilters.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        liveMapFilters.setFont(FONT_MENU);
        liveMapFilters.setOpaque(false);
        
        super.setBounds(parent.getWidth()/2-250, parent.getHeight()/2-200, 540, 400);
        super.setLayout(null);
        super.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        super.setType(Window.Type.POPUP);
        super.setResizable(false);
        super.setFocusable(false);
        // adding labels
        JSeparator[] separators = this.separators(4, 40, 40);
        this.maxLoadTxtField = this.settings_maxLoadTxtField(actionHandler);
        this.mapTypeCmbBox = this.settings_mapTypeCmbBox(actionHandler);
        JButton[] settingsButtons = this.settingsButtons(this, actionHandler);
        UWPButton filterButton = this.settingsFilterButton(actionHandler);
        this.livePeriodSlider = this.settingsLivePeriodSlider();

        Arrays.stream(separators).forEach(super::add);
        Arrays.stream(settingsButtons).forEach(super::add);
        super.add(filterButton);
        super.add(this.livePeriodSlider);
        super.add(this.maxLoadTxtField);
        super.add(this.mapTypeCmbBox);
        super.add(maxLoadLbl);
        super.add(mapType);
        super.add(livePeriod);
        super.add(liveMapFilters);

        this.maxLoadTxtField.setText(String.valueOf(UserSettings.getMaxLoadedData()));

        super.setVisible(false);
    }

    /**
     * settings opt. pane max-load text field
     */
    private JTextField settings_maxLoadTxtField(@NotNull KeyListener listener) {
        var maxLoadTxtfield = new JTextField();
        maxLoadTxtfield.setBounds(350, 10, 150, 25);
        maxLoadTxtfield.setBorder(BorderFactory.createLineBorder(DEFAULT_FONT_COLOR.get()));
        maxLoadTxtfield.setBackground(DEFAULT_FONT_COLOR.get());
        maxLoadTxtfield.setForeground(DEFAULT_ACCENT_COLOR.get());
        maxLoadTxtfield.setFont(FONT_MENU);
        maxLoadTxtfield.addKeyListener(listener);

        return maxLoadTxtfield;
    }

    private JButton[] settingsButtons(@NotNull JDialog parent, @NotNull ActionListener listener) {
        int mid = parent.getWidth() / 2;
        int height = parent.getHeight() - 80;
        var cancel = new UWPButton("Cancel");
        cancel.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        cancel.setForeground(DEFAULT_FONT_COLOR.get());
        cancel.setFont(FONT_MENU);
        cancel.setBounds(mid - 140, height, 120, 25);
        cancel.addActionListener(listener);
        var confirm = new UWPButton("Confirm");
        confirm.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        confirm.setForeground(DEFAULT_FONT_COLOR.get());
        confirm.setFont(FONT_MENU);
        confirm.setBounds(mid + 20, height, 120, 25);
        confirm.addActionListener(listener);
        return new UWPButton[] {
                cancel, confirm
        };
    }

    // TODO: 05.07.2022 Settings class

    private JComboBox<String> settings_mapTypeCmbBox(@NotNull ItemListener listener) {
        var mapTypeCmbBox = new JComboBox<>(new String[] {
                "Default Map",
                "Bing Map",
                "Transport Map"
        });
        mapTypeCmbBox.setBounds(350, 50, 150,  25);
        mapTypeCmbBox.setBorder(BorderFactory.createLineBorder(DEFAULT_FONT_COLOR.get()));
        mapTypeCmbBox.setBackground(DEFAULT_FONT_COLOR.get());
        mapTypeCmbBox.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        mapTypeCmbBox.setFont(FONT_MENU);
        mapTypeCmbBox.addItemListener(listener);

        return mapTypeCmbBox;
    }

    private JSlider settingsLivePeriodSlider() {
        var slider = new JSlider(JSlider.HORIZONTAL, 1, 10, 2);
        slider.setBounds(350, 90, 150, 25);
        slider.setToolTipText("Live-Data loading period in seconds (1-10)");

        return slider;
    }

    private UWPButton settingsFilterButton(@NotNull ActionListener listener) {
        var button = new UWPButton("Filters");
        button.setBounds(350, 130, 150, 25);
        button.setEffectColor(DEFAULT_FONT_COLOR.get());
        button.setSelectedColor(DEFAULT_MAP_ICON_COLOR.get());
        button.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        button.setFont(FONT_MENU);
        button.addActionListener(listener);

        return button;
    }

    private JSeparator[] separators(int count, int startY, int plus) {
        var seps = new JSeparator[count];
        for (int i = 0; i < count; i++) {
            var s = new JSeparator();
            s.setBounds(20, startY, 480, 2);
            s.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            seps[i] = s;
            startY += plus;
        }
        return seps;
    }

    @NotNull
    public String[] getValues() {
        return new String[] {
                this.maxLoadTxtField.getText(),
                (String) Objects.requireNonNullElse(this.mapTypeCmbBox.getSelectedItem(), "Default Map"),
                String.valueOf(this.livePeriodSlider.getValue())
        };
    }

    @NotNull
    public JComboBox<String> getMapTypeCmbBox() {
        return this.mapTypeCmbBox;
    }

}

package planespotter.display.models;

import libs.UWPButton;
import org.jetbrains.annotations.NotNull;
import planespotter.controller.ActionHandler;
import planespotter.display.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Objects;

import static planespotter.constants.DefaultColor.*;

public class SettingsPane extends JDialog {

    private final JTextField maxLoadTxtField;
    private final JComboBox<String> mapTypeCmbBox;
    private final JSlider livePeriodSlider;

    public SettingsPane(@NotNull JFrame parent, @NotNull ActionHandler actionHandler) {
        JLabel maxLoadLbl = new JLabel("Max. loaded Data:");
        maxLoadLbl.setBounds(20, 10, 300, 25);
        maxLoadLbl.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        maxLoadLbl.setFont(UserInterface.DEFAULT_FONT);
        maxLoadLbl.setOpaque(false);
        JLabel mapType = new JLabel("Map Type:");
        mapType.setBounds(20, 50, 300, 25);
        mapType.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        mapType.setFont(UserInterface.DEFAULT_FONT);
        mapType.setOpaque(false);
        JLabel livePeriod = new JLabel("Live Data Period (sec):");
        livePeriod.setBounds(20, 90, 300, 25);
        livePeriod.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        livePeriod.setFont(UserInterface.DEFAULT_FONT);
        livePeriod.setOpaque(false);
        JLabel liveMapFilters = new JLabel("Live Map / Supplier Filters: ");
        liveMapFilters.setBounds(20, 130, 300, 25);
        liveMapFilters.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        liveMapFilters.setFont(UserInterface.DEFAULT_FONT);
        liveMapFilters.setOpaque(false);
        JLabel hotkeys = new JLabel("Hotkey Settings:");
        hotkeys.setBounds(20, 170, 300, 25);
        hotkeys.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        hotkeys.setFont(UserInterface.DEFAULT_FONT);
        hotkeys.setOpaque(false);

        super.setBounds(parent.getWidth()/2-250, parent.getHeight()/2-200, 540, 400);
        super.setLayout(null);
        super.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        super.setType(Window.Type.POPUP);
        super.setResizable(false);
        super.setFocusable(false);
        // adding labels
        JSeparator[] separators = separators(5, 40, 40);
        this.maxLoadTxtField = maxLoadTxtField(actionHandler);
        this.mapTypeCmbBox = mapTypeCmbBox(actionHandler);
        JButton[] settingsButtons = mainButtons(this, actionHandler);
        this.livePeriodSlider = livePeriodSlider();
        UWPButton filterButton = filterButton(actionHandler);
        UWPButton hotkeyButton = hotkeyButton(actionHandler);

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
        super.add(hotkeys);
        super.add(hotkeyButton);

        this.maxLoadTxtField.setText(String.valueOf(50000));

        super.setVisible(false);
    }

    /**
     * settings opt. pane max-load text field
     */
    @NotNull
    private JTextField maxLoadTxtField(@NotNull KeyListener listener) {
        var maxLoadTxtfield = new JTextField();
        maxLoadTxtfield.setBounds(350, 10, 150, 25);
        maxLoadTxtfield.setBorder(BorderFactory.createLineBorder(DEFAULT_FONT_COLOR.get()));
        maxLoadTxtfield.setBackground(DEFAULT_FONT_COLOR.get());
        maxLoadTxtfield.setForeground(DEFAULT_ACCENT_COLOR.get());
        maxLoadTxtfield.setFont(UserInterface.DEFAULT_FONT);
        maxLoadTxtfield.addKeyListener(listener);

        return maxLoadTxtfield;
    }

    @NotNull
    private JButton[] mainButtons(@NotNull JDialog parent, @NotNull ActionListener listener) {
        int mid = parent.getWidth() / 2;
        int height = parent.getHeight() - 80;
        UWPButton cancel = new UWPButton("Cancel");
        cancel.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        cancel.setForeground(DEFAULT_FONT_COLOR.get());
        cancel.setFont(UserInterface.DEFAULT_FONT);
        cancel.setBounds(mid - 140, height, 120, 25);
        cancel.addActionListener(listener);
        UWPButton confirm = new UWPButton("Confirm");
        confirm.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        confirm.setForeground(DEFAULT_FONT_COLOR.get());
        confirm.setFont(UserInterface.DEFAULT_FONT);
        confirm.setBounds(mid + 20, height, 120, 25);
        confirm.addActionListener(listener);
        return new UWPButton[] {
                cancel, confirm
        };
    }

    @NotNull
    private JComboBox<String> mapTypeCmbBox(@NotNull ItemListener listener) {
        JComboBox<String> mapTypeCmbBox = new JComboBox<>(new String[] {
                "Open Street Map",
                "Bing Map",
                "Transport Map"
        });
        mapTypeCmbBox.setBounds(350, 50, 150,  25);
        mapTypeCmbBox.setBorder(BorderFactory.createLineBorder(DEFAULT_FONT_COLOR.get()));
        mapTypeCmbBox.setBackground(DEFAULT_FONT_COLOR.get());
        mapTypeCmbBox.setForeground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        mapTypeCmbBox.setFont(UserInterface.DEFAULT_FONT);
        mapTypeCmbBox.addItemListener(listener);

        return mapTypeCmbBox;
    }

    @NotNull
    private JSlider livePeriodSlider() {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 10, 2);
        slider.setBounds(350, 90, 150, 25);
        slider.setToolTipText("Live-Data loading period in seconds (1-10)");

        return slider;
    }

    @NotNull
    private UWPButton filterButton(@NotNull ActionListener listener) {
        UWPButton button = new UWPButton("Filters");
        button.setBounds(350, 130, 150, 25);
        button.setEffectColor(DEFAULT_FONT_COLOR.get());
        button.setSelectedColor(DEFAULT_MAP_ICON_COLOR.get());
        button.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        button.setFont(UserInterface.DEFAULT_FONT);
        button.addActionListener(listener);

        return button;
    }

    @NotNull
    private UWPButton hotkeyButton(@NotNull ActionListener listener) {
        UWPButton button = new UWPButton("Hotkeys");
        button.setBounds(350, 170, 150, 25);
        button.setEffectColor(DEFAULT_FONT_COLOR.get());
        button.setSelectedColor(DEFAULT_MAP_ICON_COLOR.get());
        button.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
        button.setFont(UserInterface.DEFAULT_FONT);
        button.addActionListener(listener);

        return button;
    }

    private JSeparator[] separators(int count, int startY, int plus) {
        JSeparator[] seps = new JSeparator[count];
        JSeparator s;
        for (int i = 0; i < count; i++) {
            s = new JSeparator();
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
                (String) Objects.requireNonNullElse(this.mapTypeCmbBox.getSelectedItem(), "Open Street Map"),
                String.valueOf(this.livePeriodSlider.getValue())
        };
    }

    @NotNull
    public JComboBox<String> getMapTypeCmbBox() {
        return this.mapTypeCmbBox;
    }

}

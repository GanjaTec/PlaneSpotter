package planespotter.display.models;

import libs.UWPButton;

import org.jetbrains.annotations.Range;

import planespotter.constants.UnicodeChar;
import planespotter.constants.Warning;
import planespotter.controller.Controller;
import planespotter.display.Diagrams;
import planespotter.model.Fr24Collector;
import planespotter.constants.DefaultColor;
import planespotter.statistics.Statistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static planespotter.constants.DefaultColor.*;

public abstract class Menus {

    public static final byte TYPE_STATS = 0,
                             TYPE_SUPPLIER = 1,
                             TYPE_AREA = 2;

    private static final int width = 300, height = 400;
    private static final int compWidth = width - 35, compHeight = 20;

    private static boolean shown = false;

    /**
     * shows a specific menu by menu-type constant
     *
     * @param menuType is the menu type, should be one of
     *                     STATS_MENU, SUPPLIER_MENU, AREA_MENU
     * @param owner is the dialog owner window, may be null
     */
    public static void show(@Range(from = 0, to = 2) byte menuType, Window owner) {
        if (!shown) {
            var menu = switch (menuType) {
                case 0 -> new StatisticMenu(owner);
                case 1 -> new SupplierMenu(owner);
                case 2 -> new AreaMenu(owner);
                default -> throw new IllegalStateException("Unexpected value: " + menuType);
            };
            menu.setVisible(true);
        }
    }

    private static class StatisticMenu extends JDialog {

        static final String STATS_TOP_AIRPORTS          = "Top-Airports",
                            STATS_AIRPORT_SIGNIFICANCE  = "Airport-Significance",
                            STATS_AIRLINE_SIGNIFICANCE  = "Airline-Significance",
                            STATS_HEATMAP               = "Position-HeatMap";

        StatisticMenu(Window owner) {
            super(owner, "Statistics-Menu");
            super.setLayout(null);
            super.setSize(width, height);
            super.setBackground(DefaultColor.DEFAULT_BG_COLOR.get());
            super.setLocationRelativeTo(null);
            super.setResizable(false);
            super.addWindowListener(new Listener());

            super.add(this.createStatsList());
        }

        private JList<String> createStatsList() {
            var data= Stream.of(STATS_TOP_AIRPORTS, STATS_AIRPORT_SIGNIFICANCE, STATS_AIRLINE_SIGNIFICANCE, STATS_HEATMAP)
                    .collect(Collectors.toCollection(Vector::new));
            var supList = new JList<String>();
            supList.setBounds(10, 10, compWidth, height -70);
            supList.setFixedCellWidth(compWidth);
            supList.setFixedCellHeight(compHeight);
            supList.setSelectionBackground(DEFAULT_MAP_ICON_COLOR.get());
            supList.setVisibleRowCount(10); // max 10 suppliers
            supList.setListData(data);
            supList.addListSelectionListener(e -> {
                var value = supList.getSelectedValue();
                var rightDP = (JDesktopPane) Controller.getGUI().getContainer("rightDP");
                var stats = new Statistics();
                var guiAdapter = Controller.guiAdapter;
                switch (value) {
                    // TODO: 30.06.2022 User-Input for parameters
                    case STATS_TOP_AIRPORTS -> guiAdapter.receiveChart(Diagrams.barChartPanel(rightDP, stats.topAirports(20)));
                    case STATS_AIRPORT_SIGNIFICANCE -> {
                        // TODO: 30.06.2022 move to controller
                        var input = JOptionPane.showInputDialog("Please enter a minimum significance (0-" + UnicodeChar.INFINITY.get() + ")", 250);
                        if (input.isBlank()) {
                            return;
                        }
                        try {
                            int minCount = Integer.parseInt(input);
                            guiAdapter.receiveChart(Diagrams.barChartPanel(rightDP, stats.airportSignificance(minCount)));
                        } catch (NumberFormatException nfe) {
                            Controller.guiAdapter.showWarning(Warning.INT_EXPECTED);
                        }
                    }
                    case STATS_AIRLINE_SIGNIFICANCE -> {
                        // TODO: 30.06.2022 move to controller
                        var input = JOptionPane.showInputDialog("Please enter a minimum significance (0-" + UnicodeChar.INFINITY + ")", 250);
                        if (input.isBlank()) {
                            return;
                        }
                        try {
                            int minCount = Integer.parseInt(input);
                            guiAdapter.receiveChart(Diagrams.barChartPanel(rightDP, stats.airlineSignificance(minCount)));
                        } catch (NumberFormatException nfe) {
                            Controller.guiAdapter.showWarning(Warning.INT_EXPECTED);
                        }
                    }
                    case STATS_HEATMAP -> {/* show heat map */}
                }
                super.setVisible(false);
            });

            return supList;
        }
    }

    private static class SupplierMenu extends JDialog {

        // TODO: 28.06.2022 change to Supplier and maybe SupplierMain<? extends Supplier>
        private Deque<Fr24Collector> activateDQ = new ArrayDeque<>(10);
        // WORKING LIST

        SupplierMenu(Window owner) {
            super(owner, "Supplier-Menu");
            super.setLayout(null);
            super.setSize(width, height);
            super.setBackground(DefaultColor.DEFAULT_BG_COLOR.get());
            super.setLocationRelativeTo(null);
            super.setResizable(false);
            super.addWindowListener(new Listener());

            super.add(this.createRunButton());
            super.add(this.createSupplierList());

        }

        private UWPButton createRunButton() {
            var runButton = new UWPButton("Run Supplier");
            runButton.setBounds(10, height - 70, compWidth, compHeight);
            runButton.setEffectColor(DEFAULT_FONT_COLOR.get());
            runButton.setSelectedColor(DEFAULT_MAP_ICON_COLOR.get());
            runButton.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            runButton.addActionListener(e -> {
                while (!this.activateDQ.isEmpty()) {
                    var collector = this.activateDQ.pollFirst();
                    collector.start();
                }
                super.setVisible(false);
            });
            return runButton;
        }

        private JList<String> createSupplierList() {
            var data= Stream.of("Test1", "Fr24-Supplier", "Test3")
                    .collect(Collectors.toCollection(Vector::new));
            var supList = new JList<String>();
            supList.setBounds(10, 10, compWidth, height -70);
            supList.setFixedCellWidth(compWidth);
            supList.setFixedCellHeight(compHeight);
            supList.setVisibleRowCount(10); // max 10 suppliers
            supList.setListData(data);
            supList.addListSelectionListener(e -> {
                var suppliers = supList.getSelectedValuesList()
                        .stream()
                        .filter(s -> s.contains("Fr24"))  // TODO: 29.06.2022 filter for type and create fitting Collectors
                        .map(s -> new Fr24Collector(false))
                        .toList();
                this.activateDQ = new ArrayDeque<>(suppliers);
            });

            return supList;
        }
    }

    private static class AreaMenu extends JDialog {
        AreaMenu(Window owner) {
            super(owner, "Statistics");
            super.setLayout(null);
            super.setSize(width, height);
            super.setBackground(DefaultColor.DEFAULT_BG_COLOR.get());
            super.setLocationRelativeTo(null);
            super.setResizable(false);
            super.addWindowListener(new Listener());

            //super.add();

        }
    }

    private static class Listener implements WindowListener {
        @Override
        public void windowActivated(WindowEvent e) {
            shown = true;
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            shown = false;
        }

        @Override public void windowOpened(WindowEvent e) {}
        @Override public void windowClosing(WindowEvent e) {}
        @Override public void windowClosed(WindowEvent e) {}
        @Override public void windowIconified(WindowEvent e) {}
        @Override public void windowDeiconified(WindowEvent e) {}

    }

}

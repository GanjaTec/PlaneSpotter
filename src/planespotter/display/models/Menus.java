package planespotter.display.models;

import libs.UWPButton;
import org.jetbrains.annotations.Range;
import planespotter.model.Fr24Collector;
import planespotter.constants.DefaultColor;

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

    private static final int width = 300, heigth = 400;
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
            //shown = true;
            menu.setVisible(true);
        }
    }

    private static class StatisticMenu extends JDialog {

        StatisticMenu(Window owner) {
            super(owner, "Statistics-Menu");
            super.setLayout(null);
            super.setSize(width, heigth);
            super.setBackground(DefaultColor.DEFAULT_BG_COLOR.get());
            super.setLocationRelativeTo(null);
            super.setResizable(false);
            super.addWindowListener(new Listener());

            super.add(this.createShowButton());
        }

        private UWPButton createShowButton() {
            var showButton = new UWPButton("Show");
            showButton.setBounds(10, heigth - 70, compWidth, compHeight);
            showButton.setEffectColor(DEFAULT_FONT_COLOR.get());
            showButton.setSelectedColor(DEFAULT_MAP_ICON_COLOR.get());
            showButton.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            return showButton;
        }
    }

    private static class SupplierMenu extends JDialog {

        // TODO: 28.06.2022 change to Supplier and maybe SupplierMain<? extends Supplier>
        private Deque<Fr24Collector> activateDQ = new ArrayDeque<>(10);
        // WORKING LIST

        SupplierMenu(Window owner) {
            super(owner, "Supplier-Menu");
            super.setLayout(null);
            super.setSize(width, heigth);
            super.setBackground(DefaultColor.DEFAULT_BG_COLOR.get());
            super.setLocationRelativeTo(null);
            super.setResizable(false);
            super.addWindowListener(new Listener());

            super.add(this.createRunButton());
            super.add(this.createSupplierList());

        }

        private UWPButton createRunButton() {
            var runButton = new UWPButton("Run Supplier");
            runButton.setBounds(10, heigth - 70, compWidth, compHeight);
            runButton.setEffectColor(DEFAULT_FONT_COLOR.get());
            runButton.setSelectedColor(DEFAULT_MAP_ICON_COLOR.get());
            runButton.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            runButton.addActionListener(e -> {
                //SupplierMain.start(false);
                while (!this.activateDQ.isEmpty()) {
                    var smain = this.activateDQ.pollFirst();
                    smain.start();
                }
                super.setVisible(false);
            });
            return runButton;
        }

        private JList<String> createSupplierList() {
            var data= Stream.of("Test1", "Fr24-Supplier", "Test3")
                    .collect(Collectors.toCollection(Vector::new));
            var supList = new JList<String>();
            supList.setBounds(10, 10, compWidth, heigth-70);
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
            super.setSize(width, heigth);
            super.setBackground(DefaultColor.DEFAULT_BG_COLOR.get());
            super.setLocationRelativeTo(null);
            super.setResizable(false);
            super.addWindowListener(new Listener());

            //super.add();

        }
    }

    private static class Listener implements WindowListener {
        @Override
        public void windowOpened(WindowEvent e) {
            shown = true;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            shown = false;
        }

        @Override public void windowClosed(WindowEvent e) {}
        @Override public void windowIconified(WindowEvent e) {}
        @Override public void windowDeiconified(WindowEvent e) {}
        @Override public void windowActivated(WindowEvent e) {}
        @Override public void windowDeactivated(WindowEvent e) {}
    }

}

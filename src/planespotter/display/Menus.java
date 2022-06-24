package planespotter.display;

import libs.UWPButton;
import planespotter.SupplierMain;
import planespotter.constants.DefaultColor;
import planespotter.controller.Scheduler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import static planespotter.constants.DefaultColor.*;

public abstract class Menus {

    private static final int width = 300, heigth = 400;

    private static boolean shown = false;

    public static void showStatisticMenu(Window owner) {
        if (!shown)
        new StatisticMenu(owner).setVisible(true);
    }

    public static void showSupplierMenu(Window owner) {
        if (!shown)
            new SupplierMenu(owner).setVisible(true);
    }

    public static void showAreaMenu(Window owner) {
        if (!shown)
            new AreaMenu(owner).setVisible(true);
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
            showButton.setBounds(10, heigth - 70, width - 35, 20);
            showButton.setEffectColor(DEFAULT_FONT_COLOR.get());
            showButton.setSelectedColor(DEFAULT_MAP_ICON_COLOR.get());
            showButton.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            return showButton;
        }
    }

    private static class SupplierMenu extends JDialog {
        SupplierMenu(Window owner) {
            super(owner, "Supplier-Menu");
            super.setLayout(null);
            super.setSize(width, heigth);
            super.setBackground(DefaultColor.DEFAULT_BG_COLOR.get());
            super.setLocationRelativeTo(null);
            super.setResizable(false);
            super.addWindowListener(new Listener());

            super.add(this.createRunButton());

        }

        private UWPButton createRunButton() {
            var runButton = new UWPButton("Run Supplier");
            runButton.setBounds(10, heigth - 70, width - 35, 20);
            runButton.setEffectColor(DEFAULT_FONT_COLOR.get());
            runButton.setSelectedColor(DEFAULT_MAP_ICON_COLOR.get());
            runButton.setBackground(DEFAULT_SEARCH_ACCENT_COLOR.get());
            runButton.addActionListener(e -> {
                SupplierMain.start();
                super.setVisible(false);
            });
            return runButton;
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

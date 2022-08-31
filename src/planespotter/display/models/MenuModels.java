package planespotter.display.models;

import org.jetbrains.annotations.NotNull;

import planespotter.constants.Images;
import planespotter.controller.ActionHandler;
import planespotter.display.UserInterface;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
/**
 * @name MenuModels
 * @author jml04
 * @version 1.0
 *
 * MenuModels class contains different menu component models
 */
public final class MenuModels {

    @NotNull
    public static JMenuBar topMenuBar(@NotNull final ActionHandler actionHandler) {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File"),
              liveMapMenu = new JMenu("Live-Map"),
              searchMenu = new JMenu("Search"),
              statsMenu = new JMenu("Statistics"),
              supplierMenu = new JMenu("Supplier"),
              settingsMenu = new JMenu("Settings"),
              closeMenu = new JMenu("Close View"),
              helpMenu = new JMenu("Help");
        liveMapMenu.addMouseListener(actionHandler);
        searchMenu.addMouseListener(actionHandler);
        settingsMenu.addMouseListener(actionHandler);
        closeMenu.addMouseListener(actionHandler);

        JMenuItem[] fileItems = new JMenuItem[] {
                new JMenuItem("Open", Images.OPEN_FILE_ICON_16x.get()),
                new JMenuItem("Save As", Images.SAVE_FILE_ICON_16x.get()),
                new JMenuItem("Fullscreen", Images.FULLSCREEN_ICON_16x.get()),
                new JMenuItem("Exit", Images.EXIT_ICON_16x.get())
        };
        JMenu heatMapMenu = new JMenu("Heat-Map");
        heatMapMenu.setIcon(Images.HEATMAP_ICON_16x.get());
        JMenuItem[] statsItems = new JMenuItem[] {
                new JMenuItem("Top-Airports", Images.STATS_ICON_16x.get()),
                new JMenuItem("Top-Airlines", Images.STATS_ICON_16x.get()),
                heatMapMenu
        };
        JMenuItem[] heatMapItems = new JMenuItem[] {
                new JMenuItem("Position-HeatMap"),
                new JMenuItem("coming soon...")
        };
        JMenuItem[] supplierItems = new JMenuItem[] {
                new JMenuItem("Fr24-Supplier", Images.PLANE_ICON_16x.get()),
                new JMenuItem("ADSB-Supplier", Images.PLANE_ICON_16x.get()),
                new JMenuItem("Antenna", Images.ANTENNA_ICON_16x.get())
        };
        Font font = UserInterface.DEFAULT_FONT.deriveFont(13f);

        Arrays.stream(fileItems).forEach(item -> {
            item.addMouseListener(actionHandler);
            item.setFont(font);
            fileMenu.add(item);
            fileMenu.addSeparator();
        });
        Arrays.stream(statsItems).forEach(item -> {
            if (item instanceof JMenu menu) {
                Arrays.stream(heatMapItems).forEach(i -> {
                    i.addMouseListener(actionHandler);
                    i.setFont(font);
                    menu.add(i);
                    menu.addSeparator();
                });
            } else {
                item.addMouseListener(actionHandler);
            }
            item.setFont(font);
            statsMenu.add(item);
            statsMenu.addSeparator();

        });
        Arrays.stream(supplierItems).forEach(item -> {
            item.addMouseListener(actionHandler);
            item.setFont(font);
            supplierMenu.add(item);
            supplierMenu.addSeparator();
        });
        JMenu[] menus = new JMenu[] {
                fileMenu, liveMapMenu, searchMenu, statsMenu, supplierMenu, settingsMenu, closeMenu, helpMenu
        };
        Arrays.stream(menus).forEach(m -> {
            m.setFont(font);
            menuBar.add(m);
        });
        return menuBar;
    }

    /**
     * @return file chooser for file dialog
     */
    public static JFileChooser fileSaver(JFrame parent) {
        var home = FileSystemView.getFileSystemView().getHomeDirectory();
        var fileChooser = new JFileChooser(home);
        fileChooser.setAcceptAllFileFilterUsed(false);
        var pls = new FileNameExtensionFilter("nur .pls-Dateien", "pls"); // TODO constant?
        fileChooser.setFileFilter(pls);
        fileChooser.showSaveDialog(parent);

        return fileChooser;
    }

    /**
     * @return file chooser for file dialog
     */
    public static JFileChooser fileLoader(JFrame parent) {
        File home = FileSystemView.getFileSystemView().getHomeDirectory();
        JFileChooser fileChooser = new JFileChooser(home);
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter pls = new FileNameExtensionFilter("only .pls-Files", "pls", ".pls"); // which is the right one?
        fileChooser.setFileFilter(pls);
        fileChooser.showOpenDialog(parent);

        return fileChooser;
    }

}

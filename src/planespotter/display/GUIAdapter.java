package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.constants.SearchType;
import planespotter.constants.Warning;
import planespotter.controller.Controller;
import planespotter.model.LiveMap;

import javax.swing.*;
import java.util.List;

import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.SearchType.*;

/**
 * @name GUIAdapter
 * @author jml04
 * @version 1.1
 *
 * class GUIAdapter contains methods to do some actions for the GUI (that the GUI class is not too full)
 */
public final class GUIAdapter {
    // only gui instance
    private final GUI gui;

    private static boolean warningShown = false;

    /**
     * empty constructor
     */
    public GUIAdapter(GUI gui) {
        this.gui = gui;
    }

    /**
     * shows a specific warning dialog
     *
     * @param type is the warning type which contains the warning message
     */
    public void warning(Warning type) {
        this.warning(type, null);
    }

    /**
     * shows a specific warning dialog
     *
     * @param type is the warning type which contains the warning message
     */
    public void warning(Warning type, @Nullable String addTxt) {
        if (!warningShown) {
            var message = type.message();
            if (addTxt != null) {
                message += "\n" + addTxt;
            }
            warningShown = true;
            JOptionPane.showOptionDialog(
                    gui.getContainer("window"),
                    message,
                    "Warning",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, null, null); // TODO: 06.06.2022 warning icon
            warningShown = false;
        }
    }

    /**
     * sets the JTree in listView and makes it visible
     *
     * @param tree is the tree to set
     */
    public void recieveTree(JTree tree) {
        var listPanel = (JPanel) gui.getContainer("listPanel");
        tree.setBounds(0, 0, listPanel.getWidth(), listPanel.getHeight());
        //gui.listView = tree;
        gui.addContainer("listScrollPane", new PaneModels().listScrollPane(tree, listPanel));
        var listScrollPane = gui.getContainer("listScrollPane");
        gui.getContainer("listPanel").add(listScrollPane);
        var rightDP = (JDesktopPane) gui.getContainer("rightDP");
        rightDP.moveToFront(listPanel);
        listPanel.setVisible(true);
        var viewHeadText = (JTextField) gui.getContainer("viewHeadText");
        viewHeadText.setText(DEFAULT_HEAD_TEXT + "Flight-List"); // TODO: 21.05.2022 add text
        // revalidate window -> making the tree visible
        this.requestComponentFocus(gui.listView);
    }

    /**
     *
     * @param flightTree is the flight tree to set
     * @param dpInfoTree is the @Nullable data point info tree
     */
    public void recieveInfoTree(@NotNull final JTree flightTree,
                                @Nullable final JTree dpInfoTree) {
        var infoPanel = gui.getContainer("infoPanel");
        int width = infoPanel.getWidth();
        int height = infoPanel.getHeight() / 2;
        gui.getContainer("menuPanel").setVisible(false);
        flightTree.setBounds(0, 0, width, height + 50);
        flightTree.setMaximumSize(infoPanel.getSize());
        flightTree.setBorder(LINE_BORDER);
        flightTree.setFont(FONT_MENU.deriveFont(12f));
        infoPanel.add(flightTree);
        gui.addContainer("flightInfoTree", flightTree);
        if (dpInfoTree != null) {
            this.recieveDataPointInfoTree(dpInfoTree, width, height);
        }
        var leftDP = (JDesktopPane) gui.getContainer("leftDP");
        leftDP.moveToFront(infoPanel);
        infoPanel.setVisible(true);
    }

    private void recieveDataPointInfoTree(@NotNull JTree dpInfoTree, int width, int height) {
        dpInfoTree.setBounds(0, height + 50, width, height - 50);
        dpInfoTree.setBorder(LINE_BORDER);
        dpInfoTree.setFont(FONT_MENU.deriveFont(12f));
        gui.getContainer("infoPanel").add(dpInfoTree);
        gui.addContainer("dpInfoTree", dpInfoTree);
    }

    /**
     * starts a indeterminate progressBar
     */
    public void startProgressBar() {
        var progressBar = (JProgressBar) gui.getContainer("progressBar");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Loading data...");
        progressBar.setStringPainted(true);
    }

    /**
     * sets the visibility of the progressBar
     *
     */
    public void stopProgressBar() {
        gui.getContainer("progressBar").setVisible(false);
    }

    /**
     * revalidates all swing components
     */
    public void update() {
        gui.getContainer("window").revalidate();
    }

    /**
     * requests focus for a specific component
     *
     * @param comp is the component that requests the focus
     */
    public void requestComponentFocus(JComponent comp) {
        comp.requestFocus();
    }

    /**
     * loads all search components for a certain search type
     *
     * @param type is the search type
     */
    public void loadSearch(SearchType type) {
        gui.setCurrentSearchType(type);
        switch (type) {
            case FLIGHT -> this.showSearch(gui.flightSearch);
            case PLANE -> this.showSearch(gui.planeSearch);
            case AIRLINE -> this.showSearch(gui.airlineSearch);
            case AIRPORT -> this.showSearch(gui.airportSearch);
            // @deprecated, TODO remove
            case AREA -> this.showSearch(gui.areaSearch);
        }
    }

    /**
     * sets every component from the given search visible
     *
     * @param search is the given list of search components
     */
    private void showSearch(List<JComponent> search) {
        var searchModels = gui.allSearchModels();
        for (var comps : searchModels) {
            var equals = (comps == search);
            if (comps != null) {
                for (var c : comps) {
                    c.setVisible(equals);
                }
            }
        }
    }

    /**
     * disposes all views (and opens the src screen)
     * if no other view is opened, nothing is done
     */
    public synchronized void disposeView() {
        if (gui.hasContainer("startPanel")) {
            gui.getContainer("startPanel").setVisible(false);
        } if (gui.hasContainer("listView")) {
            final var listPanel = gui.getContainer("listPanel");
            var listView = gui.getContainer("listView");
            listPanel.remove(listView);
            listView.setVisible(false);
            listView = null;
            listPanel.setVisible(false);
        } if (gui.getMap() != null) {
            final var viewer = gui.getMap();
            final var mapPanel = gui.getContainer("mapPanel");
            viewer.removeAllMapMarkers();
            viewer.removeAllMapPolygons();
            viewer.setVisible(false);
            mapPanel.remove(viewer);
            mapPanel.setVisible(false);
        } if (gui.hasContainer("flightInfoTree")) {
            var flightInfo = gui.getContainer("flightInfoTree");
            flightInfo.setVisible(false);
            flightInfo = null;
            gui.getContainer("infoPanel").setVisible(false);
        }
        var menuPanel = gui.getContainer("menuPanel");
        menuPanel.setVisible(true);
        var leftDP = (JDesktopPane) gui.getContainer("leftDP");
        leftDP.moveToFront(menuPanel);
        var viewHeadTxtLabel = (JLabel) gui.getContainer("viewHeadTxtLabel");
        viewHeadTxtLabel.setText(DEFAULT_HEAD_TEXT); // TODO EXTRA methode
        gui.setCurrentViewType(null);
        gui.getMap().setHeatMap(null);
        //LiveMap.close();
        Controller.getInstance().isLive = false;
    }

    public void setViewHeadBtVisible(boolean b) {
        gui.getContainer("fileButton").setVisible(b);
        gui.getContainer("closeViewButton").setVisible(b);
    }

    public void setFileMenuVisible(boolean b) {
        assert gui.fileMenu != null;
        for (var bt : gui.fileMenu) {
            bt.setVisible(b);
        }
    }

    public String[] searchInput() {
        switch (gui.getCurrentSearchType()) {
            case FLIGHT -> {
                return new String[] {
                        gui.search_flightID.getText(),
                        gui.search_callsign.getText()
                };
            } case PLANE -> {
                return new String[] {
                        gui.search_planeID.getText(),
                        gui.search_planetype.getText(),
                        gui.search_icao.getText(),
                        gui.search_tailNr.getText()
                };
            } case AIRPORT -> {
                return new String[] {
                        gui.search_airpID.getText(),
                        gui.search_airpTag.getText(),
                        gui.search_airpName.getText()
                };
            }
        }
        return null;
    }

    public void clearSearch() {
        var comps = new JTextField[] {
                gui.search_planetype,
                gui.search_tailNr,
                gui.search_planeID,
                gui.search_callsign,
                gui.search_flightID,
                gui.search_icao
        };
        for (var c : comps) {
            c.setText("");
        }

    }

}

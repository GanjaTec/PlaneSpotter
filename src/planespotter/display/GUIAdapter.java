package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jfree.chart.ChartPanel;

import planespotter.constants.SearchType;
import planespotter.constants.Warning;
import planespotter.display.models.PaneModels;
import planespotter.model.LiveData;
import planespotter.throwables.IllegalInputException;
import planespotter.util.Utilities;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.Sound.SOUND_DEFAULT;

/**
 * @name GUIAdapter
 * @author jml04
 * @version 1.1
 * @description
 * class GUIAdapter contains methods to do some actions for the GUI (that the GUI class is not too full)
 */
public final class GUIAdapter {
    // only gui instance
    private final GUI gui;

    private static boolean warningShown = false;

    /**
     * GUIAdapter-constructor with GUI
     *
     * @param gui is the GUI instance
     */
    public GUIAdapter(GUI gui) {
        this.gui = gui;
    }

    /**
     * shows a specific warning dialog
     *
     * @param type is the warning type which contains the warning message
     */
    public void showWarning(Warning type) {
        this.showWarning(type, null);
    }

    /**
     * shows a specific warning dialog
     *
     * @param type is the warning type which contains the warning message
     */
    public void showWarning(Warning type, @Nullable String addTxt) {
        if (!warningShown) {
            var message = type.message();
            if (addTxt != null) {
                message += "\n" + addTxt;
            }
            warningShown = true;
            try {
                JOptionPane.showOptionDialog(
                        gui.getComponent("window"),
                        message,
                        "Warning",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, null, null); // TODO: 30.06.2022 warning icon
            } finally {
                warningShown = false;
            }
        }
    }

    /**
     * this method is executed when pre-loading is done
     */
    public void onInitFinish() {
        Utilities.playSound(SOUND_DEFAULT.get());
        gui.loadingScreen.dispose();
        var window = gui.getComponent("window");
        window.setVisible(true);
        window.requestFocus();
    }

    public void receiveChart(ChartPanel chartPanel) {
        this.disposeView();

        var rightDP = (JDesktopPane) gui.getComponent("rightDP");
        rightDP.add(chartPanel);
        rightDP.moveToFront(chartPanel);
        chartPanel.setVisible(true);

        gui.chartPanel = chartPanel;
    }

    /**
     * sets the JTree in listView and makes it visible
     *
     * @param tree is the tree to set
     */
    public void receiveTree(JTree tree) {
        var listPanel = (JPanel) gui.getComponent("listPanel");
        tree.setBounds(0, 0, listPanel.getWidth(), listPanel.getHeight());
        gui.addContainer("listScrollPane", new PaneModels().listScrollPane(tree, listPanel));
        var listScrollPane = gui.getComponent("listScrollPane");
        gui.getComponent("listPanel").add(listScrollPane);
        var rightDP = (JDesktopPane) gui.getComponent("rightDP");
        rightDP.moveToFront(listPanel);
        listPanel.setVisible(true);
        var viewHeadText = (JTextField) gui.getComponent("viewHeadText");
        viewHeadText.setText(DEFAULT_HEAD_TEXT + "Flight-List"); // TODO: 21.05.2022 add text
        // revalidate window -> making the tree visible
        this.requestComponentFocus(gui.listView);
    }

    /**
     *
     * @param flightTree is the flight tree to set
     * @param dpInfoTree is the @Nullable data point info tree
     */
    public void receiveInfoTree(@NotNull final JTree flightTree,
                                @Nullable final JTree dpInfoTree) {
        var infoPanel = gui.getComponent("infoPanel");
        infoPanel.removeAll();
        int width = infoPanel.getWidth();
        int height = infoPanel.getHeight() / 2;
        gui.getComponent("menuPanel").setVisible(false);
        flightTree.setBounds(0, 0, width, height + 50);
        flightTree.setMaximumSize(infoPanel.getSize());
        flightTree.setBorder(LINE_BORDER);
        flightTree.setFont(FONT_MENU.deriveFont(12f));
        infoPanel.add(flightTree);
        gui.addContainer("flightInfoTree", flightTree);
        if (dpInfoTree != null) {
            this.receiveDataPointInfoTree(dpInfoTree, width, height);
        }
        var leftDP = (JDesktopPane) gui.getComponent("leftDP");
        leftDP.moveToFront(infoPanel);
        infoPanel.setVisible(true);
    }

    private void receiveDataPointInfoTree(@NotNull JTree dpInfoTree, int width, int height) {
        dpInfoTree.setBounds(0, height + 50, width, height - 50);
        dpInfoTree.setBorder(LINE_BORDER);
        dpInfoTree.setFont(FONT_MENU.deriveFont(12f));
        gui.getComponent("infoPanel").add(dpInfoTree);
        gui.addContainer("dpInfoTree", dpInfoTree);
    }

    /**
     * starts a indeterminate progressBar
     */
    public void startProgressBar() {
        var progressBar = (JProgressBar) gui.getComponent("progressBar");
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
        gui.getComponent("progressBar").setVisible(false);
    }

    /**
     * revalidates all swing components
     */
    public void update() {
        gui.getComponent("window").revalidate();
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
        if (gui.chartPanel != null) {
            var rightDP = (JDesktopPane) gui.getComponent("rightDP");
            rightDP.remove(gui.chartPanel);
        }
        if (gui.hasContainer("startPanel")) {
            gui.getComponent("startPanel").setVisible(false);
        } if (gui.hasContainer("listView")) {
            final var listPanel = gui.getComponent("listPanel");
            var listView = gui.getComponent("listView");
            listPanel.remove(listView);
            listView.setVisible(false);
            listPanel.setVisible(false);
        } if (gui.getMap() != null) {
            final var viewer = gui.getMap();
            final var mapPanel = gui.getComponent("mapPanel");
            viewer.removeAllMapMarkers();
            viewer.removeAllMapPolygons();
            viewer.setVisible(false);
            mapPanel.remove(viewer);
            mapPanel.setVisible(false);
        } if (gui.hasContainer("flightInfoTree")) {
            var flightInfo = gui.getComponent("flightInfoTree");
            flightInfo.setVisible(false);
            gui.getComponent("infoPanel").setVisible(false);
        }
        var menuPanel = gui.getComponent("menuPanel");
        menuPanel.setVisible(true);
        var leftDP = (JDesktopPane) gui.getComponent("leftDP");
        leftDP.moveToFront(menuPanel);
        var viewHeadTxtLabel = (JLabel) gui.getComponent("viewHeadTxtLabel");
        viewHeadTxtLabel.setText(DEFAULT_HEAD_TEXT); // TODO EXTRA methode
        gui.setCurrentViewType(null);
        gui.getMap().setHeatMap(null);
        //LiveMap.close();
        LiveData.setLive(false);
    }

    public void setViewHeadBtVisible(boolean b) {
        gui.getComponent("fileButton").setVisible(b);
        gui.getComponent("closeViewButton").setVisible(b);
    }

    public void setFileMenuVisible(boolean b) {
        assert gui.fileMenu != null;
        for (var bt : gui.fileMenu) {
            bt.setVisible(b);
        }
    }

    public String[] searchInput() throws IllegalInputException {
        var inputFields = new String[0];
        switch (gui.getCurrentSearchType()) {
            case FLIGHT -> inputFields = new String[] {
                    gui.search_flightID.getText(),
                    gui.search_callsign.getText()
            };
            case PLANE -> inputFields = new String[] {
                    gui.search_planeID.getText(),
                    gui.search_planetype.getText(),
                    gui.search_icao.getText(),
                    gui.search_tailNr.getText()
            };
            case AIRPORT -> inputFields = new String[] {
                    gui.search_airpID.getText(),
                    gui.search_airpTag.getText(),
                    gui.search_airpName.getText()
            };
        }

        int length = inputFields.length;
        for (int i = 0; i < length; i++) {
            // checking strings for illegal characters
            // or expressions before returning them
            inputFields[i] = Utilities.checkString(inputFields[i]);
        }
        return inputFields;
    }

    public void clearSearch() {
        final var blank = "";
        gui.allSearchModels().stream()
                .filter(Objects::nonNull)
                .forEach(models -> models.forEach(m -> {
                    if (m instanceof JTextField jtf) {
                        jtf.setText(blank);
                    }
                }));

    }

}

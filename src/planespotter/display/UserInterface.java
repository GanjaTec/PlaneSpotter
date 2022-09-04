package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import planespotter.constants.Configuration;
import planespotter.constants.SearchType;
import planespotter.constants.ViewType;
import planespotter.constants.Warning;
import planespotter.controller.ActionHandler;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Flight;
import planespotter.display.models.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @name UserInterface
 * @author jml04
 * @version 1.0
 *
 * @description
 * The UserInterface class represents a graphical user interface (GUI),
 * which is a lot more easy than the old GUI class
 */
public class UserInterface {

    // the default font with a size of 16, can be changed
    @NotNull public static final Font DEFAULT_FONT = new Font("DialogInput", Font.BOLD, 16);

    // main window, contains all components
    @NotNull
    private final JFrame window;

    // layer pane, contains all view layers and layer-operations
    @NotNull
    private final LayerPane layerPane;

    // search panel for data search
    @NotNull
    private final SearchPane searchPanel;

    // settings panel, contains all settings
    @NotNull
    private final SettingsPane settings;

    // map manager, manages map operations
    @NotNull
    private final MapManager mapManager;

    // the current view type
    @NotNull
    private ViewType currentViewType;

    // indicates if a warning is shown at the moment
    private boolean warningShown;

    /**
     * the {@link UserInterface} constructor,
     * creates a full UI with all its components
     *
     * @param actionHandler is the {@link ActionHandler} which handles all the actions
     */
    public UserInterface(@NotNull ActionHandler actionHandler, @NotNull TileSource defaultMapSource, @NotNull String title) {

        this.window = PaneModels.windowFrame(actionHandler, title);
        this.layerPane = new LayerPane(window.getSize());
        this.mapManager = new MapManager(this, actionHandler, defaultMapSource);
        this.searchPanel = new SearchPane(this.layerPane, actionHandler);
        this.settings = new SettingsPane(this.window, actionHandler);
        this.window.add(this.layerPane);

        this.layerPane.setDefaultBottomComponent(this.getMap());
        this.layerPane.setDefaultOverTopComponent(PaneModels.loadingScreen());
        this.layerPane.setBottomDefault();

        this.currentViewType = ViewType.MAP_LIVE;
    }

    /**
     * shows the info panel for certain {@link Flight} and {@link DataPoint}
     *
     * @param flight is the {@link Flight} to be displayed
     * @param dataPoint is the {@link DataPoint} to be displayed
     */
    public void showInfo(@NotNull Flight flight, @NotNull DataPoint dataPoint) {
        int x = 0, y = 0, width = 270, height = layerPane.getHeight();
        InfoPane infoPane = InfoPane.of(layerPane, flight, dataPoint);
        layerPane.addTop(infoPane, x, y, width, height);
        //this.layerPane.moveTop(x - width, y, width, height, x, y, 1000);
    }

    /**
     * shows a search panel by certain search type
     *
     * @param type is the {@link SearchType}
     */
    public void showSearch(@NotNull SearchType type) {
        int x = searchPanel.getX(),
            y = searchPanel.getY(),
            width = searchPanel.getWidth(),
            height = searchPanel.getHeight();
        layerPane.addTop(searchPanel, x, y, width, height);
        searchPanel.setVisible(!searchPanel.isVisible());
        searchPanel.showSearch(type);
        // trying to animate components
        //this.layerPane.moveTop(x-100, y-100, width, height, x, y, 2000);
    }

    /**
     * sets the settings pane (in)visible
     *
     * @param show indicates the visibility
     */
    public void showSettings(boolean show) {
        settings.setVisible(show);
    }

    /**
     * shows a specific warning with certain type
     *
     * @param type is the {@link Warning} type
     */
    public void showWarning(@NotNull Warning type) {
        showWarning(type, null);
    }

    /**
     * shows a specific warning dialog
     *
     * @param type is the warning type which contains the warning message
     * @param addTxt is the additional text, may be null
     */
    public void showWarning(@NotNull Warning type, @Nullable String addTxt) {
        if (!warningShown) {
            String message = type.message();
            if (addTxt != null && !addTxt.isBlank()) {
                message += "\n" + addTxt;
            }
            warningShown = true;
            try {
                JOptionPane.showOptionDialog(
                        this.getWindow(),
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
     * clears the current view, removes all {@link planespotter.dataclasses.PlaneMarker}s from the map
     * and all components from the {@link LayerPane}, sets the default bottom component (map) visible
     */
    public void clearView() {
        LayerPane layerPane = getLayerPane();
        getMapManager().clearMap();
        layerPane.removeTop();
        layerPane.setBottomDefault();
    }

    /**
     * gets a user-input {@link String} from a {@link JOptionPane}-input dialog
     *
     * @param msg is the input dialog message
     * @param initValue is the initial value in the input text field
     * @return the user input as a {@link String} or "" if the input is null
     */
    @NotNull
    public String getUserInput(@NotNull String msg, @NotNull Number initValue) {
        String input;
        return (input = JOptionPane.showInputDialog(msg, initValue)) == null ? "" : input;
    }

    /**
     * sets the visibility of the loading screen,
     * a loading-cycle gif, which is the
     * over-top component of the {@link LayerPane}
     *
     * @param show is the screen visibility
     */
    public void showLoadingScreen(boolean show) {
        this.getLayerPane().showOverTop(show);
    }

    /**
     * unselects all {@link JMenuBar} items
     */
    public void unselectMenuBar() {
        window.getJMenuBar().setSelected(null);
    }

    /**
     * sets the {@link UserInterface} to fullscreen mode
     *
     * @param fullScreen is the full-screen flag, enabled if true, else disabled
     */
    public void setFullScreen(boolean fullScreen) {
        JFrame w = this.window;
        w.dispose();
        w.setUndecorated(fullScreen);
        w.setExtendedState(fullScreen ? Frame.MAXIMIZED_BOTH : Frame.NORMAL);
        if (!fullScreen) {
            w.setSize(w.getPreferredSize());
            w.setLocationRelativeTo(null);
        }
        w.setVisible(true);
    }

    /**
     * getter for the fullscreen-enabled flag
     *
     * @return true if the fullscreen-mode is enabled, else false
     */
    public boolean isFullscreen() {
        return window.getExtendedState() == Frame.MAXIMIZED_BOTH && this.window.isUndecorated();
    }

    /**
     * getter for the map manager
     *
     * @return the UI-{@link MapManager}
     */
    @NotNull
    public MapManager getMapManager() {
        return this.mapManager;
    }

    /**
     * getter for the map viewer
     *
     * @return the map viewer of {@link TreasureMap} type
     */
    @NotNull
    public TreasureMap getMap() {
        return this.mapManager.getMapViewer();
    }

    /**
     * getter for the layer pane which includes the view layers
     *
     * @return the UI-{@link LayerPane}
     */
    @NotNull
    public LayerPane getLayerPane() {
        return this.layerPane;
    }

    /**
     * getter for the main window
     *
     * @return main window of the {@link JFrame} type
     */
    @NotNull
    public JFrame getWindow() {
        return this.window;
    }

    /**
     * getter for search panel
     *
     * @return the UI-{@link SearchPane}
     */
    @NotNull
    public SearchPane getSearchPanel() {
        return this.searchPanel;
    }

    /**
     * getter for the selected file from file chooser
     *
     * @return the selected {@link File} or null, if nothing is selected
     */
    @Nullable
    public File getSelectedFile() {
        JFileChooser fileChooser = MenuModels.fileLoader(getWindow());
        return fileChooser.getSelectedFile();
    }

    /**
     * setter for view type
     *
     * @param type is the {@link ViewType} to set
     */
    public void setViewType(@NotNull ViewType type) {
        this.currentViewType = type;
    }

    /**
     * getter for current view type
     *
     * @return the current {@link ViewType}
     */
    @NotNull
    public ViewType getCurrentViewType() {
        return currentViewType;
    }

    /**
     * getter for settings panel
     *
     * @return the UI-{@link SettingsPane}
     */
    @NotNull
    public SettingsPane getSettings() {
        return settings;
    }
}

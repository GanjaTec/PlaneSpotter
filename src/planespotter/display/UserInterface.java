package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.constants.SearchType;
import planespotter.constants.ViewType;
import planespotter.constants.Warning;
import planespotter.controller.ActionHandler;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Flight;
import planespotter.display.models.*;
import planespotter.model.nio.LiveLoader;

import javax.swing.*;
import java.io.File;

// this is a new GUI test
public class UserInterface {

    private static boolean warningShown;

    @NotNull
    private final JFrame window;

    @NotNull
    private final LayerPane layerPane;

    @NotNull
    private final SearchPane searchPanel;

    @NotNull
    private final InfoPane infoPanel;

    @NotNull
    private final SettingsPane settings;

    @NotNull
    private final MapManager mapManager;

    @NotNull
    private ViewType currentViewType;

    public UserInterface(@NotNull ActionHandler actionHandler) {

        PaneModels paneModels = new PaneModels();
        this.window = paneModels.windowFrame(actionHandler);
        this.layerPane = new LayerPane(window.getSize());
        this.mapManager = new MapManager(this, actionHandler);
        this.searchPanel = new SearchPane(this.layerPane, actionHandler);
        this.infoPanel = new InfoPane(this.layerPane);
        this.settings = new SettingsPane(this.window, actionHandler);
        this.window.add(this.layerPane);

        this.layerPane.setDefaultBottomComponent(this.getMap());
        this.layerPane.setBottomDefault();

        this.currentViewType = ViewType.MAP_LIVE;
        LiveLoader.setLive(true);
    }

    public void showInfo(@NotNull Flight flight, @NotNull DataPoint dataPoint) {
        int x = 0, y = 0, width = 270, height = this.layerPane.getHeight();
        InfoPane infoPane = InfoPane.of(this.layerPane, flight, dataPoint);
        this.layerPane.addTop(infoPane, x, y, width, height);
        //this.layerPane.moveTop(x - width, y, width, height, x, y, 1000);
    }

    public void showSearch(@NotNull SearchType type) {
        int x = searchPanel.getX(),
            y = searchPanel.getY(),
            width = searchPanel.getWidth(),
            height = searchPanel.getHeight();
        this.getLayerPane().addTop(this.searchPanel, x, y, width, height);
        this.searchPanel.setVisible(!this.searchPanel.isVisible());
        this.searchPanel.showSearch(type);
        // trying to animate components
        //this.layerPane.moveTop(x-100, y-100, width, height, x, y, 2000);
    }

    public void showSettings() {
        this.settings.setVisible(true);
    }

    public void showWarning(@NotNull Warning type) {
        this.showWarning(type, null);
    }

    /**
     * shows a specific warning dialog
     *
     * @param type is the warning type which contains the warning message
     */
    public void showWarning(@NotNull Warning type, @Nullable String addTxt) {
        if (!warningShown) {
            var message = type.message();
            if (addTxt != null) {
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

    @NotNull
    public MapManager getMapManager() {
        return this.mapManager;
    }

    @NotNull
    public TreasureMap getMap() {
        return this.mapManager.getMapViewer();
    }

    @NotNull
    public LayerPane getLayerPane() {
        return this.layerPane;
    }

    @NotNull
    public JFrame getWindow() {
        return this.window;
    }

    @NotNull
    public SearchPane getSearchPanel() {
        return this.searchPanel;
    }

    @Nullable
    public File getSelectedFile() {
        JFileChooser fileChooser = MenuModels.fileLoader(this.getWindow());
        return fileChooser.getSelectedFile();
    }

    public void setViewType(@NotNull ViewType type) {
        this.currentViewType = type;
    }

    @NotNull
    public ViewType getCurrentViewType() {
        return currentViewType;
    }

    @NotNull
    public SettingsPane getSettings() {
        return settings;
    }
}

package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import planespotter.constants.Images;
import planespotter.constants.SearchType;
import planespotter.constants.ViewType;
import planespotter.constants.Warning;
import planespotter.controller.ActionHandler;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Flight;
import planespotter.display.models.*;
import planespotter.model.ConnectionManager;
import planespotter.model.Scheduler;
import planespotter.util.Utilities;
import planespotter.util.math.MathUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static planespotter.constants.Images.*;

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
    public static final Font DEFAULT_FONT = new Font("DialogInput", Font.BOLD, 16);

    // main window, contains all components
    private final JFrame window;

    // layer pane, contains all view layers and layer-operations
    private final LayerPane layerPane;

    // search panel for data search
    private final SearchPane searchPane;

    // settings panel, contains all settings
    private final SettingsPane settings;

    // connection pane, for connection managing
    private final ConnectionPane connectionPane;

    // data upload pane
    private final UploadPane uploadPane;

    // map manager, manages map operations
    private final MapManager mapManager;

    // dev tools view
    @Nullable private DevToolsView devToolsView;

    // the current view type
    private ViewType currentViewType;

    // indicates if a warning is shown at the moment
    private boolean warningShown;

    /**
     * the {@link UserInterface} constructor,
     * creates a full UI with all its components
     *
     * @param actionHandler is the {@link ActionHandler} which handles all the actions
     */
    public UserInterface(@NotNull ActionHandler actionHandler, @NotNull TileSource defaultMapSource, @NotNull String title, @NotNull ConnectionManager connectionManager) {

        this.window = windowFrame(actionHandler, title);
        this.layerPane = new LayerPane(window.getSize());
        this.mapManager = new MapManager(this, actionHandler, defaultMapSource);
        this.searchPane = new SearchPane(this.layerPane, actionHandler);
        this.settings = new SettingsPane(this.window, actionHandler);
        this.connectionPane = new ConnectionPane(window, actionHandler, connectionManager);
        this.uploadPane = new UploadPane(this.window, "Data Upload", null, null, null);
        this.window.add(this.layerPane);

        this.layerPane.setDefaultBottomComponent(getMap());
        this.layerPane.setDefaultOverTopComponent(loadingScreen());
        this.layerPane.setBottomDefault();

        this.devToolsView = null;

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
        // component animation
        layerPane.move(infoPane, LayerPane.MoveDirection.RIGHT, x-200, y, 200);
    }

    /**
     * shows a search panel by certain search type
     *
     * @param type is the {@link SearchType}
     */
    public void showSearch(@NotNull SearchType type) {
        int x      = 10,
            y      = 175,
            width  = 250,
            height = searchPane.getHeight();
        layerPane.addTop(searchPane, x, y, width, height);
        searchPane.setVisible(!searchPane.isVisible());
        searchPane.showSearch(type);
        // component animation
        layerPane.move(searchPane, LayerPane.MoveDirection.RIGHT, x-200, y, 200);
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
     * sets the {@link UserInterface} visibility
     *
     * @param show indicates if the UI should be shown or not
     */
    public void setVisible(boolean show) {
        getWindow().setVisible(show);
    }

    /**
     * getter for {@link UserInterface} visibility
     *
     * @return true if the UI is visible, else false
     */
    public boolean isVisible() {
        return getWindow().isVisible();
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
    public boolean isFullScreen() {
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
    public SearchPane getSearchPane() {
        return this.searchPane;
    }

    /**
     * getter for the selected file from file chooser
     *
     * @return the selected {@link File} or null, if nothing is selected
     */
    @Nullable
    public File getSelectedFile() {
        JFileChooser fileChooser = showFileLoader(getWindow());
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

    /**
     * getter for {@link ConnectionPane}
     *
     * @return the UI-{@link ConnectionPane}
     */
    @NotNull
    public ConnectionPane getConnectionPane() {
        return connectionPane;
    }

    /**
     * constructs the window-{@link JFrame} with all its components
     *
     * @param listener is the {@link ActionHandler} that handles all UI-interactions
     * @param title is the window title
     * @return the main UI window
     */
    @NotNull
    private JFrame windowFrame(@NotNull ActionHandler listener, @NotNull String title) {
        // getting main window object
        JFrame window = new JFrame(title);
        // setting window start size and preferred size
        Dimension size = new Dimension(1280, 720);
        window.setSize(size);
        window.setPreferredSize(size);
        // setting default close operation, do nothing for external exit action
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // setting window location relative to null
        window.setLocationRelativeTo(null);
        // component listener for component resize
        window.addComponentListener(listener);
        // window listener for window actions like open/close
        window.addWindowListener(listener);
        // setting plane icon as window-icon
        window.setIconImage(FLYING_PLANE_ICON.get().getImage());
        // first setting to not-visible
        window.setVisible(false);
        // returning window
        window.setJMenuBar(topMenuBar(listener));

        return window;
    }

    /**
     * creates the top menu bar for the UI window
     *
     * @param actionHandler is the {@link ActionHandler} which handles all click actions
     * @return the top menu bar
     */
    @NotNull
    private JMenuBar topMenuBar(@NotNull final ActionHandler actionHandler) {
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

        JMenuItem devTools = new JMenuItem("DevTools");
        devTools.addMouseListener(actionHandler);
        devTools.setFont(DEFAULT_FONT);
        helpMenu.add(devTools);

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
                heatMapMenu,
                new JMenuItem("Flight-Simulation")
        };
        JMenuItem[] heatMapItems = new JMenuItem[] {
                new JMenuItem("Position-HeatMap"),
                new JMenuItem("Coming soon...")
        };
        JMenuItem[] supplierItems = new JMenuItem[] {
                new JMenuItem("Run Supplier", Images.PLANE_ICON_16x.get()),
                new JMenuItem("Upload to Server (BETA)", ANTENNA_ICON_16x.get()),
                new JMenuItem("Source Manager", ANTENNA_ICON_16x.get())
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
     * start screen animation, shows the start screen and increases its opacity
     *
     * @param sec is the animation time
     */
    public synchronized void startScreenAnimation(int sec) {
        ImageIcon img = Utilities.scale(START_SCREEN.get(), 800, 100);
        JLabel label = new JLabel(img);
        label.setSize(img.getIconWidth(), img.getIconHeight());
        label.setOpaque(false);
        label.setLayout(null);

        JDialog dialog = new JDialog();
        dialog.add(label);
        dialog.setSize(label.getSize());
        dialog.setLocationRelativeTo(null);
        dialog.setUndecorated(true);
        dialog.setOpacity(0.0f);
        dialog.setVisible(true);
        // easy animation
        long millis = TimeUnit.SECONDS.toMillis(sec);
        long vel = MathUtils.divide(millis, 100L);
        float opc;
        for (int s = 0; s < millis; s += vel) {
            opc = dialog.getOpacity();
            dialog.setOpacity(opc + 0.01f);
            Scheduler.sleep(vel);
        }
        dialog.setVisible(false);
    }

    /**
     * the default UI-loading screen
     *
     * @return UI loading screen as {@link JLabel}
     */
    @NotNull
    public JLabel loadingScreen() {
        ImageIcon img = LOADING_CYCLE_GIF.get();
        JLabel label = new JLabel(img);
        label.setSize(img.getIconWidth(), img.getIconHeight());
        label.setOpaque(false);
        label.setLayout(null);

        return label;
    }

    /**
     * opens a {@link JFileChooser} with save dialog and returns it
     *
     * @param parent is the parent component
     * @param extensions are the allowed file extensions
     * @return created {@link JFileChooser}
     */
    public JFileChooser showFileSaver(JFrame parent, String... extensions) {
        File home = FileSystemView.getFileSystemView().getHomeDirectory();
        JFileChooser fileChooser = new JFileChooser(home);
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(
                Arrays.toString(extensions)
                        .replaceAll("\\[", "")
                        .replaceAll("]", ""),
                Arrays.stream(extensions)
                        .map(s -> s.replaceAll("\\.", ""))
                        .toArray(String[]::new));
        fileChooser.setFileFilter(fileFilter);
        fileChooser.showSaveDialog(parent);

        return fileChooser;
    }

    /**
     * opens a {@link JFileChooser} window and returns the {@link JFileChooser} for load dialog
     *
     * @param parent is the parent component
     * @return created {@link JFileChooser}
     */
    public JFileChooser showFileLoader(JFrame parent) {
        File home = FileSystemView.getFileSystemView().getHomeDirectory();
        JFileChooser fileChooser = new JFileChooser(home);
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(".pls, .bmp", "pls", "bmp");
        fileChooser.setFileFilter(fileFilter);
        fileChooser.showOpenDialog(parent);

        return fileChooser;
    }

    @Nullable
    public DevToolsView getDevToolsView() {
        return devToolsView;
    }

    public void showDevToolsView() {
        devToolsView = DevToolsView.getInstance();
        devToolsView.setVisible(true);
    }

    public UploadPane getUploadPane() {
        return uploadPane;
    }
}

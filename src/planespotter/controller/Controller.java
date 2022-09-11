package planespotter.controller;

import libs.ZoomPane;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSourceInfo;

import planespotter.constants.*;
import planespotter.display.models.ConnectionPane;
import planespotter.display.models.PaneModels;
import planespotter.model.io.DBIn;
import planespotter.model.io.FileWizard;
import planespotter.model.nio.ADSBSupplier;
import planespotter.model.nio.FilterManager;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.nio.DataLoader;
import planespotter.throwables.*;
import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.display.models.MenuModels;
import planespotter.model.*;
import planespotter.model.io.DBOut;
import planespotter.statistics.Statistics;
import planespotter.util.Bitmap;
import planespotter.util.math.MathUtils;
import planespotter.util.Utilities;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpTimeoutException;
import java.nio.file.FileAlreadyExistsException;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

import static planespotter.constants.DefaultColor.DEFAULT_MAP_ICON_COLOR;
import static planespotter.constants.Sound.SOUND_DEFAULT;
import static planespotter.constants.ViewType.*;

/**
 * @name Controller
 * @author jml04
 * @author Lukas
 * @author Bennet
 * @version 1.2
 *
 * @description
 * The {@link Controller} is the programs main-class, it connects
 * the view with the model and holds some important instances,
 * also a static instance of itself and of the VM ({@link Runtime})
 */
public abstract class Controller implements ExceptionHandler {

    // -- static fields --

    // runtime instance
    public static final Runtime RUNTIME;

    // project root path / working directory
    public static final String ROOT_PATH;

    // ONLY Controller instance
    private static final Controller INSTANCE;

    // static initializer
    static {
        // initializing 'root' members
        RUNTIME = Runtime.getRuntime();
        INSTANCE = new Controller() {};
        ROOT_PATH = Utilities.getAbsoluteRootPath();
    }

    // -- instance fields --

    // hash code
    private final int hashCode;

    // already-clicking/-initialized flag
    private boolean clicking, initialized, terminated;

    // boolean loading is true when something is loading
    private volatile boolean loading;

    // loadedData contains all loaded DataPoints
    private volatile Vector<DataPoint> loadedData;

    // liveData contains all loaded live-Flights
    private volatile Vector<Flight> liveData;

    // scheduler, contains executor services / thread pools
    @NotNull private final Scheduler scheduler;

    // new graphical user interface
    @NotNull private final UserInterface ui;

    // live data thread
    @Nullable private Thread liveThread;

    // data loader instance (for live data tasks)
    @NotNull private final DataLoader dataLoader;

    // configuration instance
    @NotNull private final Configuration config;

    // connection manager, manages user-input connections
    @NotNull private final ConnectionManager connectionManager;

    // search object for DB-search operations
    @NotNull private final Search search;

    // 'ADSBSupplier / Fr24Supplier enabled' flag
    private boolean adsbEnabled, fr24Enabled;

    /**
     * private constructor for Controller main instance,
     * get instance with Controller.getInstance()
     */
    private Controller() {
        this.hashCode = System.identityHashCode(INSTANCE);
        this.config = new Configuration();
        initConfig();
        this.scheduler = new Scheduler();
        this.dataLoader = new DataLoader();
        this.search = new Search();
        this.connectionManager = new ConnectionManager(Configuration.CONNECTIONS_FILENAME);
        this.ui = new UserInterface(ActionHandler.getActionHandler(),
                                    (TileSource) config.getProperty("currentMapSource"),
                                    (String) config.getProperty("title"),
                                    this.connectionManager);
        this.clicking = false;
        this.terminated = false;
        this.adsbEnabled = false;
        this.fr24Enabled = true;
    }

    /**
     * getter for the static {@link Controller} instance
     *
     * @return the controller instance
     */
    @NotNull
    public static Controller getInstance() {
        return INSTANCE;
    }

    /**
     * starts the program by initializing the controller
     * and opening an {@link UserInterface} (window)
     */
    public synchronized void start() {
        Thread animation = scheduler.shortTask(() -> PaneModels.startScreenAnimation(2));
        initialize();
        try {
            scheduler.await(animation);
        } catch (InterruptedException ignored) {
        } finally {
            done(true);
        }
        getUI().getWindow().setVisible(true);
    }

    /**
     * initializes the {@link Configuration} class and
     * its constant which are saved in a {@link HashMap},
     * you can add your own properties here or with
     * {@link Configuration}.setProperty(key, value) if needed
     */
    private void initConfig() {
        // initializing static properties
        config.setProperty("title", "PlaneSpotter v0.3");
        config.setProperty("threadKeepAliveTime", 4L);
        config.setProperty("maxThreads", 80);
        config.setProperty("saveLogs", false);
        config.setProperty("mapBaseUrl", "https://a.tile.openstreetmap.de");
        config.setProperty("fr24RequestUri", "https://data-live.flightradar24.com/");
        config.setProperty("bingMap", new BingAerialTileSource());
        config.setProperty("transportMap", new OsmTileSource.TransportMap());
        config.setProperty("openStreetMap", new TMSTileSource(new TileSourceInfo("OSM", (String) config.getProperty("mapBaseUrl"), "0")));

        // initializing user properties
        FileWizard fileWizard = FileWizard.getFileWizard();
        Object[] values = null;
        try {
            values = fileWizard.readConfig(Configuration.CONFIG_FILENAME);

        } catch (Exception e) {
            // catching all exceptions here to prevent ExceptionInInitializerError
            // printing stack trace for full exception information
            e.printStackTrace();
        } finally {
            if (values == null || values.length != 4) {
                values = new Object[] {50000, TreasureMap.OPEN_STREET_MAP, 6, 12};
            }
            config.setProperty("dataLimit", values[0]);
            config.setProperty("currentMapSource", values[1]);
            config.setProperty("gridSizeLat", values[2]);
            config.setProperty("gridSizeLon", values[3]);
        }

        // should also be saved in 'filters.psc', not static
        FilterManager collectorFilterManager = new FilterManager()
                .add("RCH").add("DUKE").add("FORTE").add("CASA").add("VIVI")
                .add("EYE").add("NCR").add("LAGR").add("SNIPER").add("VALOR")
                .add("MMF").add("HOIS").add("K35R").add("SONIC").add("Q4");
        config.setProperty("collectorFilters", collectorFilterManager);
    }

    /**
     * initializes the controller by creating a new Logger
     * and initializing the Main-Tasks and Executors
     */
    private void initialize() {
        if (!this.initialized) {
            // trying to add tray icon to system tray
            Utilities.addTrayIcon(Images.PLANE_ICON_16x.get().getImage(), e -> {
                JFrame window = getUI().getWindow();
                window.setVisible(!window.isVisible());
            });
            // testing connections
            try {
                URL[] urls = new URL[] {
                        new URL((String) config.getProperty("fr24RequestUri")),
                        new URL((String) config.getProperty("mapBaseUrl"))
                };
                Utilities.connectionPreCheck(2000, urls);
            } catch (IOException | Fr24Exception e) {
                handleException(e);
            }
            // starting tasks and finishing
            boolean onlyMilitary = false;
            setAdsbEnabled(false);
            liveThread = scheduler.runThread(() -> dataLoader.runTask(this, onlyMilitary), "Live-Data Loader", true, Scheduler.HIGH_PRIO);
            this.initialized = true;
            show(MAP_LIVE);
        }
    }

    /**
     * executed on shutdown
     * shuts down the program if the uses chooses so
     * executes last tasks and closes the program carefully
     *
     * @param insertRemainingFrames indicates if the remaining loaded live-frames
     *                              should be inserted or removed
     */
    public synchronized void shutdown(boolean insertRemainingFrames) {
        // confirm dialog for shutdown
        int option = JOptionPane.showConfirmDialog(getUI().getWindow(),
                "Do you really want to exit PlaneSpotter?",
                    "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option != JOptionPane.YES_OPTION) {
            return; // no-option case, no shutdown, abort method
        }
        getUI().showLoadingScreen(true);
        // disabling tasks
        dataLoader.setLive(false);
        if (liveThread != null && liveThread.isAlive()) {
            liveThread.interrupt();
        }
        setLoading(true);
        // saving the configuration in 'config.psc'
        FileWizard fileWizard = FileWizard.getFileWizard();
        try {
            fileWizard.writeConfig(config, Configuration.CONFIG_FILENAME);
        } catch (IOException ioe) {
            handleException(ioe);
        }
        try {
            fileWizard.writeConnections(Configuration.CONNECTIONS_FILENAME, getConnectionManager());
        } catch (IOException | ExtensionException e) {
            handleException(e);
        }
        // saving log, if option is enabled
        if ((boolean) getConfig().getProperty("saveLogs")) {
            fileWizard.saveLogFile("a_log", "[DEBUG] No output text yet...");
        }
        // inserting remaining frames, if option is enabled
        DBIn dbIn = DBIn.getDBIn();
        if (insertRemainingFrames && dbIn.isEnabled()) {
            try {
                dbIn.insertRemaining(scheduler, dataLoader);
            } catch (NoAccessException | DataNotFoundException ignored) {
            }
        }
        // busy-waiting for remaining tasks
        while (scheduler.active() > 0) {
            Thread.onSpinWait();
        }
        // disabling last tasks
        dbIn.setEnabled(false);
        done(true);
        this.terminated = true;
        // shutting down scheduler
        boolean shutdown = scheduler.shutdown(1);
        byte exitStatus = MathUtils.toBinary(shutdown);
        // shutting down the VM
        RUNTIME.exit(exitStatus);

    }

    /**
     * shuts down the program directly, won't wait for any tasks or shutdown hooks,
     * saves a log with the problematic error, if there is one.
     *
     * Note: Only use with caution, changes could be deleted!
     *
     * @param problem is the problematic error (may be null)
     */
    private void emergencyShutdown(@Nullable Throwable problem) {
        // printing error stacktrace
        if (problem != null) {
            problem.printStackTrace();
        }
        // saving last error log
        FileWizard.getFileWizard().saveLogFile("ps-crash", System.out.toString());
        // shutting down VM directly, won't wait for any tasks
        RUNTIME.halt(-1);
    }

    /**
     * executed when a loading process is done, will turn
     * loading-flag to false and play a sound if wanted
     *
     * @param playSound indicates if the method should play a sound at the end
     */
    public void done(boolean playSound) {
        try {
            this.loading = false;
            if (playSound) {
                Utilities.playSound(SOUND_DEFAULT.get());
            }
        } finally {
            getUI().showLoadingScreen(false);
        }
    }

    /**
     * starts a {@link Fr24Collector} to collect Fr24-Data
     * @see planespotter.model.Fr24Collector
     * @see
     *
     * @param insertMode is the insert mode TODO ....
     */
    // TODO: 08.08.2022 add filters, INSERT MODE
    void runCollector(int insertMode) {
        Collector<Fr24Supplier> collector = new Fr24Collector(false, false, 6, 12);
        collector.start();
    }

    /**
     * creates a UI-view for a specific view-type and specific data
     *
     * @param type is the ViewType, sets the content type for the
     *             created view (e.g. different List-View-Types)
     */
    public synchronized void show(@NotNull ViewType type) {

        MapManager mapManager = getUI().getMapManager();
        DBOut dbOut = DBOut.getDBOut();

        try {
            setLoading(true);
            getUI().showLoadingScreen(true);

            if (type != MAP_LIVE) {
                dataLoader.setLive(false);
            }

            getUI().setViewType(type);
            getUI().getMapManager().clearMap();

            switch (type) {
                case LIST_FLIGHT -> { /*removed Flight-List implementation*/ }
                case MAP_LIVE -> dataLoader.setLive(true);
                case MAP_TRACKING -> mapManager.createTrackingMap(getDataList(), null, true);
                case MAP_TRACKING_NP -> mapManager.createTrackingMap(getDataList(), null, false);
                case MAP_FROMSEARCH -> mapManager.createSearchMap(getDataList(), false);
                // significance map should be improved
                //case MAP_SIGNIFICANCE -> showSignificanceMap(mapManager, dbOut);
                // heatmap should be implemented
                case MAP_HEATMAP -> showBitmap(null, null);
            }
        } finally {
            done(false);
        }
    }

    /**
     * search method for different data from the DB
     *
     * @param inputs are the inputs in the search fields
     * @param button is the clicked search button, 0 = LIST, 1 = MAP
     */
    // TODO: 24.05.2022 DEBUG PLANE SEARCH, AIRLINE SEARCH
    // TODO: 29.08.2022 plane, airline search , flights: flightNr
    public void search(@NotNull String[] inputs, int button) {

        try {
            Utilities.checkInputs(inputs);
        } catch (IllegalInputException e) {
            handleException(e);
            return;
        }

        if (button == 1) {
            getUI().showLoadingScreen(true);
            setLoading(true);

            SearchType currentSearchType = getUI().getSearchPane().getCurrentSearchType();
            ViewType showType;
            try {
                showType = switch (currentSearchType) {
                    case AIRLINE -> {
                        setDataList(search.forAirline(inputs));
                        yield MAP_TRACKING_NP;
                    }
                    case AIRPORT -> {
                        setDataList(search.forAirport(inputs));
                        yield MAP_TRACKING_NP;
                    }
                    case FLIGHT -> {
                        setDataList(search.forFlight(inputs));
                        yield MAP_TRACKING;
                    }
                    case PLANE -> {
                        setDataList(search.forPlane(inputs));
                        yield MAP_FROMSEARCH;
                    }
                    default -> null;
                };
                if (showType != null) {
                    show(showType);
                }
            } catch (DataNotFoundException dnf) {
                handleException(dnf);
            } finally {
                done(false);
            }
        }
    }

    /**
     * confirms and saves the changed user settings
     * in the 'configuration.psc' file
     *
     * @param data are the settings values, [0], [1] and [2] must be filled
     */
    public void confirmSettings(@NotNull String... data) {

        if (data.length != 3) {
            throw new InvalidDataException("Settings data is invalid!");
        }

        int dataLimit, livePeriodSec; TileSource currentMapSource;

        getUI().showLoadingScreen(true);
        try {
            // data[0]
            dataLimit = Integer.parseInt(data[0]);
            getConfig().setProperty("dataLimit", dataLimit);
            // data[1]
            currentMapSource = switch (data[1]) {
                case "Bing Map" -> TreasureMap.BING_MAP;
                case "Transport Map" -> TreasureMap.TRANSPORT_MAP;
                default /*"Open Street Map"*/ -> TreasureMap.OPEN_STREET_MAP;
            };
            getConfig().setProperty("currentMapSource", currentMapSource);
            getUI().getMap().setTileSource(currentMapSource);
            // data[2]
            livePeriodSec = Integer.parseInt(data[2]);
            dataLoader.setLiveDataPeriod(livePeriodSec);
        } finally {
            // saving config after reset
            try {
                FileWizard.getFileWizard().writeConfig(config, Configuration.CONFIG_FILENAME);
            } catch (IOException ioe) {
                handleException(ioe);
            }
            done(false);
        }
    }

    /**
     * executed when the live map is clicked, goes through all {@link MapMarker}s,
     * chooses the clicked one, if there is one and does further action
     *
     * @param clickedCoord is the clicked {@link Coordinate}
     * @return boolean if a {@link MapMarker} was hit
     */
    // TODO: 14.08.2022 move to MapManager
    boolean onLiveClick(@NotNull ICoordinate clickedCoord) {

        TreasureMap map;
        List<MapMarker> markers;
        MapManager mapManager;
        List<MapMarker> newMarkerList;
        boolean markerHit = false;
        int counter;

        if (!this.clicking && dataLoader.isLive()) {
            try {
                this.clicking = true;
                map = getUI().getMap();
                markers = map.getMapMarkerList();
                mapManager = getUI().getMapManager();
                newMarkerList = new ArrayList<>();
                counter = 0;
                PlaneMarker newMarker;
                Coordinate markerCoord;
                int heading = 0;
                for (MapMarker marker : markers) {
                    if (marker instanceof PlaneMarker pm) {
                        heading = pm.getHeading();
                    }
                    markerCoord = marker.getCoordinate();
                    if (!markerHit && mapManager.isMarkerHit(markerCoord, clickedCoord)) {
                        markerHit = true;
                        newMarker = new PlaneMarker(markerCoord, heading, true, true);
                        onMarkerHit(MAP_LIVE, newMarker, counter, null, null);
                    } else {
                        newMarker = new PlaneMarker(markerCoord, heading, true, false);
                        newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
                    }
                    newMarker.setName(marker.getName());
                    newMarkerList.add(newMarker);
                    counter++;
                }
                if (markerHit) {
                    getUI().getMap().setMapMarkerList(newMarkerList);
                }
            } finally {
                this.clicking = false;
            }
        }
        return markerHit;
    }

    /**
     * is executed when a map marker is clicked and the current is MAP_ALL,
     * chooses the clicked {@link MapMarker}, if there is one and does further action
     */
    // TODO: 09.09.2022 debug, improve
    // TODO: 14.08.2022 move to mapManager
    boolean onClick_all(@NotNull ICoordinate clickedCoord) { // TODO aufteilen

        List<MapMarker> mapMarkers; List<MapMarker> newMarkerList; Vector<DataPoint> data;
        Coordinate markerCoord; PlaneMarker newMarker; MapManager mapManager; DBOut dbOut; Position pos;
        boolean markerHit = false;
        int counter, heading;

        if (!this.clicking) {
            try {
                this.clicking = true;
                mapMarkers = getUI().getMap().getMapMarkerList();
                newMarkerList = new ArrayList<>();
                mapManager = getUI().getMapManager();
                counter = 0;
                dbOut = DBOut.getDBOut();
                if ((data = getDataList()) == null) {
                    return false;
                }
                // going through markers
                for (MapMarker m : mapMarkers) {
                    markerCoord = m.getCoordinate();
                    pos = Position.parsePosition(markerCoord);
                    heading = data.get(counter).heading();
                    if (mapManager.isMarkerHit(markerCoord, clickedCoord) && !markerHit) {
                        markerHit = true;
                        newMarker = PlaneMarker.fromPosition(pos, heading, true, true);
                        onMarkerHit(MAP_FROMSEARCH, newMarker, counter, data, dbOut);
                    } else {
                        newMarker = PlaneMarker.fromPosition(pos, heading, true, false);
                    }
                    //newMarker.setName(m.getName());
                    newMarkerList.add(newMarker);
                    counter++;
                }
                if (markerHit) {
                    getUI().getMap().setMapMarkerList(newMarkerList);
                }
            } finally {
                this.clicking = false;
            }
        }
        return markerHit;
    }

    /**
     * executed when a clicked {@link MapMarker} was found, further action is done here
     * like changing the clicked {@link MapMarker} and showing information
     *
     * @param viewType is the current {@link ViewType}
     * @param marker is the clicked {@link MapMarker}
     * @param counter is the {@link MapMarker} index in the marker list (from {@link TreasureMap})
     * @param dataPoints is the current {@link DataPoint} list
     * @param dbOut is the {@link DBOut} instance
     */
    // TODO: 09.09.2022 improve
    public void onMarkerHit(ViewType viewType, @NotNull PlaneMarker marker,
                             int counter, Vector<DataPoint> dataPoints,
                             DBOut dbOut) {

        Flight flight = null;
        int flightID;
        DataPoint dataPoint;

        switch (viewType) {
            case MAP_FROMSEARCH -> {
                marker.setBackColor(Color.RED);
                dataPoint = dataPoints.get(counter);
                flightID = dataPoint.flightID();
                try {
                    flight = dbOut.getFlightByID(flightID);
                    getUI().showInfo(flight, dataPoint);
                } catch (DataNotFoundException ignored) {
                }
            }
            case MAP_LIVE -> {
                marker.setBackColor(Color.RED);
                flight = getLiveDataList().get(counter);
                dataPoint = flight.dataPoints().get(0);
                getUI().showInfo(flight, dataPoint);
            }
        }
        if (flight != null) {
            getUI().getMapManager().setSelectedICAO(flight.plane().icao());
        }
    }

    /**
     * executed when the tracking map is clicked,
     * chooses the clicked {@link MapMarker}, if there is one and does further action
     *
     * @param clickedCoord is the clicked {@link Coordinate}
     */
    // TODO: 14.08.2022 move to MapManager
    boolean onTrackingClick(@NotNull ICoordinate clickedCoord) { // TODO aufteilen
        if (getDataList() == null) {
            return false;
        }
        TreasureMap map = getUI().getMap();
        List<MapMarker> markers = map.getMapMarkerList();
        MapManager mapManager = getUI().getMapManager();
        Coordinate markerCoord;
        DataPoint dp;
        Flight flight;
        DBOut dbOut =  DBOut.getDBOut();
        int flightID, counter = 0;
        boolean markerHit = false;

        for (MapMarker m : markers) {
            markerCoord = m.getCoordinate();
            if (mapManager.isMarkerHit(markerCoord, clickedCoord)) {
                markerHit = true;
                dp = getDataList().get(counter);
                flightID = dp.flightID();
                map.setMapMarkerList(mapManager.resetTrackingMarkers(m));
                try {
                    flight = dbOut.getFlightByID(flightID);
                    getUI().showInfo(flight, dp);
                } catch (DataNotFoundException e) {
                    handleException(e);
                }
                break;
            }
            counter++;
        }
        return markerHit;
    }

    /**
     * Connects / disconnects a the current selected {@link planespotter.model.ConnectionManager.Connection}
     *
     * @param connect indicates if a {@link planespotter.model.ConnectionManager.Connection}
     *                should be connected or disconnected
     * @param mixWithFr24 indicates if the {@link planespotter.dataclasses.Frame}s, loaded by the
     *                    {@link planespotter.model.ConnectionManager.Connection} should be
     *                    mixed with {@link Fr24Frame}s
     */
    public void setConnection(boolean connect, boolean mixWithFr24) {
        ActionHandler onAction = ActionHandler.getActionHandler();
        ConnectionManager cmg = getConnectionManager();
        ConnectionManager.Connection selectedConn = cmg.getSelectedConn();
        try {
            cmg.disconnectAll();
            setAdsbEnabled(connect);
            setFr24Enabled(mixWithFr24);
            if (selectedConn != null) {
                selectedConn.setMixWithFr24(mixWithFr24);
                selectedConn.setConnected(connect);
                cmg.setSelectedConn(selectedConn.name);
            } else {
                cmg.setSelectedConn(null);
            }
            if (connect) {
                if (selectedConn == null) {
                    return;
                }
                URI uri = selectedConn.uri;
                System.out.println("Connecting to " + uri + "...");

                getConfig().setProperty("adsbRequestUri", uri);
                show(MAP_LIVE);
            }
        } finally {
            getUI().getConnectionPane().showConnection(connect ? selectedConn : null, onAction);
        }
    }

    /**
     * adds a {@link planespotter.model.ConnectionManager.Connection} to the {@link ConnectionManager}
     *
     * @param connPane is the {@link ConnectionPane} instance
     * @param connList is the {@link JList}, that contains all connections and also the selected one
     * @param model is the {@link ListModel} of the {@link JList}
     */
    public void addConnection(@NotNull ConnectionPane connPane, @NotNull JList<String> connList, @NotNull ListModel<String> model) {
        String[] input = connPane.getInput();
        int size = model.getSize();
        Vector<String> listData = new Vector<>(size);
        for (int i = 0; i < size; i++) {
            listData.add(model.getElementAt(i));
        }
        ConnectionManager connMngr = getConnectionManager();
        String key = input[0];
        if (key == null || key.isBlank()) {
            ui.showWarning(Warning.FIELDS_NOT_FILLED);
            return;
        }
        try {
            if (input.length == 2) {
                String uri = input[1];
                if (uri == null || uri.isBlank()) {
                    ui.showWarning(Warning.FIELDS_NOT_FILLED);
                    return;
                }
                connMngr.add(key, uri);
            } else if (input.length == 4) {
                String  host = input[1],
                        port = input[2],
                        path = input[3];

                if (    host == null   || port == null
                     || host.isBlank() || port.isBlank()) {
                    ui.showWarning(Warning.FIELDS_NOT_FILLED);
                    return;
                }
                if (path == null || path.isBlank()) {
                    path = null;
                }
                connMngr.add(key, host, port, path);
            }
            listData.add(key);
            connList.setListData(listData);
        } catch (KeyCheckFailedException ex) {
            ui.showWarning(Warning.NAME_NOT_UNIQUE);
        } catch (IllegalInputException e) {
            ui.showWarning(Warning.ILLEGAL_INPUT, "URI contains illegal expressions!");
        }
        connPane.closeAddDialog();
    }

    /**
     * removes {@link planespotter.model.ConnectionManager.Connection}s from
     * the {@link ConnectionPane} and the {@link ConnectionManager}
     *
     * @param connList is the {@link JList}, that contains all connections and also the selected one
     * @param selectedValues are the selected {@link JList} values to be removed
     * @param model is the {@link ListModel} of the {@link JList}
     */
    public void removeConnection(@NotNull JList<String> connList, @NotNull List<String> selectedValues, @NotNull ListModel<String> model) {
        int size = model.getSize();
        Vector<String> listData = new Vector<>(size);
        String val;
        for (int i = 0; i < size; i++) {
            val = model.getElementAt(i);
            if (!selectedValues.contains(val)) {
                listData.add(val);
            }
        }
        connList.setListData(listData);

        ConnectionManager connMngr = getConnectionManager();
        connMngr.setSelectedConn(null);
        selectedValues.forEach(connMngr::remove);
    }

    /**
     * tries to save the current view (bitmap or map viewer) in a file
     * file types: ('.bmp' / '.pls')
     */
    public void saveFile() {
        getUI().showLoadingScreen(true);
        setLoading(true);

        // TODO visible rect beim repainten speichern
        // TODO 2.Möglichkeit: center und zoom speichern
        try {
            ViewType currentViewType = getUI().getCurrentViewType();
            String extensions = currentViewType == MAP_HEATMAP ? ".bmp" : ".pls";
            JFileChooser fileChooser = MenuModels.fileSaver(getUI().getWindow(), extensions);
            Rectangle rect = /*(gui.getCurrentVisibleRect() != null) ? gui.getCurrentVisibleRect() :*/ null;
            File selected = fileChooser.getSelectedFile();
            if (selected == null) {
                done(false);
                return;
            }
            String warningMsg = "Couldn't write file, please check the file type!";
            switch (currentViewType) {
                case MAP_HEATMAP -> {
                    Component bottom = getUI().getLayerPane().getBottom();
                    if (selected.getName().endsWith(".bmp") && bottom instanceof ZoomPane zoomPane) {
                        // writing Bitmap image with FileWizard
                        Image bitmap = zoomPane.getContent();
                        FileWizard.getFileWizard().writeBitmapImg(bitmap, BufferedImage.TYPE_BYTE_GRAY, selected);
                    } else {
                        getUI().showWarning(Warning.INVALID_DATA, warningMsg + extensions);
                    }
                }
                case MAP_TRACKING, MAP_TRACKING_NP, MAP_FROMSEARCH, MAP_SIGNIFICANCE -> {
                    if (selected.getName().endsWith(".pls")) {
                        // new MapData object with loadedData, view type and visible rectangle
                        MapData mapData = new MapData(getDataList(), currentViewType, rect);
                        FileWizard.getFileWizard().savePlsFile(mapData, selected);
                    } else {
                        getUI().showWarning(Warning.INVALID_DATA, warningMsg + extensions);
                    }
                }
                default -> getUI().showWarning(Warning.NOT_SUPPORTED_YET, "These filetypes are not savable yet!");
            }
        } catch (DataNotFoundException | IOException e) {
            handleException(e);
        } finally {
            done(false);
        }
    }

    /**
     * loads a specific file, which the user selects in a file chooser,
     * if the filename ends with '.bmp' or '.pls', the file is loaded into the view
     */
    public void loadFile() {

        getUI().showLoadingScreen(true);
        try {
            File file = getUI().getSelectedFile();
            if (file == null) {
                return;
            }
            String filename = file.getName();
            if (filename.endsWith(".bmp")) {
                BufferedImage buf = ImageIO.read(file);
                showBitmap(null, buf);
                // TODO: 31.08.2022 show Bitmap from File or Image
            } else if (filename.endsWith(".pls")) {
                FileWizard fileWizard = FileWizard.getFileWizard();
                MapData loaded = fileWizard.loadPlsFile(file);
                setDataList(loaded.data());
                show(loaded.viewType());
            }
        } catch (InvalidDataException | IOException e) {
            handleException(e);
        } finally {
            done(false);
        }
    }

    /**
     * Overwritten method from {@link ExceptionHandler} interface.
     * Handles exceptions, if an exception occurs,
     * execute this method to handle it
     *
     * @param thr is the throwable (usually an exception)
     *            which is thrown and going to be handled
     */
    @Override
    public void handleException(final Throwable thr) {

        if (thr instanceof OutOfMemoryError oom) {
            getUI().showWarning(Warning.OUT_OF_MEMORY);
            emergencyShutdown(oom);

        } else if (thr instanceof SQLException sql) {
            String message = sql.getMessage();
            thr.printStackTrace();
            getUI().showWarning(Warning.SQL_ERROR, message);
            if (message.contains("BUSY")) {
                // emergency shutdown because of DB-Bug
                emergencyShutdown(sql);
            }

        } else if (thr instanceof DataNotFoundException dnf) {
            getUI().showWarning(Warning.NO_DATA_FOUND, dnf.getMessage());

        } else if (thr instanceof IOException ioe) {
            // handling for all I/O-exceptions
            if (ioe instanceof SSLHandshakeException ssl) {
                getUI().showWarning(Warning.HANDSHAKE, ssl.getMessage());
                dataLoader.setLive(false);
                DBIn dbIn = DBIn.getDBIn();
                dbIn.setEnabled(false);
                Scheduler.sleepSec(60);
                dbIn.setEnabled(true);
                dataLoader.setLive(true);

            } else if (ioe instanceof HttpTimeoutException) {
                getUI().showWarning(Warning.TIMEOUT, "Http Connection timed out!");

            } else if (ioe instanceof ConnectException cex) {
                getUI().showWarning(Warning.NO_CONNECTION, cex.getMessage());

            } else if (thr instanceof FileAlreadyExistsException fae) {
                getUI().showWarning(Warning.FILE_ALREADY_EXISTS, fae.getMessage());
                fae.printStackTrace();

            } else if (thr instanceof FileNotFoundException fnf) {
                getUI().showWarning(Warning.FILE_NOT_FOUND, fnf.getMessage());
            }

        } else if (thr instanceof TimeoutException) {
            getUI().showWarning(Warning.TIMEOUT);

        } else if (thr instanceof ExtensionException ext) {
            getUI().showWarning(Warning.WRONG_FILE_EXTENSION, ext.getMessage());

        } else if (thr instanceof RejectedExecutionException) {
            boolean terminated = thr.getMessage().contains("TERMINATED");
            getUI().showWarning(Warning.REJECTED_EXECUTION, terminated ? "Executor is terminated!" : "");

        } else if (thr instanceof IllegalInputException) {
            getUI().showWarning(Warning.ILLEGAL_INPUT);

        } else if (thr instanceof InvalidDataException ide) {
            getUI().showWarning(Warning.INVALID_DATA, ide.getMessage() + ((ide.getCause() != null) ? ("\n" + ide.getCause().getMessage()) : ""));
            ide.printStackTrace();

        } else if (thr instanceof ClassNotFoundException cnf) {
            getUI().showWarning(Warning.UNKNOWN_ERROR, cnf.getMessage());
            cnf.printStackTrace();

        } else if (thr instanceof NumberFormatException nfe) {
            getUI().showWarning(Warning.NUMBER_EXPECTED, nfe.getMessage());

        } else if (thr instanceof Fr24Exception frex) {
            if (frex.getMessage().endsWith("not reachable!")) {
                getUI().showWarning(Warning.URL_NOT_REACHABLE);
            }

        } else if (thr != null) {
            getUI().showWarning(Warning.UNKNOWN_ERROR, thr.getMessage());
            thr.printStackTrace();
        }
    }

    /**
     * shows the significance map for specific airports
     *
     * @param mapManager is the {@link MapManager} instance
     * @param dbOut is the {@link DBOut} instance
     */
    @Deprecated(since = "Bitmap")
    private void showSignificanceMap(@NotNull MapManager mapManager, @NotNull DBOut dbOut) {

        Deque<Airport> aps;
        Map<Airport, Integer> signifMap;
        Statistics stats = new Statistics();

        try {
            aps = dbOut.getAllAirports();
            signifMap = stats.airportSignificance(aps);
            mapManager.createSignificanceMap(signifMap, getUI().getMap());
        } catch (DataNotFoundException dnf) {
            handleException(dnf);
        }
    }

    /**
     * shows a {@link Bitmap} in the view, {@link Bitmap} can be created new with
     * gridSize and data {@link Vector} or can just be given as parameter ({@link Bitmap} or {@link BufferedImage})
     *
     * @param bitmap is the {@link Bitmap} object to be displayed, may be null
     * @param buf is the {@link BufferedImage} to be displayed, may be null
     */
    public void showBitmap(@Nullable Bitmap bitmap, @Nullable BufferedImage buf) {
        if (bitmap == null && buf == null) {
            String input = getUI().getUserInput("Please enter a grid size (0.025 - 2.0)", 0.5);
            if (input.isBlank()) {
                return;
            }
            scheduler.exec(() -> {
                try {
                    setLoading(true);
                    getUI().showLoadingScreen(true);
                    getUI().setViewType(MAP_HEATMAP);
                    float gridSize = Float.parseFloat(input);
                    if (gridSize < 0.025f || gridSize > 2.0f) {
                        getUI().showWarning(Warning.OUT_OF_RANGE, "grid size must be between 0.025 and 2.0!");
                        return;
                    }
                    Bitmap bmp = new Statistics().globalPositionBitmap(gridSize);
                    Diagrams.showPosHeatMap(getUI(), bmp);
                } catch (NumberFormatException nfe) {
                    getUI().showWarning(Warning.NUMBER_EXPECTED, "Please enter a float value (0.025 - 2.0)");
                } catch (DataNotFoundException | OutOfMemoryError e) {
                    handleException(e);
                } finally {
                    done(false);
                }
            }, "Loading Data", false, Scheduler.MID_PRIO, true);
        } else if (bitmap != null) {
            Diagrams.showPosHeatMap(getUI(), bitmap);
        } else {
            Diagrams.showPosHeatMap(getUI(), buf);
        }
    }

    /**
     * shows the {@link ConnectionPane}, where the {@link ConnectionManager} runs on
     */
    public void showConnectionManager() {
        ConnectionPane pane = getUI().getConnectionPane();
        pane.setVisible(true);
    }

    /**
     * getter for the {@link DataLoader} instance
     *
     * @return the {@link DataLoader} instance
     */
    @NotNull
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    /**
     * sets the loadedData-{@link Vector}
     *
     * @param loadedData is the loaded data, a {@link Vector} of {@link DataPoint}s
     */
    public void setDataList(@Nullable Vector<DataPoint> loadedData) {
        this.loadedData = loadedData;
    }

    /**
     * sets the liveData-{@link Vector}
     *
     * @param liveData is the live data, a {@link Vector} of {@link Flight}s
     */
    public void setLiveDataList(@NotNull Vector<Flight> liveData) {
        this.liveData = liveData;
    }

    /**
     * getter for the loadedData-{@link Vector} that contains all loaded {@link DataPoint}s
     *
     * @return the loadedData {@link Vector}
     */
    @Nullable
    public Vector<DataPoint> getDataList() {
        return this.loadedData;
    }

    /**
     * getter for the liveData-{@link Vector} that contains all live {@link Flight}s
     *
     * @return the liveData {@link Vector}
     */
    @NotNull
    public Vector<Flight> getLiveDataList() {
        return this.liveData;
    }

    /**
     * getter for the scheduler
     *
     * @return main scheduler instance
     */
    @NotNull
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * getter for the user interface
     *
     * @return main UserInterface instance
     */
    @NotNull
    public UserInterface getUI() {
        return this.ui;
    }

    /**
     * getter for {@link DataLoader} instance
     *
     * @return the {@link DataLoader} instance
     */
    @NotNull
    public DataLoader getLiveLoader() {
        return this.dataLoader;
    }

    /**
     * getter for the {@link Configuration} instance,
     * which contains all property constants
     *
     * @return the {@link Configuration} instance
     */
    @NotNull
    public Configuration getConfig() {
        return config;
    }

    /**
     * @return loading flag, true if Controller is loading something
     */
    public boolean isLoading() {
        return this.loading;
    }

    /**
     * sets the loading flag to a certain value
     *
     * @param b is the loading value ( true or false )
     */
    public void setLoading(boolean b) {
        this.loading = b;
    }

    /**
     * getter for 'terminated' flag
     *
     * @return true if the {@link Controller} is terminated, else false
     */
    public boolean isTerminated() {
        return this.terminated;
    }

    /**
     * getter for 'adsb enabled' flag
     *
     * @return true if adsb supplier is enabled, else false
     */
    public boolean isAdsbEnabled() {
        return this.adsbEnabled;
    }

    /**
     * sets the 'adsb enabled' flag to true
     *
     * @param adsbEnabled indicates if the {@link ADSBSupplier} should be enabled
     */
    public void setAdsbEnabled(boolean adsbEnabled) {
        this.adsbEnabled = adsbEnabled;
    }

    /**
     * getter for 'Fr24Supplier enabled' flag
     *
     * @return the Fr24Suppplier enabled' flag
     */
    public boolean isFr24Enabled() {
        return this.fr24Enabled;
    }

    /**
     * sets the 'Fr24Supplier enabled' flag
     *
     * @param fr24Enabled indicates if the 'Fr24Supplier enabled' flag should be enabled/disabled
     */
    public void setFr24Enabled(boolean fr24Enabled) {
        this.fr24Enabled = fr24Enabled;
    }

    /**
     * getter for the {@link ConnectionManager} instance
     *
     * @return the {@link ConnectionManager}
     */
    @NotNull
    public ConnectionManager getConnectionManager() {
        return this.connectionManager;
    }

    /**
     * getter for the Controller hash code
     *
     * @return controller hash code
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

}

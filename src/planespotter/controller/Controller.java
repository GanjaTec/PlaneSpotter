package planespotter.controller;

import de.gtec.util.bmp.Filler;
import de.gtec.util.math.WeightMovingAverage;
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
import planespotter.constants.props.Configuration;
import planespotter.constants.props.Property;
import planespotter.dataclasses.*;
import planespotter.dataclasses.Frame;
import planespotter.display.MapManager;
import planespotter.display.StatsView;
import planespotter.display.TreasureMap;
import planespotter.display.UserInterface;
import planespotter.display.models.*;
import planespotter.model.*;
import planespotter.model.io.*;
import planespotter.model.nio.ADSBSupplier;
import planespotter.model.nio.DataProcessor;
import planespotter.model.nio.FilterManager;
import planespotter.model.nio.client.DataUploader;
import planespotter.model.simulation.FlightSimulation;
import planespotter.model.simulation.Simulator;
import planespotter.throwables.*;
import planespotter.util.Bitmap;
import planespotter.util.Utilities;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpTimeoutException;
import java.nio.file.FileAlreadyExistsException;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static planespotter.constants.DefaultColor.DEFAULT_MAP_ICON_COLOR;
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
public final class Controller implements ExceptionHandler {

    // -- static fields --

    // runtime instance
    public static final Runtime RUNTIME;

    // project root path / working directory
    public static final String ROOT_PATH;

    // ONLY Controller instance
    private static final Controller INSTANCE;

    private static int initLevel;

    // static initializer
    static {
        // initializing 'root' members
        RUNTIME = Runtime.getRuntime();
        ROOT_PATH = Utilities.getAbsoluteRootPath();

        // initializing database file, recreating if invalid
        initDB();

        // initializing Controller singleton instance
        INSTANCE = new Controller();

        // TODO: 26.03.2023 DOWNLOAD DLL's
        Filler.setUseNativeFill(false); // my java code java seems to be faster here
        WeightMovingAverage.setUseNativeAvg(true);
    }

    // -- instance fields --

    // hash code
    private final int hashCode;

    // 'already-clicking' flag
    private boolean clicking, terminated;

    // loadedData contains all loaded DataPoints
    private volatile Vector<DataPoint> loadedData;

    // liveData contains all loaded live-Flights
    private volatile Vector<Flight> liveData;

    // scheduler, contains executor services / thread pools
    private final Scheduler scheduler;

    // new graphical user interface
    private final UserInterface ui;

    // live data thread
    private Thread liveThread;

    // data loader instance (for live data tasks)
    private final DataProcessor dataProcessor;

    // configuration instance
    private final Configuration config;

    // connection manager, manages user-input connections
    private final ConnectionManager connectionManager;

    // search object for DB-search operations
    private final Search search;

    // data-REST-uploader
    private final DataUploader<Frame> restUploader;

    private Fr24Collector fr24Collector;

    // DataLoader mask
    private int dataMask;

    /**
     * private constructor for Controller main instance,
     * get instance with Controller.getInstance()
     */
    private Controller() {
        this.hashCode = System.identityHashCode(INSTANCE);
        this.config = new Configuration();
        initConfig(); // Config
        this.scheduler = new Scheduler();
        this.dataProcessor = new DataProcessor();
        this.search = new Search();
        this.connectionManager = new ConnectionManager(Configuration.CONNECTIONS_FILENAME);
        FileWizard fileWizard = FileWizard.getFileWizard();
        initLevel++; // IO
        this.ui = new UserInterface(ActionHandler.getActionHandler(),
                fileWizard.translateSource((String) config.getProperty("currentMapSource").val),
                (String) config.getProperty("title").val,
                this.connectionManager);
        this.clicking = false;
        this.terminated = false;
        ConnectionSource upcon = connectionManager.getUploadConnection();
        this.restUploader = new DataUploader<>(upcon != null ? upcon.uri.getHost() : "127.0.0.1",
                (int) config.getProperty("uploader.threshold").val,
                scheduler, ui.getUploadPane());
        this.dataMask = DataProcessor.FR24_MASK | DataProcessor.LOCAL_WRITE_MASK; // local write with FR24-Frames
        initLevel++; // UI
    }

    /**
     * initializes the database file named 'plane.db',
     * replaces it if the file is invalid
     */
    private static void initDB() {
        if (initLevel > InitLevel.NOTHING) {
            return;
        }
        initLevel++; // CONFIG
        File dbFile = new File("plane.db");
        if (dbFile.exists()) {
            long space = dbFile.getTotalSpace();
            if (space > 10) {
                return;
            }
            if (!dbFile.delete()) {
                return;
            }
        }
        // FIXME: 26.03.2023 program crashes, bcause file not found
        // building database
        PyAdapter.runScript("python-helper/helper/dbBuilder.py", void.class);

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
        if (initLevel != InitLevel.UI) {
            throw new RuntimeException("Wrong initLevel: " + initLevel + " (expected: " + InitLevel.UI + ")");
        }
        Thread animation = scheduler.shortTask(() -> getUI().startScreenAnimation(2));
        initialize();
        try {
            scheduler.await(animation);
        } catch (InterruptedException ignored) {
        } finally {
            done(true);
        }
        getUI().setVisible(true);
        showLicenseDialog();
    }

    void showLicenseDialog() {
        File license = new File(Paths.LICENSE + "FR24_License.txt");
        String text;
        try {
            text = FileWizard.getFileWizard().readUTF(license);
        } catch (IOException e) {
            text = "Could not read license file!";
        }
        TextDialog.showDialog(getUI().getWindow(), text);
    }

    /**
     * initializes the {@link Configuration} class and
     * its constant which are saved in a {@link HashMap},
     * you can add your own properties here or with
     * {@link Configuration}.setProperty(key, value) if needed
     */
    private void initConfig() {
        if (initLevel > InitLevel.CONFIG) {
            return;
        }
        initLevel++;
        // initializing static properties
        config.setProperty("title", "PlaneSpotter v0.5-alpha");
        config.setProperty("threadKeepAliveTime", 4L);
        config.setProperty("maxThreads", 40);
        config.setProperty("saveLogs", false);
        config.setProperty("uploader.threshold", 5000);
        config.setProperty("mapBaseUrl", "https://a.tile.openstreetmap.de");
        config.setProperty("fr24RequestUri", "https://data-live.flightradar24.com/");
        config.setProperty("bingMap", new BingAerialTileSource());
        config.setProperty("transportMap", new OsmTileSource.TransportMap());
        config.setProperty("openStreetMap", new TMSTileSource(new TileSourceInfo("OSM", (String) config.getProperty("mapBaseUrl").val, "0")));

        // test only
        config.setProperty("receiverRequestUri", "http://192.168.178.47:8080/data/receiver.json");

        // initializing user properties
        FileWizard fileWizard = FileWizard.getFileWizard();
        Configuration props = null;
        try {
            props = fileWizard.readConfig(new File(Configuration.CONFIG_FILENAME));
        } catch (Exception e) {
            // catching all exceptions here to prevent ExceptionInInitializerError
            // printing stack trace for full exception information
            e.printStackTrace();
        } finally {
            if (props == null || props.elements() != 4) {
                props = new Configuration(new Property[] {
                        new Property("dataLimit", 50000),
                        new Property("currentMapSource", "OSM"),
                        new Property("gridSizeLat", 6),
                        new Property("gridSizeLon", 12)
                });
            }
            config.merge(props);
        }

        // should also be saved in 'filters.psc', not static
        FilterManager collectorFilterManager = new FilterManager()
                .addAll("RCH", "DUKE", "FORTE", "CASA", "VIVI", "EYE",
                        "NCR", "LAGR", "SNIPER", "VALOR", "MMF", "HOIS",
                        "K35R", "SONIC", "Q4", "CL", "MARTI");
        config.setProperty("collectorFilters", collectorFilterManager);
    }

    /**
     * initializes the controller by creating a new Logger
     * and initializing the Main-Tasks and Executors
     */
    private void initialize() {
        if (initLevel >= InitLevel.INITIALIZED) {
            return;
        }
        // trying to add tray icon to system tray
        Utilities.addTrayIcon(Images.PLANE_ICON_16x.get().getImage(), e -> ui.setVisible(!ui.isVisible()));
        // testing connections
        try {
            URL[] urls = new URL[] {
                    new URL((String) config.getProperty("fr24RequestUri").val),
                    new URL((String) config.getProperty("mapBaseUrl").val)
            };
            Utilities.connectionPreCheck(2000, urls);
        } catch (IOException | Fr24Exception e) {
            handleException(e);
        }
        // starting tasks and finishing
        DataOutputManager.initialize(getDataMask());
        setAdsbEnabled(false);
        liveThread = scheduler.runThread(() -> dataProcessor.run(this), "LiveData Loader", true, Scheduler.HIGH_PRIO);
        show(MAP_LIVE);
        initLevel++;
    }

    /**
     * executed on shutdown
     * shuts down the program if the uses chooses so
     * executes last tasks and closes the program carefully
     *
     * @param processRemainingFrames indicates if the remaining loaded live-frames
     *                              should be inserted or removed
     */
    public synchronized void shutdown(boolean processRemainingFrames) {
        // confirm dialog for shutdown
        int option = JOptionPane.showConfirmDialog(getUI().getWindow(),
                "Do you really want to exit PlaneSpotter?",
                "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option != JOptionPane.YES_OPTION) {
            return; // no-option case, no shutdown, abort method
        }
        getUI().showLoadingScreen(true);
        // disabling tasks
        dataProcessor.setLive(false);
        if (liveThread != null && liveThread.isAlive()) {
            liveThread.interrupt();
        }
        setLoading(true);
        // saving the configuration in 'config.psc'
        FileWizard fileWizard = FileWizard.getFileWizard();
        try {
            fileWizard.writeConfig(config, new File(Configuration.CONFIG_FILENAME));
        } catch (IOException | ExtensionException ioe) {
            handleException(ioe);
        }
        try {
            fileWizard.writeConnections(Configuration.CONNECTIONS_FILENAME, getConnectionManager());
        } catch (IOException | ExtensionException e) {
            handleException(e);
        }
        // saving log, if option is enabled
        if ((boolean) getConfig().getProperty("saveLogs").val) {
            fileWizard.saveLogFile("a_log", "[DEBUG] No output text yet...");
        }
        // inserting remaining frames, if option is enabled
        DBIn dbIn = DBIn.getDBIn();
        if (processRemainingFrames) {
            if (isLocalDBWriteEnabled() && fr24Collector != null) {
                try {
                    fr24Collector.getInserter().insertRemaining(scheduler, dataProcessor);
                } catch (NoAccessException ignored) {
                }
            }
            if (isUploadEnabled()) {
                restUploader.addData(dataProcessor.pollFrames(Integer.MAX_VALUE).collect(Collectors.toList()));
                restUploader.upload();
            }
        }
/*        // busy-waiting for remaining tasks
        while (scheduler.active() > 0) {
            Threading.onSpinWait();
        }
*/
        try {
            // shutting down scheduler
            scheduler.shutdown(processRemainingFrames ? 20 : 3);
            // disabling last tasks
            dbIn.setEnabled(false);
            done(true);
            this.terminated = true;
        } finally {
            // finally, shutting down JVM
            RUNTIME.exit(ExitStatus.SUCCESS);
        }

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
        RUNTIME.halt(ExitStatus.HALT);
    }

    /**
     * executed when a loading process is done, will turn
     * loading-flag to false and play a sound if wanted
     *
     * @param playSound indicates if the method should play a sound at the end
     */
    public void done(boolean playSound) {
        try {
            if (playSound) {
                Utilities.playSound(WinSound.SOUND_DEFAULT.get());
            }
        } finally {
            getUI().showLoadingScreen(false);
        }
    }

    /**
     * starts a {@link Fr24Collector} to collect Fr24-Data
     * @see Fr24Collector
     */
    // TODO: 08.08.2022 add filters, INSERT MODE
    void runCollector() {
        int lat = (int) getConfig().getProperty("gridSizeLat").val;
        int lon = (int) getConfig().getProperty("gridSizeLon").val;
        try {
            fr24Collector = new Fr24Collector(false, false, lat, lon, getDataMask(), Inserter.INSERT_ALL);
        } catch (DataNotFoundException e) {
            handleException(e);
            return;
        }
        fr24Collector.start();
    }

    /**
     *
     *
     * @param host
     */
    public void setUploadConnection(String host) throws NoAccessException {
        restUploader.setHost(host);
        URI uri = URI.create(restUploader.getHost());
        try {
            Utilities.connectionPreCheck(5, uri.toURL());
        } catch (ConnectException | MalformedURLException e) {
            throw new NoAccessException(host + " not accessible!");
        }
        try {
            connectionManager.setUploadConnection("http://" + host + ":8080");
        } catch (KeyCheckFailedException | IllegalInputException ignored) {
        }
    }

    /**
     * creates a UI-view for a specific view-type and specific data
     *
     * @param type is the ViewType, sets the content type for the
     *             created view (e.g. different List-View-Types)
     */
    public synchronized void show(@NotNull ViewType type) {

        MapManager mapManager = getUI().getMapManager();

        try {
            setLoading(true);
            getUI().showLoadingScreen(true);

            if (type != MAP_LIVE) {
                dataProcessor.setLive(false);
            }

            getUI().setViewType(type);
            getUI().getMapManager().clearMap();

            switch (type) {
                case LIST_FLIGHT -> { /*removed Flight-List implementation*/ }
                case MAP_LIVE -> dataProcessor.setLive(true);
                case MAP_TRACKING -> mapManager.createTrackingMap(getDataList(), null, true);
                case MAP_TRACKING_NP -> mapManager.createTrackingMap(getDataList(), null, false);
                case MAP_FROMSEARCH -> mapManager.createSearchMap(getDataList(), false);
                // significance map should be improved
                //case MAP_SIGNIFICANCE -> showSignificanceMap(mapManager, dbOut);
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
     * updates the {@link TreasureMap} with live {@link Flight}s and {@link ReceiverFrame} data
     *
     * @param receiverData is the receiver data
     */
    public void updateMap(@Nullable ReceiverFrame receiverData) {
        getUI().getMapManager().updateMap(getLiveDataList(), receiverData);
    }

    // TODO: 18.01.2023 comment
    /**
     *
     */
    public void runMapFlightSimulation() {

        String idOrCallSign = JOptionPane.showInputDialog("Please enter a flight ID or call sign!");

        FlightSimulation simulation = FlightSimulation.of(idOrCallSign);
        if (simulation == null) {
            getUI().showWarning(Warning.INVALID_DATA, "Input data is invalid, try another one!");
            return;
        }
        LayerPane lp = getUI().getLayerPane();
        MapManager mapManager = getUI().getMapManager();
        SimulationAddons addons = new SimulationAddons(lp);
        Simulator<DataPoint> simulator = new Simulator<>(0, 300, simulation);
        simulator.setOnTick(() -> {
            addons.setStatus(simulator.getStatus());
            addons.setRemaining(simulator.getRemainingMillis());
        });
        simulator.setOnStop(() -> {
            addons.setStatus(simulator.getStatus());
            addons.setRemaining(simulator.getRemainingMillis());
        });
        simulator.setOnClose(() -> {
            addons.setStatus(simulator.getStatus());
            addons.setRemaining(simulator.getRemainingMillis());
            mapManager.clearMap();
            lp.removeTop();
        });
        addons.setStartAction(e -> simulator.start());
        addons.setStopAction(e -> simulator.stop());
        addons.setCloseAction(e -> simulator.close());
        lp.addTop(addons, 0, 0, lp.getWidth(), lp.getHeight());
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

        int dataLimit, livePeriod; TileSource currentMapSource;

        getUI().showLoadingScreen(true);
        try {
            // data[0]
            dataLimit = Integer.parseInt(data[0]);
            getConfig().setProperty("dataLimit", dataLimit);
            // data[1]
            getConfig().setProperty("currentMapSource", data[1]);
            currentMapSource = switch (data[1]) {
                case "Bing Map" -> TreasureMap.BING_MAP;
                case "Transport Map" -> TreasureMap.TRANSPORT_MAP;
                default /*"Open Street Map"*/ -> TreasureMap.OPEN_STREET_MAP;
            };
            getUI().getMap().setTileSource(currentMapSource);
            // data[2]
            livePeriod = Integer.parseInt(data[2]) * 1000;
            dataProcessor.setLiveDataPeriod(livePeriod);
        } finally {
            // saving config after reset
            try {
                FileWizard.getFileWizard().writeConfig(config, new File(Configuration.CONFIG_FILENAME));
            } catch (IOException | ExtensionException ioe) {
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

        if (!this.clicking && dataProcessor.isLive()) {
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
     * Connects / disconnects a the current selected {@link ConnectionSource}
     *
     * @param connect indicates if a {@link ConnectionSource}
     *                should be connected or disconnected
     * @param mixWithFr24 indicates if the {@link Frame}s, loaded by the
     *                    {@link ConnectionSource} should be
     *                    mixed with {@link Fr24Frame}s
     */
    public void setConnection(boolean connect, boolean mixWithFr24) {
        ActionHandler onAction = ActionHandler.getActionHandler();
        ConnectionManager cmg = getConnectionManager();
        ConnectionSource selectedConn = cmg.getSelectedConn();
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
     * adds a {@link ConnectionSource} to the {@link ConnectionManager}
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
     * removes {@link ConnectionSource}s from
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

        connectionManager.setSelectedConn(null);
        for (String selected : selectedValues) {
            connectionManager.remove(selected);
        }
    }

    /**
     * tries to save the current view (bitmap or map viewer) in a file
     * file types: ('.bmp' / '.pls')
     */
    public void saveSelectedFile() {
        getUI().showLoadingScreen(true);
        setLoading(true);

        // TODO visible rect beim repainten speichern
        // TODO 2.Möglichkeit: center und zoom speichern
        try {
            ViewType currentViewType = getUI().getCurrentViewType();
            String extensions = currentViewType == MAP_HEATMAP ? ".bmp" : ".pls";
            JFileChooser fileChooser = getUI().showFileSaver(getUI().getWindow(), extensions);
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
    public void loadSelectedFile() {

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
                dataProcessor.setLive(false);
                DBIn dbIn = DBIn.getDBIn();
                dbIn.setEnabled(false);
                Scheduler.sleepSec(60);
                dbIn.setEnabled(true);
                dataProcessor.setLive(true);

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
            getUI().showWarning(Warning.UNKNOWN_ERROR, cnf + "\n" + cnf.getMessage());
            cnf.printStackTrace();

        } else if (thr instanceof NumberFormatException nfe) {
            getUI().showWarning(Warning.NUMBER_EXPECTED, nfe.getMessage());

        } else if (thr instanceof Fr24Exception frex) {
            if (frex.getMessage().endsWith("not reachable!")) {
                getUI().showWarning(Warning.URL_NOT_REACHABLE);
            }
        } else if (thr instanceof InterruptedException ie) {
            System.err.println(ie + ": " + ie.getMessage());

        } else if (thr != null) {
            getUI().showWarning(Warning.UNKNOWN_ERROR, thr + "\n" + thr.getMessage());
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
            mapManager.createSignificanceMap(signifMap);
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
                    StatsView.showPosHeatMap(getUI(), bmp);
                } catch (NumberFormatException nfe) {
                    getUI().showWarning(Warning.NUMBER_EXPECTED, "Please enter a float value (0.025 - 2.0)");
                } catch (DataNotFoundException | OutOfMemoryError e) {
                    handleException(e);
                } finally {
                    done(false);
                }
            }, "Loading Data", false, Scheduler.MID_PRIO, true);
        } else if (bitmap != null) {
            StatsView.showPosHeatMap(getUI(), bitmap);
        } else {
            StatsView.showPosHeatMap(getUI(), buf);
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
     * getter for the {@link DataProcessor} instance
     *
     * @return the {@link DataProcessor} instance
     */
    @NotNull
    public DataProcessor getDataLoader() {
        return dataProcessor;
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
     * sets the loading flag to a certain value
     *
     * @param b is the loading value ( true or false )
     */
    public void setLoading(boolean b) {
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
        return (this.dataMask & DataProcessor.ADSB_MASK) == DataProcessor.ADSB_MASK;
    }

    /**
     * sets the 'adsb enabled' flag to true
     *
     * @param adsbEnabled indicates if the {@link ADSBSupplier} should be enabled
     */
    public void setAdsbEnabled(boolean adsbEnabled) {
        if (adsbEnabled) {
            this.dataMask |= DataProcessor.ADSB_MASK;
        } else if (isAdsbEnabled()) {
            this.dataMask -= DataProcessor.ADSB_MASK;
        }
    }

    /**
     * getter for 'Fr24Supplier enabled' flag
     *
     * @return the Fr24Suppplier enabled' flag
     */
    public boolean isFr24Enabled() {
        return (dataMask & DataProcessor.FR24_MASK) == DataProcessor.FR24_MASK;
    }

    /**
     * sets the 'Fr24Supplier enabled' flag
     *
     * @param fr24Enabled indicates if the 'Fr24Supplier enabled' flag should be enabled/disabled
     */
    public void setFr24Enabled(boolean fr24Enabled) {
        if (fr24Enabled) {
            this.dataMask |= DataProcessor.FR24_MASK;
        } else if (isFr24Enabled()) {
            this.dataMask -= DataProcessor.FR24_MASK;
        }
    }

    /**
     *
     *
     * @return
     */
    public boolean isLocalDBWriteEnabled() {
        return (dataMask & DataProcessor.LOCAL_WRITE_MASK) == DataProcessor.LOCAL_WRITE_MASK;
    }

    /**
     *
     *
     * @param enable
     */
    public void setLocalDBWriteEnabled(boolean enable) {
        if (enable) {
            this.dataMask |= DataProcessor.LOCAL_WRITE_MASK;
        } else if (isLocalDBWriteEnabled()) {
            this.dataMask -= DataProcessor.LOCAL_WRITE_MASK;
        }
        DataOutputManager om = DataOutputManager.getOutputManager();
        om.setDataMask(this.dataMask);
    }

    /**
     *
     *
     * @return
     */
    public boolean isUploadEnabled() {
        return (dataMask & DataProcessor.UPLOAD_MASK) == DataProcessor.UPLOAD_MASK;
    }

    /**
     *
     *
     * @param enable
     */
    public void setUploadEnabled(boolean enable) {
        if (enable) {
            this.dataMask |= DataProcessor.UPLOAD_MASK;
        } else if (isUploadEnabled()) {
            this.dataMask -= DataProcessor.UPLOAD_MASK;
        }
        DataOutputManager om = DataOutputManager.getOutputManager();
        om.setDataMask(this.dataMask);
    }

    /**
     * getter for {@link DataProcessor} mask
     *
     * @return DataLoader mask
     * @see DataProcessor for mask constants
     */
    public int getDataMask() {
        return dataMask;
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
     * getter for {@link DataUploader} instance
     *
     * @return the REST data uploader
     */
    public DataUploader<Frame> getRestUploader() {
        return restUploader;
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
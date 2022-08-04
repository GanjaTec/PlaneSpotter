package planespotter.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import planespotter.model.io.DBIn;
import planespotter.model.io.FileWizard;
import planespotter.model.nio.LiveLoader;
import planespotter.util.LRUCache;
import planespotter.constants.UserSettings;
import planespotter.constants.ViewType;
import planespotter.constants.Warning;
import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.display.models.MenuModels;
import planespotter.model.*;
import planespotter.model.io.DBOut;
import planespotter.statistics.Statistics;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.IllegalInputException;
import planespotter.throwables.InvalidDataException;
import planespotter.util.Logger;
import planespotter.util.math.MathUtils;
import planespotter.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static planespotter.constants.DefaultColor.DEFAULT_MAP_ICON_COLOR;
import static planespotter.constants.ViewType.*;

/**
 * @name Controller
 * @author jml04
 * @author Lukas
 * @author Bennet
 * @version 1.1
 *
 * main controller - responsible for connection between model and view
 * has static controller, scheduler, gui, action handler and logger instance
 */
// TODO: 15.07.2022 make most instances not static anymore
public abstract class Controller {

    // static fields

    // runtime instance
    public static final Runtime runtime;
    // project root path / working directory
    public static final String ROOT_PATH;
    // ONLY Controller instance
    private static final Controller INSTANCE;
    // live data loading period
    private int LIVE_DATA_PERIOD_SEC = 2;
    // logger for whole program
    private Logger logger;
    // live data thread
    private Thread liveThread;
    // static initializer
    static {
        // initializing 'root' members
        runtime = Runtime.getRuntime();
        INSTANCE = new Controller() {};
        ROOT_PATH = Utilities.getAbsoluteRootPath();
    }

    // instance fields

    // hash code
    private final int hashCode;
    // already-clicking/-initialized flag
    private boolean clicking, initialized;
    // boolean loading is true when something is loading
    private volatile boolean loading;
    // loadedData contains all loaded DataPoints
    public volatile Vector<DataPoint> loadedData;
    // liveData contains all loaded live-Flights
    public volatile Vector<Flight> liveData;
    // scheduler, contains executor services / thread pools
    private final Scheduler scheduler;
    // only GUI instance
    private final GUI gui;
    // proto test-cache
    public final LRUCache<String, Object> cache;

    /**
     * private constructor for Controller main instance,
     * get instance with Controller.getInstance()
     */
    private Controller() {
        this.hashCode = System.identityHashCode(INSTANCE);
        this.clicking = false;
        this.scheduler = new Scheduler();
        this.gui = new GUI(ActionHandler.getActionHandler());
        this.cache = new LRUCache<>(40); // TODO best cache size
    }

    /**
     * @return ONE and ONLY controller instance
     */
    @NotNull
    public static Controller getInstance() {
        return INSTANCE;
    }

    /**
     * @return main logger
     */
    @NotNull
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * @return main scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * @return main GUI instance
     */
    public GUI getGUI() {
        return this.gui;
    }

    /**
     * sets the period for the live-data loader in seconds
     *
     * @param sec is the period in seconds
     */
    public void setLiveDataPeriod(@Range(from = 1, to = 10) int sec) {
        LIVE_DATA_PERIOD_SEC = sec;
    }

    /**
     * starts the program by initializing the controller
     * and opening a GUI-window
     */
    public synchronized void start() {
        this.initialize();
        this.openWindow();
        this.gui.onInitFinish();
        this.done();
    }

    /**
     * initializes the controller by creating a new Logger
     * and initializing the Main-Tasks and Executors
     */
    private void initialize() {
        if (!this.initialized) {
            logger = new Logger(this);
            logger.log("initializing Controller...", this);
            this.initTasks();
            logger.successLog("Controller initialized sucsessfully!", this);
            this.initialized = true;
        }
    }

    /**
     * starts all Controller tasks with the Scheduler
     */
    private void initTasks() {
        if (!this.initialized) {
            this.logger.log("initializing Executors...", this);
            // executing on-start tasks
            this.liveThread = this.scheduler.runThread(this::liveDataTask, "Live-Data Loader", true, Scheduler.HIGH_PRIO);
            // TODO: 02.07.2022 insert-while-live-option (as permanent parallel like live Data task)
            //SCHEDULER.schedule(() -> DBIn.insert(SCHEDULER, 50), "Insert Live Data", 20, 10);

            this.logger.successLog("Executors initialized successfully!", this);
        }
    }

    /**
     * opens a new GUI window as a thread
     */
    private synchronized void openWindow() {
        this.setLoading(true);
        logger.log("initialising GUI...", this.gui);
        // starting loading screen
        scheduler.exec(this.gui::startLoadingScreen, "Loading Screen", false, Scheduler.MID_PRIO, false);

        logger.successLog("GUI initialized sucsessfully!", this.gui);
        this.gui.getComponent("window").setVisible(true);
        this.done();
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
        int option = JOptionPane.showConfirmDialog(this.gui.getComponent("window"),
                "Do you really want to exit PlaneSpotter?",
                    "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        // yes-option
        if (option == JOptionPane.YES_OPTION) {
            logger.infoLog("Shutting down program, please wait...", this);
            // disabling tasks
            this.gui.getComponent("progressBar").setVisible(true);
            LiveLoader.setLive(false);
            if (liveThread != null && liveThread.isAlive()) {
                liveThread.interrupt();
            }
            this.setLoading(true);
            // saving the configuration in 'config.psc'
            FileWizard.getFileWizard().saveConfig();
            // inserting remaining frames, if option is enabled
            if (insertRemainingFrames) {
                DBIn.insertRemaining(scheduler, 1000);
            }
            // waiting for remaining tasks
            while (scheduler.active() > 0) {
                try {
                    this.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.notifyAll();
            // disabling last tasks
            DBIn.setEnabled(false);
            this.done();
            logger.close();
            // shutting down scheduler
            boolean shutdown = scheduler.shutdown(1);
            byte out = MathUtils.toBinary(shutdown);
            // shutting down the VM
            System.exit(out);

        }
    }

    private void emergencyShutdown(@Nullable Throwable problem) {
        // printing error stacktrace
        if (problem != null) {
            problem.printStackTrace();
        }
        // saving last error log
        FileWizard.getFileWizard().saveLogFile("ps-crash", System.out.toString());
        // shutting down VM directly, won't wait for any tasks
        runtime.halt(-1);
    }

    private synchronized void liveDataTask() {
        // loading init-live-data
        this.loadLiveData();
        // endless live-data task
        for (;;) {
            // trying to await the live-data period
            try {
                this.wait(TimeUnit.SECONDS.toMillis(LIVE_DATA_PERIOD_SEC));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.notify();
            }
            // loading live-data, if live-map is enabled
            if (LiveLoader.isLive()) {
                this.loadLiveData();
            }
        }
    }

    /**
     * loads Fr24-data directly into the liveData-Collection,
     * but not into the DB
     * @see LiveLoader
     */
    private synchronized void loadLiveData() {

        List<MapMarker> markerList;
        TreasureMap map;
        // checking for 'already loading' and setting controller loading
        if (!this.isLoading()) {
            this.setLoading(true);
            map = this.gui.getMap();
            // loading direct live-data
            this.liveData = LiveLoader.loadDirectly(scheduler, map);
            // transforming liveData-flight-Vector into list of MapMarkers
            markerList = this.liveData.stream()
                    .map(flight -> {
                        // transforming to MapMarker
                        final Position pos = flight.dataPoints().get(0).pos();
                        final double lat = pos.lat(),
                                     lon = pos.lon();
                        final MapMarkerDot marker = new MapMarkerDot(new Coordinate(lat, lon));
                        marker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
                        return marker;
                    })
                    .collect(Collectors.toList());
            // setting new map marker list on the map
            map.setMapMarkerList(markerList);
            this.done();
        }
    }

    /**
     * executed when a loading process is done, will turn
     * loading-flag to false and stop the GUI-progressBar
     */
    public void done() {
        this.loading = false;
        if (this.gui != null) {
            this.gui.stopProgressBar();
        }
    }

    /**
     * creates a GUI-view for a specific view-type and specific data
     *
     * @param type is the ViewType, sets the content type for the
     *             created view (e.g. different List-View-Types)
     * @param headText is the view-head-text
     */
    public synchronized void show(@NotNull ViewType type,
                                  @NotNull final String headText) {

        MapManager mapManager = this.gui.getMapManager();
        DBOut dbOut = DBOut.getDBOut();

        this.setLoading(true);

        this.gui.disposeView();
        this.gui.setCurrentViewType(type);
        switch (type) {
            case LIST_FLIGHT -> this.showFlightList(dbOut);
            case MAP_LIVE -> this.showLiveMap(headText, mapManager);
            case MAP_FROMSEARCH -> this.showSearchMap(headText, mapManager);
            case MAP_TRACKING -> this.showTrackingMap(headText, mapManager);
            case MAP_TRACKING_NP -> this.showTrackingMapNoPoints(headText, mapManager);
            // significance map should be improved
            case MAP_SIGNIFICANCE -> this.showSignificanceMap(headText, mapManager, dbOut);
            case MAP_HEATMAP -> {}
        }
        this.done();
        logger.successLog("view loaded!", this);
    }

    /**
     * search method for the this.gui-search
     *
     * @param inputs are the inputs in the search fields
     * @param button is the clicked search button, 0 = LIST, 1 = MAP
     */
    // TODO: 24.05.2022 DEBUG PLANE SEARCH, AIRLINE SEARCH
    public void search(String[] inputs, int button) {

        Search search;
        if (button == 1) {
            this.setLoading(true);
            this.gui.startProgressBar();
            search = new Search();
            try {
                switch (this.gui.getCurrentSearchType()) {
                    case AIRLINE -> {
                        this.loadedData = search.forAirline(inputs);
                        this.show(ViewType.MAP_TRACKING_NP, "Flight Search Results");
                    }
                    case AIRPORT -> {
                        this.loadedData = search.forAirport(inputs);
                        this.show(ViewType.MAP_TRACKING_NP, "Flight Search Results");
                    }
                    case FLIGHT -> {
                        this.loadedData = search.forFlight(inputs);
                        this.show(ViewType.MAP_TRACKING, "Flight Search Results");
                    }
                    case PLANE -> {
                        this.loadedData = search.forPlane(inputs);
                        this.show(ViewType.MAP_FROMSEARCH, "Plane Search Results");
                    }
                }
            } catch (DataNotFoundException e) {
                this.handleException(e);
            } finally {
                this.gui.stopProgressBar();
            }
        }
    }

    /**
     *
     * @param data [0] and [1] must be filled
     */
    public void confirmSettings(@NotNull String... data) {

        TileSource currentMapSource;
        int maxLoadedData, livePeriodSec;

        this.gui.startProgressBar();
        try {
            // data[0]
            maxLoadedData = Integer.parseInt(data[0]);
            UserSettings.setMaxLoadedData(maxLoadedData);
            currentMapSource = UserSettings.getCurrentMapSource();
            // data[1]
            switch (data[1]) {
                case "Bing Map" -> currentMapSource = UserSettings.BING_MAP;
                case "Default Map" -> currentMapSource = UserSettings.DEFAULT_MAP;
                case "Transport Map" -> currentMapSource = UserSettings.TRANSPORT_MAP;
            }
            UserSettings.setCurrentMapSource(currentMapSource);
            this.gui.getMap().setTileSource(currentMapSource);
            // data[2]
            livePeriodSec = Integer.parseInt(data[2]);
            setLiveDataPeriod(livePeriodSec);
        } finally {
            // saving config after reset
            FileWizard.getFileWizard().saveConfig();
            this.gui.stopProgressBar();
        }
    }

    void onLiveClick(ICoordinate clickedCoord) {

        TreasureMap map;
        List<MapMarker> markers;
        MapManager mapManager;
        TreePlantation tpl;
        List<MapMarker> newMarkerList;
        JPanel menu;
        boolean markerHit;
        int counter;

        if (!this.clicking && LiveLoader.isLive()) {
            try {
                this.clicking = true;
                map = this.gui.getMap();
                markers = map.getMapMarkerList();
                mapManager = this.gui.getMapManager();
                tpl = this.gui.getTreePlantation();
                newMarkerList = new ArrayList<>();
                markerHit = false;
                counter = 0;
                DefaultMapMarker newMarker;
                Coordinate markerCoord;
                for (MapMarker marker : markers) {
                    markerCoord = marker.getCoordinate();
                    newMarker = new DefaultMapMarker(markerCoord, 0);
                    if (!markerHit && mapManager.isMarkerHit(markerCoord, clickedCoord)) {
                        markerHit = true;
                        menu = (JPanel) this.gui.getComponent("menuPanel");
                        this.markerHit(MAP_LIVE, newMarker, counter, null, null, tpl, logger, menu);
                    } else {
                        newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
                    }
                    newMarker.setName(marker.getName());
                    newMarkerList.add(newMarker);
                    counter++;
                }
                if (markerHit) {
                    this.gui.getMap().setMapMarkerList(newMarkerList);
                }
            } finally {
                this.clicking = false;
            }
        }
    }

    /**
     * is executed when a map marker is clicked and the current is MAP_ALL
     */
    void onClick_all(ICoordinate clickedCoord) { // TODO aufteilen

        List<MapMarker> mapMarkers;
        List<MapMarker> newMarkerList;
        Vector<DataPoint> data;
        Coordinate markerCoord;
        DefaultMapMarker newMarker;
        MapManager mapManager;
        DBOut dbOut;
        TreePlantation tpl;
        Logger log;
        JPanel menu;
        boolean markerHit;
        int counter;

        if (!this.clicking) {
            try {
                this.clicking = true;
                mapMarkers = this.gui.getMap().getMapMarkerList();
                newMarkerList = new ArrayList<>();
                markerHit = false;
                mapManager = this.gui.getMapManager();
                counter = 0;
                data = this.loadedData;
                dbOut = DBOut.getDBOut();
                tpl = this.gui.getTreePlantation();
                log = this.getLogger();
                menu = (JPanel) this.gui.getComponent("menuPanel");
                // going though markers
                for (MapMarker m : mapMarkers) {
                    markerCoord = m.getCoordinate();
                    newMarker = new DefaultMapMarker(markerCoord, 90); // FIXME: 13.05.2022 // FIXME 19.05.2022
                    if (mapManager.isMarkerHit(markerCoord, clickedCoord) && !markerHit) {
                        markerHit = true;
                        this.markerHit(MAP_FROMSEARCH, newMarker, counter, data, dbOut, tpl, log, menu);
                    } else {
                        newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
                    }
                    newMarker.setName(m.getName());
                    newMarkerList.add(newMarker);
                    counter++;
                }
                if (markerHit) {
                    this.gui.getMap().setMapMarkerList(newMarkerList);
                }
            } finally {
                this.clicking = false;
            }
        }
    }

    private void markerHit(ViewType viewType, DefaultMapMarker marker,
                           int counter, Vector<DataPoint> dataPoints,
                           DBOut dbOut, TreePlantation treePlantation,
                           Logger logger, JPanel menuPanel) {

        Map<Integer, DataPoint> dps;
        Flight flight;
        int flightID;

        switch (viewType) {
            case MAP_FROMSEARCH -> {
                marker.setBackColor(Color.RED);
                menuPanel.setVisible(false);
                flightID = dataPoints.get(counter).flightID();
                try {
                    //this.show(MAP_TRACKING, "Flight '" + flightID + "'", String.valueOf(flightID));
                    this.showTrackingMap("Flight '" + flightID + "'", this.gui.getMapManager());
                    flight = dbOut.getFlightByID(flightID);
                    treePlantation.createFlightInfo(flight, this.gui);
                } catch (DataNotFoundException e) {
                    logger.errorLog("flight with the ID " + flightID + " doesn't exist!", this);
                }
            }
            case MAP_LIVE -> {
                marker.setBackColor(Color.RED);
                flight = this.liveData.get(counter);
                menuPanel.setVisible(false);
                dps = flight.dataPoints();
                treePlantation.createDataPointInfo(flight, dps.get(0), this.gui);
            }
        }
    }

    /**
     *
     * @param clickedCoord is the clicked coordinate
     */
    public void onTrackingClick(ICoordinate clickedCoord) { // TODO aufteilen
        TreasureMap map = this.gui.getMap();
        List<MapMarker> markers = map.getMapMarkerList();
        MapManager mapManager = this.gui.getMapManager();
        TreePlantation tpl = this.gui.getTreePlantation();
        Coordinate markerCoord;
        DataPoint dp;
        Flight flight;
        DBOut dbOut =  DBOut.getDBOut();
        int flightID, counter = 0;

        for (MapMarker m : markers) {
            markerCoord = m.getCoordinate();
            if (mapManager.isMarkerHit(markerCoord, clickedCoord)) {
                this.gui.getComponent("infoPanel").removeAll();
                dp = this.loadedData.get(counter);
                flightID = dp.flightID();
                map.setMapMarkerList(mapManager.resetTrackingMarkers(m));
                try {
                    flight = dbOut.getFlightByID(flightID);
                    tpl.createDataPointInfo(flight, dp, this.gui);
                } catch (DataNotFoundException e) {
                    this.handleException(e);
                    this.getLogger().errorLog("flight with the ID " + flightID + " doesn't exist!", this);
                }
                break;
            }
            counter++;
        }
    }

    public void saveFile() {

        JFileChooser fileChooser;
        Rectangle rect;
        MapData mapData;
        File selected;

        this.setLoading(true);
        this.gui.startProgressBar();

        this.gui.setCurrentVisibleRect(this.gui.getMap().getVisibleRect()); // TODO visible rect beim repainten speichern

        fileChooser = MenuModels.fileSaver((JFrame) this.gui.getComponent("window"));
        rect = (this.gui.getCurrentVisibleRect() != null) ? this.gui.getCurrentVisibleRect() : null;
        selected = fileChooser.getSelectedFile();
        if (selected == null) {
            this.done();
            return;
        }
        // new MapData object with loadedData, view type and visible rectangle
        mapData = new MapData(this.loadedData, MAP_TRACKING, rect);
        try {
            FileWizard.getFileWizard().savePlsFile(mapData, selected);
        } catch (DataNotFoundException | FileAlreadyExistsException e) {
            this.handleException(e);
        } finally {
            this.done();
        }
    }

    public void loadFile() {

        TreasureMap trackingMap;
        MapManager mapManager = this.gui.getMapManager();
        JFileChooser fileChooser = MenuModels.fileLoader((JFrame) this.gui.getComponent("window"));
        File file = fileChooser.getSelectedFile();
        if (file == null) {
            return;
        }
        try {
            this.loadedData = FileWizard.getFileWizard().loadPlsFile(file).data();
            trackingMap = mapManager.createTrackingMap(this.loadedData, null, true, this.gui);
            // receiving tracking map
            mapManager.receiveMap(trackingMap, "Loaded from File", MAP_TRACKING);
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * handles exceptions, if an exception occurs,
     * execute this method to handle it
     *
     * @param thr is the throwable (usually an exception)
     *            which is thrown and will be handled
     */
    public void handleException(final Throwable thr) {

        if (thr instanceof DataNotFoundException dnf) {
            this.gui.showWarning(Warning.NO_DATA_FOUND, dnf.getMessage());
        } else if (thr instanceof SQLException sql) {
            String message = sql.getMessage();
            thr.printStackTrace();
            this.gui.showWarning(Warning.SQL_ERROR, message);
            if (message.contains("BUSY")) {
                // emergency shutdown because of DB-Bug
                this.emergencyShutdown(sql);
            }
        } else if (thr instanceof TimeoutException) {
            this.gui.showWarning(Warning.TIMEOUT);
        } else if (thr instanceof RejectedExecutionException) {
            this.gui.showWarning(Warning.REJECTED_EXECUTION);
        } else if (thr instanceof IllegalInputException) {
            this.gui.showWarning(Warning.ILLEGAL_INPUT);
        } else if (thr instanceof InvalidDataException ide) {
            this.gui.showWarning(Warning.UNKNOWN_ERROR, ide.getMessage() + "\n" + ide.getCause().getMessage());
            ide.printStackTrace();
        } else if (thr instanceof ClassNotFoundException cnf) {
            this.gui.showWarning(Warning.UNKNOWN_ERROR, cnf.getMessage());
            cnf.printStackTrace();
        } else if (thr instanceof FileAlreadyExistsException fae) {
            this.gui.showWarning(Warning.FILE_ALREADY_EXISTS, fae.getMessage());
            fae.printStackTrace();
        } else {
            this.gui.showWarning(Warning.UNKNOWN_ERROR, thr.getMessage());
            thr.printStackTrace();
        }
    }

    // private methods

    private void showSignificanceMap(String headText, MapManager bbn, DBOut dbOut) {

        Deque<Airport> aps;
        Map<Airport, Integer> signifMap;
        TreasureMap map;
        Statistics stats = new Statistics();

        try {
            aps = dbOut.getAllAirports();
            signifMap = stats.airportSignificance(aps);
            map = bbn.createSignificanceMap(signifMap, this.gui.getMap());
            bbn.receiveMap(map, headText, MAP_HEATMAP);
        } catch (DataNotFoundException e) {
            logger.errorLog(e.getMessage(), this);
            e.printStackTrace();
        }
    }

    private void showTrackingMapNoPoints(String headText, MapManager mapManager) {

        Flight flight = null;
        TreasureMap flightRouteMap;
        DBOut dbOut = DBOut.getDBOut();

        int flightID = (!this.loadedData.isEmpty()) ? this.loadedData.get(0).flightID() : -1;
        try {
            flight = dbOut.getFlightByID(flightID);
        } catch (DataNotFoundException dnf) {
            logger.errorLog(dnf.getMessage(), this);
        }
        flightRouteMap = mapManager.createTrackingMap(this.loadedData, flight, false, this.gui);
        mapManager.receiveMap(flightRouteMap, headText, MAP_TRACKING_NP);
    }

    private void showTrackingMap(String headText, @NotNull MapManager mapManager) {

        TreasureMap trackingMap;
        DBOut dbOut = DBOut.getDBOut();
        Flight flight = null;
        Vector<DataPoint> data = this.loadedData;

        int flightID = (!data.isEmpty()) ? data.get(0).flightID() : -1;
        try {
            flight = (flightID != -1) ? dbOut.getFlightByID(flightID) : null;
        } catch (DataNotFoundException ignored) {
        }
        trackingMap = mapManager.createTrackingMap(data, flight, true, this.gui);
        mapManager.receiveMap(trackingMap, headText, MAP_TRACKING);
    }

    private void showSearchMap(String headText, MapManager bbn) {

        Vector<Position> data = Utilities.parsePositionVector(this.loadedData);
        TreasureMap viewer = bbn.createLiveMap(data, this.gui.getMap());
        bbn.receiveMap(viewer, headText, MAP_FROMSEARCH);
    }

    private void showLiveMap(final String headText, @NotNull final MapManager mapManager) {

        Vector<Position> data;
        TreasureMap viewer;

        if (this.liveData == null || this.liveData.isEmpty()) {
            this.gui.showWarning(Warning.LIVE_DATA_NOT_FOUND);
            return;
        }
        data = Utilities.parsePositionVector(this.liveData);
        viewer = mapManager.createLiveMap(data, this.gui.getMap());

        LiveLoader.setLive(true);
        mapManager.receiveMap(viewer, headText, MAP_LIVE);
    }

    private void showFlightList(DBOut dbOut) {

        List<Flight> flights;
        Deque<Integer> fids;
        TreePlantation treePlant;
        int flightID;

        if (this.loadedData == null || this.loadedData.isEmpty()) {
            this.gui.showWarning(Warning.NO_DATA_FOUND);
            return;
        }
        flights = new ArrayList<>();
        flightID = -1;
        try {
            fids = dbOut.getLiveFlightIDs(10000, 25000);
            while (!fids.isEmpty()) {
                flightID = fids.poll();
                flights.add(dbOut.getFlightByID(flightID));
            }
        } catch (DataNotFoundException e) {
            logger.errorLog("flight with  ID " + flightID + " doesn't exist!", this);
            this.handleException(e);
        }
        treePlant = this.gui.getTreePlantation();
        treePlant.createTree(treePlant.allFlightsTreeNode(flights), this.gui);
    }

    /**
     * @return loading flag, true if Controller is loading something
     */
    public boolean isLoading() {
        return this.loading;
    }

    public void setLoading(boolean b) {
        this.loading = b;
    }

    /**
     * @return controller hash code
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

}

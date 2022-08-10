package planespotter.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import planespotter.constants.SearchType;
import planespotter.model.io.DBIn;
import planespotter.model.io.FileWizard;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.nio.LiveLoader;
import planespotter.throwables.NoAccessException;
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
import static planespotter.constants.Sound.SOUND_DEFAULT;
import static planespotter.constants.ViewType.*;

/**
 * @name Controller
 * @author jml04
 * @author Lukas
 * @author Bennet
 * @version 1.1
 *
 * @description
 * main controller - responsible for connection between model and view
 */
public abstract class Controller {

    // static fields

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

    /**
     * @return ONE and ONLY controller instance
     */
    @NotNull
    public static Controller getInstance() {
        return INSTANCE;
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

    // new graphical user interface
    private final UserInterface ui;

    // TODO: 10.08.2022 move to Search -> searchCache
    // proto test-cache
    public final LRUCache<String, Object> cache;

    // live data loading period
    private int liveDataPeriodSec;

    // logger for whole program
    private Logger logger;

    // live data thread
    private Thread liveThread;

    /**
     * private constructor for Controller main instance,
     * get instance with Controller.getInstance()
     */
    private Controller() {
        this.hashCode = System.identityHashCode(INSTANCE);
        this.clicking = false;
        this.scheduler = new Scheduler();
        this.cache = new LRUCache<>(100); // TODO best cache size
        this.ui = new UserInterface(ActionHandler.getActionHandler());
        this.liveDataPeriodSec = 2;
    }

    /**
     * starts the program by initializing the controller
     * and opening a UI-window
     */
    public synchronized void start() {
        this.initialize();
        this.ui.getWindow().setVisible(true);
        this.done(true);
    }

    /**
     * initializes the controller by creating a new Logger
     * and initializing the Main-Tasks and Executors
     */
    private void initialize() {
        if (!this.initialized) {
            logger = new Logger(this);
            logger.log("Initializing Controller...", this);
            this.initTasks();
            logger.successLog("Controller initialized successfully!", this);
            this.initialized = true;
        }
    }

    /**
     * starts all Controller tasks with the Scheduler
     */
    private void initTasks() {
        if (!this.initialized) {
            this.logger.log("Running init tasks...", this);
            // executing on-start tasks
            this.liveThread = this.scheduler.runThread(this::liveDataTask, "Live-Data Loader", true, Scheduler.HIGH_PRIO);
            // TODO: 02.07.2022 insert-while-live-option (as permanent parallel like live Data task)
            //SCHEDULER.schedule(() -> DBIn.insert(SCHEDULER, 50), "Insert Live Data", 20, 10);
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
        int option = JOptionPane.showConfirmDialog(this.ui.getWindow(),
                "Do you really want to exit PlaneSpotter?",
                    "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        // yes-option
        if (option == JOptionPane.YES_OPTION) {
            this.logger.infoLog("Shutting down program, please wait...", this);
            // disabling tasks
            LiveLoader.setLive(false);
            if (liveThread != null && liveThread.isAlive()) {
                liveThread.interrupt();
            }
            this.setLoading(true);
            // saving the configuration in 'config.psc'
            FileWizard.getFileWizard().saveConfig();
            // inserting remaining frames, if option is enabled
            if (insertRemainingFrames && DBIn.isEnabled()) {
                try {
                    DBIn.insertRemaining(this.scheduler);
                } catch (NoAccessException nae) {
                    this.logger.infoLog("DBIn is disabled, skipping insert...", this);
                }
            }
            // waiting for remaining tasks
            while (this.scheduler.active() > 0) {
                try {
                    this.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.notifyAll();
            // disabling last tasks
            DBIn.setEnabled(false);
            this.done(true);
            this.logger.close();
            // shutting down scheduler
            boolean shutdown = this.scheduler.shutdown(1);
            byte exitStatus = MathUtils.toBinary(shutdown);
            // shutting down the VM
            RUNTIME.exit(exitStatus);

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
        RUNTIME.halt(-1);
    }

    private synchronized void liveDataTask() {
        // loading init-live-data
        this.loadLiveData();
        // endless live-data task
        for (;;) {
            // trying to await the live-data period
            try {
                this.wait(TimeUnit.SECONDS.toMillis(this.liveDataPeriodSec));
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

            map = this.ui.getMap();
            // loading direct live-data
            this.liveData = LiveLoader.loadDirectly(this.scheduler, map);
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
            this.done(false);
        }
    }

    /**
     * executed when a loading process is done, will turn
     * loading-flag to false and play a sound if wanted
     *
     * @param playSound indicates if the method should play a sound at the end
     */
    public void done(boolean playSound) {
        this.loading = false;
        if (playSound) {
            Utilities.playSound(SOUND_DEFAULT.get());
        }
    }

    // TODO: 08.08.2022 add filters
    void runFr24Collector() {
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

        MapManager mapManager = this.ui.getMapManager();
        DBOut dbOut = DBOut.getDBOut();

        this.setLoading(true);

        UserInterface ui = this.getUI();
        ui.setViewType(type);
        ui.getMapManager().clearMap();
        if (type != MAP_LIVE) {
            LiveLoader.setLive(false);
        }
        switch (type) {
            case LIST_FLIGHT -> this.showFlightList(dbOut);
            case MAP_LIVE -> LiveLoader.setLive(true);
            case MAP_FROMSEARCH -> this.showSearchMap(mapManager);
            case MAP_TRACKING -> this.showTrackingMap(mapManager);
            case MAP_TRACKING_NP -> this.showTrackingMapNoPoints(mapManager);
            // significance map should be improved
            case MAP_SIGNIFICANCE -> this.showSignificanceMap(mapManager, dbOut);
            case MAP_HEATMAP -> {}
        }
        this.done(false);
        logger.successLog("view loaded!", this);
    }

    /**
     * search method for different data from the DB
     *
     * @param inputs are the inputs in the search fields
     * @param button is the clicked search button, 0 = LIST, 1 = MAP
     */
    // TODO: 24.05.2022 DEBUG PLANE SEARCH, AIRLINE SEARCH
    public void search(String[] inputs, int button)
            throws IllegalInputException {

        Utilities.checkInputs(inputs);

        Search search;
        if (button == 1) {
            this.setLoading(true);

            search = new Search();
            SearchType currentSearchType = this.ui.getSearchPanel().getCurrentSearchType();
            try {
                switch (currentSearchType) {
                    case AIRLINE -> {
                        this.loadedData = search.forAirline(inputs);
                        this.show(MAP_TRACKING_NP);
                        //this.show(ViewType.MAP_TRACKING_NP, "Flight Search Results");
                    }
                    case AIRPORT -> {
                        this.loadedData = search.forAirport(inputs);
                        this.show(MAP_TRACKING_NP);
                        //this.show(ViewType.MAP_TRACKING_NP, "Flight Search Results");
                    }
                    case FLIGHT -> {
                        this.loadedData = search.forFlight(inputs);
                        this.show(MAP_TRACKING);
                        //this.show(ViewType.MAP_TRACKING, "Flight Search Results");
                    }
                    case PLANE -> {
                        this.loadedData = search.forPlane(inputs);
                        this.show(MAP_FROMSEARCH);
                        //this.show(ViewType.MAP_FROMSEARCH, "Plane Search Results");
                    }
                }
            } catch (DataNotFoundException e) {
                this.handleException(e);
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
            this.ui.getMap().setTileSource(currentMapSource);
            // data[2]
            livePeriodSec = Integer.parseInt(data[2]);
            setLiveDataPeriod(livePeriodSec);
        } finally {
            // saving config after reset
            FileWizard.getFileWizard().saveConfig();
        }
    }

    boolean onLiveClick(@NotNull ICoordinate clickedCoord) {

        TreasureMap map;
        List<MapMarker> markers;
        MapManager mapManager;
        List<MapMarker> newMarkerList;
        boolean markerHit = false;
        int counter;

        if (!this.clicking && LiveLoader.isLive()) {
            try {
                this.clicking = true;
                map = this.ui.getMap();
                markers = map.getMapMarkerList();
                mapManager = this.ui.getMapManager();
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
                        this.markerHit(MAP_LIVE, newMarker, counter, null, null, logger);
                    } else {
                        newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
                    }
                    newMarker.setName(marker.getName());
                    newMarkerList.add(newMarker);
                    counter++;
                }
                if (markerHit) {
                    this.ui.getMap().setMapMarkerList(newMarkerList);
                }
            } finally {
                this.clicking = false;
            }
        }
        return markerHit;
    }

    /**
     * is executed when a map marker is clicked and the current is MAP_ALL
     */
    boolean onClick_all(@NotNull ICoordinate clickedCoord) { // TODO aufteilen

        List<MapMarker> mapMarkers;
        List<MapMarker> newMarkerList;
        Vector<DataPoint> data;
        Coordinate markerCoord;
        DefaultMapMarker newMarker;
        MapManager mapManager;
        DBOut dbOut;
        Logger log;
        boolean markerHit = false;
        int counter;

        if (!this.clicking) {
            try {
                this.clicking = true;
                mapMarkers = this.ui.getMap().getMapMarkerList();
                newMarkerList = new ArrayList<>();
                mapManager = this.ui.getMapManager();
                counter = 0;
                data = this.loadedData;
                dbOut = DBOut.getDBOut();
                log = this.getLogger();
                // going though markers
                for (MapMarker m : mapMarkers) {
                    markerCoord = m.getCoordinate();
                    newMarker = new DefaultMapMarker(markerCoord, 90); // FIXME: 13.05.2022 // FIXME 19.05.2022
                    if (mapManager.isMarkerHit(markerCoord, clickedCoord) && !markerHit) {
                        markerHit = true;
                        this.markerHit(MAP_FROMSEARCH, newMarker, counter, data, dbOut, log);
                    } else {
                        newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
                    }
                    newMarker.setName(m.getName());
                    newMarkerList.add(newMarker);
                    counter++;
                }
                if (markerHit) {
                    this.ui.getMap().setMapMarkerList(newMarkerList);
                }
            } finally {
                this.clicking = false;
            }
        }
        return markerHit;
    }

    private void markerHit(ViewType viewType, DefaultMapMarker marker,
                           int counter, Vector<DataPoint> dataPoints,
                           DBOut dbOut, Logger logger) {

        Flight flight;
        int flightID;
        DataPoint dataPoint;

        switch (viewType) {
            case MAP_FROMSEARCH -> {
                marker.setBackColor(Color.RED);
                dataPoint = dataPoints.get(counter);
                flightID = dataPoint.flightID();
                try {
                    flight = dbOut.getFlightByID(flightID);
                    //this.ui.showMap(MAP_TRACKING);
                    this.ui.showInfo(flight, dataPoint);
                } catch (DataNotFoundException e) {
                    logger.errorLog("flight with the ID " + flightID + " doesn't exist!", this);
                }
            }
            case MAP_LIVE -> {
                marker.setBackColor(Color.RED);
                flight = this.liveData.get(counter);
                dataPoint = flight.dataPoints().get(0);
                this.ui.showInfo(flight, dataPoint);
            }
        }
    }

    /**
     *
     * @param clickedCoord is the clicked coordinate
     */
    boolean onTrackingClick(ICoordinate clickedCoord) { // TODO aufteilen
        TreasureMap map = this.ui.getMap();
        List<MapMarker> markers = map.getMapMarkerList();
        MapManager mapManager = this.ui.getMapManager();
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
                dp = this.loadedData.get(counter);
                flightID = dp.flightID();
                map.setMapMarkerList(mapManager.resetTrackingMarkers(m));
                try {
                    flight = dbOut.getFlightByID(flightID);
                    this.ui.showInfo(flight, dp);
                } catch (DataNotFoundException e) {
                    this.handleException(e);
                    this.getLogger().errorLog("flight with the ID " + flightID + " doesn't exist!", this);
                }
                break;
            }
            counter++;
        }
        return markerHit;
    }

    public void saveFile() {

        JFileChooser fileChooser;
        Rectangle rect;
        MapData mapData;
        File selected;

        this.setLoading(true);

        //this.gui.setCurrentVisibleRect(this.ui.getMap().getVisibleRect()); // TODO visible rect beim repainten speichern

        fileChooser = MenuModels.fileSaver(this.ui.getWindow());
        rect = /*(this.gui.getCurrentVisibleRect() != null) ? this.gui.getCurrentVisibleRect() :*/ null;
        selected = fileChooser.getSelectedFile();
        if (selected == null) {
            this.done(false);
            return;
        }
        // new MapData object with loadedData, view type and visible rectangle
        ViewType currentViewType = this.ui.getCurrentViewType();
        mapData = new MapData(this.loadedData, currentViewType, rect);
        try {
            FileWizard.getFileWizard().savePlsFile(mapData, selected);
        } catch (DataNotFoundException | FileAlreadyExistsException e) {
            this.handleException(e);
        } finally {
            this.done(false);
        }
    }

    public void loadFile() {

        File file = this.ui.getSelectedFile();
        if (file == null) {
            return;
        }
        try {
            FileWizard fileWizard = FileWizard.getFileWizard();
            MapData loaded = fileWizard.loadPlsFile(file);
            this.loadedData = loaded.data();
            this.show(loaded.viewType());
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * handles exceptions, if an exception occurs,
     * execute this method to handle it
     *
     * @param thr is the throwable (usually an exception)
     *            which is thrown and going to be handled
     */
    public void handleException(final Throwable thr) {

        if (thr instanceof DataNotFoundException dnf) {
            this.ui.showWarning(Warning.NO_DATA_FOUND, dnf.getMessage());
        } else if (thr instanceof SQLException sql) {
            String message = sql.getMessage();
            thr.printStackTrace();
            this.ui.showWarning(Warning.SQL_ERROR, message);
            if (message.contains("BUSY")) {
                // emergency shutdown because of DB-Bug
                this.emergencyShutdown(sql);
            }
        } else if (thr instanceof TimeoutException) {
            this.ui.showWarning(Warning.TIMEOUT);
        } else if (thr instanceof RejectedExecutionException) {
            this.ui.showWarning(Warning.REJECTED_EXECUTION);
        } else if (thr instanceof IllegalInputException) {
            this.ui.showWarning(Warning.ILLEGAL_INPUT);
        } else if (thr instanceof InvalidDataException ide) {
            this.ui.showWarning(Warning.UNKNOWN_ERROR, ide.getMessage() + "\n" + ide.getCause().getMessage());
            ide.printStackTrace();
        } else if (thr instanceof ClassNotFoundException cnf) {
            this.ui.showWarning(Warning.UNKNOWN_ERROR, cnf.getMessage());
            cnf.printStackTrace();
        } else if (thr instanceof FileAlreadyExistsException fae) {
            this.ui.showWarning(Warning.FILE_ALREADY_EXISTS, fae.getMessage());
            fae.printStackTrace();
        } else {
            this.ui.showWarning(Warning.UNKNOWN_ERROR, thr.getMessage());
            thr.printStackTrace();
        }
    }

    // private methods

    private void showSignificanceMap(@NotNull MapManager bbn, DBOut dbOut) {

        Deque<Airport> aps;
        Map<Airport, Integer> signifMap;
        Statistics stats = new Statistics();

        try {
            aps = dbOut.getAllAirports();
            signifMap = stats.airportSignificance(aps);
            bbn.createSignificanceMap(signifMap, this.ui.getMap());
        } catch (DataNotFoundException e) {
            logger.errorLog(e.getMessage(), this);
            e.printStackTrace();
        }
    }

    private void showTrackingMapNoPoints(@NotNull MapManager mapManager) {

        Flight flight = null;
        DBOut dbOut = DBOut.getDBOut();

        int flightID = (!this.loadedData.isEmpty()) ? this.loadedData.get(0).flightID() : -1;
        try {
            flight = dbOut.getFlightByID(flightID);
        } catch (DataNotFoundException dnf) {
            logger.errorLog(dnf.getMessage(), this);
        }
        mapManager.createTrackingMap(this.loadedData, flight, false);
    }

    private void showTrackingMap(@NotNull MapManager mapManager) {

        DBOut dbOut = DBOut.getDBOut();
        Flight flight = null;
        Vector<DataPoint> data = this.loadedData;

        int flightID = (!data.isEmpty()) ? data.get(0).flightID() : -1;
        try {
            flight = (flightID != -1) ? dbOut.getFlightByID(flightID) : null;
        } catch (DataNotFoundException ignored) {
        }
        mapManager.createTrackingMap(data, flight, true);
    }

    private void showSearchMap(@NotNull MapManager mapManager) {
        if (this.loadedData.isEmpty()) {
            return;
        }
        mapManager.createTrackingMap(this.loadedData, null, true);
    }

    private void showFlightList(DBOut dbOut) {

        List<Flight> flights;
        Deque<Integer> fids;
        TreePlantation treePlant;
        int flightID;

        if (this.loadedData == null || this.loadedData.isEmpty()) {
            this.ui.showWarning(Warning.NO_DATA_FOUND);
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
        /*treePlant = this.ui.getTreePlantation();
        treePlant.createTree(treePlant.allFlightsTreeNode(flights), this.gui);*/
    }

    /**
     * getter for the logger
     *
     * @return main logger instance
     */
    @NotNull
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * getter for the scheduler
     *
     * @return main scheduler instance
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * getter for the user interface
     *
     * @return main UserInterface instance
     */
    public UserInterface getUI() {
        return this.ui;
    }

    /**
     * sets the period for the live-data loader in seconds
     *
     * @param sec is the period in seconds
     */
    public void setLiveDataPeriod(@Range(from = 1, to = 10) int sec) {
        liveDataPeriodSec = sec;
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

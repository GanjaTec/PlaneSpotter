package planespotter.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.Range;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import planespotter.model.io.FileWizard;
import planespotter.util.LRUCache;
import planespotter.constants.UserSettings;
import planespotter.constants.ViewType;
import planespotter.constants.Warning;
import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.display.models.MenuModels;
import planespotter.model.*;
import planespotter.model.io.DBOut;
import planespotter.model.io.DBWriter;
import planespotter.statistics.RasterHeatMap;
import planespotter.statistics.Statistics;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.IllegalInputException;
import planespotter.throwables.InvalidDataException;
import planespotter.util.Logger;
import planespotter.util.math.MathUtils;
import planespotter.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.nio.file.FileAlreadyExistsException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
public abstract class Controller {

    // static fields

    // runtime instance
    public static final Runtime RUNTIME;
    // project root path / working directory
    public static final String ROOT_PATH;
    // ONLY Controller instance
    private static final Controller INSTANCE;
    // scheduler, contains executor services / thread pools
    private static final Scheduler SCHEDULER;
    // gui action handler (contains listeners)
    private static final ActionHandler ACTION_HANDLER;
    // only GUI instance
    private static final GUI GUI;
    // gui adapter
    public static final GUIAdapter GUI_ADAPTER; // TODO private with GETTER
    // proto test-cache
    public static final LRUCache<String, Object> CACHE;
    // live data loading period
    private static int LIVE_DATA_PERIOD_SEC = 2;
    // supplier enabled flag, indicates if the supplier is running right now
    // TODO: 28.06.2022 move to Supplier Mains
    private static boolean supplierRunning;
    // logger for whole program
    private static Logger logger;
    // live data thread
    private static Thread liveThread;
    // static initializer
    static {
        // initializing 'root' members
        RUNTIME = Runtime.getRuntime();
        INSTANCE = new Controller() {};
        ROOT_PATH = Utilities.getAbsoluteRootPath();
        // initializing one-instance members
        SCHEDULER = new Scheduler();
        ACTION_HANDLER = new ActionHandler();
        GUI = new GUI(ACTION_HANDLER);
        GUI_ADAPTER = new GUIAdapter(GUI);
        CACHE = new LRUCache<>(40); // TODO best cache size
        setSupplierRunning(false);
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

    /**
     * private constructor for Controller,
     * get instance with Controller.getInstance()
     */
    private Controller() {
        this.hashCode = System.identityHashCode(INSTANCE);
        this.clicking = false;
    }

    /**
     * @return ONE and ONLY controller instance
     */
    public static Controller getInstance() {
        return INSTANCE;
    }

    /**
     * @return main logger
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * @return main scheduler
     */
    public static Scheduler getScheduler() {
        return SCHEDULER;
    }

    /**
     * @return main gui
     */
    public static GUI getGUI() {
        return GUI;
    }

    /**
     * @return main ActionHandler
     */
    public static ActionHandler getActionHandler() {
        return ACTION_HANDLER;
    }

    public static boolean isSupplierRunning() {
        return supplierRunning;
    }

    public static void setLiveDataPeriodSec(@Range(from = 1, to = 10) int s) {
        LIVE_DATA_PERIOD_SEC = s;
    }

    public static void setSupplierRunning(boolean run) {
        supplierRunning = run;
    }

    /**
     * starts the program by initializing the controller
     * and opening a GUI-window
     */
    public synchronized void start() {
        this.initialize();
        this.openWindow();
        GUI_ADAPTER.onInitFinish();
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
            logger.sucsessLog("Controller initialized sucsessfully!", this);
            this.initialized = true;
        }
    }

    /**
     * starts all Controller tasks with the Scheduler
     */
    private void initTasks() {
        if (!this.initialized) {
            logger.log("initializing Executors...", this);

            liveThread = SCHEDULER.runThread(this::liveDataTask, "Live-Data PreLoader", true, Scheduler.HIGH_PRIO);
            SCHEDULER.schedule(() -> {
                        if (!this.loading) FileWizard.getFileWizard().saveConfig();
                    }, 60, 300)
                    .schedule(() -> {
                        if (!this.loading && !isSupplierRunning()) {
                            var current = Thread.currentThread();
                            current.setName("Data Inserter");
                            current.setPriority(2);
                            DBWriter.insert(SCHEDULER, 500);
                        }
                    }, 20, 20)
                    .schedule(() -> {
                        System.gc();
                        logger.log("Calling Garbage Collector...", this);
                    }, 10, 10);
                    // loading live date if live map is open
                    /*.schedule(() -> {
                        if (LiveData.isLive() && !this.isLoading()) {
                            Thread.currentThread().setName("Live Loader");
                            this.loadLiveData();
                        }
                    }, 0, LIVE_DATA_PERIOD_SEC);*/

            logger.sucsessLog("Executors initialized sucsessfully!", this);
        }
    }

    /**
     * opens a new GUI window as a thread
     */
    private synchronized void openWindow() {
        this.setLoading(true);
        logger.log("initialising GUI...", GUI);
        // starting loading screen
        SCHEDULER.exec(GUI::startLoadingScreen, "Loading Screen", false, Scheduler.MID_PRIO, false);

        logger.sucsessLog("GUI initialized sucsessfully!", GUI);
        GUI.getComponent("window").setVisible(true);
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
        int option = JOptionPane.showConfirmDialog(GUI.getComponent("window"),
                "Do you really want to exit PlaneSpotter?",
                    "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            logger.infoLog("Shutting down program, please wait...", this);
            GUI.getComponent("progressBar").setVisible(true);
            LiveData.setLive(false);
            if (liveThread != null) {
                liveThread.interrupt();
            }
            this.setLoading(true);
            FileWizard.getFileWizard().saveConfig();
            if (insertRemainingFrames) {
                DBWriter.insertRemaining(SCHEDULER, 1000);
            }
            while (SCHEDULER.active() > 0) {
                try {
                    this.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.notifyAll();
            DBWriter.setEnabled(false);
            this.done();
            logger.close();
            boolean shutdown = SCHEDULER.shutdown(1);
            byte out = MathUtils.toBinary(shutdown);
            System.exit(out);

        }
    }

    private void emergencyShutdown(Throwable problem) {
        problem.printStackTrace();
        FileWizard.getFileWizard().saveLogFile("error", System.out.toString());
        RUNTIME.halt(-1);
    }

    private synchronized void liveDataTask() {
        this.loadLiveData();
        for (;;) {
            try {
                this.wait(TimeUnit.SECONDS.toMillis(LIVE_DATA_PERIOD_SEC));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.notify();
            }
            if (LiveData.isLive()) {
                this.loadLiveData();
            }
        }
    }

    /**
     * loads the live data from Fr24 directly into the View
     * @indev
     */
    private synchronized void loadLiveData() {
        this.setLoading(true);

        this.liveData = LiveData.directLiveData(SCHEDULER, GUI.getMap());
        var map = GUI.getMap();
        var markers = new ArrayList<MapMarker>();
        this.liveData.stream()
                .map(flight -> {
                    final var pos = flight.dataPoints().get(0).pos();
                    final double lat = pos.lat(),
                            lon = pos.lon();
                    return new MapMarkerDot(new Coordinate(lat, lon));
                })
                .forEach(m -> {
                    m.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
                    markers.add(m);
                });
        //map.removeAllMapMarkers(); // redundant?
        map.setMapMarkerList(markers);
        this.done();
    }

    /**
     * this method is executed when a loading process is done
     * sets this.loading to false
     */
    public void done() {
        this.loading = false;
        if (GUI != null) {
            GUI_ADAPTER.stopProgressBar();
        }
    }

    /**
     * creates a GUI-view for a specific view-type and specific data
     *
     * @param type is the ViewType, sets the content type for the
     *             created view (e.g. different List-View-Types)
     * @param headText is the view-head-text
     * @param data are the flight-ids to show, may be null if the data is
     *             collected directly from this.loadedData
     */
    public synchronized void show(@NotNull ViewType type,
                                  @NotNull final String headText,
                                  @Nullable String... data) {
        var mapManager = GUI.getMapManager();
        this.setLoading(true);
        var dbOut = DBOut.getDBOut();
        // TODO ONLY HERE: dispose GUI view(s)
        GUI_ADAPTER.disposeView();
        GUI.setCurrentViewType(type);
        switch (type) {
            case LIST_FLIGHT -> this.showFlightList(dbOut);
            case MAP_LIVE -> this.showLiveMap(headText, mapManager);
            case MAP_FROMSEARCH -> this.showSearchMap(headText, mapManager);
            case MAP_TRACKING -> this.showTrackingMap(headText, mapManager, dbOut, data);
            case MAP_TRACKING_NP -> this.showTrackingMapNoPoints(headText, mapManager, data);
            case MAP_SIGNIFICANCE -> this.showSignificanceMap(headText, mapManager, dbOut);
            case MAP_HEATMAP -> this.showRasterHeatMap(headText, mapManager, dbOut);
        }
        this.done();
        logger.sucsessLog("view loaded!", this);
    }

    /**
     * search method for the GUI-search
     *
     * @param inputs are the inputs in the search fields
     * @param button is the clicked search button, 0 = LIST, 1 = MAP
     */
    // TODO: 24.05.2022 DEBUG PLANE SEARCH
    public void search(String[] inputs, int button) { // TODO button abfragen??
        var gad = GUI_ADAPTER;
        this.setLoading(true);
        try {
            gad.startProgressBar();
            var search = new Search();
            switch (GUI.getCurrentSearchType()) {
                case AIRLINE -> {} // TODO implement
                case AIRPORT -> this.searchForAirport(inputs, button, search);
                case FLIGHT -> this.searchForFlight(inputs, button, search);
                case PLANE -> this.searchForPlane(inputs, button, search);
            }
        } catch (DataNotFoundException e) {
            this.handleException(e);
        } finally {
            gad.stopProgressBar();
        }
    }

    /**
     *
     * @param data [0] and [1] must be filled
     */
    public void confirmSettings(String... data) {
        if (data[0] == null || data[1] == null || data[2] == null) {
            throw new IllegalArgumentException("Please fill all fields! (with the right params)");
        }
        GUI_ADAPTER.startProgressBar();
        try {
            // data[0]
            int maxLoadedData = Integer.parseInt(data[0]);
            UserSettings.setMaxLoadedData(maxLoadedData);
            var map = UserSettings.getCurrentMapSource();
            // data[1]
            switch (data[1]) {
                case "Bing Map" -> map = UserSettings.BING_MAP;
                case "Default Map" -> map = UserSettings.DEFAULT_MAP;
                case "Transport Map" -> map = UserSettings.TRANSPORT_MAP;
            }
            UserSettings.setCurrentMapSource(map);
            GUI.getMap().setTileSource(map);
            // data[2]
            int livePeriodSec = Integer.parseInt(data[2]);
            setLiveDataPeriodSec(livePeriodSec);
        } finally {
            GUI_ADAPTER.stopProgressBar();
        }
    }

    void onLiveClick(ICoordinate clickedCoord) {
        if (!this.clicking && LiveData.isLive()) {
            try {
                this.clicking = true;
                var map = GUI.getMap();
                var markers = map.getMapMarkerList();
                var mapManager = GUI.getMapManager();
                var tpl = GUI.getTreePlantation();
                var newMarkerList = new ArrayList<MapMarker>();
                boolean markerHit = false;
                int counter = 0;
                DefaultMapMarker newMarker;
                Coordinate markerCoord;
                for (var marker : markers) {
                    markerCoord = marker.getCoordinate();
                    newMarker = new DefaultMapMarker(markerCoord, 0);
                    if (!markerHit && mapManager.isMarkerHit(markerCoord, clickedCoord)) {
                        markerHit = true;
                        var menu = (JPanel) GUI.getComponent("menuPanel");
                        this.markerHit(MAP_LIVE, newMarker, counter, null, null, tpl, logger, menu);
                    } else {
                        newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
                    }
                    newMarker.setName(marker.getName());
                    newMarkerList.add(newMarker);
                    counter++;
                }
                if (markerHit) {
                    GUI.getMap().setMapMarkerList(newMarkerList);
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
        if (!this.clicking) {
            try {
                this.clicking = true;
                var mapMarkers = GUI.getMap().getMapMarkerList();
                var newMarkerList = new ArrayList<MapMarker>();
                Coordinate markerCoord;
                DefaultMapMarker newMarker;
                boolean markerHit = false;
                var bbn = GUI.getMapManager();
                var ctrl = Controller.getInstance();
                int counter = 0;
                var data = ctrl.loadedData;
                var dbOut = DBOut.getDBOut();
                var tpl = GUI.getTreePlantation();
                var logger = Controller.getLogger();
                var menu = (JPanel) GUI.getComponent("menuPanel");
                // going though markers
                for (var m : mapMarkers) {
                    markerCoord = m.getCoordinate();
                    newMarker = new DefaultMapMarker(markerCoord, 90); // FIXME: 13.05.2022 // FIXME 19.05.2022
                    if (bbn.isMarkerHit(markerCoord, clickedCoord) && !markerHit) {
                        markerHit = true;
                        this.markerHit(MAP_FROMSEARCH, newMarker, counter, data, dbOut, tpl, logger, menu);
                    } else {
                        newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
                    }
                    newMarker.setName(m.getName());
                    newMarkerList.add(newMarker);
                    counter++;
                }
                if (markerHit) {
                    GUI.getMap().setMapMarkerList(newMarkerList);
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
        switch (viewType) {
            case MAP_FROMSEARCH -> {
                marker.setBackColor(Color.RED);
                menuPanel.setVisible(false);
                int flightID = dataPoints.get(counter).flightID();
                try {
                    //this.show(MAP_TRACKING, "Flight '" + flightID + "'", String.valueOf(flightID));
                    this.showTrackingMap("Flight '" + flightID + "'", GUI.getMapManager(),
                                         dbOut, new String[] { String.valueOf(flightID) });
                    var flight = dbOut.getFlightByID(flightID);
                    treePlantation.createFlightInfo(flight, GUI_ADAPTER);
                } catch (DataNotFoundException e) {
                    logger.errorLog("flight with the ID " + flightID + " doesn't exist!", this);
                }
            }
            case MAP_LIVE -> {
                marker.setBackColor(Color.RED);
                var flight = this.liveData.get(counter);
                menuPanel.setVisible(false);
                var dps = flight.dataPoints();
                treePlantation.createDataPointInfo(flight, dps.get(0), GUI_ADAPTER);
            }
        }
    }

    /**
     *
     * @param clickedCoord is the clicked coordinate
     */
    public void onTrackingClick(ICoordinate clickedCoord) { // TODO aufteilen
        var map = GUI.getMap();
        var markers = map.getMapMarkerList();
        Coordinate markerCoord;
        int counter = 0;
        var bbn = GUI.getMapManager();
        var ctrl = Controller.getInstance();
        DataPoint dp;
        int flightID;
        Flight flight;
        var tpl = GUI.getTreePlantation();
        var dbOut =  DBOut.getDBOut();
        for (var m : markers) {
            markerCoord = m.getCoordinate();
            if (bbn.isMarkerHit(markerCoord, clickedCoord)) {
                GUI.getComponent("infoPanel").removeAll();
                dp = ctrl.loadedData.get(counter);
                flightID = dp.flightID();
                map.setMapMarkerList(bbn.resetTrackingMarkers(m));
                try {
                    flight = dbOut.getFlightByID(flightID);
                    tpl.createDataPointInfo(flight, dp, GUI_ADAPTER);
                } catch (DataNotFoundException e) {
                    Controller.getLogger().errorLog("flight with the ID " + flightID + " doesn't exist!", this);
                }
                break;
            }
            counter++;
        }
    }

    public void saveFile() {
        GUI.setCurrentVisibleRect(GUI.getMap().getVisibleRect()); // TODO visible rect beim repainten speichern
        var fileChooser = MenuModels.fileSaver((JFrame) GUI.getComponent("window"));
        var rect = (GUI.getCurrentVisibleRect() != null) ? GUI.getCurrentVisibleRect() : null;
        // new MapData object with loadedData, view type and visible rectangle
        var mapData = new MapData(this.loadedData, MAP_TRACKING, rect);
        try {
            FileWizard.getFileWizard().savePlsFile(mapData, fileChooser.getSelectedFile(), this);
        } catch (DataNotFoundException | FileAlreadyExistsException e) {
            this.handleException(e);
        }
    }

    public void loadFile() {
        try {
            var mapManager = GUI.getMapManager();
            var fileChooser = MenuModels.fileLoader((JFrame) GUI.getComponent("window"));

            this.loadedData = FileWizard.getFileWizard().loadPlsFile(fileChooser);
            var trackingMap = mapManager.createTrackingMap(this.loadedData, null, true, Controller.GUI_ADAPTER);
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
     * @param thr is the throwable (exceptions in the most cases)
     *            which is thrown and will be handled
     */
    public void handleException(final Throwable thr) {
        if (thr instanceof DataNotFoundException dnf) {
            GUI_ADAPTER.showWarning(Warning.NO_DATA_FOUND, dnf.getMessage());
        } else if (thr instanceof SQLException sql) {
            var message = sql.getMessage();
            thr.printStackTrace();
            GUI_ADAPTER.showWarning(Warning.SQL_ERROR, message + "\n" + sql.getSQLState());
            if (message.contains("BUSY")) {
                // emergency shutdown because of DB-Bug
                this.emergencyShutdown(sql);
            }
        } else if (thr instanceof TimeoutException) {
            GUI_ADAPTER.showWarning(Warning.TIMEOUT);
        } else if (thr instanceof RejectedExecutionException) {
            GUI_ADAPTER.showWarning(Warning.REJECTED_EXECUTION);
        } else if (thr instanceof IllegalInputException) {
            GUI_ADAPTER.showWarning(Warning.ILLEGAL_INPUT);
        } else if (thr instanceof InvalidDataException ide) {
            GUI_ADAPTER.showWarning(Warning.UNKNOWN_ERROR, ide.getMessage() + "\n" + ide.getCause().getMessage());
            ide.printStackTrace();
        } else if (thr instanceof ClassNotFoundException cnf) {
            GUI_ADAPTER.showWarning(Warning.UNKNOWN_ERROR, cnf.getMessage());
            cnf.printStackTrace();
        } else if (thr instanceof FileAlreadyExistsException fae) {
            GUI_ADAPTER.showWarning(Warning.FILE_ALREADY_EXISTS, fae.getMessage());
            fae.printStackTrace();
        } else {
            GUI_ADAPTER.showWarning(Warning.UNKNOWN_ERROR, thr.getMessage());
            thr.printStackTrace();
        }
    }

    // private methods

    private void showRasterHeatMap(String heatText, MapManager bbn, DBOut dbOut) {
        try {
            var positions = (Vector<Position>) CACHE.get("allTrackingPosVec");
            if (positions == null) {
                positions = dbOut.getAllTrackingPositions();
                if (!CACHE.put("allTrackingPosVec", positions)) {
                    System.out.println("Cache is full of Senior Data!");
                }
            }
            var viewer = GUI.getMap();
            var img = new RasterHeatMap(1f) // TODO replace durch addHeatMap
                    .heat(positions)
                    .createImage();
            bbn.createRasterHeatMap(img, viewer)
                    .receiveMap(viewer, heatText, MAP_HEATMAP);
        } catch (DataNotFoundException e) {
            this.handleException(e);
        }
    }

    private void showCircleHeatMap(String headText, MapManager bbn, DBOut dbOut) {
        //var liveTrackingBetween = dbOut.getLiveTrackingBetween(10000, 25000);
        //var positions = Utilities.parsePositionVector(liveTrackingBetween);
        //var positionHeatMap = new Statistics().positionHeatMap(positions);
        //var map = bbn.createPrototypeHeatMap(positionHeatMap)
        Vector<Position> positions;
        try {
            positions = dbOut.getAllTrackingPositions();
            var viewer = GUI.getMap();
            viewer.setHeatMap(new RasterHeatMap(1f) // TODO: 26.05.2022 addHeatMap
                    .heat(positions)
                    .createImage());
            bbn.receiveMap(viewer, headText, MAP_HEATMAP);
        } catch (DataNotFoundException e) {
            this.handleException(e);
        }
    }

    private void showSignificanceMap(String headText, MapManager bbn, DBOut dbOut) {
        try {
            var aps = dbOut.getAllAirports();
            var signifMap = new Statistics().airportSignificance(aps);
            var map = bbn.createSignificanceMap(signifMap, GUI.getMap());
            bbn.receiveMap(map, headText, MAP_HEATMAP);
        } catch (DataNotFoundException e) {
            logger.errorLog(e.getMessage(), this);
            e.printStackTrace();
        }
    }

    private void showTrackingMapNoPoints(String headText, MapManager bbn, @Nullable String[] data) {
        try {
            loadedData = new Vector<>();
            var out = DBOut.getDBOut();
            int flightID = -1;
            if (data.length == 1) {
                assert data[0] != null;
                flightID = Integer.parseInt(data[0]);
                loadedData.addAll(out.getTrackingByFlight(flightID));
            }
            else if (data.length > 1) {
                for (var id : data) {
                    assert id != null;
                    flightID = Integer.parseInt(id);
                    loadedData.addAll(out.getTrackingByFlight(flightID));
                }
            }
            var flight = out.getFlightByID(flightID);
            var flightRoute = bbn.createTrackingMap(this.loadedData, flight, false, GUI_ADAPTER);
            bbn.receiveMap(flightRoute, headText, MAP_TRACKING_NP);
        } catch (NumberFormatException e) {
            logger.errorLog("NumberFormatException while trying to parse the ID-String! Must be an int!", this);
        } catch (DataNotFoundException e) {
            logger.errorLog(e.getMessage(), this);
        }
    }

    private void showTrackingMap(String headText, MapManager bbn, DBOut dbOut, @Nullable String[] data) {
        try {
            int flightID = -1;

            Vector<DataPoint> flightTracking;
            String key;
            if (data.length == 1) {
                assert data[0] != null;
                flightID = Integer.parseInt(data[0]);
                key = "tracking" + flightID;
                flightTracking = (Vector<DataPoint>) CACHE.get(key);
                if (flightTracking == null) {
                    flightTracking = dbOut.getTrackingByFlight(flightID); // FIXME: 29.06.2022 lade ich das doppelt?
                    CACHE.put(key, flightTracking);
                }
                this.loadedData.addAll(flightTracking);

            } else if (data.length > 1) {
                key = "tracking" + Arrays.toString(data);
                this.loadedData = (Vector<DataPoint>) CACHE.get(key);
                if (this.loadedData == null) {
                    this.loadedData = new Vector<>();
                    for (var id : data) {
                        assert id != null;
                        flightID = Integer.parseInt(id);
                        flightTracking = dbOut.getTrackingByFlight(flightID);
                        this.loadedData.addAll(flightTracking);
                        CACHE.put(key, this.loadedData);
                    }

                }
            }
            if (flightID == -1) {
                throw new InvalidDataException("Flight may not be null!");
            }
            var flight = dbOut.getFlightByID(flightID);
            var trackingMap = bbn.createTrackingMap(this.loadedData, flight, true, GUI_ADAPTER);
            bbn.receiveMap(trackingMap, headText, MAP_TRACKING);
        } catch (NumberFormatException e) {
            logger.errorLog("NumberFormatException while trying to parse the ID-String! Must be an int!", this);
        } catch (DataNotFoundException e) {
            logger.errorLog(e.getMessage(), this);
        }
    }

    private void showSearchMap(String headText, MapManager bbn) {
        var data = Utilities.parsePositionVector(this.loadedData);
        var viewer = bbn.createLiveMap(data, GUI.getMap());
        bbn.receiveMap(viewer, headText, MAP_FROMSEARCH);
    }

    private void showLiveMap(final String headText, @NotNull final MapManager mapManager) {
        if (this.liveData == null || this.liveData.isEmpty()) {
            GUI_ADAPTER.showWarning(Warning.LIVE_DATA_NOT_FOUND);
            return;
        }
        var data = Utilities.parsePositionVector(this.liveData);
        var viewer = mapManager.createLiveMap(data, GUI.getMap());

        LiveData.setLive(true);
        mapManager.receiveMap(viewer, headText, MAP_LIVE);
    }

    private void showFlightList(DBOut dbOut) {
        if (this.loadedData == null || this.loadedData.isEmpty()) {
            GUI_ADAPTER.showWarning(Warning.NO_DATA_FOUND);
            return;
        }
        var flights = new ArrayList<Flight>();
        int flightID = -1;
        try {
            var fids = dbOut.getLiveFlightIDs(10000, 25000);
            while (!fids.isEmpty()) {
                flightID = fids.poll();
                flights.add(dbOut.getFlightByID(flightID));
            }
        } catch (DataNotFoundException e) {
            logger.errorLog("flight with  ID " + flightID + " doesn't exist!", this);
            this.handleException(e);
        }
        var treePlant = GUI.getTreePlantation();
        treePlant.createTree(treePlant.allFlightsTreeNode(flights), GUI_ADAPTER);
    }

    private void searchForPlane(String[] inputs, int button, Search search)
            throws DataNotFoundException {

        this.loadedData = search.verifyPlane(inputs);
        var idsNoDupl = new ArrayList<Integer>();
        int flightID;
        for (var dp : this.loadedData) {
            flightID = dp.flightID();
            if (!idsNoDupl.contains(flightID)) {
                idsNoDupl.add(flightID);
            }
        }
        int size = idsNoDupl.size();
        var ids = new String[size];
        for (int i = 0; i < size; i++) {
            ids[i] = idsNoDupl.get(i) + "";
        }
        if (button == 1) {
            var headText = "Plane Search Results:";
            if (!GUI.search_planeID.getText().isBlank()) {
                this.show(ViewType.MAP_TRACKING, headText, ids); // ganze route -> nur bei einer id / wird evtl noch entfernt
            } else {
                this.show(ViewType.MAP_FROMSEARCH, headText, ids); // nur letzte data points
            }
        }
    }

    private void searchForFlight(String[] inputs, int button, Search search)
            throws DataNotFoundException {

        this.loadedData = search.verifyFlight(inputs);
        if (this.loadedData.size() == 1) {
            var dp = this.loadedData.get(0);
            if (button == 1) {
                this.show(ViewType.MAP_TRACKING, "Flight Search Results", dp.flightID() + "");
            }
        } else {
            var idsNoDupl = new ArrayList<Integer>();
            int flightID;
            for (var dp : this.loadedData) {
                flightID = dp.flightID();
                if (!idsNoDupl.contains(flightID)) {
                    idsNoDupl.add(flightID);
                }
            }
            int size = idsNoDupl.size();
            var ids = new String[size];
            for (int i = 0; i < size; i++) {
                ids[i] = idsNoDupl.get(i) + "";
            }
            if (button == 1) {
                this.show(ViewType.MAP_TRACKING, "Flight Search Results", ids);
            }
        } // TODO !!! show soll Datapoints bekommen und nicht fids, das ist eine weitere anfrage;
    }

    private void searchForAirport(String[] inputs, int button, Search search) throws DataNotFoundException {
        loadedData = search.verifyAirport(inputs);
        var idsNoDupl = new ArrayList<Integer>();
        int flightID;
        for (var dp : loadedData) {
            flightID = dp.flightID();
            if (!idsNoDupl.contains(flightID)) {
                idsNoDupl.add(flightID);
            }
        }
        int size = idsNoDupl.size();
        var ids = new String[size];
        for (int i = 0; i < size; i++) {
            ids[i] = idsNoDupl.get(i) + "";
        }
        if (button == 1) {
            this.show(ViewType.MAP_TRACKING_NP, "Flight Search Results", ids);
        }
    }

    /**
     * @return loading flag, true if Controller is loading something
     */
    public final boolean isLoading() {
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

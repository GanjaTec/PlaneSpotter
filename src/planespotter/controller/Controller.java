package planespotter.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import planespotter.constants.*;
import planespotter.display.models.PaneModels;
import planespotter.model.io.DBIn;
import planespotter.model.io.FileWizard;
import planespotter.model.nio.Fr24Supplier;
import planespotter.model.nio.LiveLoader;
import planespotter.throwables.NoAccessException;
import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.display.models.MenuModels;
import planespotter.model.*;
import planespotter.model.io.DBOut;
import planespotter.statistics.Statistics;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.IllegalInputException;
import planespotter.throwables.InvalidDataException;
import planespotter.util.Bitmap;
import planespotter.util.math.MathUtils;
import planespotter.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.*;
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

    // -- instance fields --

    // hash code
    private final int hashCode;

    // already-clicking/-initialized flag
    private boolean clicking, initialized, terminated;

    // boolean loading is true when something is loading
    private volatile boolean loading;

    // loadedData contains all loaded DataPoints
    public volatile Vector<DataPoint> loadedData;

    // liveData contains all loaded live-Flights
    public volatile Vector<Flight> liveData;

    // scheduler, contains executor services / thread pools
    @NotNull private final Scheduler scheduler;

    // new graphical user interface
    @NotNull private final UserInterface ui;

    // live data thread
    @Nullable private Thread liveThread;

    // live loader instance (for live data tasks)
    @NotNull private final LiveLoader liveLoader;

    /**
     * private constructor for Controller main instance,
     * get instance with Controller.getInstance()
     */
    private Controller() {
        this.hashCode = System.identityHashCode(INSTANCE);
        this.scheduler = new Scheduler();
        this.ui = new UserInterface(ActionHandler.getActionHandler());
        this.liveLoader = new LiveLoader();
        this.clicking = false;
        this.terminated = false;
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
     * and opening a UI-window
     */
    public synchronized void start() {
        PaneModels.startScreenAnimation(2);
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
            this.liveLoader.setLive(true);
            this.liveThread = this.scheduler.runThread(() -> this.liveLoader.liveDataTask(this), "Live-Data Loader", true, Scheduler.HIGH_PRIO);
            this.initialized = true;
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
        if (option != JOptionPane.YES_OPTION) {
            return; // no-option case, no shutdown, abort method
        }
        this.ui.showLoadingScreen(true);
        // disabling tasks
        this.liveLoader.setLive(false);
        if (this.liveThread != null && this.liveThread.isAlive()) {
            this.liveThread.interrupt();
        }
        this.setLoading(true);
        // saving the configuration in 'config.psc'
        FileWizard fileWizard = FileWizard.getFileWizard();
        fileWizard.saveConfig();
        if (Configuration.SAVE_LOGS) {
            fileWizard.saveLogFile("a_log", "[DEBUG] No output text yet...");
        }
        // inserting remaining frames, if option is enabled
        if (insertRemainingFrames && DBIn.isEnabled()) {
            try {
                DBIn.insertRemaining(this.scheduler, this.liveLoader);
            } catch (NoAccessException ignored) {
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
        this.terminated = true;
        // shutting down scheduler
        boolean shutdown = this.scheduler.shutdown(1);
        byte exitStatus = MathUtils.toBinary(shutdown);
        // shutting down the VM
        RUNTIME.exit(exitStatus);

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
            this.ui.showLoadingScreen(false);
        }
    }

    /**
     * starts a {@link Fr24Collector} to collect Fr24-Data
     * @see planespotter.model.Fr24Collector
     */
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

        try {
            this.setLoading(true);
            this.ui.showLoadingScreen(true);

            if (type != MAP_LIVE) {
                this.liveLoader.setLive(false);
            }
            this.ui.setViewType(type);
            this.ui.getMapManager().clearMap();

            switch (type) {
                case LIST_FLIGHT -> { /*removed Flight-List implementation*/ }
                case MAP_LIVE -> this.liveLoader.setLive(true);
                case MAP_FROMSEARCH,
                        MAP_TRACKING -> this.showTrackingMap(mapManager, true);
                case MAP_TRACKING_NP -> this.showTrackingMap(mapManager, false);
                // significance map should be improved
                case MAP_SIGNIFICANCE -> this.showSignificanceMap(mapManager, dbOut);
                // heatmap should be implemented
                case MAP_HEATMAP -> {  }
            }
        } finally {
            this.done(false);
        }
    }

    /**
     * search method for different data from the DB
     *
     * @param inputs are the inputs in the search fields
     * @param button is the clicked search button, 0 = LIST, 1 = MAP
     */
    // TODO: 24.05.2022 DEBUG PLANE SEARCH, AIRLINE SEARCH
    public void search(@NotNull String[] inputs, int button) {

        try {
            Utilities.checkInputs(inputs);
        } catch (IllegalInputException e) {
            this.handleException(e);
            return;
        }

        Search search;
        if (button == 1) {
            this.ui.showLoadingScreen(true);
            this.setLoading(true);

            search = new Search();
            SearchType currentSearchType = this.ui.getSearchPanel().getCurrentSearchType();
            try {
                switch (currentSearchType) {
                    case AIRLINE -> {
                        this.loadedData = search.forAirline(inputs);
                        this.show(MAP_TRACKING_NP);
                    }
                    case AIRPORT -> {
                        this.loadedData = search.forAirport(inputs);
                        this.show(MAP_TRACKING_NP);
                    }
                    case FLIGHT -> {
                        this.loadedData = search.forFlight(inputs);
                        this.show(MAP_TRACKING);
                    }
                    case PLANE -> {
                        this.loadedData = search.forPlane(inputs);
                        this.show(MAP_FROMSEARCH);
                    }
                }
            } catch (DataNotFoundException dnf) {
                this.handleException(dnf);
            } finally {
                this.done(false);
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

        this.ui.showLoadingScreen(true);
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
            this.liveLoader.setLiveDataPeriod(livePeriodSec);
        } finally {
            // saving config after reset
            FileWizard.getFileWizard().saveConfig();
            this.done(false);
        }
    }

    // TODO: 14.08.2022 move to MapManager
    boolean onLiveClick(@NotNull ICoordinate clickedCoord) {

        TreasureMap map;
        List<MapMarker> markers;
        MapManager mapManager;
        List<MapMarker> newMarkerList;
        boolean markerHit = false;
        int counter;

        if (!this.clicking && this.liveLoader.isLive()) {
            try {
                this.clicking = true;
                map = this.ui.getMap();
                markers = map.getMapMarkerList();
                mapManager = this.ui.getMapManager();
                newMarkerList = new ArrayList<>();
                counter = 0;
                PlaneMarker newMarker;
                Coordinate markerCoord;
                int heading = 0;
                for (MapMarker marker : markers) {
                    if (marker instanceof PlaneMarker dmm) {
                        heading = dmm.getHeading();
                    }
                    markerCoord = marker.getCoordinate();
                    if (!markerHit && mapManager.isMarkerHit(markerCoord, clickedCoord)) {
                        markerHit = true;
                        newMarker = new PlaneMarker(markerCoord, heading, true, true);
                        this.onMarkerHit(MAP_LIVE, newMarker, counter, null, null);
                    } else {
                        newMarker = new PlaneMarker(markerCoord, heading, true, false);
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
    // TODO: 14.08.2022 move to mapManager
    boolean onClick_all(@NotNull ICoordinate clickedCoord) { // TODO aufteilen

        List<MapMarker> mapMarkers;
        List<MapMarker> newMarkerList;
        Vector<DataPoint> data;
        Coordinate markerCoord;
        PlaneMarker newMarker;
        MapManager mapManager;
        DBOut dbOut;
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
                // going though markers
                for (MapMarker m : mapMarkers) {
                    markerCoord = m.getCoordinate();
                    if (mapManager.isMarkerHit(markerCoord, clickedCoord) && !markerHit) {
                        markerHit = true;
                        newMarker = new PlaneMarker(markerCoord, 90, true, true);
                        this.onMarkerHit(MAP_FROMSEARCH, newMarker, counter, data, dbOut);
                    } else {
                        newMarker = new PlaneMarker(markerCoord, 90, true, false);
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
                    this.ui.showInfo(flight, dataPoint);
                } catch (DataNotFoundException ignored) {
                }
            }
            case MAP_LIVE -> {
                marker.setBackColor(Color.RED);
                flight = this.liveData.get(counter);
                dataPoint = flight.dataPoints().get(0);
                this.ui.showInfo(flight, dataPoint);
            }
        }
        if (flight != null) {
            this.ui.getMapManager().setSelectedICAO(flight.plane().icao());
        }
    }

    /**
     *
     * @param clickedCoord is the clicked coordinate
     */
    // TODO: 14.08.2022 move to MapManager
    boolean onTrackingClick(@NotNull ICoordinate clickedCoord) { // TODO aufteilen
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

        this.ui.showLoadingScreen(true);
        this.setLoading(true);

        //this.gui.setCurrentVisibleRect(this.ui.getMap().getVisibleRect()); // TODO visible rect beim repainten speichern
        // TODO 2.Möglichkeit: center und zoom speichern

        try {
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
            FileWizard.getFileWizard().savePlsFile(mapData, selected);
        } catch (DataNotFoundException | FileAlreadyExistsException e) {
            this.handleException(e);
        } finally {
            this.done(false);
        }
    }

    public void loadFile() {

        this.ui.showLoadingScreen(true);
        try {
            File file = this.ui.getSelectedFile();
            if (file == null) {
                return;
            }
            FileWizard fileWizard = FileWizard.getFileWizard();
            MapData loaded = fileWizard.loadPlsFile(file);
            this.loadedData = loaded.data();
            this.show(loaded.viewType());
        } catch (FileNotFoundException | InvalidDataException e) {
            this.handleException(e);
        } finally {
            this.done(false);
        }
    }

    /**
     * handles exceptions, if an exception occurs,
     * execute this method to handle it
     *
     * @param thr is the throwable (usually an exception)
     *            which is thrown and going to be handled
     */
    public void handleException(@NotNull final Throwable thr) {

        if (thr instanceof OutOfMemoryError oom) {
            this.ui.showWarning(Warning.OUT_OF_MEMORY);
            this.emergencyShutdown(oom);
        } else if (thr instanceof SQLException sql) {
            String message = sql.getMessage();
            thr.printStackTrace();
            this.ui.showWarning(Warning.SQL_ERROR, message);
            if (message.contains("BUSY")) {
                // emergency shutdown because of DB-Bug
                this.emergencyShutdown(sql);
            }
        } else if (thr instanceof DataNotFoundException dnf) {
            this.ui.showWarning(Warning.NO_DATA_FOUND, dnf.getMessage());
        } else if (thr instanceof TimeoutException) {
            this.ui.showWarning(Warning.TIMEOUT);
        } else if (thr instanceof RejectedExecutionException) {
            boolean terminated = thr.getMessage().contains("TERMINATED");
            this.ui.showWarning(Warning.REJECTED_EXECUTION, terminated ? "Executor is terminated!" : "");
        } else if (thr instanceof IllegalInputException) {
            this.ui.showWarning(Warning.ILLEGAL_INPUT);
        } else if (thr instanceof InvalidDataException ide) {
            this.ui.showWarning(Warning.INVALID_DATA, ide.getMessage() + ((ide.getCause() != null) ? ("\n" + ide.getCause().getMessage()) : ""));
            ide.printStackTrace();
        } else if (thr instanceof ClassNotFoundException cnf) {
            this.ui.showWarning(Warning.UNKNOWN_ERROR, cnf.getMessage());
            cnf.printStackTrace();
        } else if (thr instanceof FileAlreadyExistsException fae) {
            this.ui.showWarning(Warning.FILE_ALREADY_EXISTS, fae.getMessage());
            fae.printStackTrace();
        } else if (thr instanceof FileNotFoundException fnf) {
            this.ui.showWarning(Warning.FILE_NOT_FOUND, fnf.getMessage());
        } else if (thr instanceof NumberFormatException nfe) {
            this.ui.showWarning(Warning.NUMBER_EXPECTED, nfe.getMessage());
        } else {
            this.ui.showWarning(Warning.UNKNOWN_ERROR, thr.getMessage());
            thr.printStackTrace();
        }
    }

    private void showSignificanceMap(@NotNull MapManager bbn, @NotNull DBOut dbOut) {

        Deque<Airport> aps;
        Map<Airport, Integer> signifMap;
        Statistics stats = new Statistics();

        try {
            aps = dbOut.getAllAirports();
            signifMap = stats.airportSignificance(aps);
            bbn.createSignificanceMap(signifMap, this.ui.getMap());
        } catch (DataNotFoundException dnf) {
            this.handleException(dnf);
        }
    }

    private void showTrackingMap(@NotNull MapManager mapManager, boolean showPoints) {
        if (this.loadedData.isEmpty()) {
            return;
        }

        DBOut dbOut = DBOut.getDBOut();
        Flight flight = null;

        int flightID = this.loadedData.get(0).flightID();
        try {
            flight = (flightID != -1) ? dbOut.getFlightByID(flightID) : null;
        } catch (DataNotFoundException ignored) {
        }
        mapManager.createTrackingMap(this.loadedData, flight, showPoints);
    }

    public void showBitmap() {
        String input = this.ui.getUserInput("Please enter a grid size (0.1 - 2.0)", 0.5);
        if (input.isBlank()) {
            return;
        }
        this.scheduler.exec(() -> {
            try {
                this.setLoading(true);
                this.ui.showLoadingScreen(true);
                float gridSize = Float.parseFloat(input);
                Bitmap bitmap = new Statistics().globalPositionBitmap(gridSize);
                Diagrams.showPosHeatMap(this.ui, bitmap);
            } catch (NumberFormatException nfe) {
                this.ui.showWarning(Warning.NUMBER_EXPECTED, "Please enter a float value (0.1 - 2.0)");
            } catch (DataNotFoundException | OutOfMemoryError e) {
                this.handleException(e);
            } finally {
                this.done(false);
            }
        }, "Loading Data", false, Scheduler.MID_PRIO, true);
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
     * getter for {@link LiveLoader} instance
     *
     * @return the {@link LiveLoader} instance
     */
    @NotNull
    public LiveLoader getLiveLoader() {
        return this.liveLoader;
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
     * getter for the Controller hash code
     *
     * @return controller hash code
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

}

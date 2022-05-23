package planespotter.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.constants.SearchType;
import planespotter.constants.ViewType;
import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.model.*;
import planespotter.model.io.DBOut;
import planespotter.model.io.FileMaster;
import planespotter.model.io.OutputWizard;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;

import static planespotter.constants.GUIConstants.DefaultColor.DEFAULT_MAP_ICON_COLOR;
import static planespotter.constants.GUIConstants.Sound.SOUND_DEFAULT;

/**
 * @name Controller
 * @author jml04
 * @author Lukas
 * @author Bennet
 * @version 1.1
 *
 * main controller - responsible for connection between model and view
 * has static controller, scheduler, watchDog and logger instances
 */
public class Controller {
    // ONLY Controller instance
    private static final Controller mainController;
    /**
     * executor services / thread pools
     */
    private static final Scheduler scheduler;
    // logger for whole program
    private static Logger logger;
    // boolean loading is true when something is loading (volatile?)
    public volatile boolean loading;
    // boolean loggerOn is true when the logger is visible
    public boolean loggerOn;
    // only GUI instance
    private GUI gui;
    // lists for live flights and loaded flights
    public volatile Vector<DataPoint> liveData, loadedData;
    // current loaded search
    public SearchType currentSearchType;
    // current view type ( in action )
    public ViewType currentViewType;

    public Rectangle currentVisibleRect;

    static {
        scheduler = new Scheduler();
        mainController = new Controller();
    }

    // hash code
    private final int hashCode = System.identityHashCode(mainController);


    /**
     * constructor - private -> only ONE instance ( getter: Controller.getInstance() )
     */
    private Controller() {
        this.initialize();
        this.startExecutors();
    }

    /**
     * @return ONE and ONLY controller instance
     */
    public static Controller getInstance() {
        return mainController;
    }

    /**
     * initializes the controller
     */
    private void initialize() {
        logger = new Logger(this);
        this.currentSearchType = SearchType.PLANE;
        logger.log("initializing Controller...", this);
        Thread.currentThread().setName("Planespotter-Main");
        logger.sucsessLog("Controller initialized sucsessfully!", this);
    }

    /**
     * initializes all executors
     * :: -> method reference
     */
    private void startExecutors() {
        logger.log("initializing Executors...", this);
        scheduler.schedule(new FileMaster()::saveConfig, 60, 300);
        scheduler.schedule(() -> {
            System.gc();
            logger.log("Calling Garbage Collector...", this);
        }, 10, 10);
        scheduler.schedule(() -> {
            Thread.currentThread().setName("Output-Wizard-LiveLoader");
            this.loadLiveData();
        }, 0, 10); // -> live data
        logger.sucsessLog("Executors initialized sucsessfully!", this);
    }

    /**
     * starts the program, opens a gui and initializes the controller
     */
    public synchronized void start() {
        this.openWindow();
        this.loadLiveData();
        try {
            while (this.loading) {
                this.wait();
            }
            this.notify();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.donePreLoading();
        this.done();
    }

    /**
     * opens a new GUI window as a thread
     */
    private synchronized void openWindow() {
        this.gui = new GUI();
        this.loading = true;
        logger.log("initialising GUI...", this.gui);
        scheduler.exec(gui, "Planespotter-GUI", false, Scheduler.MID_PRIO, false);
        logger.sucsessLog("GUI initialized sucsessfully!", this.gui);
        logger.sucsessLog("Display-Package initialized sucsessfully!", this);
        this.done();
    }

    /**
     * reloads the data ( static -> able to executed by scheduled_exe )
     * used for live map
     */
    public synchronized void loadLiveData() {
        if (!this.loading) {
            long startTime = System.nanoTime();
            this.loading = true;
            int startID = 0;
            int endID = UserSettings.getMaxLoadedData();
            int dataPerTask = 12500; // testen!
            this.liveData = new Vector<>();
            var outputWizard = new OutputWizard(scheduler, startID, endID, dataPerTask, 0);
            scheduler.exec(outputWizard, "Output-Wizard", true, 9, true);
            this.waitForFinish();
            this.done();
            double elapsed = (System.nanoTime() - startTime) / Math.pow(1000, 3);
            logger.sucsessLog("loaded Live-Data in " + elapsed +
                                 " seconds!", this);
            logger.infoLog("-> completed: " + scheduler.completed() + ", active: " + scheduler.active() +
                              ", largestPoolSize: " + scheduler.largestPoolSize(), this);
            if (this.currentViewType != null) {
                switch (this.currentViewType) {
                    case MAP_ALL, MAP_TRACKING, MAP_TRACKING_NP, MAP_FROMSEARCH -> {
                        // TODO reload map -> neue methode
                    }
                }
            }
        }
    }

    /**
     * waits while data is loading and then adds all loaded data to the live data Flights list
     * // active waiting
     */
    synchronized void waitForFinish() {
        // waits until there is no running thread, then breaks
        while (true) {
            if (scheduler.active() == 0) break;
        }
    }

    /**
     * this method is executed when a loading process is done
     */
    public void done() {
        this.loading = false;
        if (this.gui != null) {
            var gsl = new GUISlave();
            gsl.stopProgressBar();
            gsl.update();
        }
    }

    /**
     * this method is executed when pre-loading is done
     */
    public void donePreLoading() {
        Utilities.playSound(SOUND_DEFAULT.get());
        gui.loadingScreen.dispose();
        gui.window.setVisible(true);
        gui.window.requestFocus();
    }

    /**
     * @creates a GUI-view for a specific view-type
     * @param type is the ViewType, sets the content type for the
     *             created view (e.g. different List-View-Types)
     */
    public synchronized void show(@NotNull ViewType type, String headText, @Nullable String... data) {
        var gsl = new GUISlave();
        // TODO verschiedene Möglichkeiten (für große Datenmengen)
        var bbn = new BlackBeardsNavigator();
        this.loading = true;
        var dbOut = new DBOut();
        // TODO ONLY HERE: dispose GUI view(s)
        gsl.disposeView();
        this.currentViewType = type;
        switch (type) {
            case LIST_FLIGHT -> this.showFlightList(dbOut);
            case MAP_ALL, MAP_FROMSEARCH -> this.showLiveMap(headText, gsl, bbn);
            case MAP_TRACKING -> this.showTrackingMap(headText, gsl, bbn, dbOut, data);
            case MAP_TRACKING_NP -> this.showTrackingMapNoPoints(headText, gsl, bbn, data);
            case MAP_SIGNIFICANCE -> this.showSignificanceMap(headText, gsl, bbn, dbOut);
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
    public void search(String[] inputs, int button) { // TODO button abfragen??
        var gsl = new GUISlave();
        this.loading = true;
        try {
            gsl.startProgressBar();
            var search = new Search();
            switch (this.currentSearchType) {
                case AIRLINE -> {} // TODO implement
                case AIRPORT -> this.searchForAirport(inputs, button, search);
                case FLIGHT -> this.searchForFlight(inputs, button, search);
                case PLANE -> this.searchForPlane(inputs, button, search);
                case AREA -> {} // TODO change to OTHER, not AREA
            }
        } catch (DataNotFoundException ignored) {
        } finally {
            gsl.stopProgressBar();
        }
    }

    /**
     *
     * @param data [0] and [1] must be filled
     */
    public void confirmSettings(String... data) {
        if (data[0] == null || data[1] == null) {
            throw new IllegalArgumentException("Please fill all fields! (with the right params)");
        }
        var us = new UserSettings();
        us.setMaxLoadedData(Integer.parseInt(data[0]));
        var map = UserSettings.getCurrentMapSource();
        switch (data[1]) {
            case "Bing Map" -> map = us.bingMap;
            case "Default Map" -> map = us.tmstMap;
            case "Transport Map" -> map = us.transportMap;
        }
        us.setCurrentMapSource(map);
        new GUISlave().mapViewer().setTileSource(map);
    }

    /**
     * is executed when a map marker is clicked
     *
     * @param point is the clicked map point (no coordinate)
     */
    public synchronized void mapClicked(Point point) {
        var clicked = new GUISlave().mapViewer().getPosition(point);
        switch (this.currentViewType) {
            case MAP_ALL, MAP_FROMSEARCH -> this.onClick_all(clicked);
            case MAP_TRACKING -> this.onClick_tracking(clicked);
        }
    }

    /**
     * is executed when a map marker is clicked and the current is MAP_ALL
     */
    boolean clicking = false;
    private void onClick_all(ICoordinate clickedCoord) { // TODO aufteilen
        if (!this.clicking) {
            this.clicking = true;
            var markers = new GUISlave().mapViewer().getMapMarkerList();
            var newMarkerList = new ArrayList<MapMarker>();
            Coordinate markerCoord;
            CustomMapMarker newMarker;
            boolean markerHit = false;
            var bbn = new BlackBeardsNavigator();
            var ctrl = Controller.getInstance();
            int counter = 0;
            var data = ctrl.loadedData;
            var dbOut = new DBOut();
            var tpl = new TreePlantation();
            var logger = Controller.getLogger();
            var menu = gui.pMenu;
            var info = gui.pInfo;
            var dpleft = gui.dpleft;
            // going though markers
            for (var m : markers) {
                markerCoord = m.getCoordinate();
                newMarker = new CustomMapMarker(markerCoord, 90); // FIXME: 13.05.2022 // FIXME 19.05.2022
                if (bbn.isMarkerHit(markerCoord, clickedCoord)) {
                    markerHit = true;
                    this.markerHit(ViewType.MAP_ALL, newMarker, counter, data, dbOut, tpl, logger, menu, info, dpleft);
                } else {
                    newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
                }
                newMarker.setName(m.getName());
                newMarkerList.add(newMarker);
                counter++;
            }
            if (markerHit) {
                gui.mapViewer.setMapMarkerList(newMarkerList);
            }
            this.clicking = false;
        }
    }

    private void markerHit(ViewType viewType, CustomMapMarker marker,
                           int counter, Vector<DataPoint> dataPoints,
                           DBOut dbOut, TreePlantation treePlantation,
                           Logger logger, JPanel menuPanel,
                           JPanel infoPanel, JDesktopPane dpleft) {
        switch (viewType) {
            case MAP_ALL -> {
                marker.setBackColor(Color.RED);
                menuPanel.setVisible(false);
                int flightID = dataPoints.get(counter).flightID(); // FIXME: 15.05.2022 WAS IST MIT DEM COUNTER LOS
                //  (keine info beim click - flight is null)
                try {
                    var flight = dbOut.getFlightByID(flightID);
                /*infoPanel.removeAll();
                dpleft.moveToFront(infoPanel);*/ // ist bei tracking auch nicht
                    treePlantation.createFlightInfo(flight);
                } catch (DataNotFoundException e) {
                    logger.errorLog("flight with the ID " + flightID + " doesn't exist!", this);
                }
            }
        }
    }

    /**
     *
     * @param clickedCoord is the clicked coordinate
     */
    public void onClick_tracking(ICoordinate clickedCoord) { // TODO aufteilen
        var map = new GUISlave().mapViewer();
        var markers = map.getMapMarkerList();
        Coordinate markerCoord;
        int counter = 0;
        var bbn = new BlackBeardsNavigator();
        var ctrl = Controller.getInstance();
        DataPoint dp;
        int flightID;
        Flight flight;
        var tpl = new TreePlantation();
        var dbOut = new DBOut();
        for (var m : markers) {
            markerCoord = m.getCoordinate();
            if (bbn.isMarkerHit(markerCoord, clickedCoord)) {
                gui.pInfo.removeAll();
                dp = ctrl.loadedData.get(counter);
                flightID = dp.flightID();
                try {
                    flight = dbOut.getFlightByID(flightID); // TODO woanders!!!
                    tpl.createDataPointInfo(flight, dp);
                } catch (DataNotFoundException e) {
                    Controller.getLogger().errorLog("flight with the ID " + flightID + " doesn't exist!", this);
                }
                map.setMapMarkerList(bbn.resetTrackingMarkers(m));
            }
            counter++;
        }
    }

    /**
     * @return main logger
     */
    public static Logger getLogger () {
        return logger;
    }

    /**
     * @return main scheduler
     */
    public static Scheduler getScheduler () {
        return scheduler;
    }

    /**
     * @return main gui
     */
    public GUI gui () {
        return this.gui;
    }

    /**
     * program exit method
     */
    public synchronized void exit () {
        logger.close();
        System.exit(0);
    }

    private void showSignificanceMap(String headText, GUISlave gsl, BlackBeardsNavigator bbn, DBOut dbOut) {
        try {
            var aps = dbOut.getAllAirports();
            var signifMap = new Statistics().airportSignificance(aps);
            var map = bbn.createSignificanceMap(signifMap);
            gsl.recieveMap(map, headText);
        } catch (DataNotFoundException e) {
            logger.errorLog(e.getMessage(), this);
            e.printStackTrace();
        }
    }

    private void showTrackingMapNoPoints(String headText, GUISlave gsl, BlackBeardsNavigator bbn, @Nullable String[] data) {
        try {
            loadedData = new Vector<>();
            var out = new DBOut();
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
            var flightRoute = bbn.createTrackingMap(flight, false);
            gsl.recieveMap(flightRoute, headText);
        } catch (NumberFormatException e) {
            logger.errorLog("NumberFormatException while trying to parse the ID-String! Must be an int!", this);
        } catch (DataNotFoundException e) {
            logger.errorLog(e.getMessage(), this);
        }
    }

    private void showTrackingMap(String headText, GUISlave gsl, BlackBeardsNavigator bbn, DBOut dbOut, @Nullable String[] data) {
        try {
            int flightID = -1;
            if (data.length == 1) {
                assert data[0] != null;
                flightID = Integer.parseInt(data[0]);
                loadedData.addAll(dbOut.getTrackingByFlight(flightID));
            }
            else if (data.length > 1) {
                for (var id : data) {
                    assert id != null;
                    flightID = Integer.parseInt(id);
                    loadedData.addAll(dbOut.getTrackingByFlight(flightID));
                }
            }
            if (flightID == -1) {
                throw new IllegalArgumentException("Flight may not be null!");
            }
            var flight = dbOut.getFlightByID(flightID);
            var flightRoute = bbn.createTrackingMap(flight, true);
            gsl.recieveMap(flightRoute, headText);
        } catch (NumberFormatException e) {
            logger.errorLog("NumberFormatException while trying to parse the ID-String! Must be an int!", this);
        } catch (DataNotFoundException e) {
            logger.errorLog(e.getMessage(), this);
        }
    }

    private void showLiveMap(String headText, GUISlave gsl, BlackBeardsNavigator bbn) {
        this.loadedData = this.liveData;
        var allMap = bbn.createLiveMap();
        gsl.recieveMap(allMap, headText);
    }

    private void showFlightList(DBOut dbOut) {
        var flights = new ArrayList<Flight>();
        Flight flight;
        int flightID;
        for (int i = 0; i < 100; i++) {  // TODO anders machen! dauert zu lange, zu viele Anfragen!
            flightID = liveData.get(i).flightID();
            try {
                flight = dbOut.getFlightByID(flightID);
                flights.add(flight);
            } catch (DataNotFoundException e) {
                logger.errorLog("flight with the ID " + flightID + " doesn't exist!", this);
            }
        }
        var treePlant = new TreePlantation();
        treePlant.createTree(treePlant.allFlightsTreeNode(flights));
    }

    private void searchForPlane(String[] inputs, int button, Search search) throws DataNotFoundException {
        loadedData = search.verifyPlane(inputs);
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
            var headText = "Plane Search Results:";
            if (!gui.search_planeID.getText().isBlank()) {
                this.show(ViewType.MAP_TRACKING, headText, ids); // ganze route -> nur bei einer id / wird evtl noch entfernt
            } else {
                this.show(ViewType.MAP_FROMSEARCH, headText, ids); // nur letzte data points
            }
        }
    }

    private void searchForFlight(String[] inputs, int button, Search search) throws DataNotFoundException {
        loadedData = search.verifyFlight(inputs);
        if (loadedData.size() == 1) {
            var dp = loadedData.get(0);
            if (button == 1) {
                this.show(ViewType.MAP_TRACKING, dp.flightID() + "");
            }
        } else {
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
     * @return controller hash code
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

}

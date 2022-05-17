package planespotter.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import planespotter.constants.SearchType;
import planespotter.constants.ViewType;
import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.model.*;
import planespotter.throwables.DataNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static planespotter.constants.GUIConstants.Sound.SOUND_DEFAULT;

/**
 * @name    Controller
 * @author  @all Lukas   jml04   Bennet
 * @version 1.1
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
    // thread watch dog
    private static WatchDog watchDog;
    // boolean loading is true when something is loading (volatile?)
    public volatile boolean loading;
    // boolean loggerOn is true when the logger is visible
    public boolean loggerOn;
    // only GUI instance
    private GUI gui;
    // lists for live flights and loaded flights
    public volatile Vector<DataPoint> liveData, loadedData;
    // current loaded search
    public SearchType currentSearchType = SearchType.FLIGHT;

    static {
        scheduler = new Scheduler();
        mainController = new Controller();
    }

    /**
     * constructor - private -> only ONE instance ( getter: Controller.getInstance() )
     */
    private Controller () {
        this.initialize();
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
    private void initialize () {
        logger = new Logger(this);
        watchDog = new WatchDog();
        logger.log("initializing Controller...", this);
        liveData = new Vector<>();
        this.startExecutors();
        Thread.currentThread().setName("Planespotter-Main");
        logger.sucsessLog("Controller initialized sucsessfully!", this);
    }

    /**
     * initializes all executors
     * :: -> method reference
     */
    private void startExecutors () {
        logger.log("initializing Executors...", this);
        //scheduler.runAsThread(new DataMaster().dataLoader(), "Data-Loader"); // läuft noch nicht
        scheduler.runAsThread(watchDog, "Watch-Dog", true);
        scheduler.schedule(new FileMaster()::saveConfig, 60, 300);
        scheduler.schedule(() -> {
            System.gc();
            logger.log("Calling Garbage Collector...", this);
        }, 10, 10);
        scheduler.schedule(this::loadLiveData, 0, 10); // -> live data
        logger.sucsessLog("Executors initialized sucsessfully!", this);
    }

    /**
     * starts the program, opens a gui and initializes the controller
     */
    public synchronized void start () {
        try {
            this.openWindow();
            this.loadLiveData();
            while (this.loading) {
                this.wait();
            }
            this.notify();
            this.donePreLoading();
            this.done();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * opens a new GUI window as a thread
     */
    private synchronized void openWindow () {
        gui = new GUI();
        this.loading = true;
        logger.log("initialising GUI...", gui);
        scheduler.exec(gui, "Planespotter-GUI");
        logger.sucsessLog("GUI initialized sucsessfully!", gui);
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
            int endID = new UserSettings().getMaxLoadedData();
            int dataPerTask = 5000; // testen!
            this.liveData = new Vector<>();
            var outputWizard = new OutputWizard(scheduler, 0, startID, endID, dataPerTask, 0);
            scheduler.exec(outputWizard);
            this.waitForFinish();
            this.done();
            logger.sucsessLog("loaded Live-Data in " + (System.nanoTime() - startTime) / Math.pow(1000, 3) +
                    " seconds!", this);
            logger.infoLog("-> completed: " + scheduler.completed() + ", active: " + scheduler.active() +
                    ", largestPoolSize: " + scheduler.largestPoolSize(), this);
            if (BlackBeardsNavigator.currentViewType != null) {
                switch (BlackBeardsNavigator.currentViewType) {
                    case MAP_ALL, MAP_TRACKING, MAP_TRACKING_NP, MAP_FROMSEARCH -> {
                        // TODO reload map
                    }
                }
            }
        }
    }

    /**
     * waits while data is loading and then adds all loaded data to the live data Flights list
     * // active waiting
     */
    synchronized void waitForFinish () {
        // waits until there is no running thread, then breaks
        while (true) {
            if (scheduler.active() == 0) break;
        }
    }

    /**
     * this method is executed when a loading process is done
     */
    void done () {
        this.loading = false;
        if (this.gui != null) {
            var gsl = new GUISlave();
            gsl.progressbarVisible(false);
            gsl.revalidateAll();
        }
    }

    /**
     * this method is executed when pre-loading is done
     */
    public void donePreLoading () {
        new Utilities().playSound(SOUND_DEFAULT.get());
        gui.loadingScreen.dispose();
        gui.window.setVisible(true);
        gui.window.requestFocus();
    }

    /**
     * @creates a GUI-view for a specific view-type
     * @param type is the ViewType, sets the content type for the
     *             created view (e.g. different List-View-Types)
     */
    public synchronized void show (@NotNull ViewType type, String headText, @Nullable String... data) {
        // TODO ONLY HERE: dispose GUI view(s)
        new GUISlave().disposeView();
        // TODO verschiedene Möglichkeiten (für große Datenmengen)
        var bbn = new BlackBeardsNavigator();
        BlackBeardsNavigator.currentViewType = type;
        this.loading = true;
        switch (type) {
            case LIST_FLIGHT -> {
                List<Flight> flights = new ArrayList<>();
                Flight flight;
                var dbOut = new DBOut();
                int flightID;
                for (int i = 0; i < 100; i++) {  // TODO anders machen! dauert zu lange, zu viele Anfragen!
                    flightID = liveData.get(i).getFlightID();
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
            case MAP_ALL, MAP_FROMSEARCH -> {
                this.loadedData = this.liveData;
                bbn.createAllFlightsMap();
            }
            case MAP_TRACKING -> {
                try {
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
                    bbn.createFlightRoute(flight, headText, true);
                } catch (NumberFormatException e) {
                    logger.errorLog("NumberFormatException while trying to parse the ID-String! Must be an int!", this);
                } catch (DataNotFoundException e) {
                    logger.errorLog(e.getMessage(), this);
                }
            }
            case MAP_TRACKING_NP -> {
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
                    bbn.createFlightRoute(flight, headText, false);
                } catch (NumberFormatException e) {
                    logger.errorLog("NumberFormatException while trying to parse the ID-String! Must be an int!", this);
                } catch (DataNotFoundException e) {
                    logger.errorLog(e.getMessage(), this);
                }
            }
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
    public void search (String[] inputs, int button) { // TODO button abfragen??
        var gsl = new GUISlave();
        this.loading = true;
        try {
            gsl.progressbarStart();
            var search = new Search();
            switch (this.currentSearchType) {
                case AIRLINE -> {
                }
                case AIRPORT -> {
                    loadedData = search.verifyAirport(inputs);
                    var idsNoDupl = new ArrayList<Integer>();
                    int flightID;
                    for (var dp : loadedData) {
                        flightID = dp.getFlightID();
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
                case FLIGHT -> {
                    loadedData = search.verifyFlight(inputs);
                    if (loadedData.size() == 1) {
                        var dp = loadedData.get(0);
                        if (button == 1) {
                            this.show(ViewType.MAP_TRACKING, dp.getFlightID() + "");
                        }
                    } else {
                        var idsNoDupl = new ArrayList<Integer>();
                        int flightID;
                        for (var dp : loadedData) {
                            flightID = dp.getFlightID();
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
                case PLANE -> {
                    loadedData = search.verifyPlane(inputs);
                    var idsNoDupl = new ArrayList<Integer>();
                    int flightID;
                    for (var dp : loadedData) {
                        flightID = dp.getFlightID();
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
                case AREA -> {
                }
            }
        } catch (DataNotFoundException ignored) {
        } finally {
            gsl.progressbarVisible(false);
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
     *
     * @return main watch dog
     */
    public static WatchDog getWatchDog () {
        return watchDog;
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

}

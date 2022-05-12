package planespotter.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.constants.SearchType;
import planespotter.constants.ViewType;
import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.model.DBOut;
import planespotter.model.FileMaster;
import planespotter.model.Search;
import planespotter.model.WatchDog;
import planespotter.throwables.DataNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * @name    Controller
 * @author  @all Lukas   jml04   Bennet
 * @version 1.1
 */
public class Controller {
    /**
     * executor services / thread pools
     */
    private final Scheduler scheduler;
    // boolean loading is true when something is loading
    public volatile boolean loading;
    // boolean loggerOn is true when the logger is visible
    public boolean loggerOn;
    // logger for whole program
    private Logger logger;
    // thread watch dog
    private WatchDog watchDog;
    // only GUI instance
    private GUI gui;
    // lists for live flights and loaded flights
    public List<DataPoint> liveData, loadedData;
    // hash map for all map markers
    public HashMap<Integer, DataPoint> allMapData = new HashMap<>();
    // current loaded search
    public SearchType currentSearchType = SearchType.FLIGHT;

    // ONLY Controller instance
    private static final Controller mainController = new Controller();

    /**
     * constructor - private -> only ONE instance ( getter: Controller.getInstance() )
     */
    private Controller () {
        this.scheduler = new Scheduler();
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
        gui = new GUI();
        this.logger = new Logger(this);
        this.watchDog = new WatchDog(this);
        this.logger.log("initializing Controller...", this);
        this.startExecutors();
        liveData = new CopyOnWriteArrayList<>();
        Thread.currentThread().setName("planespotter-main");
        this.logger.sucsessLog("Controller initialized sucsessfully!", this);
    }

    /**
     * initializes all executors
     * :: -> method reference
     */
    private void startExecutors () {
        this.logger.log("initializing Executors...", this);
        this.scheduler.schedule(new FileMaster()::saveConfig, 60, 300);
        this.scheduler.schedule(System::gc, 20, 20);
        this.scheduler.schedule(this::loadLiveData, 0, 10); // -> live data
        this.scheduler.runTask(this.watchDog::watch, "Watch Dog");
        this.scheduler.runTask(() -> this.watchDog.watch(this.gui.worker), "WatchDog-SwingWatcher");
        this.logger.sucsessLog("Executors initialized sucsessfully!", this);
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
            new GUISlave().donePreLoading();
            this.done();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * opens a new GUI window as a thread
     */
    private synchronized void openWindow () {
        this.loading = true;
        if (gui != null) {
            this.logger.log("initialising GUI...", gui);
            this.scheduler.exec(gui);
            this.logger.sucsessLog("GUI initialized sucsessfully!", gui);
        }
        GUISlave.initialize();
        BlackBeardsNavigator.initialize();// TODO hier MapViewer zuweisen! dann nicht mehr
        this.logger.sucsessLog("Display-Package initialized sucsessfully!", this);
        this.done();
    }

    /**
     * reloads the data ( static -> able to executed by scheduled_exe )
     * used for live map
     */
    public synchronized Runnable loadLiveData() {
        if (!this.loading) {
            long startTime = System.nanoTime();
            this.loading = true;
            liveData = new ArrayList<>();
            new DataMaster().load();
            this.waitForFinish();
            this.logger.sucsessLog("loaded Live-Data in " + (System.nanoTime() - startTime) / Math.pow(1000, 3) +
                    " seconds!", this);
            this.logger.infoLog("-> completed: " + this.scheduler.completed() + ", active: " + this.scheduler.active() +
                    ", largestPoolSize: " + this.scheduler.largestPoolSize(), this);
            if (BlackBeardsNavigator.currentViewType != null) {
                switch (BlackBeardsNavigator.currentViewType) {
                    case MAP_ALL, MAP_TRACKING, MAP_TRACKING_NP, MAP_FROMSEARCH -> {
                        // TODO reload map
                    }
                }
            }
        }
        return this::loadLiveData;
    }

    /**
     * waits while data is loading and then adds all loaded data to the live data Flights list
     * // active waiting
     */
    synchronized void waitForFinish () {
        // waits until there is no running thread, then breaks
        while (true) {
            if (this.scheduler.active() == 0) break;
        }
    }

    /**
     * this method is executed when a loading process is done
     */
    void done () {
        loading = false;
        if (gui != null) {
            var gsl = new GUISlave();
            gsl.progressbarVisible(false);
            gsl.revalidateAll();
        }
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
        this.loading = true;
        switch (type) {
            case LIST_FLIGHT -> {
                List<Flight> flights = new ArrayList<>();
                Flight flight;
                for (int i = 0; i < 100; i++) {  // TODO anders machen! dauert zu lange, zu viele Anfragen!
                    int fid = liveData.get(i).getFlightID();
                    flight = new DataMaster().flightByID(fid);
                    flights.add(flight);
                }
                var treePlant = new TreePlantation();
                treePlant.createTree(treePlant.allFlightsTreeNode(flights));
            }
            case MAP_ALL -> {
                bbn.createAllFlightsMap(liveData);
            }
            case MAP_FROMSEARCH -> {
                bbn.createAllFlightsMap(loadedData);
            }
            case MAP_TRACKING -> {
                try {
                    var dps = new ArrayList<DataPoint>();
                    var out = new DBOut();
                    int flightID = -1;
                    if (data.length == 1) {
                        assert data[0] != null;
                        flightID = Integer.parseInt(data[0]);
                        dps.addAll(out.getTrackingByFlight(flightID));
                    }
                    else if (data.length > 1) {
                        for (String id : data) {
                            assert id != null;
                            flightID = Integer.parseInt(id);
                            dps.addAll(out.getTrackingByFlight(flightID));
                        }
                    }
                    var flight = out.getFlightByID(flightID);
                    bbn.createFlightRoute(dps, flight, headText, true);
                } catch (NumberFormatException e) {
                    this.logger.errorLog("NumberFormatException while trying to parse the ID-String! Must be an int!", this);
                } catch (DataNotFoundException e) {
                    this.logger.errorLog(e.getMessage(), this);
                }
            }
            case MAP_TRACKING_NP -> {
                try {
                    var dps = new ArrayList<DataPoint>();
                    var out = new DBOut();
                    int flightID = -1;
                    if (data.length == 1) {
                        assert data[0] != null;
                        flightID = Integer.parseInt(data[0]);
                        dps.addAll(out.getTrackingByFlight(flightID));
                    }
                    else if (data.length > 1) {
                        for (String id : data) {
                            assert id != null;
                            flightID = Integer.parseInt(id);
                            dps.addAll(out.getTrackingByFlight(flightID));
                        }
                    }
                    var flight = out.getFlightByID(flightID);
                    bbn.createFlightRoute(dps, flight, headText, false);
                } catch (NumberFormatException e) {
                    this.logger.errorLog("NumberFormatException while trying to parse the ID-String! Must be an int!", this);
                } catch (DataNotFoundException e) {
                    this.logger.errorLog(e.getMessage(), this);
                }
            }
        }
        BlackBeardsNavigator.currentViewType = type;
        this.done();
        this.logger.sucsessLog("view loaded!", this);
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
                    var ids = new String[idsNoDupl.size()];
                    for (int i = 0; i < idsNoDupl.size(); i++) {
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
                        var ids = new String[idsNoDupl.size()];
                        for (int i = 0; i < idsNoDupl.size(); i++) {
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
                    var ids = new String[idsNoDupl.size()];
                    for (int i = 0; i < idsNoDupl.size(); i++) {
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
    public Logger getLogger () {
        return this.logger;
    }

    /**
     * @return main scheduler
     */
    public Scheduler getScheduler () {
        return this.scheduler;
    }

    /**
     *
     * @return main watch dog
     */
    public WatchDog getWatchDog() {
        return this.watchDog;
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

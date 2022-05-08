package planespotter.controller;

import org.jetbrains.annotations.Nullable;
import planespotter.constants.SearchType;
import planespotter.constants.ViewType;
import planespotter.dataclasses.*;
import planespotter.display.BlackBeardsNavigator;
import planespotter.display.GUI;
import planespotter.display.GUISlave;
import planespotter.model.DBOut;
import planespotter.model.FileMaster;
import planespotter.model.Search;
import planespotter.throwables.DataNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

import static planespotter.constants.Configuration.*;
import static planespotter.constants.GUIConstants.*;

/**
 * @name    Controller
 * @author  @all Lukas   jml04   Bennet
 * @version 1.1
 */
// TODO we need a scheduled executor to update the data in background
// TODO -> starts a background worker every (?) minutes
////////////////////////////////////////////////////////////////////
// TODO eventuell flights tabelle aufteilen in: flightsNow/flightsEnded -> zwei verschiedene DB-Anfragen (?)
// TODO -> so würde man die Zeit der DB-Anfrage deutlich verkürzen, da nicht unnötig nicht-gebrauchte felder gelesen werden müssen
public class Controller {
    /**
     * executor services / thread pools
     */
    // TODO eventuell JoinForkPool einbauen, kann Aufgaben threaded rekursiv verarbeiten
    //  (wenn Aufgabe zu groß, wird sie in weitere Teilaufgaben(Threads) aufgeteilt)
    // ThreadPoolExecutor for thread execution in a thread pool -> package-private (only usable in controller package)
    static final ThreadPoolExecutor exe = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private static final ScheduledExecutorService scheduled_exe = Executors.newScheduledThreadPool(1); // erstmal 1, wird noch mehr
    // boolean loading is true when something is loading
    public static boolean loading;

    /**
     * constructor - private -> only ONE instance ( getter: Controller.getInstance() )
     */
    private Controller () {
        this.initialize();
    }
    // ONLY Controller instance
    private static final Controller mainController = new Controller();

    // only GUI instance
    static GUI gui;
    // preloadedFlights list
    public static List<DataPoint> liveData, loadedData;
    // hash map for all map markers
    public static HashMap<Integer, DataPoint> allMapData = new HashMap<>();
    // current loaded search
    public static SearchType currentSearchType = SearchType.PLANE;

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
        this.log("initializing Controller...");
        this.startExecutors();
        liveData = new CopyOnWriteArrayList<>();
        Thread.currentThread().setName("planespotter-main");
        this.sucsessLog("Controller initialized sucsessfully!");
    }

    /**
     * initializes all executors
     * :: -> method reference
     */
    private void startExecutors () {
        this.log("initializing Executors...");
        var sec = TimeUnit.SECONDS;
        exe.setKeepAliveTime(KEEP_ALIVE_TIME, sec);
        exe.setMaximumPoolSize(MAX_THREADPOOL_SIZE);
        scheduled_exe.scheduleAtFixedRate(FileMaster::saveConfig, 60, 300, sec);
        scheduled_exe.scheduleAtFixedRate(System::gc, 20, 20, sec);
        scheduled_exe.scheduleAtFixedRate(Controller::loadLiveData, 10, 10, sec); // -> live data
        this.sucsessLog("Executors initialized sucsessfully!");
    }

    /**
     * starts the program, opens a gui and initializes the controller
     */
    public synchronized void start () {
        try {
            this.openWindow();
            Controller.loadLiveData();
            while (loading) {
                this.wait();
            }
            this.notify();
            GUISlave.donePreLoading();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * opens a new GUI window as a thread
     */
    private void openWindow () {
        loading = true;
        this.log("initialising GUI...");
        if (gui == null) {
            gui = new GUI();
            exe.execute(gui);
        }
        GUISlave.initialize();
        BlackBeardsNavigator.initialize(); // TODO hier MapViewer zuweisen! dann nicht mehr
        this.done();
        this.sucsessLog("GUI initialized sucsessfully!");
    }

    /**
     * reloads the data ( static -> able to executed by scheduled_exe )
     * used for live map
     */
    public static void loadLiveData () {
        long startTime = System.nanoTime();
        loading = true;
        liveData = new ArrayList<>();
        new DataMaster().load();
        mainController.waitForFinish();
        mainController.sucsessLog("loaded data in " + (System.nanoTime()-startTime)/Math.pow(1000, 3) +
                " seconds!" + "\n" + ANSI_ORANGE + " -> completed: " + exe.getCompletedTaskCount() +
                ", active: " + exe.getActiveCount() + ", largestPoolSize: " + exe.getLargestPoolSize());
    }

    /**
     * waits while data is loading and then adds all loaded data to the live data Flights list
     * // active waiting
     */
    synchronized void waitForFinish () {
        // waits until there is no running thread, then breaks
        while (true) {
            if (exe.getActiveCount() == 0) break;
        }
    }

    /**
     * this method is executed when a loading process is done
     */
    void done () {
        loading = false;
        if (gui != null) {
            GUISlave.progressbarVisible(false);
            GUISlave.revalidateAll();
        }
    }

    /**
     * @creates a GUI-view for a specific view-type
     * @param type is the ViewType, sets the content type for the
     *             created view (e.g. different List-View-Types)
     */
    public synchronized void show (ViewType type, @Nullable String... data) {
        // TODO ONLY HERE: dispose GUI view(s)
        GUISlave.disposeView();
        // TODO verschiedene Möglichkeiten (für große Datenmengen)
        switch (type) {
            case LIST_FLIGHT -> {
                //TreePlantation.createTree(TreePlantation.allFlightsTreeNode (preLoadedFlights));
            }
            case MAP_ALL -> {
                BlackBeardsNavigator.createAllFlightsMap(liveData);
                BlackBeardsNavigator.currentViewType = ViewType.MAP_ALL;
            }
            case MAP_FROMSEARCH -> {
                BlackBeardsNavigator.createAllFlightsMap(loadedData);
                BlackBeardsNavigator.currentViewType = ViewType.MAP_FROMSEARCH;
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
                    BlackBeardsNavigator.createFlightRoute(dps, flight);
                    BlackBeardsNavigator.currentViewType = ViewType.MAP_TRACKING;
                } catch (NumberFormatException e) {
                    this.errorLog("NumberFormatException while trying to parse the ID-String! Must be an int!");
                } catch (DataNotFoundException e) {
                    this.errorLog(e.getMessage());
                }
            }
        }
        this.done();
        this.sucsessLog("view loaded!");
    }

    /**
     * search method for the GUI-search
     *
     * @param inputs are the inputs in the search fields
     * @param button is the clicked search button, 0 = LIST, 1 = MAP
     */
    public void search (String[] inputs, int button) { // TODO button abfragen??
        try {
            GUISlave.progressbarStart();
            var search = new Search();
            switch (Controller.currentSearchType) {
                case AIRLINE -> {
                }
                case AIRPORT -> {
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
                            this.show(ViewType.MAP_TRACKING, ids);
                        }
                    }
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
                        if (!gui.search_planeID.getText().isBlank()) {
                            this.show(ViewType.MAP_TRACKING, ids); // ganze route -> nur bei einer id / wird evtl noch entfernt
                        } else {
                            this.show(ViewType.MAP_FROMSEARCH, ids); // nur letzte data points
                        }
                    }
                }
                case AREA -> {
                }
            }
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        } finally {
            GUISlave.progressbarVisible(false);
        }
    }

    /**
     * FIXME should only be used in DBOut! - but some strings need to be stripped while loading them into the gui
     * @param in is the string to strip
     * @return input-string, but without the "s
     */
    public static String stripString (String in) {
        return in.replaceAll("\"", "");
    }

    /**
     * System.out.println, but with style
     */
    public void log (String txt) {
        System.out.println( EKlAuf + this.getClass().getSimpleName() + EKlZu + " " + txt + ANSI_RESET);
    }

    /**
     * uses this.log to make a 'sucsess' log
     */
    public void sucsessLog (String txt) {
        this.log(ANSI_GREEN + txt);
    }

    /**
     * uses this.log to make an 'error' log
     */
    public void errorLog (String txt) {
        this.log(ANSI_RED + txt);
    }

    public static GUI gui () {
        return gui;
    }

    /**
     * program exit method
     */
    public static synchronized void exit () {
        System.exit(0);
    }

}

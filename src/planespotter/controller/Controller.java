package planespotter.controller;

import planespotter.constants.SearchType;
import planespotter.constants.ViewType;
import planespotter.dataclasses.*;
import planespotter.display.BlackBeardsNavigator;
import planespotter.display.GUI;
import planespotter.display.GUISlave;
import planespotter.display.TreePlantation;
import planespotter.model.FileMaster;
import planespotter.model.Search;
import planespotter.throwables.DataNotFoundException;

import java.util.ArrayList;
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
    // preloadedFlights list ( should also be thread-safe )
    public static List<Flight> preloadedFlights;

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
        this.log("initializing controller...");
        this.initExecutors();
        preloadedFlights = new CopyOnWriteArrayList<>();
        Thread.currentThread().setName("planespotter-main");
        this.sucsessLog("executors initialized sucsessfully!");
    }

    /**
     * initializes all executors
     * :: -> method reference
     */
    private void initExecutors () {
        this.log("initializing executors...");
        var sec = TimeUnit.SECONDS;
        exe.setKeepAliveTime(KEEP_ALIVE_TIME, sec);
        exe.setMaximumPoolSize(MAX_THREADPOOL_SIZE);
        scheduled_exe.scheduleAtFixedRate(FileMaster::saveConfig, 60, 300, sec);
        scheduled_exe.scheduleAtFixedRate(System::gc, 20, 20, sec);
        //scheduled_exe.scheduleAtFixedRate(Controller::reloadData, 60, 60, sec); // -> live data
        this.sucsessLog("executors initialized sucsessfully!");
    }

    /**
     * starts the program, opens a gui and initializes the controller
     */
    public synchronized void start () {
        try {
            this.openWindow();
            this.loadData();
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
        this.log("initialising the GUI...");
        if (gui == null) {
            gui = new GUI();
            exe.execute(gui);
        }
        GUISlave.initialize();
        BlackBeardsNavigator.initialize(); // TODO hier MapViewer zuweisen! dann nicht mehr
        this.done();
    }

    /**
     * loads the DB-data
     */
    public void loadData () {
        long startTime = System.nanoTime();
        loading = true;
        if (gui != null) {
            GUISlave.progressbarStart();
        }
        preloadedFlights = new ArrayList<>();
        new DataMaster().loadFlightsParallel();
        this.done();
        this.sucsessLog("loaded data in " + (System.nanoTime()-startTime)/Math.pow(1000, 3) +
                    " seconds!" + "\n" + ANSI_ORANGE + " -> completed: " + exe.getCompletedTaskCount() +
                    ", active: " + exe.getActiveCount() + ", largestPoolSize: " + exe.getLargestPoolSize());
    }

    /**
     * reloads the data ( static -> able to executed by scheduled_exe )
     * used for live map
     */
    private static void reloadData () {
        loading = true;
        preloadedFlights = new ArrayList<>();
        new DataMaster().loadFlightsParallel();
        loading = false;
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
    public synchronized void createDataView (ViewType type, String data) {
        // TODO ONLY HERE: dispose GUI view(s)
        GUISlave.disposeView();
        // TODO verschiedene Möglichkeiten (für große Datenmengen)
        switch (type) {
            case LIST_FLIGHT -> {
                TreePlantation.createTree(TreePlantation.allFlightsTreeNode(preloadedFlights));
            }
            case MAP_ALL -> {
                BlackBeardsNavigator.createAllFlightsMap(preloadedFlights);
                BlackBeardsNavigator.currentViewType = ViewType.MAP_ALL;
            }
            case MAP_TRACKING -> {
                try {
                    // TODO recieve-methoden in BBNavigator, bzw. TreePlantation packen (?)
                    int flightID = Integer.parseInt(data);
                    var dataMaster = new DataMaster(); // progressbar?
                    var dps = dataMaster.loadTracking(flightID);
                    var flight = new DataMaster().flightByID(flightID);
                    //dataMaster.waitForFinish(); // // FIXME: 29.04.2022 wartet nicht, dps ist null
                    BlackBeardsNavigator.createFlightRoute(dps, flight); // dps is null
                    BlackBeardsNavigator.currentViewType = ViewType.MAP_TRACKING;
                } catch (NumberFormatException e) {
                    this.log(ANSI_YELLOW + "NumberFormatException while trying to parse the ID-String! Must be an int!");
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
     */
    public void search (SearchType type, String[] inputs) { // TODO button abfragen??
        try {
            var search = new Search();
            switch (type) {
                case AIRLINE -> {
                }
                case AIRPORT -> {
                }
                case FLIGHT -> {
                    var flight = search.verifyFlight(inputs);
                    this.createDataView(ViewType.MAP_TRACKING, flight.getID() + "");
                }
                // weitere cases
                case PLANE -> {
                    //var plane = search.
                }
                case AREA -> {
                }
            }
        } catch (DataNotFoundException e) {
            e.printStackTrace();
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

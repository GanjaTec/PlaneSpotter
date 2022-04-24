package planespotter.controller;

import planespotter.constants.ViewType;
import planespotter.dataclasses.*;
import planespotter.display.GUI;
import planespotter.display.BlackBeardsNavigator;
import planespotter.display.GUISlave;
import planespotter.display.TreePlantation;
import planespotter.model.DBOut;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static planespotter.constants.Configuration.MAX_THREADPOOL_SIZE;
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
        this.initExecutors();
    }
    // ONLY Controller instance
    private static final Controller mainController = new Controller();

    // only GUI instance
    static GUI gui;
    // preloadedFlights list ( should also be thread-safe )
    public static volatile List<Flight> preloadedFlights = new CopyOnWriteArrayList<>();

    /**
     * @return ONE and ONLY controller instance
     */
    public static Controller getInstance() {
        return mainController;
    }

    /**
     * initializes the controller
     * :: -> method reference
     */
    private void initExecutors() {
        this.log("initializing executors...");
        // TODO: setting up controller thread
        Thread.currentThread().setName("planespotter-main");
        exe.setKeepAliveTime(1L, TimeUnit.SECONDS);
        exe.setMaximumPoolSize(MAX_THREADPOOL_SIZE);
        scheduled_exe.scheduleAtFixedRate(Controller::garbageCollector, 15, 15, TimeUnit.SECONDS);
        this.log(ANSI_GREEN + "executors initialized sucsessfully!");
    }

    /**
     * starts the program, opens a gui and initializes the controller
     */
    public synchronized void start() {
        try {
            this.openWindow();
            this.loadData();
            while (loading) {
                wait();
            }
            GUISlave.donePreLoading();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * opens a new GUI window as a thread
     */
    private void openWindow() {
        loading = true;
        this.log("initialising the GUI...");
        if (gui == null) {
            gui = new GUI();
            exe.execute(gui);
        }
        GUISlave.initialize();
        TreePlantation.initialize();
        BlackBeardsNavigator.initialize();
        this.done();
    }

    /**
     * loads the DB-data
     */
    public void loadData() {
        long startTime = System.nanoTime();
        loading = true;
        if (gui != null) {
            GUISlave.progressbarStart();
        }
        preloadedFlights = new ArrayList<>();
        new IOMaster().loadFlightsParallel();
        this.log(ANSI_GREEN + "loaded data in " + (System.nanoTime()-startTime)/Math.pow(1000, 3)
                    + " seconds!" + "\n" + ANSI_ORANGE + " -> completed: " + exe.getCompletedTaskCount() +
                    ", active: " + exe.getActiveCount() + ", largestPoolSize: " + exe.getLargestPoolSize());
    }

    /**
     * this method is executed when a loading process is done
     */
    void done() {
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
    public void createDataView(ViewType type, String data) {
        // TODO ONLY HERE: dispose GUI view(s)
        gui.disposeView();
        // TODO verschiedene Möglichkeiten (für große Datenmengen)
        switch (type) {
            case LIST_FLIGHT -> TreePlantation.createTree(TreePlantation.allFlightsTreeNode(preloadedFlights));
            case MAP_ALL -> BlackBeardsNavigator.createAllFlightsMap(preloadedFlights);
            case MAP_FLIGHTROUTE -> {
                try {
                    // TODO recieve-methoden in BBNavigator, bzw. TreePlantation packen (?)
                    int flightID = Integer.parseInt(data);
                    BlackBeardsNavigator.createFlightRoute(new DBOut().getTrackingByFlight(flightID));
                } catch (NumberFormatException e) {
                    BlackBeardsNavigator.createFlightRoute(new DBOut().getTrackingByFlight(107));
                    this.log(ANSI_YELLOW + "NumberFormatException while trying to parse the ID-String!");
                }
            }
        }
        this.done();
        this.log(ANSI_GREEN + "view loaded!");
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
     * System.err.println, but with style
     */
    public void errorLog (String txt) {
        System.err.println( EKlAuf + this.getClass().getSimpleName() + EKlZu + ANSI_RED + " " + txt + ANSI_RESET);
    }

    public static GUI gui () {
        return gui;
    }

    /**
     * tries to call the garbage collector ( System.gc() )
     */
    public static void garbageCollector () {
        System.gc();
    }

    /**
     * program exit method
     */
    public static void exit () {
        System.exit(0);
    }

}

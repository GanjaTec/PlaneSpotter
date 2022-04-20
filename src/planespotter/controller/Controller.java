package planespotter.controller;

import planespotter.constants.ViewType;
import planespotter.dataclasses.*;
import planespotter.display.GUI;
import planespotter.display.BlackBeardsNavigator;
import planespotter.display.TreePlantation;
import planespotter.model.DBOut;
import planespotter.model.ThreadedOutputWizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

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
    // ThreadPoolExecutor for thread execution in a thread pool
    private static ThreadPoolExecutor exe = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    // boolean loading is true when something is loading
    public static boolean loading;

    /**
     * constructor (bisher nicht benötigt) -> private -> nur eine instanz
     */
    private Controller () {
        this.init();
    }

    // ONLY Controller instance
    private static final Controller mainController = new Controller();
    // GUI
    private static volatile GUI gui;
    // preloadedFlights list ( should also be thread-safe )
    public static volatile List<Flight> preloadedFlights = new CopyOnWriteArrayList<>();
    // preloadedFlights queue ( thread-safe )
    public static volatile Queue<List<Flight>> listQueue = new ConcurrentLinkedQueue<>();
    public static volatile int ready = 0;

    /**
     * initializes the controller
     */
    public void init () {
        // TODO: setting up controller thread
        Thread.currentThread().setName("planespotter-main");
        Thread.currentThread().setDaemon(true);
        exe.setKeepAliveTime(10L, TimeUnit.SECONDS);
        exe.setMaximumPoolSize(11);
    }

    /**
     * openWindow() opens a new GUI window as a thread
     * // TODO überprüfen
     */
    public void openWindow() {
        loading = true;
        long startTime = System.nanoTime();
        System.out.println(EKlAuf + "Controller" + EKlZu + " initialisation started!");
        gui = new GUI();
        exe.execute(gui);
        try {
            this.loadFlightsThreaded();
            System.out.println( EKlAuf + "Controller" + EKlZu + ANSI_GREEN + " pre-loaded DB-data!" + ANSI_ORANGE +
                                " -> completed: " + exe.getCompletedTaskCount() + ", active: " +
                                exe.getActiveCount() + ", largestPoolSize: " + exe.getLargestPoolSize() + ", time: " +
                                (System.nanoTime()+-startTime)/Math.pow(1000, 3) + " seconds" + ANSI_RESET);
            gui.donePreLoading();
        } catch (Exception e) {
            System.err.println("preloading-tasks interrupted by controller!");
            e.printStackTrace();
        }
        gui.progressbarVisible(false);
        loading = false;
    }

    /**
     *
     * @return ONE and ONLY controller instance
     */
    public static final Controller getInstance() {
        return mainController;
    }

    /**
     * returns the gui
     *
     * @deprecated
     */
    public static GUI getGUI () {
        return (gui != null) ? gui : null;
    }

    /**
     * @set the maxLoadedFlights variable in DBOut
     */
    public static void setMaxLoadedData (int max) {
        DBOut.maxLoadedFlights = max;
    }

    /**
     * @return maxLoadedFlights variable from DBOut
     */
    public static int getMaxLoadedData () {
        return DBOut.maxLoadedFlights;
    }

    public static void startGUIBackgroundWizard () {

    }

    /**
     * @creates a GUI-view for a specific view-type
     * @param type is the ViewType, sets the content type for the
     *             created view (e.g. different List-View-Types)
     */
    public void createDataView(ViewType type, String data) {
        // TODO ONLY HERE: dispose GUI view(s)
        gui.disposeView();
        try {
            long startTime = System.nanoTime();
            // TODO verschiedene Möglichkeiten (für große Datenmengen)
            if (preloadedFlights == null) {
                this.loadFlightsThreaded();
            }
            switch (type) {
                case LIST_FLIGHT:
                    gui.recieveTree(new TreePlantation().createTree(TreePlantation.createFlightTreeNode(preloadedFlights), gui));
                    break;
                case MAP_ALL:
                    new BlackBeardsNavigator(gui).createAllFlightsMap(preloadedFlights);
                    break;
                case MAP_FLIGHTROUTE:
                    try {
                        if (data.isBlank()) {
                            new BlackBeardsNavigator(gui).createFlightRoute(new DBOut().getTrackingByFlight(107));
                        } else {
                            int flightID = Integer.parseInt(data);
                            new BlackBeardsNavigator(gui).createFlightRoute(new DBOut().getTrackingByFlight(flightID));
                            gui.recieveInfoTree(new TreePlantation().createTree(TreePlantation.createOneFlightTreeNode(flightID), gui));
                        }
                    } catch (NumberFormatException e) {
                        new BlackBeardsNavigator(gui).createFlightRoute(new DBOut().getTrackingByFlight(107));
                    }
                    break;
            }
            System.out.println( EKlAuf + "Controller" + EKlZu + ANSI_GREEN + " loaded " + getMaxLoadedData() + " DB-entries in " +
                                (System.nanoTime()-startTime)/Math.pow(1000, 3) + " seconds!" + ANSI_RESET);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * reloads the DB-data
     */
    // not working correctly
    public void reloadData() {
        long startTime = System.nanoTime();
        loading = true;
        preloadedFlights = new ArrayList<>();
        this.loadFlightsThreaded();
        System.out.println(EKlAuf + "Controller" + EKlZu + ANSI_GREEN + " reloaded data in " + (System.nanoTime()-startTime)/Math.pow(1000, 3) + " seconds!" + ANSI_RESET);
    }

    /**
     * this method is executed when a loading process is done
     */
    private static void done() {
        ready = 0;
        loading = false;
        gui.progressbarVisible(false);
    }

    /**
     * loads flights from the db into a list
     * doesn't look good, but works parallel
     * starts 4 threads/outputWizards which load the flights into a thread-safe queue
     * and then into the main list ( preloadedFlights )
     */
    private void loadFlightsThreaded () {
        int from0 = 12000; // startet erst bei ID 12000, weil davor sowieso alles ended->sonst schlechte aufteilung auf threads
        int plus = (getMaxLoadedData()-from0)/4;
        int from1 = from0 + plus;
        int from2 = from1 + plus;
        int from3 = from2 + plus;
        ThreadedOutputWizard out0 = new ThreadedOutputWizard(0, from0, from1);
        ThreadedOutputWizard out1 = new ThreadedOutputWizard(1, from1, from2);
        ThreadedOutputWizard out2 = new ThreadedOutputWizard(2, from2, from3);
        ThreadedOutputWizard out3 = new ThreadedOutputWizard(3, from3, (from3+plus));
        preloadedFlights = new CopyOnWriteArrayList<>();
        exe.execute(out0);
        exe.execute(out1);
        exe.execute(out2);
        exe.execute(out3);
            while (ready < 40) { // waits until all threads are ready ( every thread does 'ready+=10' when ready )
            }
        while (!listQueue.isEmpty()) { // adding all loaded lists to the main list ( listQueue is threadSafe )
            preloadedFlights.addAll(listQueue.poll());
        }
        done();
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

    /** // nur test
     * TestObjekt:
     * @return Test-List-Object
     */
    public static List<Flight> testFlightList() {
        List<Flight> list = new ArrayList<>();
        Flight flight1 = new Flight(1234, new Airport(030, "BER", "Berlin", new Position(222.22, 333.33)),
                new Airport(040, "HH", "Hamburg", new Position(123.45, 98.76)),
                "HHBER",
                new Plane(10045, "ABC111", "11", "Passagierflugzeug", "REG111", new Airline(21, "A21A", "Airline21")),
                "BERHH1", null);
        Flight flight2 = new Flight(6543, new Airport(324, "MI", "Minden", new Position(37.26237, 325.563)),
                new Airport(367, "BEV", "Beverungen", new Position(52553.45, 58.5576)),
                "MIBEV",
                new Plane(10045, "ABC111", "11", "Passagierflugzeug", "REG111", new Airline(21, "A21A", "Airline21")),
                "MIBEV1", null);
        list.add(flight1);
        list.add(flight2);
        return list;
    }

}

package planespotter.controller;

import planespotter.constants.ViewType;
import planespotter.dataclasses.*;
import planespotter.display.GUI;
import planespotter.display.BlackBeardsNavigator;
import planespotter.display.TreePlantation;
import planespotter.display.UserSettings;
import planespotter.model.DBOut;
import planespotter.model.ThreadedOutputWizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private static final ThreadPoolExecutor exe = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    // boolean loading is true when something is loading
    public static boolean loading;

    /**
     * constructor (bisher nicht benötigt) -> private -> nur eine instanz
     */
    private Controller () {
        this.initController();
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
     *
     * @return ONE and ONLY controller instance
     */
    public static Controller getInstance() {
        return mainController;
    }

    /**
     * initializes the controller
     */
    public void initController () {
        this.log("initializing controller...");
        // TODO: setting up controller thread
        Thread.currentThread().setName("planespotter-main");
        exe.setKeepAliveTime(10L, TimeUnit.SECONDS);
        exe.setMaximumPoolSize(11);
        this.log(ANSI_GREEN + "controller initialized sucsessfully!");
    }

    /**
     * starts the program, opens a gui and initializes the controller
     */
    public synchronized void start () {
        try {
            this.openWindow();
            this.loadData();
            while (loading) {
                wait();
            }
            gui.donePreLoading();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * openWindow() opens a new GUI window as a thread
     * // TODO überprüfen
     */
    public void openWindow() {
        long startTime = System.nanoTime();
        loading = true;
        this.log("initialising the GUI...");
        if (gui == null) {
            gui = new GUI();
            exe.execute(gui);
        }
        done();
    }

    /**
     * reloads the DB-data
     */
    // not working correctly
    public void loadData() {
        long startTime = System.nanoTime();
        loading = true;
        if (gui != null) {
            gui.progressbarStart();
        }
        preloadedFlights = new ArrayList<>();
        this.loadFlightsThreaded();
        while (loading) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        done();
        this.log(ANSI_GREEN + "loaded data in " + (System.nanoTime()-startTime)/Math.pow(1000, 3)
                    + " seconds!" + "\n" + ANSI_ORANGE + " -> completed: " + exe.getCompletedTaskCount() +
                    ", active: " + exe.getActiveCount() + ", largestPoolSize: " + exe.getLargestPoolSize());
    }

    /**
     * this method is executed when a loading process is done
     */
    private static void done() {
        ready = 0;
        loading = false;
        if (gui != null) {
            gui.progressbarVisible(false);
            gui.revalidateAll();
        }
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
            this.log(ANSI_GREEN + "loaded " + UserSettings.getMaxLoadedFlights() + " DB-entries in " +
                        (System.nanoTime()-startTime)/Math.pow(1000, 3) + " seconds!" + "\n" + ANSI_ORANGE +
                        " -> completed: " + exe.getCompletedTaskCount() + ", active: " + exe.getActiveCount() +
                        ", largestPoolSize: " + exe.getLargestPoolSize());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * loads flights from the db into a list
     * doesn't look good, but works parallel
     * starts 4 threads/outputWizards which load the flights into a thread-safe queue
     * and then into the main list ( preloadedFlights )
     */
    private void loadFlightsThreaded () {
        int from0 = 12000; // startet erst bei ID 12000, weil davor sowieso alles ended->sonst schlechte aufteilung auf threads
        int plus = (UserSettings.getMaxLoadedFlights()-from0)/4;
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
            preloadedFlights.addAll(Objects.requireNonNull(listQueue.poll()));
        }
        done();
    }

    /**
     * System.out.println, but with style
     */
    public void log (String txt) {
        System.out.println( EKlAuf + this.getClass().getSimpleName() + EKlZu + " " + txt + ANSI_RESET);
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
        Flight flight1 = new Flight(1234, new Airport(30, "BER", "Berlin", new Position(222.22, 333.33)),
                new Airport(40, "HH", "Hamburg", new Position(123.45, 98.76)),
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

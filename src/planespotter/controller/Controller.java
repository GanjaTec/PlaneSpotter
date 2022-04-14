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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static planespotter.constants.GUIConstants.ANSI_GREEN;
import static planespotter.constants.GUIConstants.ANSI_RESET;

/**
 * @name    Controller
 * @author  @all Lukas   jml04   Bennet
 * @version 1.1
 */
// TODO we need a scheduled executor to update the data in background
// TODO -> starts a background worker every (?) minutes
public class Controller implements Runnable {
    // test-ThreadPoolExecutor
    static ThreadPoolExecutor exe = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    //
    public static boolean loading;

    /**
     * class variables
     */
    private int threadNumber;
    private String threadName;

    /**
     * constructor (bisher nicht benötigt)
     */
    public Controller (int threadNumber) {
        this.threadNumber = threadNumber;
        this.threadName = "Controller-Thread" + this.threadNumber;
    }

    /**
     * Controller run method
     */
    @Override
    public void run() {
    }

    /**
     * the GUI
     */
    private static volatile GUI gui;

    private static List<Flight> preloadedFlights = new ArrayList<>();

    /**
     * * * * * * * * * * * * * * *
     * static controller methods *
     * * * * * * * * * * * * * * *
     **/

    /**
     * openWindow() opens a new GUI window as a thread
     * // TODO überprüfen
     */
    public static void openWindow () {
        loading = true;
        long startTime = System.nanoTime();
        System.out.println("[Controller] initialisation started!");
        gui = new GUI();
        exe.execute(gui);
        try {
            loadFlightsThreaded(preloadedFlights);
            System.out.println("[Controller] " + ANSI_GREEN + "pre-loaded DB-data in " + (System.nanoTime()+-startTime)/Math.pow(1000, 3) + " seconds!" + ANSI_RESET);
            gui.donePreLoading();
        } catch (Exception e) {
            System.err.println("preloading-tasks interrupted by controller!");
            e.printStackTrace();
        }
        gui.progressbarVisible(false);
        loading = false;
    }

    /**
     * returns the gui
     */
    public static GUI getGUI () {
        return (gui != null) ? gui : null;
    }

    /**
     * sets the MaxLoadedData variable in DBOut
     */
    public static void setMaxLoadedData (int max) {
        DBOut.maxLoadedFlights = max;
    }

    /**
     * @return MaxLoadedData variable from DBOut
     */
    public static int getMaxLoadedData () {
        return DBOut.maxLoadedFlights;
    }

    /**
     * creates a GUI-view for a specific view-type
     * @param type is the ViewType, sets the content type for the
     *             created view (e.g. different List-View-Types)
     */
    public static void createDataView (ViewType type, String data) {
        gui.disposeView();
        try {
            long startTime = System.nanoTime();
            List<Flight> listFlights = new ArrayList<>();
            // TODO verschiedene Möglichkeiten (für große Datenmengen)
            if (preloadedFlights == null) {
                loadFlightsThreaded(listFlights);
            }
            switch (type) {
                case LIST_FLIGHT:
                    if (preloadedFlights == null) {
                        gui.recieveTree(new TreePlantation().createTree(TreePlantation.createFlightTreeNode(listFlights), gui));
                    } else {
                        gui.recieveTree(new TreePlantation().createTree(TreePlantation.createFlightTreeNode(preloadedFlights), gui));
                    }
                    gui.window.revalidate();
                    break;
                case MAP_ALL:
                    if (preloadedFlights == null) {
                        new BlackBeardsNavigator(gui).createAllFlightsMap(listFlights);
                    } else {
                        new BlackBeardsNavigator(gui).createAllFlightsMap(preloadedFlights);
                    }
                    gui.window.revalidate();
                    break;
                case MAP_FLIGHTROUTE:
                    try {
                        if (data.isBlank()) {
                            new BlackBeardsNavigator(gui).createFlightRoute(new DBOut().getTrackingByFlight(107));
                        } else {
                            int flightID = Integer.parseInt(data);
                            new BlackBeardsNavigator(gui).createFlightRoute(new DBOut().getTrackingByFlight(flightID));
                        }
                    } catch (NumberFormatException e) {
                        new BlackBeardsNavigator(gui).createFlightRoute(new DBOut().getTrackingByFlight(107));
                    }
                    gui.window.revalidate();
                    break;
            }
            System.out.println( "[Controller] " + ANSI_GREEN + "loaded " + getMaxLoadedData() + " DB-entries in " +
                                (System.nanoTime()-startTime)/Math.pow(1000, 3) + " seconds!" + ANSI_RESET);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * reloads the DB-data
     */
    // not working correctly
    public static void reloadData () {
        long startTime = System.nanoTime();
        loading = true;
        preloadedFlights = new ArrayList<>();
        loadFlightsThreaded(preloadedFlights);
        System.out.println("[Controller] " + ANSI_GREEN + "reloaded data in " + (System.nanoTime()-startTime)/Math.pow(1000, 3) + " seconds!" + ANSI_RESET);
    }

    /**
     * this method is executed when a loading process is done
     */
    private static void done() {
        loading = false;
        gui.progressbarVisible(false);
    }

    /**
     * loads flights from the db into a list (threaded)
     * doesn't look good, but works parallel
     * @param toList is the list where the data will be added
     */
    private static void loadFlightsThreaded (List<Flight> toList) {
        int from0 = 0;
        int from1 = from0 + getMaxLoadedData()/4;
        int from2 = from1 + getMaxLoadedData()/4;
        int from3 = from2 + getMaxLoadedData()/4;
        if (getMaxLoadedData() <= 1000) {
            ThreadedOutputWizard out0 = new ThreadedOutputWizard(0);
            ThreadedOutputWizard out1 = new ThreadedOutputWizard(1);
            ThreadedOutputWizard out2 = new ThreadedOutputWizard(2);
            ThreadedOutputWizard out3 = new ThreadedOutputWizard(3);
            exe.execute(out0);
            exe.execute(out1);
            exe.execute(out2);
            exe.execute(out3);
            List<Flight> list0 = out0.getAllFlightsFromID(from0, from1);
            List<Flight> list1 = out1.getAllFlightsFromID(from1, from2-1);
            List<Flight> list2 = out2.getAllFlightsFromID(from2, from3-1);
            List<Flight> list3 = out3.getAllFlightsFromID(from3, (from3+getMaxLoadedData()/4)-1);
            synchronized (toList) {     // this methods waits if toList is modified
                toList.addAll(list0);
                toList.addAll(list1);
                toList.addAll(list2);
                toList.addAll(list3);
            }
        } else {
            // "from" weiter aufteilen, kleinere Schritte, mehr Threads
            // evtl. hier PRINT output ?
            ThreadedOutputWizard out0 = new ThreadedOutputWizard(0);
            ThreadedOutputWizard out1 = new ThreadedOutputWizard(1);
            ThreadedOutputWizard out2 = new ThreadedOutputWizard(2);
            ThreadedOutputWizard out3 = new ThreadedOutputWizard(3);
            exe.execute(out0);
            exe.execute(out1);
            exe.execute(out2);
            exe.execute(out3);
            List<Flight> list0 = out0.getAllFlightsFromID(from0, from1);
            List<Flight> list1 = out1.getAllFlightsFromID(from1, from2-1);
            List<Flight> list2 = out2.getAllFlightsFromID(from2, from3-1);
            List<Flight> list3 = out3.getAllFlightsFromID(from3, (from3+getMaxLoadedData()/4)-1);
            //toList = new ArrayList<>();
            synchronized (toList) {     // this methods waits if toList is modified
                toList.addAll(list0);
                toList.addAll(list1);
                toList.addAll(list2);
                toList.addAll(list3);
            }
        }
        done();
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

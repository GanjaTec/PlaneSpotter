package planespotter.controller;

import planespotter.constants.ViewType;
import planespotter.dataclasses.*;
import planespotter.display.GUI;
import planespotter.display.MapManager;
import planespotter.display.TreePlantation;
import planespotter.model.DBOut;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @name    Controller
 * @author  @all Lukas   jml04   Bennet
 * @version 1.1
 */
public class Controller implements Runnable {
    // test-ThreadPoolExecutor
    static ThreadPoolExecutor exe = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

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
    private static GUI gui;

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
        gui = new GUI();
        gui.run();
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
            DBOut dbOut = new DBOut(0);
            //exe.execute(dbOut);
            switch (type) {
                case LIST_FLIGHT:
                    // läuft noch nicht / soll laden der Daten in mehrere Threads aufteilen
                    List<Flight> listFlights = new ArrayList<>();
                    for (int id = 0; id <= getMaxLoadedData(); ) {
                        synchronized (listFlights) {
                            listFlights.addAll(loadFlightsInBackground(id));
                        }
                        id = id + 250;
                    }
                    gui.recieveTree(new TreePlantation().createTree(TreePlantation.createFlightTreeNode(listFlights), gui));
                    gui.window.revalidate();
                    break;
                case MAP_ALL:
                    List<Flight> listMap = new ArrayList<>();
                    for (int id = 0; id <= getMaxLoadedData(); ) {
                        synchronized (listMap) {
                            listMap.addAll(loadFlightsInBackground(id));
                        }
                        id = id + 250;
                    }
                    new MapManager(gui).createAllFlightsMap(listMap);
                    gui.window.revalidate();
                    break;
                case MAP_FLIGHTROUTE:
                    new MapManager(gui).createFlightRoute(dbOut.getFlightsByCallsign(data));
                    gui.window.revalidate();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * program exit method
     */
    public static void exit () {
        System.exit(0);
    }

    /**
     * berechnet die quersumme einer zahl
     * @unused
     */
    public static int checksum (int n) {
        if (n <= 9) {
            return n;
        }
        return n%10 + checksum(n/10);
    }

    /**
     * background task method
     * starts a background task from BackgroundWorker class
     */
    private static List<Flight> loadFlightsInBackground (int id) {
        List<Flight> list = new ArrayList<>();
        long startTime = System.nanoTime();
        DBOut thread1 = new DBOut(1);
        exe.execute(thread1);
            list = thread1.getAllFlightsFromID(id);
                //TODO fix Exception in thread "AWT-EventQueue-0" java.util.ConcurrentModificationException
        exe.remove(thread1);
        System.out.println("[DBOut] loaded 250 DB-entries (from id " + id + ") in " + (System.nanoTime()-startTime)/Math.pow(1000, 3) + " seconds!");
        return list;
    }


}

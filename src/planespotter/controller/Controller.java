package planespotter.controller;

import planespotter.constants.ViewType;
import planespotter.dataclasses.*;
import planespotter.display.GUI;
import planespotter.display.MapManager;
import planespotter.display.TreePlantation;
import planespotter.model.DBOut;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static planespotter.constants.GUIConstants.*;

/**
 * @name    Controller
 * @author  @all Lukas   jml04   Bennet
 * @version 1.1
 */
public class Controller implements Runnable {
    // test-ThreadPoolExecutor
    static ThreadPoolExecutor exe = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

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
        exe.execute(gui);
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
            int from0 = getMaxLoadedData();
            int from1 = from0 + getMaxLoadedData()/4;
            int from2 = from1 + getMaxLoadedData()/4;
            int from3 = from2 + getMaxLoadedData()/4;
            DBOut out0 = new DBOut(0, exe);
            DBOut out1 = new DBOut(1, exe);
            DBOut out2 = new DBOut(2, exe);
            DBOut out3 = new DBOut(3, exe);
            List<Flight> list0 = out0.getAllFlightsFromID(from0);
            List<Flight> list1 = out1.getAllFlightsFromID(from1);
            List<Flight> list2 = out2.getAllFlightsFromID(from2);
            List<Flight> list3 = out3.getAllFlightsFromID(from3);
            switch (type) {
                case LIST_FLIGHT:
                    List<Flight> listFlights = new ArrayList<>();
                    listFlights.addAll(list0);
                    listFlights.addAll(list1);
                    listFlights.addAll(list2);
                    listFlights.addAll(list3);
                    gui.recieveTree(new TreePlantation().createTree(TreePlantation.createFlightTreeNode(listFlights), gui));
                    gui.window.revalidate();
                    break;
                case MAP_ALL:
                    // TODO // Dieses Threading ist besser als das alte von LIST_FLIGHT
                    // TODO // MAP_ALL braucht ca. 9s im Gegensatz zu LIST_FLIGHT mit ca. 12s (bei 2000 DB-entries)
                    List<Flight> listMap = new ArrayList<>();
                    listMap.addAll(list0);
                    listMap.addAll(list1);
                    listMap.addAll(list2);
                    listMap.addAll(list3);
                    new MapManager(gui).createAllFlightsMap(listMap);
                    gui.window.revalidate();
                    break;
                case MAP_FLIGHTROUTE:   // läuft nicht
                    //new MapManager(gui).createFlightRoute(mainOut.getAllFlights());
                    gui.window.revalidate();
                    break;
            }
            System.out.println( "[DBOut] " + ANSI_GREEN + "loaded " + getMaxLoadedData() + " DB-entries in " +
                                (System.nanoTime()-startTime)/Math.pow(1000, 3) + " seconds!" + ANSI_RESET);
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
    private static List<Flight> loadFlightsInBackgroundFromID (int id) {
        List<Flight> list = new ArrayList<>();
        long startTime = System.nanoTime();
        DBOut thread1 = new DBOut(5, exe);
        exe.execute(thread1);
            list = thread1.getAllFlightsFromID(id);
                //TODO fix Exception in thread "AWT-EventQueue-0" java.util.ConcurrentModificationException
        exe.remove(thread1);
        System.out.println("[DBOut] loaded 250 DB-entries (from id " + id + ") in " + (System.nanoTime()-startTime)/Math.pow(1000, 3) + " seconds!");
        return list;
    }


}

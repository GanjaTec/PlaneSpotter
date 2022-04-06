package planespotter.controller;

import planespotter.constants.ViewType;
import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.model.DBOut;

import java.util.ArrayList;
import java.util.List;

public class Controller {

    /**
     * constructor (bisher nicht benötigt)
     */
    public Controller () {}

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
     * creates a GUI-view for a specific view-type
     * @param type is the ViewType, sets the content type for the
     *             created view (e.g. different List-View-Types)
     */
    public static void createDataView (ViewType type, String data) {
        gui.disposeView();
        try {
            switch (type) {
                case LIST_FLIGHT:
                    gui.loadView(type, new DBOut().getAllFlights());
                    break;
                case MAP_ALL:
                    gui.loadView(type, new DBOut().getAllFlights());
                    break;
                case MAP_FLIGHTROUTE:
                    gui.loadView(type, new DBOut().getFlightsByCallsign(data));
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



}

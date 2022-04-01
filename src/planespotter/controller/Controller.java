package planespotter.controller;

import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.model.DBOut;
import planespotter.model.TreePlantation;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Controller {

    /**
     * constructor (bisher nicht benötigt)
     */
    public Controller () {}

    /**
     * * * * * * * * * * * * * * *
     * static controller methods *
     * * * * * * * * * * * * * * *
     **/

    /**
     * openFrame() opens a frame
     */
    public static void openWindow () {

        GUI gui = new GUI();

        loadList(gui);

    }

    /**
     *
     */
    public static void loadList (GUI gui) {
        gui.progressbarVisible(true);
        for (int i = gui.progressbarValue(); i <= 100; i++) {
            gui.progressbarPP();
            try {
                TimeUnit.MILLISECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            gui.progressbarVisible(false);
            createFlightTree();     // nur zum testen
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * creates flight tree in GUI
     * sets tree to GUI.listView
     */
    public static void createFlightTree () throws SQLException {
        // laeuft noch nicht, zu viele Daten
        List<Flight> list = new DBOut().getAllFlights();
        //List<Flight> list = testFlightList();
        JTree tree = TreePlantation.createListView(TreePlantation.createFlightTree(list));
        GUI.recieveTree(tree);
        //return tree;
    }

    /**
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

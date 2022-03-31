package planespotter;

import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.model.DBOut;
import planespotter.model.TreeFactory;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class Controller {

    /**
     * HashMap containing all visible frames
     */
    private static HashMap<Class, Boolean> framesvisible = new HashMap<>();

    public Controller () {}

    /**
     * * * * * * * * * * * * * * *
     * static controller methods *
     * * * * * * * * * * * * * * *
     **/



    /**
     * openFrame() opens a frame
     * @param c is the Frame-Class to be opened
     */
    public static void openWindow (Class c) {
        if (c == GUI.class) {
            //createFlightTree(); // nur zum testen // auskommentieren!
            new GUI();
        }
    }

    /**
     * @return flight tree node
     *      ->for flight list
     */
    public static void createFlightTree () {
        try {
            List<Flight> list = new DBOut().getAllFlights();
            GUI.recieveTree(TreeFactory.createFlightTree(list));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * program exit method
     */
    public static void exit () { System.exit(0); }





}

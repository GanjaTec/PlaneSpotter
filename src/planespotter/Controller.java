package planespotter;

import planespotter.Exceptions.JFrameNotFoundException;
import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.model.DBOut;
import planespotter.model.Deserializer;
import planespotter.model.TreeFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.lang.module.Configuration;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import static planespotter.model.Supplier.fr24get;

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
     * @param opener is the JFrame who opens the new Window
     */
    public static void openWindow (Class c, JFrame opener) {
        if (opener != null) opener.setVisible(false);
        if (c == GUI.class) new GUI();
    }

    /**
     * @return flight tree node
     *      ->for flight list
     */
    public static DefaultMutableTreeNode flightTree () {
        try {
            DBOut out = new DBOut();
            return TreeFactory.createFlightTree(out.getAllFlights());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



}

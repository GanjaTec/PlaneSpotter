package planespotter.model;

import planespotter.dataclasses.Flight;
import planespotter.dataclasses.ObjectType;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Iterator;
import java.util.List;

public class TreeFactory {

    /**
     *
     */
    public static DefaultMutableTreeNode createFlightTree (List<Flight> list) {
        Iterator<Flight> it = list.iterator();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        while (it.hasNext()) {
            Flight f = it.next();
            String titleStr = "ID: " + f.getID() + " ,   Airline: " + f.getPlane().getAirline().getName();
            // Title Object
            DefaultMutableTreeNode title = new DefaultMutableTreeNode(titleStr);
            // Attributes
            DefaultMutableTreeNode fid = new DefaultMutableTreeNode("ID: " + f.getID());
            DefaultMutableTreeNode fnr = new DefaultMutableTreeNode("FlightNr: " + f.getFlightnr());

            title.add(fid);
            title.add(fnr);
            root.add(title);
        }

    return root;
    }

}

package planespotter.model;

import planespotter.dataclasses.Airline;
import planespotter.dataclasses.Airport;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.Plane;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Iterator;
import java.util.List;

import static planespotter.constants.GUIConstants.*;

/**
 * @name TreeFactory
 * @author jml04
 * @version 1.0
 *
 * class TreePlantation implements methods to create tree structures
 * "abstract" and "static" may be deleted in the future (?)
 */
public abstract class TreePlantation {

    /**T
     * creates a new list component
     * @return new JList for data models
     */
    public static JTree createListView (DefaultMutableTreeNode node) {
        JTree listView;

        listView = new JTree(node);
        // Exception in thread "main" java.lang.IndexOutOfBoundsException: Index 1 out of bounds for length 1
        // bei großen Datensaetzen? schon gefixt?
        listView.setFont(FONT_MENU);
        listView.setBackground(Color.WHITE);
        listView.setForeground(DEFAULT_FG_COLOR);
        listView.setBounds(0, 0, 1000, 650);

        return listView;
    }

    /**
     * creates a 'Tree' of Flights
     * @return root, the root of the tree
     */
    public static DefaultMutableTreeNode createFlightTree (List<Flight> list) {
        Iterator<Flight> it = list.iterator();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        while (it.hasNext()) {
            try {
                Flight f = it.next();
                String titleStr = "Flugnummer: " + f.getFlightnr() + " ,   Airline: " + f.getPlane().getAirline().getName();
                // Title Object
                DefaultMutableTreeNode flight = new DefaultMutableTreeNode(titleStr);
                // Attributes
                DefaultMutableTreeNode flight_id = new DefaultMutableTreeNode("ID: " + f.getID());
                DefaultMutableTreeNode flight_nr = new DefaultMutableTreeNode("FlightNr: " + f.getFlightnr());

                Airport startAirport = f.getStart();
                DefaultMutableTreeNode start = new DefaultMutableTreeNode("Startflughafen: " + startAirport.getName());
                DefaultMutableTreeNode start_id = new DefaultMutableTreeNode("ID: " + startAirport.getID());
                DefaultMutableTreeNode start_tag = new DefaultMutableTreeNode("Tag: " + startAirport.getTag());
                DefaultMutableTreeNode start_name = new DefaultMutableTreeNode("Name: " + startAirport.getName());

                Airport destAirport = f.getDest();
                DefaultMutableTreeNode dest = new DefaultMutableTreeNode("Zielflughafen: " + destAirport.getName());
                DefaultMutableTreeNode dest_id = new DefaultMutableTreeNode("ID: " + destAirport.getID());
                DefaultMutableTreeNode dest_tag = new DefaultMutableTreeNode("Tag: " + destAirport.getTag());
                DefaultMutableTreeNode dest_name = new DefaultMutableTreeNode("Name: " + destAirport.getName());

                Plane p = f.getPlane();
                Airline airline = p.getAirline();
                DefaultMutableTreeNode plane = new DefaultMutableTreeNode("Plane: " + p.getPlanetype() + " von: " + airline.getName());
                DefaultMutableTreeNode plane_id = new DefaultMutableTreeNode("ID: " + p.getID());
                DefaultMutableTreeNode plane_icao = new DefaultMutableTreeNode("ICAO: " + p.getIcao());
                DefaultMutableTreeNode plane_reg = new DefaultMutableTreeNode("Registration: " + p.getRegistration());
                DefaultMutableTreeNode plane_tnr = new DefaultMutableTreeNode("Tailnumber: " + p.getTailnr());
                DefaultMutableTreeNode plane_type = new DefaultMutableTreeNode("Type: " + p.getPlanetype());
                DefaultMutableTreeNode p_airline = new DefaultMutableTreeNode("Airline: " + airline.getName());
                DefaultMutableTreeNode p_airline_id = new DefaultMutableTreeNode("ID: " + airline.getID());
                DefaultMutableTreeNode p_airline_tag = new DefaultMutableTreeNode("Tag: " + airline.getTag());
                DefaultMutableTreeNode p_airline_name = new DefaultMutableTreeNode("Name: " + airline.getName());

                // TODO: completing the airline node
                p_airline.add(p_airline_id);
                p_airline.add(p_airline_tag);
                p_airline.add(p_airline_name);
                // TODO: completing plane node
                plane.add(plane_id);
                plane.add(plane_icao);
                plane.add(plane_reg);
                plane.add(plane_tnr);
                plane.add(plane_type);
                plane.add(p_airline);
                // TODO: completing dest node
                dest.add(dest_id);
                dest.add(dest_tag);
                dest.add(dest_name);
                // TODO: completing start node
                start.add(start_id);
                start.add(start_tag);
                start.add(start_name);
                // TODO: completing flight node
                flight.add(flight_id);
                flight.add(flight_nr);
                flight.add(start);
                flight.add(dest);
                flight.add(plane);
                // TODO: adding flight node to root node
                root.add(flight);
            } catch (Exception e) {

            }
        }

    return root;
    }

}

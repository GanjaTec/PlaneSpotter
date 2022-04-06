package planespotter.display;

import planespotter.constants.Bounds;
import planespotter.constants.Paths;
import planespotter.dataclasses.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
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
public final class TreePlantation {

    /**
     * default plane icon in the JTree
     */
    private static final Icon PLANE_ICON = new ImageIcon(Paths.SRC_PATH + "tree_plane_icon.png");

    /**
     * creates a new list component
     * @return new JList for data models
     * @param node is the root node of the given tree
     */
    public static JTree createTree (DefaultMutableTreeNode node) {
        JTree listView;
        // initialisation new JTree
        listView = new JTree(node);
        // Exception in thread "main" java.lang.IndexOutOfBoundsException: Index 1 out of bounds for length 1
        // bei großen Datensaetzen? schon gefixt?

        // setting tree attributes
        listView.setFont(FONT_MENU);
        listView.setBackground(DEFAULT_BG_COLOR);
        listView.setBounds(Bounds.RIGHT);

        // creating tree cell renderer
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setBackgroundNonSelectionColor(DEFAULT_BG_COLOR);
        renderer.setBackgroundSelectionColor(DEFAULT_BORDER_COLOR);
        renderer.setBorderSelectionColor(Color.ORANGE);
        renderer.setTextNonSelectionColor(new Color(255, 255, 102));
        renderer.setTextSelectionColor(Color.ORANGE);
        renderer.setLeafIcon(PLANE_ICON);
        // TODO icons setzen

        // adding it to the tree
        listView.setCellRenderer(renderer);

        // return
        return listView;
    }


    public static DefaultMutableTreeNode createFlightTreeNode (List<Flight> list) {
        Iterator<Flight> it = list.iterator();
        // root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        while (it.hasNext()) {
            Flight f = it.next();
            String titleStr = "Flugnummer: " + f.getFlightnr() + " ,   Airline: " + f.getPlane().getAirline().getName();
            // Title Object
            DefaultMutableTreeNode flight = new DefaultMutableTreeNode(titleStr);
            // Attributes
            DefaultMutableTreeNode flight_id = new DefaultMutableTreeNode("ID: " + f.getID());
            DefaultMutableTreeNode flight_nr = new DefaultMutableTreeNode("FlightNr: " + f.getFlightnr());
            // start airport
            Airport startAirport = f.getStart();
            DefaultMutableTreeNode start = new DefaultMutableTreeNode("Startflughafen: " + startAirport.getName());
            DefaultMutableTreeNode start_id = new DefaultMutableTreeNode("ID: " + startAirport.getID());
            DefaultMutableTreeNode start_tag = new DefaultMutableTreeNode("Tag: " + startAirport.getTag());
            DefaultMutableTreeNode start_name = new DefaultMutableTreeNode("Name: " + startAirport.getName());
            // dest airport
            Airport destAirport = f.getDest();
            DefaultMutableTreeNode dest = new DefaultMutableTreeNode("Zielflughafen: " + destAirport.getName());
            DefaultMutableTreeNode dest_id = new DefaultMutableTreeNode("ID: " + destAirport.getID());
            DefaultMutableTreeNode dest_tag = new DefaultMutableTreeNode("Tag: " + destAirport.getTag());
            DefaultMutableTreeNode dest_name = new DefaultMutableTreeNode("Name: " + destAirport.getName());
            // plane and airline
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
        }

    return root;
    }


    /**
     * creates a 'Tree' of
     * @return root, the root node of the tree
     */
    public static DefaultMutableTreeNode createAirlineTreeNode (List<Airline> list) {
        // list iterator for going through the list
        Iterator<Airline> it = list.iterator();
        // root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");

        while (it.hasNext()) {
            // title object
            Airline airline = it.next();
            DefaultMutableTreeNode title = new DefaultMutableTreeNode("Airline: " + airline.getName());

            DefaultMutableTreeNode airline_id = new DefaultMutableTreeNode("ID: " + airline.getID());
            DefaultMutableTreeNode airline_tag = new DefaultMutableTreeNode("IATA-Tag: " + airline.getTag());
            DefaultMutableTreeNode airline_name = new DefaultMutableTreeNode("Name: " + airline.getName());

            // TODO: adding everything to title node
            title.add(airline_id);
            title.add(airline_tag);
            title.add(airline_name);

            // TODO: addding title to root node
            root.add(title);
        }

        return root;
    }

}

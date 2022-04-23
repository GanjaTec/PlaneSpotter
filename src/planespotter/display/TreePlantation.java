package planespotter.display;

import planespotter.constants.Paths;
import planespotter.controller.Controller;
import planespotter.controller.IOMaster;
import planespotter.dataclasses.Airline;
import planespotter.dataclasses.Airport;
import planespotter.dataclasses.Flight;
import planespotter.dataclasses.Plane;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.Iterator;
import java.util.List;

import static planespotter.constants.GUIConstants.*;

/**
 * @name TreePlantation
 * @author jml04
 * @version 1.1
 *
 * class TreePlantation implements methods to create tree structures
 */
public final class TreePlantation {

    /**
     * only GUI instance
     */
    private static GUI gui;
    /**
     * default plane icon in the JTree
     */
    private static final Icon PLANE_ICON = new ImageIcon(Paths.SRC_PATH + "tree_plane_icon.png");

    /**
     * private constructor
     */
    private TreePlantation () {
    }

    /**
     * initializes TreePlantation
     */
    public static void initialize () {
        gui = Controller.gui();
    }

    /**
     * creates a new list component
     *
     * @param treeNode is the root node of the given tree
     */
    public static void createTree(DefaultMutableTreeNode treeNode) {
        // initialisation new JTree
        JTree tree = TreePlantation.defaultTree(treeNode);
        tree.setVisible(true);
        gui.recieveTree(tree);
    }

    /**
     * creates an info tree for a certain flight
     *
     * @param id is the flight id from the flight to show
     */
    static void createInfoTree(int id) {
        JTree tree = TreePlantation.defaultTree(TreePlantation.oneFlightTreeNode(id));
        tree.setVisible(true);
        gui.recieveInfoTree(tree);
    }

    /**
     * @param treeNode is the root tree node
     * @return default JTree
     */
    private static JTree defaultTree (DefaultMutableTreeNode treeNode) {
        JTree tree = new JTree(treeNode);
        tree.setFont(FONT_MENU);
        tree.setBackground(DEFAULT_BG_COLOR);
        // creating tree cell renderer
        CustomCellRenderer renderer = new CustomCellRenderer();
        renderer.setBorderSelectionColor(Color.ORANGE);
        renderer.setTextNonSelectionColor(new Color(255, 255, 102));
        renderer.setTextSelectionColor(Color.ORANGE);
        renderer.setLeafIcon(PLANE_ICON);
        // TODO icons setzen
        tree.setCellRenderer(renderer);

        return tree;
    }

    /**
     *
     * @param list is the list of flights to be converted into a tree node
     * @return DefaultMutableTreeNode, the root node of the JTree, with all its children nodes
     */
    public static DefaultMutableTreeNode allFlightsTreeNode (List<Flight> list) {
        Iterator<Flight> it = list.iterator();
        // root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        while (it.hasNext()) {
            Flight f = it.next();
            String titleStr = "Flight-Nr: " + f.getFlightnr() + " ,   Planetype: " + f.getPlane().getPlanetype();
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
     * creates only ONE flight tree node
     * @return DefaultMutableTreeNode, represents a flight (as a tree)
     */
    public static DefaultMutableTreeNode oneFlightTreeNode (int id) {
        Flight f = new IOMaster().flightByID(id);
        // root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        String titleStr = "FNR: " + f.getFlightnr() + ", Type: " + f.getPlane().getPlanetype();
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

        return root;
    }


    /**
     * creates a 'Tree' of
     * @return root, the root node of the tree
     */
    public static DefaultMutableTreeNode allAirlinesTreeNode (List<Airline> list) {
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

    /**
     * private class CustomCellRenderer is a custom tree cell renderer
     * it modifies the style of the tree cells
     */
    private static class CustomCellRenderer extends DefaultTreeCellRenderer {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                          boolean sel, boolean exp, boolean leaf,
                                                          int row, boolean hasFocus ) {
                super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
                if (row % 2 == 0) {
                    setBackgroundNonSelectionColor(DEFAULT_BG_COLOR);
                } else {
                    setBackgroundNonSelectionColor(new Color(150, 150, 150));
                }
                setBackgroundSelectionColor(DEFAULT_BORDER_COLOR);

                return this;
            }
        }

}

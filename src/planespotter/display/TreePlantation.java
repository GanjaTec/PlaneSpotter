package planespotter.display;

import planespotter.constants.Paths;
import planespotter.controller.Controller;
import planespotter.controller.DataMaster;
import planespotter.dataclasses.*;
import planespotter.model.Utilities;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.Iterator;
import java.util.List;

import static planespotter.constants.DefaultObject.DEFAULT_FLIGHT;
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
    private static final Icon PLANE_ICON = new ImageIcon(Paths.SRC_PATH + "planespotter/images/tree_plane_icon.png");

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
        var tree = TreePlantation.defaultTree(treeNode);
        tree.setVisible(true);
        GUISlave.recieveTree(tree);
    }

    /**
     * creates an info tree for a certain flight
     *
     * @param flight is the flight to show
     */
    static void createFlightInfo (Flight flight) {
        JTree tree = TreePlantation.defaultTree(TreePlantation.flightInfoTreeNode(flight));
        tree.setVisible(true);
        GUISlave.recieveInfoTree(tree);
    }

    /**
     * creates an info tree for a certain flight
     *
     * @param dp is the data point to show
     */
    static void createDataPointInfo (DataPoint dp) {
        JTree tree = TreePlantation.defaultTree(TreePlantation.dataPointInfoTreeNode(dp));
        tree.setVisible(true);
        GUISlave.recieveInfoTree(tree);
    }



    /**
     * @param treeNode is the root tree node
     * @return default JTree
     */
    private static JTree defaultTree (DefaultMutableTreeNode treeNode) {
        var tree = new JTree(treeNode);
        tree.setFont(FONT_MENU);
        tree.setBackground(DEFAULT_BG_COLOR);
        tree.setOpaque(false);
        // creating tree cell renderer
        var renderer = new CustomCellRenderer();
        renderer.setBorderSelectionColor(Color.ORANGE);
        renderer.setTextNonSelectionColor(new Color(255, 255, 102));
        renderer.setTextSelectionColor(Color.ORANGE);
        renderer.setLeafIcon(PLANE_ICON);
        renderer.setOpaque(false);
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
        var root = new DefaultMutableTreeNode("");
        while (it.hasNext()) {
            Flight f = it.next();
            String titleStr = "Flight-Nr: " + f.getFlightnr() + " ,   Planetype: " + f.getPlane().getPlanetype();
            // Title Object
            var flight = new DefaultMutableTreeNode(titleStr);
            // Attributes
            var flight_id = new DefaultMutableTreeNode("ID: " + f.getID());
            var flight_nr = new DefaultMutableTreeNode("FlightNr: " + f.getFlightnr());
            // start airport
            Airport startAirport = f.getStart();
            var start = new DefaultMutableTreeNode("Startflughafen: " + startAirport.getName());
            var start_id = new DefaultMutableTreeNode("ID: " + startAirport.getID());
            var start_tag = new DefaultMutableTreeNode("Tag: " + startAirport.getTag());
            var start_name = new DefaultMutableTreeNode("Name: " + startAirport.getName());
            // dest airport
            Airport destAirport = f.getDest();
            var dest = new DefaultMutableTreeNode("Zielflughafen: " + destAirport.getName());
            var dest_id = new DefaultMutableTreeNode("ID: " + destAirport.getID());
            var dest_tag = new DefaultMutableTreeNode("Tag: " + destAirport.getTag());
            var dest_name = new DefaultMutableTreeNode("Name: " + destAirport.getName());
            // plane and airline
            Plane p = f.getPlane();
            Airline airline = p.getAirline();
            var plane = new DefaultMutableTreeNode("Plane: " + p.getPlanetype() + " von: " + airline.getName());
            var plane_id = new DefaultMutableTreeNode("ID: " + p.getID());
            var plane_icao = new DefaultMutableTreeNode("ICAO: " + p.getIcao());
            var plane_reg = new DefaultMutableTreeNode("Registration: " + p.getRegistration());
            var plane_tnr = new DefaultMutableTreeNode("Tailnumber: " + p.getTailnr());
            var plane_type = new DefaultMutableTreeNode("Type: " + p.getPlanetype());
            var p_airline = new DefaultMutableTreeNode("Airline: " + airline.getName());
            var p_airline_id = new DefaultMutableTreeNode("ID: " + airline.getID());
            var p_airline_tag = new DefaultMutableTreeNode("Tag: " + airline.getTag());
            var p_airline_name = new DefaultMutableTreeNode("Name: " + airline.getName());

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
     * // FIXME: 24.04.2022 flightNr, planeType, ICAO, registration und tailNr haben noch "s (aktuell muss hier stripString angewandt werden)
     * creates only ONE flight tree node
     * @return DefaultMutableTreeNode, represents a flight (as a tree)
     */
    public static DefaultMutableTreeNode flightInfoTreeNode (Flight f) {
        String strToStrip;
        //Flight f;
        /*if (id == -1) {
            f = DEFAULT_FLIGHT;
        } else {
            f = new DataMaster().flightByID(id);
        }*/   // plane and airline
            var plane = f.getPlane();
            var airline = plane.getAirline();
        // root node
        var root = new DefaultMutableTreeNode("");
        // Attributes
        var flight_id = new DefaultMutableTreeNode("Flight-ID: " + f.getID());
        strToStrip = "Flight-Nr.: " + f.getFlightnr();
        var flight_nr = new DefaultMutableTreeNode(Controller.stripString(strToStrip));
        strToStrip = "Callsign: " + f.getCallsign();
        var flight_callsign = new DefaultMutableTreeNode(Controller.stripString(strToStrip));
        var plane_id = new DefaultMutableTreeNode("Plane-ID: " + plane.getID());
        strToStrip = "Plane-Type: " + plane.getPlanetype();
        var plane_type = new DefaultMutableTreeNode(Controller.stripString(strToStrip));
        strToStrip = "Plane-ICAO: " + plane.getIcao();
        var plane_icao = new DefaultMutableTreeNode(Controller.stripString(strToStrip));
        strToStrip = "Plane-Registration: " + plane.getRegistration();
        var plane_reg = new DefaultMutableTreeNode(Controller.stripString(strToStrip));
        strToStrip = "Plane-Tailnr.: " + plane.getTailnr();
        var plane_tnr = new DefaultMutableTreeNode(Controller.stripString(strToStrip));
        var p_airline_id = new DefaultMutableTreeNode("Airline-ID: " + airline.getID());
        var p_airline_tag = new DefaultMutableTreeNode("Airline-Tag: " + airline.getTag());
        var p_airline_name = new DefaultMutableTreeNode("Airline-Name: " + airline.getName());
        // start airport
        Airport startAirport = f.getStart();
        var start = new DefaultMutableTreeNode("Start-Airport");
        var start_id = new DefaultMutableTreeNode("ID: " + startAirport.getID());
        var start_tag = new DefaultMutableTreeNode("Tag: " + startAirport.getTag());
        var start_name = new DefaultMutableTreeNode("Name: " + startAirport.getName());
        // dest airport
        Airport destAirport = f.getDest();
        var dest = new DefaultMutableTreeNode("Destination-Airport");
        var dest_id = new DefaultMutableTreeNode("ID: " + destAirport.getID());
        var dest_tag = new DefaultMutableTreeNode("Tag: " + destAirport.getTag());
        var dest_name = new DefaultMutableTreeNode("Name: " + destAirport.getName());

            // TODO: completing dest node
            dest.add(dest_id);
            dest.add(dest_tag);
            dest.add(dest_name);
            // TODO: completing start node
            start.add(start_id);
            start.add(start_tag);
            start.add(start_name);
        // TODO: completing flight node
        root.add(flight_id);
        root.add(flight_nr);
        root.add(flight_callsign);
        root.add(plane_id);
        root.add(plane_type);
        root.add(plane_icao);
        root.add(plane_reg);
        root.add(plane_tnr);
        root.add(p_airline_id);
        root.add(p_airline_tag);
        root.add(p_airline_name);
        root.add(start);
        root.add(dest);
        // TODO: adding flight node to root node

        return root;
    }

    /**
     *
     */
    private static DefaultMutableTreeNode dataPointInfoTreeNode (DataPoint dp) {
        int id = dp.getID(),
            flightID = dp.getFlightID(),
            speed = dp.getSpeed(),
            height = Utilities.feetToMeters(dp.getAltitude()),
            heading = dp.getHeading(),
            sqawk = dp.getSqawk();
        double  lat = dp.getPos().getLat(),
                lon = dp.getPos().getLon();
        long timestamp = dp.getTimestemp();

        var root = new DefaultMutableTreeNode("");
        root.add(new DefaultMutableTreeNode("ID: " + id));
        root.add(new DefaultMutableTreeNode("Flight-ID: " + flightID));
        root.add(new DefaultMutableTreeNode("Speed: " + speed));
        root.add(new DefaultMutableTreeNode("Height: " + height));
        root.add(new DefaultMutableTreeNode("Heading: " + heading));
        root.add(new DefaultMutableTreeNode("Sqawk-Code: " + sqawk));
        root.add(new DefaultMutableTreeNode("Position: " + lat + ", "+ lon));
        root.add(new DefaultMutableTreeNode("Timestamp: " + timestamp));

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
        var root = new DefaultMutableTreeNode("");

        while (it.hasNext()) {
            // title object
            Airline airline = it.next();
            var title = new DefaultMutableTreeNode("Airline: " + airline.getName());
            var airline_id = new DefaultMutableTreeNode("ID: " + airline.getID());
            var airline_tag = new DefaultMutableTreeNode("IATA-Tag: " + airline.getTag());
            var airline_name = new DefaultMutableTreeNode("Name: " + airline.getName());

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

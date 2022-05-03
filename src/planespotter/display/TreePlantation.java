package planespotter.display;

import planespotter.constants.Paths;
import planespotter.controller.Controller;
import planespotter.dataclasses.*;
import planespotter.model.Utilities;

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
     * default plane icon in the JTree
     */
    private static final Icon PLANE_ICON = new ImageIcon(Paths.SRC_PATH + "planespotter/images/tree_plane_icon.png");

    /**
     * private constructor
     */
    private TreePlantation () {
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
        var it = list.iterator();
        // root node
        var root = new DefaultMutableTreeNode("");
        while (it.hasNext()) {
            Flight f = it.next();
            String titleStr = "Flight-Nr: " + f.getFlightnr() + " ,   Planetype: " + f.getPlane().getPlanetype();
            // Title Object
            var flight = new DefaultMutableTreeNode(titleStr);
            // Attributes
            flight.add(new DefaultMutableTreeNode("ID: " + f.getID()));
            flight.add(new DefaultMutableTreeNode("FlightNr: " + f.getFlightnr()));
            // start airport
            Airport startAirport = f.getStart();
            var start = new DefaultMutableTreeNode("Startflughafen: " + startAirport.getName());
            start.add(new DefaultMutableTreeNode("ID: " + startAirport.getID()));
            start.add(new DefaultMutableTreeNode("Tag: " + startAirport.getTag()));
            start.add(new DefaultMutableTreeNode("Name: " + startAirport.getName()));
            // dest airport
            Airport destAirport = f.getDest();
            var dest = new DefaultMutableTreeNode("Zielflughafen: " + destAirport.getName());
            dest.add(new DefaultMutableTreeNode("ID: " + destAirport.getID()));
            dest.add(new DefaultMutableTreeNode("Tag: " + destAirport.getTag()));
            dest.add(new DefaultMutableTreeNode("Name: " + destAirport.getName()));
            // plane and airline
            Plane p = f.getPlane();
            Airline airline = p.getAirline();
            var plane = new DefaultMutableTreeNode("Plane: " + p.getPlanetype() + " von: " + airline.getName());
            plane.add(new DefaultMutableTreeNode("ID: " + p.getID()));
            plane.add(new DefaultMutableTreeNode("ICAO: " + p.getIcao()));
            plane.add(new DefaultMutableTreeNode("Registration: " + p.getRegistration()));
            plane.add(new DefaultMutableTreeNode("Tailnumber: " + p.getTailnr()));
            plane.add(new DefaultMutableTreeNode("Type: " + p.getPlanetype()));
                var p_airline = new DefaultMutableTreeNode("Airline: " + airline.getName());
                p_airline.add(new DefaultMutableTreeNode("ID: " + airline.getID()));
                p_airline.add(new DefaultMutableTreeNode("Tag: " + airline.getTag()));
                p_airline.add(new DefaultMutableTreeNode("Name: " + airline.getName()));
            plane.add(p_airline);
            // TODO: completing flight node
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
            var plane = f.getPlane();
            var airline = plane.getAirline();
        // root node
        var root = new DefaultMutableTreeNode("");
        // Attributes
        root.add(new DefaultMutableTreeNode("Flight-ID: " + f.getID()));
        strToStrip = "Flight-Nr.: " + f.getFlightnr();
        root.add(new DefaultMutableTreeNode(Controller.stripString(strToStrip)));
        strToStrip = "Callsign: " + f.getCallsign();
        root.add(new DefaultMutableTreeNode(Controller.stripString(strToStrip)));
        root.add(new DefaultMutableTreeNode("Plane-ID: " + plane.getID()));
        strToStrip = "Plane-Type: " + plane.getPlanetype();
        root.add(new DefaultMutableTreeNode(Controller.stripString(strToStrip)));
        strToStrip = "Plane-ICAO: " + plane.getIcao();
        root.add(new DefaultMutableTreeNode(Controller.stripString(strToStrip)));
        strToStrip = "Plane-Registration: " + plane.getRegistration();
        root.add(new DefaultMutableTreeNode(Controller.stripString(strToStrip)));
        strToStrip = "Plane-Tailnr.: " + plane.getTailnr();
        root.add(new DefaultMutableTreeNode(Controller.stripString(strToStrip)));
        root.add(new DefaultMutableTreeNode("Airline-ID: " + airline.getID()));
        root.add(new DefaultMutableTreeNode("Airline-Tag: " + airline.getTag()));
        root.add(new DefaultMutableTreeNode("Airline-Name: " + airline.getName()));
        // start airport
        Airport startAirport = f.getStart();
        var start = new DefaultMutableTreeNode("Start-Airport");
        start.add(new DefaultMutableTreeNode("ID: " + startAirport.getID()));
        start.add(new DefaultMutableTreeNode("Tag: " + startAirport.getTag()));
        start.add(new DefaultMutableTreeNode("Name: " + startAirport.getName()));
        root.add(start);
        // dest airport
        Airport destAirport = f.getDest();
        var dest = new DefaultMutableTreeNode("Destination-Airport");
        dest.add(new DefaultMutableTreeNode("ID: " + destAirport.getID()));
        dest.add(new DefaultMutableTreeNode("Tag: " + destAirport.getTag()));
        dest.add(new DefaultMutableTreeNode("Name: " + destAirport.getName()));
        root.add(dest);

        return root;
    }

    /**
     * @param dp is the data point to be shown
     * @return data point info root tree node
     */
    private static DefaultMutableTreeNode dataPointInfoTreeNode (DataPoint dp) {
        int id = dp.getID(),
            flightID = dp.getFlightID(),
            speed = Utilities.feetToMeters(dp.getSpeed()),
            height = Utilities.feetToMeters(dp.getAltitude()),
            heading = dp.getHeading(),
            sqawk = dp.getSqawk();
        double  lat = dp.getPos().getLat(),
                lon = dp.getPos().getLon();
        long timestamp = dp.getTimestemp();

        var root = new DefaultMutableTreeNode("");
        root.add(new DefaultMutableTreeNode("ID: " + id));
        root.add(new DefaultMutableTreeNode("Flight-ID: " + flightID));
        root.add(new DefaultMutableTreeNode("Speed: " + speed + "  km/h"));
        root.add(new DefaultMutableTreeNode("Height: " + height + "m"));
        root.add(new DefaultMutableTreeNode("Heading: " + heading + "°"));
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

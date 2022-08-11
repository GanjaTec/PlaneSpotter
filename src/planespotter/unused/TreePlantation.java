package planespotter.unused;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.dataclasses.*;
import planespotter.unused.GUI;
import planespotter.util.Utilities;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.Iterator;
import java.util.List;

import static planespotter.constants.DefaultColor.*;
import static planespotter.constants.GUIConstants.*;

/**
 * @name TreePlantation
 * @author jml04
 * @version 1.1
 *
 * class TreePlantation implements methods to create tree structures
 */
@Deprecated(since = "new UserInterface class", forRemoval = true)
public final class TreePlantation {

    /**
     * default plane icon in the JTree
     */
    // FIXME: 24.06.2022 REPLACE WITH GOOD ICON for Info Tree Values
    private final Icon PLANE_ICON = null;

    /**
     * private constructor
     */
    public TreePlantation() {
    }

    /**
     * creates a new list component
     *
     * @param treeNode is the root node of the given tree
     */
    public void createTree(DefaultMutableTreeNode treeNode, GUI gui) {
        // initialisation new JTree
        var tree = this.defaultTree(treeNode);
        tree.setVisible(true);
        gui.receiveTree(tree);
    }

    /**
     * creates an info tree for a certain flight
     *
     * @param flight is the flight to show
     */
    public void createFlightInfo(Flight flight, GUI gui) {
        JTree tree = this.defaultTree(this.flightInfoTreeNode(flight));
        tree.setVisible(true);
        gui.receiveInfoTree(tree, null);
    }

    /**
     * creates an info tree for a certain flight
     *
     * @param dp is the data point to show
     */
    public void createDataPointInfo(Flight flight, DataPoint dp, GUI gui) {
        var flightInfo = this.defaultTree(this.flightInfoTreeNode(flight));
        var dpInfo = this.defaultTree(this.dataPointInfoTreeNode(dp));
        gui.receiveInfoTree(flightInfo, dpInfo);
        dpInfo.setVisible(true);
    }



    /**
     * @param treeNode is the root tree node
     * @return default JTree
     */
    @NotNull
    private  JTree defaultTree(@NotNull DefaultMutableTreeNode treeNode) {
        var tree = new JTree(treeNode);
        tree.setFont(FONT_MENU);
        tree.setBackground(DEFAULT_BG_COLOR.get());
        tree.setOpaque(false);
        // creating tree cell renderer
        var renderer = new TreeCellRendererImpl();
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
    public  DefaultMutableTreeNode allFlightsTreeNode(List<Flight> list) {
        var it = list.iterator();
        // root node
        var root = new DefaultMutableTreeNode("");
        while (it.hasNext()) {
            Flight f = it.next();
            String titleStr = "Flight-Nr: " + f.flightNr() + " ,   Planetype: " + f.plane().planeType();
            // Title Object
            var flight = new DefaultMutableTreeNode(titleStr);
            // Attributes
            flight.add(new DefaultMutableTreeNode("ID: " + f.id()));
            flight.add(new DefaultMutableTreeNode("FlightNr: " + f.flightNr()));
            // src airport
            Airport startAirport = f.src();
            var start = new DefaultMutableTreeNode("Startflughafen: " + startAirport.name());
            start.add(new DefaultMutableTreeNode("ID: " + startAirport.id()));
            start.add(new DefaultMutableTreeNode("Tag: " + startAirport.iataTag()));
            start.add(new DefaultMutableTreeNode("Name: " + startAirport.name()));
            // dest airport
            Airport destAirport = f.dest();
            var dest = new DefaultMutableTreeNode("Zielflughafen: " + destAirport.name());
            dest.add(new DefaultMutableTreeNode("ID: " + destAirport.id()));
            dest.add(new DefaultMutableTreeNode("Tag: " + destAirport.iataTag()));
            dest.add(new DefaultMutableTreeNode("Name: " + destAirport.name()));
            // plane and airline
            Plane p = f.plane();
            Airline airline = p.airline();
            var plane = new DefaultMutableTreeNode("Plane: " + p.planeType() + " von: " + airline.name());
            plane.add(new DefaultMutableTreeNode("ID: " + p.id()));
            plane.add(new DefaultMutableTreeNode("ICAO: " + p.icao()));
            plane.add(new DefaultMutableTreeNode("Registration: " + p.registration()));
            plane.add(new DefaultMutableTreeNode("Tailnumber: " + p.tailNr()));
            plane.add(new DefaultMutableTreeNode("Type: " + p.planeType()));
                var p_airline = new DefaultMutableTreeNode("Airline: " + airline.name());
                p_airline.add(new DefaultMutableTreeNode("ID: " + airline.id()));
                p_airline.add(new DefaultMutableTreeNode("Tag: " + airline.iataTag()));
                p_airline.add(new DefaultMutableTreeNode("Name: " + airline.name()));
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
    public DefaultMutableTreeNode flightInfoTreeNode(@NotNull Flight f) {
        String strToStrip;
        var plane = f.plane();
        var airline = plane.airline();
        // root node
        var root = new DefaultMutableTreeNode("");
        // Attributes
        root.add(new DefaultMutableTreeNode("Flight-ID: " + f.id()));
        strToStrip = "Flight-Nr.: " + f.flightNr();
        root.add(new DefaultMutableTreeNode(Utilities.stripString(strToStrip)));
        strToStrip = "Callsign: " + f.callsign();
        root.add(new DefaultMutableTreeNode(Utilities.stripString(strToStrip)));
        root.add(new DefaultMutableTreeNode("Plane-ID: " + plane.id()));
        strToStrip = "Plane-Type: " + plane.planeType();
        root.add(new DefaultMutableTreeNode(Utilities.stripString(strToStrip)));
        strToStrip = "Plane-ICAO: " + plane.icao();
        root.add(new DefaultMutableTreeNode(Utilities.stripString(strToStrip)));
        strToStrip = "Plane-Registration: " + plane.registration();
        root.add(new DefaultMutableTreeNode(Utilities.stripString(strToStrip)));
        strToStrip = "Plane-tailNr.: " + plane.tailNr();
        root.add(new DefaultMutableTreeNode(Utilities.stripString(strToStrip)));
        root.add(new DefaultMutableTreeNode("Airline-ID: " + airline.id()));
        root.add(new DefaultMutableTreeNode("Airline-Tag: " + airline.iataTag()));
        root.add(new DefaultMutableTreeNode("Airline-Name: " + airline.name()));
        root.add(new DefaultMutableTreeNode("Airline-Country: " + airline.country()));
        // src airport
        Airport startAirport = f.src();
        var start = new DefaultMutableTreeNode("Start-Airport");
        start.add(new DefaultMutableTreeNode("ID: " + startAirport.id()));
        start.add(new DefaultMutableTreeNode("Tag: " + startAirport.iataTag()));
        start.add(new DefaultMutableTreeNode("Name: " + startAirport.name()));
        root.add(start);
        // dest airport
        Airport destAirport = f.dest();
        var dest = new DefaultMutableTreeNode("Destination-Airport");
        dest.add(new DefaultMutableTreeNode("ID: " + destAirport.id()));
        dest.add(new DefaultMutableTreeNode("Tag: " + destAirport.iataTag()));
        dest.add(new DefaultMutableTreeNode("Name: " + destAirport.name()));
        root.add(dest);

        return root;
    }

    /**
     * @param dp is the data point to be shown
     * @return data point info root tree node
     */
    private DefaultMutableTreeNode dataPointInfoTreeNode(DataPoint dp) {
        int id = dp.id(),
            flightID = dp.flightID(),
            speed = Utilities.knToKmh(dp.speed()),
            height = Utilities.feetToMeters(dp.altitude()),
            heading = dp.heading(),
            sqawk = dp.squawk();
        double  lat = dp.pos().lat(),
                lon = dp.pos().lon();
        long timestamp = dp.timestamp();

        var root = new DefaultMutableTreeNode("");
        root.add(new DefaultMutableTreeNode("ID: " + id));
        root.add(new DefaultMutableTreeNode("Flight-ID: " + flightID));
        root.add(new DefaultMutableTreeNode("Speed: " + speed + " km/h"));
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
    public DefaultMutableTreeNode allAirlinesTreeNode(List<Airline> list) {
        // list iterator for going through the list
        Iterator<Airline> it = list.iterator();
        // root node
        var root = new DefaultMutableTreeNode("");

        while (it.hasNext()) {
            // title object
            Airline airline = it.next();
            var title = new DefaultMutableTreeNode("Airline: " + airline.name());
            var airline_id = new DefaultMutableTreeNode("ID: " + airline.id());
            var airline_tag = new DefaultMutableTreeNode("IATA-Tag: " + airline.iataTag());
            var airline_name = new DefaultMutableTreeNode("Name: " + airline.name());

            // adding everything to title node
            title.add(airline_id);
            title.add(airline_tag);
            title.add(airline_name);

            // addding title to root node
            root.add(title);
        }

        return root;
    }

    @NotNull
    private JList<String> defaultJList(int width, int height, @Nullable String... listData) {
        JList<String> jList = (listData == null) ? new JList<>() : new JList<>(listData);
        jList.setCellRenderer(new ListCellRendererImpl());
        jList.setSelectionBackground(DEFAULT_MAP_ICON_COLOR.get());
        jList.setBounds(0, 0, width, height);

        return jList;
    }

    public JList<String> flightInfoList(@NotNull Flight flight) {
        Plane plane = flight.plane();
        Airport src = flight.src();
        Airport dest = flight.dest();
        Airline airline = plane.airline();
        String[] listData = new String[] {
                "Flight-ID: " + flight.id(),
                "Flight-Number: " + flight.flightNr(),
                "Callsign: " + flight.callsign(),
                "Start-Airport-ID: " + src.id(),
                "Start-Airport-IATA-Tag: " + src.iataTag(),
                "Start-Airport-Name: " + src.name(),
                "Dest.-Airport-ID: " + dest.id(),
                "Dest.-Airport-IATA-Tag: " + dest.iataTag(),
                "Dest.-Airport-Name: " + dest.name(),
                "Plane-ID: " + plane.id(),
                "Plane-ICAO-Tag: " + plane.icao(),
                "Plane-Registration: " + plane.registration(),
                "Plane-Type: " + plane.planeType(),
                "Plane-Tail-Number" + plane.tailNr(),
                "Airline-ID: " + airline.id(),
                "Airline-IATA-Tag: " + airline.iataTag(),
                "Airline-Name: " + airline.name(),
                "Airline-Country: " + airline.country(),

        };
        return defaultJList(270, 400, listData);
    }



    /**
     * private class CustomCellRenderer is a custom tree cell renderer
     * it modifies the style of the tree cells
     */
    private static class TreeCellRendererImpl extends DefaultTreeCellRenderer {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                          boolean sel, boolean exp, boolean leaf,
                                                          int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
                if (row % 2 == 0) {
                    setBackgroundNonSelectionColor(DEFAULT_BG_COLOR.get());
                } else {
                    setBackgroundNonSelectionColor(new Color(150, 150, 150));
                }
                setBackgroundSelectionColor(DEFAULT_SEARCH_ACCENT_COLOR.get());

                return this;
            }
        }

    /**
     * private ListCellRenderer implementation for different background colors,
     * modifies cells of a JList
     */
    public static class ListCellRendererImpl extends DefaultListCellRenderer {

        public ListCellRendererImpl() {
            super();
            super.setBorder(LINE_BORDER);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            boolean to2 = index % 2 == 0;
            super.setBackground(to2 ? DEFAULT_BG_COLOR.get() : new Color(150, 150, 150));

            return this;
        }
    }

}

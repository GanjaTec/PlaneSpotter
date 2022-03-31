package planespotter.unused;

import javax.swing.*;

import planespotter.dataclasses.Flight;
import planespotter.dataclasses.Frame;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

import planespotter.model.DBOut;

public class ListView implements ActionListener, WindowListener {

    /**
     * components
     */
    private final JFrame frame, owner;
    private final JMenuBar mb;
    private final JMenu mdatei, mimport, mexport;
    private final JMenuItem reload, close;
    private final JList list;
    private JScrollPane scrollpane;

    /**
     * constructor ListView
     *
     * creates a list-view frame
     */
    public ListView (JFrame owner) {
        JList list1;

        // TODO: set the owner
        this.owner = owner;

        // TODO: new list view frame
        frame = new JFrame("List-View");
        frame.setSize(700, 800);
        frame.setResizable(false);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setBackground(Color.GRAY);
        frame.addWindowListener(this);

                // TODO: new 'reload' menu item
                reload = new JMenuItem("Reload List");
                reload.addActionListener(this);
                reload.setVisible(true);

                // TODO: new 'exit' menu item
                close = new JMenuItem("Close");
                close.addActionListener(this);
                reload.setVisible(true);

            // TODO: new 'Datei' menu
            mdatei = new JMenu("Datei");
            mdatei.add(reload);
            mdatei.add(close);
            mdatei.setVisible(true);

            // TODO: new 'Import' menu
            mimport = new JMenu("Import");
            mimport.addActionListener(this);
            mimport.setVisible(true);

            // TODO: new 'Export' menu
            mexport = new JMenu("Export");
            mexport.addActionListener(this);
            mexport.setVisible(true);

        mb = new JMenuBar();
        mb.setBounds(0, 0, frame.getWidth(), 30);
        mb.add(mdatei);
        mb.add(mimport);
        mb.add(mexport);
        mb.setVisible(true);

        //test
        JMenu lbl = new JMenu("test-string");
        lbl.setVisible(true);

        //ListOut out = new ListOut();

        //list = new JList(Controller.titleArray(Controller.createObjectList()));
        String[] items = null;
        try {
            DBOut dbo = new DBOut();
            items = new String[dbo.getAllFlights().size()];

            for (Flight f : dbo.getAllFlights()) {
                String title = "ID: " + f.getID() + " ,  FlugNr: " + f.getFlightnr() + " ,  Planetype: " +
                            f.getPlane().getPlanetype() + " ,  Airline: " + f.getPlane().getAirline().getName();
                            items[dbo.getAllFlights().indexOf(f)] = title;
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        list = new JList(items);
        list.setBounds(0, 25, 700, 775);
        list.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));



        //scrollpane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        //scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //scrollpane.setVisible(true);
        /*
        list.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int scroll = e.getScrollAmount();
                System.out.println(scroll);
            }
        });
        //list.setVisible(true);
        scrollpane.add(list);
        frame.add(scrollpane);
        */

        frame.add(mb);
        //frame.add(list);

        frame.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == close) {
            frame.dispose();
            owner.setVisible(true);
        } else if (src == reload) {

        }
    }


    /**
     * adds data to the list
     * @param toList is the JList which the data is given to
     * @param data is the data Frame given by the Controller
     */
    private void addListItem (JList toList, Frame data) {

    }

    /**
     * adds all data to the list
     * @param toList is the JList which the data is given to
     * @param data is the data Frame array given by the Controller
     */
    private void fillList (JList toList, Frame[] data) {
        for (Frame f : data) {

        }
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {}

    @Override
    public void windowClosed(WindowEvent e) {
        owner.setVisible(true);
    }

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}
}

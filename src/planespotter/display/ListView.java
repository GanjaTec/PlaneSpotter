package planespotter.display;

import javax.swing.*;

import planespotter.dataclasses.Frame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import planespotter.Controller;

public class ListView implements ActionListener {

    /**
     * components
     */
    private final JFrame frame, owner;
    private final JMenuBar mb;
    private final JMenu mdatei, mimport, mexport;
    private final JMenuItem reload, close;
    private final JList list;  //JList<Data> datalist;
    private final JScrollPane scrollpane;

    /**
     * constructor ListView
     *
     * creates a list-view frame
     */
    public ListView (JFrame owner) {

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

        list = new JList(Controller.titleArray(Controller.createObjectList()));

        list.setBounds(0, 25, 700, 775);
        list.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));


        scrollpane = new JScrollPane(list);
        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollpane.setVisible(true);

        list.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                
            }
        });
        frame.add(scrollpane);
        list.setVisible(true);

        frame.add(mb);
        frame.add(list);

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
}

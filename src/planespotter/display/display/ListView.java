package planespotter.display.display;

import planespotter.display.connection.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ListView implements ActionListener {

    /**
     * components
     */
    private final JFrame frame, owner;
    private final JMenuBar mb;
    private final JMenu mdatei, mimport, mexport;
    private final JMenuItem reload, close;
    private final JTable table;  //JList<Data> datalist;

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
        frame.setSize(600, 800);
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

        table = new JTable();
        table.setBounds(0, 0, 600, 800);

        table.setVisible(true);


        frame.add(mb);
        frame.add(table);

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
}

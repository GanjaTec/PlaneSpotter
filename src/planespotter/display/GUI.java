package planespotter.display;


import planespotter.Controller;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;

/**
 * @name
 * @author
 * @version
 */
public class GUI implements     ActionListener, KeyListener, ListSelectionListener,
                                MouseListener, MouseWheelListener, ChangeListener {

    /**
     * components
     */
    private JFrame frame;
    //private JDesktopPane dpleft, dpright;
    private JInternalFrame flist, fmap, fmenu, finfo;
    private JPanel pTitle, pList, pMap, pMenu, pSearch, pfinfo;
    private JLabel title, bground;
    private JList menulist;
    private JTree listView;
    protected JTextField search;
    protected JRadioButton rbFlight, rbAirline;
    protected JButton exit;

    /**
     * local class constants
     */
    private final ImageIcon img = new ImageIcon(this.getClass().getResource("/background.jpg")),
                            img_exit = new ImageIcon(this.getClass().getResource("/img_exit.png"));
    private final Font  font = new Font("Broadway", Font.BOLD, 20),
                        font_menu = new Font("DialogInput", Font.BOLD, 20);
    private final Border LINE_BORDER = BorderFactory.createLineBorder(Color.CYAN, 1);
    private final Color DEFAULT_BG_COLOR = Color.DARK_GRAY,
                        DEFAULT_FG_COLOR = Color.CYAN;

    /**
     * constructor for NeueGUI
     *
     */
    public GUI() {

        JFrame frame = this.initialize();
        frame.setVisible(true);


    }

    /**
     * initialize method
     * creates new GUI frame
     */
    private JFrame initialize () {

        // TODO: setting up frame
        frame = new JFrame("PlaneSpotter");
        frame.setSize(1280, 720);
        frame.setUndecorated(true);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setBackground(DEFAULT_BG_COLOR);

        /*
        // TODO: setting up right desktop pane
        dpright = new JDesktopPane();
        dpright.setBorder(LINE_BORDER);
        dpright.setBackground(DEFAULT_BG_COLOR);
        // TODO: setting up left desktop pane
        dpleft = new JDesktopPane();
        dpleft.setBorder(LINE_BORDER);
        dpleft.setBackground(DEFAULT_BG_COLOR);
         */

        // TODO: setting up internal list frame
        flist = new JInternalFrame("List-Ansicht", false);
        flist.setBounds(100, 50, 1180, 670);
        flist.setClosable(false);
        flist.setLayout(null);
        flist.setBorder(BorderFactory.createEmptyBorder());
        flist.setBackground(DEFAULT_BG_COLOR);
        flist.setFocusable(false);
        //flist.hide();

        // TODO: setting up internal map frame
        fmap = new JInternalFrame("Map-Ansicht", false);
        fmap.setBounds(100, 50, 1180, 670);
        fmap.setClosable(false);
        fmap.setLayout(null);
        fmap.setBorder(BorderFactory.createEmptyBorder());
        fmap.setBackground(DEFAULT_BG_COLOR);
        fmap.setFocusable(false);
        //fmap.hide();

        // internal frames //

        // TODO: setting up internal menu frame
        fmenu = new JInternalFrame("Menu", false);
        fmenu.setBounds(0, 50, 100, 670);
        fmenu.setBackground(DEFAULT_BG_COLOR);
        fmenu.setFocusable(false);

        // TODO: setting up internal finfo frame
        finfo = new JInternalFrame("finfo", false);
        finfo.setBounds(0, 50, 100, 670);
        finfo.setBackground(DEFAULT_BG_COLOR);
        finfo.setFocusable(false);

        // panels //

        // TODO: setting up title panel
        pTitle = new JPanel();
        pTitle.setBounds(0, 0, 1280, 50);
        pTitle.setOpaque(true);
        pTitle.setBackground(DEFAULT_BG_COLOR);
        pTitle.setLayout(null);

        // TODO: setting up list panel
        pList = new JPanel();
        pList.setBounds(100, 50, 1180, 670);
        pList.setBackground(DEFAULT_BG_COLOR);

        // TODO: setting up map panel
        pMap = new JPanel();
        pMap.setBounds(100, 50, 1180, 670);
        pMap.setBackground(DEFAULT_BG_COLOR);

        // TODO: setting up menu panel
        pMenu = new JPanel();
        pMenu.setBounds(0, 50, 100, 670);
        pMenu.setBackground(DEFAULT_BG_COLOR);

        // TODO: setting up search panel
        pSearch = new JPanel();
        pSearch.setBounds(0, 50, 100, 270);
        pSearch.addKeyListener(this);
        pSearch.setBackground(DEFAULT_BG_COLOR);

        // TODO: setting up finfo panel
        pfinfo = new JPanel();
        pfinfo.setBounds(0, 50, 100, 670);
        pfinfo.setBackground(DEFAULT_BG_COLOR);

        // text field //

        // TODO: setting up search text field
        search = new JTextField();
        search.setToolTipText("Search");
        search.setBounds(0, 50, 100, 30);
        search.setBackground(Color.WHITE);
        search.setFont(font);
        search.setBorder(LINE_BORDER);

        // TODO: setting up exit button
        exit = new JButton(img_exit);
        exit.setOpaque(true);
        exit.setBackground(DEFAULT_BG_COLOR);
        exit.setBounds(1230, 0, 50, 50);
        exit.addActionListener(this);

        // TODO: setting up title label
        title = new JLabel("PlaneSpotter");
        title.setFont(font.deriveFont(45f));
        title.setForeground(DEFAULT_FG_COLOR);
        title.setFocusable(false);
        title.setBounds((pTitle.getWidth() / 2) - (pTitle.getWidth() / 8), 0, 1280, 50);

        // TODO: setting up background image
        bground = new JLabel(img);
        bground.setSize(frame.getSize());

        // // // // // // // //
        // TODO: setting up menu list
        String[] items = {"Datei", "View", "Exit"};
        menulist = new JList(items);
        menulist.addListSelectionListener(this);
        menulist.setBounds(0, 50, 100, 670);
        menulist.setBackground(DEFAULT_BG_COLOR);
        menulist.setForeground(DEFAULT_FG_COLOR);
        menulist.setFont(font_menu);


        // TODO: setting up radio button: "search for airline"
        rbAirline = new JRadioButton();
        rbAirline.addChangeListener(this);

        // TODO: setting up radio button: "search for flight"
        rbFlight = new JRadioButton();
        rbFlight.addChangeListener(this);


        // TODO: adding everything to search panel
        pSearch.add(search);
        pSearch.add(rbAirline);
        pSearch.add(rbFlight);
        // TODO: adding everything to menu panel
        pMenu.add(menulist);
        // TODO: adding everything to title panel
        pTitle.add(title);
        pTitle.add(exit);
        // TODO: adding everything to list panel
        pList.add(createListView());

        // TODO: adding everything to internal menu frame
        fmenu.add(pMenu);
        // TODO: adding everything to internal finfo frame
        finfo.add(pfinfo);
        // TODO: adding everything to internal map frame
        fmap.add(pMap);
        // TODO: adding everything to internal list frame
        flist.add(pList);

        /*
        // TODO: adding internal frames to dpright
        dpright.add(flist);
        dpright.add(fmap);
        // TODO: adding internal frames to dpleft
        dpleft.add(fmenu);
        dpleft.add(finfo);
         */

        // TODO: adding title panel to frame
        frame.add(pTitle);
        /*
        // TODO: adding desktop panes to frame
        frame.add(dpright);
        frame.add(dpleft);

        dpright.setVisible(true);
        dpleft.setVisible(true);
        dpright.moveToFront(flist);
        dpleft.moveToFront(fmenu);
         */
        frame.add(flist);
        frame.add(fmap);
        frame.add(fmenu);
        frame.add(finfo);

        flist.show();
        //fmap.show();
        fmenu.show();
        //finfo.show();

        return frame;
    }

    /**T
     * creates a new list component
     * @return new JList for data models
     */
    private JTree createListView () {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode n2 = new DefaultMutableTreeNode("test 2");
        DefaultMutableTreeNode n3 = new DefaultMutableTreeNode("test test objekt test");
        root.add(n2);
        root.add(n3);
        listView = new JTree(root);

        //listView = new JTree(Controller.flightTree());
        // Exception in thread "main" java.lang.IndexOutOfBoundsException: Index 1 out of bounds for length 1
        listView.setFont(font_menu);
        listView.setBackground(DEFAULT_BG_COLOR);
        listView.setForeground(DEFAULT_FG_COLOR);
        listView.setBounds(100, 50, 1180, 670);

        return listView;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == exit)  System.exit(0);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        Object src = e.getSource();
    }
}

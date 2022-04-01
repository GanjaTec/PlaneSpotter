package planespotter.display;


import planespotter.controller.Controller;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

import static planespotter.constants.GUIConstants.*;

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
    private JDesktopPane dpleft, dpright;
    private JInternalFrame flist, fmap, fmenu, finfo;
    private static JPanel mainpanel, pTitle, pList, pMap, pMenu, pSearch, pInfo;
    private JLabel title, icon, bground;
    private JMenu datei, view, settings, search_settings;
    private JMenuItem iport, export, exit, view_list, view_map;
    private static JTree listView;
    //private static DefaultMutableTreeNode tree;
    private JTextField search;
    private JRadioButton rbFlight, rbAirline;
    private JProgressBar progressbar;
    private JMenuBar menubar;

    /**
     * class constants
     */
    // icons / images
    private final ImageIcon img = new ImageIcon(this.getClass().getResource("/background.jpg")),
                            planespotter_icon = new ImageIcon(this.getClass().getResource("/planespotter_icon.png"));
    // line border
    private final Border    LINE_BORDER = BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR, 1),
                            MENU_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR), BorderFactory.createLineBorder(DEFAULT_FG_COLOR));


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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(null);

        // TODO: setting up mainpanel
        mainpanel = new JPanel();
        mainpanel.setBounds(0, 0, 1266, 685); // mainpanel width: 1266
        mainpanel.setLayout(null);
        mainpanel.setBackground(DEFAULT_BG_COLOR);


        // TODO: setting up right desktop pane
        dpright = new JDesktopPane();
        dpright.setBorder(LINE_BORDER);
        dpright.setBackground(DEFAULT_BG_COLOR);
        dpright.setBounds(280, 70, 986, 615);
        // TODO: setting up left desktop pane
        dpleft = new JDesktopPane();
        dpleft.setBorder(LINE_BORDER);
        dpleft.setBackground(DEFAULT_BG_COLOR);
        dpleft.setBounds(0, 70, 280, 615);


            // TODO: setting up internal list frame
            flist = new JInternalFrame("List-View", false);
            flist.setBounds(0, 0, 986, 615);
            flist.setClosable(false);
            flist.setLayout(null);
            flist.setBackground(DEFAULT_BG_COLOR);
            flist.setFocusable(false);
            flist.setBorder(LINE_BORDER);
            //flist.hide();

            // TODO: setting up internal map frame
            fmap = new JInternalFrame("Map-Ansicht", false);
            fmap.setBounds(0, 0, 985, 615);
            fmap.setClosable(false);
            fmap.setLayout(null);
            fmap.setBorder(BorderFactory.createEmptyBorder());
            fmap.setBackground(DEFAULT_BG_COLOR);
            fmap.setFocusable(false);
            //fmap.hide();

            // TODO: setting up internal menu frame
            fmenu = new JInternalFrame("Menu", false);
            fmenu.setBounds(0, 0, 280, 615);
            fmenu.setBackground(DEFAULT_BG_COLOR);
            //fmenu.setFocusable(false);
            fmenu.setBorder(LINE_BORDER);
            fmenu.setLayout(null);
    
            // TODO: setting up internal finfo frame
            finfo = new JInternalFrame("finfo", false);
            finfo.setBounds(0, 0, 280, 615);
            finfo.setBackground(DEFAULT_BG_COLOR);
            finfo.setFocusable(false);
            //finfo.setBorder(LINE_BORDER);

                // panels //
        
                // TODO: setting up title panel
                pTitle = new JPanel();
                pTitle.setBounds(0, 0, 1266, 70);
                pTitle.setOpaque(true);
                pTitle.setBackground(DEFAULT_BG_COLOR);
                pTitle.setLayout(null);
                pTitle.setBorder(LINE_BORDER);

                // TODO: setting up list panel
                pList = new JPanel();
                pList.setBounds(0, 0, 990, 615);
                pList.setBackground(DEFAULT_BG_COLOR);
                pList.setLayout(null);
        
                // TODO: setting up map panel
                pMap = new JPanel();
                pMap.setBounds(0, 0, 990, 615);
                pMap.setBackground(DEFAULT_BG_COLOR);

                // TODO: setting up menu panel
                pMenu = new JPanel();
                pMenu.setBounds(0, 0, 280, 615);
                pMenu.setBackground(DEFAULT_BG_COLOR);
                pMenu.setLayout(null);

                // TODO: setting up info panel
                pInfo = new JPanel();
                pInfo.setBounds(0, 0, 280, 615);
                pInfo.setBackground(DEFAULT_BG_COLOR);

                    // TODO: setting up search text field
                    search = new JTextField();
                    search.setToolTipText("Search");
                    search.setBounds(10, 470, 260, 25);
                    search.setBackground(Color.WHITE);
                    search.setFont(FONT_MENU);
                    search.setBorder(MENU_BORDER);

                    // TODO: setting up icon image label
                    icon = new JLabel(planespotter_icon);
                    icon.setOpaque(true);
                    icon.setBackground(DEFAULT_BG_COLOR);
                    icon.setFocusable(false);
                    icon.setBounds(709, 5, 60, 60);
                    icon.setBorder(BorderFactory.createEmptyBorder());

                    // TODO: setting up title label
                    title = new JLabel("P l a n e S p    t t e r");
                    title.setFont(TITLE_FONT.deriveFont(65f));
                    title.setForeground(DEFAULT_FG_COLOR);
                    title.setFocusable(false);
                    title.setBounds(310, 0, 1280, 70);

                    // TODO: setting up background image
                    bground = new JLabel(img);
                    bground.setSize(mainpanel.getSize());

                    // TODO: setting up menubar
                    menubar = new JMenuBar();
                    menubar.setBackground(DEFAULT_BG_COLOR);
                    menubar.setForeground(DEFAULT_FG_COLOR);
                    menubar.setBounds(0, 0, 280, 590);
                    menubar.setLayout(null);

                    // TODO: setting up datei menu
                    datei = new JMenu("File");
                    datei.setFont(FONT_MENU);
                    datei.setBorder(MENU_BORDER);
                    datei.setBackground(DEFAULT_BG_COLOR);
                    datei.setForeground(DEFAULT_FG_COLOR);
                    datei.setBounds(10, 15, 260,  25);
                    datei.addActionListener(this);

                    // TODO: setting up view menu
                    view = new JMenu("View");
                    view.setFont(FONT_MENU);
                    view.setBorder(MENU_BORDER);
                    view.setBackground(DEFAULT_BG_COLOR);
                    view.setForeground(DEFAULT_FG_COLOR);
                    view.setBounds(10, 55, 260, 25);
                    view.addActionListener(this);

                        // TODO: setting up view_list item
                        view_list = new JMenuItem("List-View");
                        view_list.addMouseListener(this);
                        view_list.setBounds(10, 70, 260, 25);
                        view_list.setBackground(DEFAULT_BG_COLOR);
                        view_list.setForeground(DEFAULT_FG_COLOR);
                        view_list.setBorder(MENU_BORDER);
                        view_list.setFont(FONT_MENU);
                        view_list.setOpaque(true);
                        view_list.setVisible(true);

                    // TODO: setting up settings menu
                    settings = new JMenu("Settings");
                    settings.setFont(FONT_MENU);
                    settings.setBorder(MENU_BORDER);
                    settings.setBackground(DEFAULT_BG_COLOR);
                    settings.setForeground(DEFAULT_FG_COLOR);
                    settings.setBounds(10, 95, 260,  25);
                    settings.addActionListener(this);

                    // TODO: setting up search-settings menu
                    search_settings = new JMenu("Search-Filter");
                    search_settings.setFont(FONT_MENU);
                    search_settings.setBorder(MENU_BORDER);
                    search_settings.setBackground(DEFAULT_BG_COLOR);
                    search_settings.setForeground(DEFAULT_FG_COLOR);
                    search_settings.setBounds(10, 515, 260, 25);
                    search_settings.addActionListener(this);

                    // TODO: seting up progress bar
                    progressbar = new JProgressBar(0, 100);
                    progressbar.setBorder(MENU_BORDER);
                    progressbar.setBackground(DEFAULT_BG_COLOR);
                    progressbar.setForeground(DEFAULT_FG_COLOR);
                    progressbar.setBounds(10, 555, 260, 25);
                    progressbar.setVisible(false);
                    progressbar.setValue(0);

                    // TODO: setting up radio button: "search for airline"
                    rbAirline = new JRadioButton();
                    rbAirline.addChangeListener(this);

                    // TODO: setting up radio button: "search for flight"
                    rbFlight = new JRadioButton();
                    rbFlight.addChangeListener(this);


                    // TODO: adding everything to view item
                    view.add(view_list);

                    // TODO: adding everything to menubar
                    menubar.add(datei);
                    menubar.add(view);
                    menubar.add(settings);
                    menubar.add(search_settings);
                    menubar.add(search);
                    menubar.add(progressbar);

                // TODO: adding menubar to panel
                pMenu.add(menubar);

                // TODO: adding everything to title panel
                pTitle.add(icon);
                pTitle.add(title);

        /*
        // TODO: adding everything to list panel
        try {
            Controller.createFlightTree();
        } catch (SQLException e) {
            e.printStackTrace();
        } // kommt noch in einen ActionListener oder so
        */




            // TODO: adding everything to internal menu frame
            fmenu.add(pMenu);
            // TODO: adding everything to internal finfo frame
            finfo.add(pInfo);
            // TODO: adding everything to internal map frame
            fmap.add(pMap);
            // TODO: adding everything to internal list frame
            flist.add(pList);
            // removing mouselisteners // klappt nicht sow wie es soll
            for (MouseListener l : frame.getMouseListeners()) {
                flist.removeMouseListener(l);

            }


        // TODO: adding internal frames to dpright
        dpright.add(flist);
        dpright.add(fmap);
        // TODO: adding internal frames to dpleft
        dpleft.add(fmenu);
        dpleft.add(finfo);


        // TODO: adding title panel to frame
        mainpanel.add(pTitle);

        dpright.setVisible(true);
        dpleft.setVisible(true);
        dpright.moveToFront(flist);
        dpleft.moveToFront(fmenu);

        // TODO: adding desktop panes to frame
        mainpanel.add(dpright);
        mainpanel.add(dpleft);
        
        // TODO: adding mainpanel to frame
        frame.add(mainpanel);

        flist.show();
        //fmap.show();
        fmenu.show();
        //finfo.show();



        return frame;
    }

    /**
     *
     */
    public void progressbarVisible (boolean v) {
        progressbar.setVisible(v);
    }

    /**
     *
     */
    public static void listVisible(boolean v) {
        //listView.setVisible(v);
    }

    /**
     *
     */
    public int progressbarValue () {
        return progressbar.getValue();
    }

    /**
     * progressbar-plus-plus
     * progressbar value goes +1
     */
    public void progressbarPP () {
        progressbar.setValue(progressbar.getValue() + 1);
    }


    /**
     * static, damit der controller die methode nutzen kann, ohne eine neue gui zu oefnen
     * @param node is the tree node to recieve
     */
    //@unused
    public static void recieveTree (DefaultMutableTreeNode node) {
        //tree = node;
    }

    /**
     *
     * @return listView Tree
     */
    public static JTree getListView () {
        return listView;
    }

    /**
     *
     * @param tree is the tree to set
     */
    public static void setListView (JTree tree) {
        listView = tree;
        listView.setVisible(true);
        // TODO: setting up list scrollpane
        JScrollPane spList = new JScrollPane();
        spList.setViewportView(listView);
        spList.setBounds(0, 0, 985, 615);
        // TODO: adding list scrollpane to list pane
        pList.add(spList);
    }


    // listeners //

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

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
        if (src == view_list) {
            Controller.loadList(this);
        } // ansicht mit radio button regeln
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

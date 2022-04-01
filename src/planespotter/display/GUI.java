package planespotter.display;


import planespotter.constants.Bounds;
import planespotter.controller.Controller;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.*;
import java.awt.event.*;

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
    private JLabel title, icon, bground, title_bground, list_title;
    private JMenu datei, settings, search_settings;
    private JMenuItem iport, export, exit, view_list, view_map;
    private static JTree listView;
    //private static DefaultMutableTreeNode tree;
    private JTextField search;
    private JRadioButton rbFlight, rbAirline;
    private JProgressBar progressbar;
    private JMenuBar menubar;
    private JButton btList, btMap;

    /**
     * class constants
     */
// icons / images
    private final ImageIcon img = new ImageIcon(this.getClass().getResource("/background.jpg")),
                            title_bground_img = new ImageIcon(this.getClass().getResource("/title_background.jpg")),
                            flying_plane_icon = new ImageIcon(this.getClass().getResource("/flying_plane_icon.png"));
    // line border
    private final Border    LINE_BORDER = BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR, 1),
                            MENU_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createLineBorder(DEFAULT_FG_COLOR));


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

        //default desktop width
        int WIDTH_RIGHT = 1259-280;
        int WIDTH_LEFT = 1259-WIDTH_RIGHT; // unnötig (=279)
        // large menu item width
        int WIDTH_MENUITEM = WIDTH_LEFT-25;

        // TODO: setting up frame
        frame = new JFrame("PlaneSpotter");
        frame.setSize(Bounds.ALL.getSize());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(null);

        // TODO: setting up mainpanel
        mainpanel = new JPanel();
        mainpanel.setBounds(Bounds.MAINPANEL); // mainpanel width: 1260
        mainpanel.setLayout(null);
        mainpanel.setBackground(DEFAULT_BG_COLOR);
        mainpanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 6));

        // TODO: setting up right desktop pane
        dpright = new JDesktopPane();
        dpright.setBorder(LINE_BORDER);
        dpright.setBackground(DEFAULT_BG_COLOR);
        dpright.setDesktopManager(new DefaultDesktopManager());
        dpright.setBounds(Bounds.RIGHT_MAIN);
        // TODO: setting up left desktop pane
        dpleft = new JDesktopPane();
        dpleft.setBorder(LINE_BORDER);
        dpleft.setBackground(DEFAULT_BG_COLOR);
        dpleft.setDesktopManager(new DefaultDesktopManager());
        dpleft.setBounds(Bounds.LEFT_MAIN);


            // TODO: setting up internal list frame
            flist = new JInternalFrame("List-View", false);
            flist.setBounds(Bounds.RIGHT);
            flist.setClosable(false);
            flist.setLayout(null);
            flist.setBackground(DEFAULT_BORDER_COLOR);
            flist.setFocusable(false);
            flist.setBorder(LINE_BORDER);
            //flist.hide();

            // TODO: setting up internal map frame
            fmap = new JInternalFrame("Map-Ansicht", false);
            fmap.setBounds(Bounds.RIGHT);
            fmap.setClosable(false);
            fmap.setLayout(null);
            fmap.setBorder(BorderFactory.createEmptyBorder());
            fmap.setBackground(DEFAULT_BG_COLOR);
            fmap.setFocusable(false);
            //fmap.hide();

            // TODO: setting up internal menu frame
            fmenu = new JInternalFrame("Menu", false);
            fmenu.setBounds(Bounds.LEFT);
            fmenu.setBackground(DEFAULT_BORDER_COLOR);
            //fmenu.setFocusable(false);
            fmenu.setBorder(LINE_BORDER);
            fmenu.setLayout(null);
    
            // TODO: setting up internal finfo frame
            finfo = new JInternalFrame("finfo", false);
            finfo.setBounds(Bounds.LEFT);
            finfo.setBackground(DEFAULT_BG_COLOR);
            finfo.setFocusable(false);
            //finfo.setBorder(LINE_BORDER);

                // panels //
        
                // TODO: setting up title panel
                pTitle = new JPanel();
                pTitle.setBounds(Bounds.TITLE);
                pTitle.setOpaque(true);
                pTitle.setBackground(DEFAULT_BG_COLOR);
                pTitle.setLayout(null);
                pTitle.setBorder(LINE_BORDER);

                    // TODO: setting up title backround img
                    title_bground = new JLabel(title_bground_img);
                    title_bground.setBounds(Bounds.TITLE);
                    title_bground.setBorder(LINE_BORDER);

                    /*
                    // TODO: setting up list panel title
                    list_title = new JLabel("Data List");
                    list_title.setBounds(315, 5, 300, 25);
                    list_title.setForeground(DEFAULT_FG_COLOR);
                    list_title.setBorder(LINE_BORDER);
                    */

                // TODO: setting up list panel
                pList = new JPanel();
                pList.setBounds(0, 0, WIDTH_RIGHT, 615);
                pList.setBackground(DEFAULT_BG_COLOR);
                pList.setLayout(null);
        
                // TODO: setting up map panel
                pMap = new JPanel();
                pMap.setBounds(0, 0, WIDTH_RIGHT, 615);
                pMap.setBackground(DEFAULT_BG_COLOR);

                // TODO: setting up menu panel
                pMenu = new JPanel();
                pMenu.setBounds(0, 0, WIDTH_LEFT, 615);
                pMenu.setBackground(DEFAULT_BG_COLOR);
                pMenu.setLayout(null);

                // TODO: setting up info panel
                pInfo = new JPanel();
                pInfo.setBounds(0, 0,  WIDTH_LEFT, 615);
                pInfo.setBackground(DEFAULT_BG_COLOR);

                    // TODO: setting up search text field
                    search = new JTextField();
                    search.setToolTipText("Search");
                    search.setBounds(10, 470, WIDTH_MENUITEM, 25);
                    search.setBackground(Color.WHITE);
                    search.setFont(FONT_MENU);
                    search.setBorder(MENU_BORDER);
                    search.addKeyListener(this);

                    /*
                    // TODO: setting up icon image label
                    icon = new JLabel(planespotter_icon);
                    icon.setOpaque(true);
                    icon.setBackground(DEFAULT_BG_COLOR);
                    icon.setFocusable(false);
                    icon.setBounds(709, 5, 60, 60);
                    icon.setBorder(BorderFactory.createEmptyBorder());
                     */

                    // TODO: setting up title label
                    title = new JLabel("P l a n e S p    t t e r");
                    title.setFont(TITLE_FONT);
                    title.setForeground(DEFAULT_FG_COLOR);
                    title.setFocusable(false);
                    title.setBounds(400, 0, 1660, 70);

                    // TODO: setting up background image
                    bground = new JLabel(img);
                    bground.setSize(mainpanel.getSize());

                    // TODO: setting up menubar
                    menubar = new JMenuBar();
                    menubar.setBackground(DEFAULT_BG_COLOR);
                    menubar.setForeground(DEFAULT_FG_COLOR);
                    menubar.setBounds(0, 0, WIDTH_LEFT, 590);
                    menubar.setLayout(null);

                    // TODO: setting up datei menu
                    datei = new JMenu("File");
                    datei.setFont(FONT_MENU);
                    datei.setBorder(MENU_BORDER);
                    datei.setBackground(DEFAULT_BG_COLOR);
                    datei.setForeground(DEFAULT_FONT_COLOR);
                    datei.setBounds(10, 15, WIDTH_MENUITEM,  25);
                    datei.addActionListener(this);

                    // TODO setting up list button
                    btList = new JButton("List-View");
                    btList.setBackground(Color.DARK_GRAY);
                    btList.setForeground(DEFAULT_FONT_COLOR);
                    btList.setBorder(MENU_BORDER);
                    btList.addMouseListener(this);
                    btList.setBounds(10, 55, 120, 25);
                    btList.setFont(FONT_MENU);

                    // TODO setting up list button
                    btMap = new JButton("List-View");
                    btMap.setBackground(Color.DARK_GRAY);
                    btMap.setForeground(DEFAULT_FONT_COLOR);
                    btMap.setBorder(MENU_BORDER);
                    btMap.addMouseListener(this);
                    btMap.setBounds(145, 55, 120, 25);
                    btMap.setFont(FONT_MENU);


                    // TODO: setting up settings menu
                    settings = new JMenu("Settings");
                    settings.setFont(FONT_MENU);
                    settings.setBorder(MENU_BORDER);
                    settings.setBackground(DEFAULT_BG_COLOR);
                    settings.setForeground(DEFAULT_FONT_COLOR);
                    settings.setBounds(10, 95, WIDTH_MENUITEM,  25);
                    settings.addActionListener(this);

                    // TODO: setting up search-settings menu
                    search_settings = new JMenu("Search-Filter");
                    search_settings.setFont(FONT_MENU);
                    search_settings.setBorder(MENU_BORDER);
                    search_settings.setBackground(DEFAULT_BG_COLOR);
                    search_settings.setForeground(DEFAULT_FONT_COLOR);
                    search_settings.setBounds(10, 515, WIDTH_MENUITEM, 25);
                    search_settings.addActionListener(this);

                    // TODO: seting up progress bar
                    progressbar = new JProgressBar(0, 100);
                    progressbar.setBorder(MENU_BORDER);
                    progressbar.setBackground(DEFAULT_BG_COLOR);
                    progressbar.setForeground(new Color(0, 255, 0));
                    progressbar.setBounds(10, 555, WIDTH_MENUITEM, 25);
                    progressbar.setVisible(false);
                    progressbar.setValue(0);

                    // TODO: setting up radio button: "search for airline"
                    rbAirline = new JRadioButton();
                    rbAirline.addChangeListener(this);

                    // TODO: setting up radio button: "search for flight"
                    rbFlight = new JRadioButton();
                    rbFlight.addChangeListener(this);


                    // TODO: adding everything to view item
                    //view.add(view_list);

                    // TODO: adding everything to menubar
                    menubar.add(datei);
                    menubar.add(btList);
                    menubar.add(btMap);
                    menubar.add(settings);
                    menubar.add(search_settings);
                    menubar.add(search);
                    menubar.add(progressbar);

                // TODO: adding menubar to panel
                pMenu.add(menubar);

                // TODO: adding everything to title panel
                //pTitle.add(icon);
                pTitle.add(title);
                pTitle.add(title_bground);

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

        removeAllTitlePanes();

        flist.show();

        //fmap.show();
        fmenu.show();
        //finfo.show();



        return frame;
    }

    /**
     * removes the title pane from intl. list frame
     * -> makes it un-movable
     */
    private void removeListTitlePane () {
        BasicInternalFrameTitlePane titlePane =(BasicInternalFrameTitlePane)((BasicInternalFrameUI)flist.getUI()).getNorthPane();
        flist.remove(titlePane);
        BasicInternalFrameTitlePane newTitlePane =(BasicInternalFrameTitlePane)((BasicInternalFrameUI)flist.getUI()).getNorthPane();
        newTitlePane.setFocusable(false);
        newTitlePane.setEnabled(false);
        flist.add(newTitlePane);
    }

    /**
     * removes all title panes from all intl. frames
     * -> makes it un-movable
     */
    private void removeAllTitlePanes () {
        //flist
        BasicInternalFrameTitlePane titlePane =(BasicInternalFrameTitlePane)((BasicInternalFrameUI)flist.getUI()).getNorthPane();
        flist.remove(titlePane);
        //fmap
        titlePane =(BasicInternalFrameTitlePane)((BasicInternalFrameUI)fmap.getUI()).getNorthPane();
        fmap.remove(titlePane);
        //fmanu
        titlePane =(BasicInternalFrameTitlePane)((BasicInternalFrameUI)fmenu.getUI()).getNorthPane();
        fmenu.remove(titlePane);
        //finfo
        titlePane =(BasicInternalFrameTitlePane)((BasicInternalFrameUI)finfo.getUI()).getNorthPane();
        finfo.remove(titlePane);
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
    public static void recieveTree (JTree tree) {
        listView = tree;
        listView.setVisible(true);
        // TODO: setting up list scrollpane
        JScrollPane spList = new JScrollPane();
        spList.setViewportView(listView);
        spList.setBounds(Bounds.RIGHT);
        // TODO: adding list scrollpane to list pane
        pList.add(spList);
    }


    /********************************************
     *                listeners                 *
     * *****************************************+
     */

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

    }

    @Override
    public void keyTyped(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            enterText();
        }
    }

        /**
         * enters the text in the textfield
         */
        private void enterText () {
            String text = search.getText().toLowerCase();
            if (!text.isBlank()) {
                switch (text) {
                    case "exit":
                        Controller.exit();
                        break;
                    default:

                }
            }
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

package planespotter.display;


import planespotter.constants.Bounds;
import planespotter.constants.ViewType;
import planespotter.controller.Controller;
import planespotter.dataclasses.Flight;
import planespotter.model.DBOut;

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
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static planespotter.constants.GUIConstants.*;

/**
 * @name
 * @author
 * @version
 */
public class GUI extends Thread implements     ActionListener, KeyListener, ListSelectionListener,
                                MouseListener, MouseWheelListener, ChangeListener {

    /**
     * components
     */
    private JFrame frame;
    private JDesktopPane dpleft, dpright;
    private JInternalFrame flist, fmap, fmenu, finfo;
    private static JPanel mainpanel, pTitle, pList, pMap, pMenu, pInfo;
    private JLabel title, bground, title_bground;
    private static JTree listView;
    private JTextField search;
    private JRadioButton rbFlight, rbAirline;
    private JProgressBar progressbar;
    private JMenuBar menubar;
    private JButton datei, settings, search_settings, btList, btMap;

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
            fmap.setBackground(DEFAULT_BORDER_COLOR);
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
                    datei = new JButton("File");
                    datei.setFont(FONT_MENU);
                    datei.setBorder(MENU_BORDER);
                    datei.setBackground(DEFAULT_ACCENT_COLOR);
                    datei.setForeground(DEFAULT_FONT_COLOR);
                    datei.setBounds(10, 15, WIDTH_MENUITEM,  25);
                    datei.addActionListener(this);

                    // TODO setting up list button
                    btList = new JButton("List-View");
                    btList.setBackground(DEFAULT_ACCENT_COLOR);
                    btList.setForeground(DEFAULT_FONT_COLOR);
                    btList.setBorder(MENU_BORDER);
                    btList.setBounds(10, 55, 120, 25);
                    btList.setFont(FONT_MENU);
                    btList.addActionListener(this);

                    // TODO setting up list button
                    btMap = new JButton("Map-View");
                    btMap.setBackground(DEFAULT_ACCENT_COLOR);
                    btMap.setForeground(DEFAULT_FONT_COLOR);
                    btMap.setBorder(MENU_BORDER);
                    btMap.setBounds(145, 55, 120, 25);
                    btMap.setFont(FONT_MENU);
                    btMap.addActionListener(this);


                    // TODO: setting up settings menu
                    settings = new JButton("Settings");
                    settings.setFont(FONT_MENU);
                    settings.setBorder(MENU_BORDER);
                    settings.setBackground(DEFAULT_ACCENT_COLOR);
                    settings.setForeground(DEFAULT_FONT_COLOR);
                    settings.setBounds(10, 95, WIDTH_MENUITEM,  25);
                    settings.addActionListener(this);

                    // TODO: setting up search-settings menu
                    search_settings = new JButton("Search-Filter");
                    search_settings.setFont(FONT_MENU);
                    search_settings.setBorder(MENU_BORDER);
                    search_settings.setBackground(DEFAULT_ACCENT_COLOR);
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

        search.requestFocus();


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
     * @param tree is the tree to set
     */
    public void recieveTree (JTree tree) {
        listView = tree;

        // TODO: setting up list scrollpane
        JScrollPane spList = new JScrollPane();
        spList.add(listView);
        spList.setViewportView(listView);
        spList.setBounds(Bounds.RIGHT);
        spList.setBackground(DEFAULT_BG_COLOR);
        // TODO: adding list scrollpane to list pane
        pList.add(spList);
        // revalidate frame -> to make the tree visible
        frame.revalidate();
        System.out.println("tree created");
    }

    /**
     * enters the text in the textfield (use for key listener)
     */
    private void enterText () {
        String text = search.getText().toLowerCase();
        if (!text.isBlank()) {
            switch (text) {
                case "exit":
                    Controller.exit();
                    break;
                case "loadlist":
                    Controller.createDataView(ViewType.LIST_FLIGHT);
                default:

            }
        }
        search.setText("");
    }

    /**
     * @return running instance of the GUI
     */
    public GUI getInstance () {
        return (this != null) ? this : null;
    }

    /**
     *
     */
    public void loadList () {
        this.progressbarVisible(true);
        for (int i = this.progressbarValue(); i <= 100; i++) {
            this.progressbarPP();
            /*try {                                     // hier wird nur herumgespielt
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("interrupted");
            }*/
        }
        this.progressbarVisible(false);
        createFlightTree();     // nur zum testen // wird noch verallgemeinert (comment in: Controller)
    }

    /**
     * creates flight tree in GUI
     * sets tree to GUI.listView
     * TODO: add param class -> switch case -> to create every tree wanted in one method
     */
    private void createFlightTree () {
        // laeuft noch nicht, zu viele Daten
        try {
            //Airline air = new DBOut().getAirlineByTag("RYR");
            //List<Airline> list = new ArrayList<>();
            //list.add(air);
            List<Flight> list = new DBOut().getAllFlights();
            //List<Flight> list = testFlightList();
            this.recieveTree(TreePlantation.createTree(TreePlantation.createFlightTreeNode(list)));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /********************************************
     *                listeners                 *
     * *****************************************+
     */

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btList) {
            Controller.createDataView(ViewType.LIST_FLIGHT);
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }


    @Override
    public void keyPressed(KeyEvent e) {
        Object src = e.getSource();
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            if (search.hasFocus())
                enterText();
        }
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

package planespotter.display;


import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import planespotter.constants.Bounds;
import planespotter.constants.GUIConstants;
import planespotter.constants.ViewType;
import planespotter.controller.Controller;
import planespotter.dataclasses.Flight;
import planespotter.model.DBOut;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.List;

import static planespotter.constants.GUIConstants.*;

/**
 * @name
 * @author
 * @version
 */
public class GUI extends Thread implements ActionListener, KeyListener {

    /**
     * components
     */
    protected JFrame window;
    protected JDesktopPane dpleft, dpright;
    protected JInternalFrame flist, fmap, fmenu, finfo;
    protected JPanel mainpanel, pTitle, pList, pMap, pMenu, pInfo;
    protected JLabel title, bground, title_bground;
    protected JTree listView;
    protected JMapViewer mapViewer;
    protected JTextField search;
    protected JRadioButton rbFlight, rbAirline;
    protected JProgressBar progressbar;
    protected JMenuBar menubar;
    protected JButton datei, settings, search_settings, btList, btMap, closeView;

    /**
     * view semaphor
     * can be:
     *  null -> no view opened
     *  not null -> view opened
     */
    protected static Component runningView = null;

    /**
     * constructor for GUI
     */
    public GUI() {
    }

    /**
     * thread run method
     */
    public void run () {
        JFrame window = this.initialize();
        window.setVisible(true);
    }

    /**
     * initialize method
     * creates new GUI window
     */
    protected JFrame initialize () {

        // TODO: setting up window
        window = new JFrame("PlaneSpotter");
        window.setSize(Bounds.ALL.getSize());
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setLayout(null);

        // TODO: initializing mainpanel
        mainpanel = PanelModels.mainPanel();

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

        // TODO: initializing flist
        flist = InternalFrameModels.internalListFrame();
        // TODO: initializing fmap
        fmap = InternalFrameModels.internalMapFrame();
        // TODO: initializing fmenu
        fmenu = InternalFrameModels.internalMenuFrame();
        // TODO: initializing finfo
        finfo = InternalFrameModels.internalInfoFrame();

            // TODO: initializing title panel
            pTitle = PanelModels.titlePanel();
            // TODO: initializing list panel
            pList = PanelModels.listPanel();
            // TODO: initializing map panel
            pMap = PanelModels.mapPanel();
            // TODO: initializing menu panel
            pMenu = PanelModels.menuPanel();
            // TODO: initializing info panel
            pInfo = PanelModels.infoPanel();
            // TODO: initializing background label
            bground = PanelModels.backgroundLabel();

            // TODO: initializing pTitle
            menubar = MenuModels.menuBar();

                // TODO: initializing buttons
                datei = MenuModels.fileButton();
                datei.addActionListener(this);
                btList = MenuModels.listButton();
                btList.addActionListener(this);
                btMap = MenuModels.mapButton();
                btMap.addActionListener(this);
                settings = MenuModels.settingsButton();
                settings.addActionListener(this);
                search = MenuModels.searchTextField();
                search.addKeyListener(this);
                search_settings = MenuModels.searchFilterButton();
                search_settings.addActionListener(this);
                progressbar = MenuModels.progressBar();

            // TODO: initializing close view button
            closeView = MenuModels.closeViewButton();
            closeView.addActionListener(this);

            // Adding to Window

                // TODO: adding everything to menubar
                menubar.add(datei);
                menubar.add(btList);
                menubar.add(btMap);
                menubar.add(settings);
                menubar.add(search);
                menubar.add(search_settings);
                menubar.add(progressbar);

            // TODO: adding menubar to menu panel
            pMenu.add(menubar);

            // TODO: adding everything to internal menu frame
            fmenu.add(pMenu);
            // TODO: adding everything to internal finfo frame
            finfo.add(pInfo);
            // TODO: adding everything to internal map frame
            fmap.add(pMap);
            // TODO: adding everything to internal list frame
            flist.add(pList);

            //läuft noch nicht                 // auslagern
            dpright.add(closeView);
            closeView.setVisible(true);
            closeView.grabFocus();

        // TODO: adding internal frames to dpright
        dpright.add(flist);
        dpright.add(fmap);
        // TODO: adding internal frames to dpleft
        dpleft.add(fmenu);
        dpleft.add(finfo);

                // TODO: adding to pTitle
                pTitle.add(PanelModels.titleBgLabel(title_bground_img));
                pTitle.add(PanelModels.titleTxtLabel());

        // TODO: adding title panel to frame
        mainpanel.add(pTitle);
        //mainpanel.add(bground);
        // TODO: moving flist and fmenu to front
        dpright.setVisible(true);
        dpleft.setVisible(true);
        dpright.moveToFront(flist);
        dpleft.moveToFront(fmenu);
        fmenu.show();

        // TODO: adding desktop panes to frame
        mainpanel.add(dpright);
        mainpanel.add(dpleft);
        
        // TODO: adding mainpanel to frame
        window.add(mainpanel);
        // TODO: removing all internal frame title panes
        removeAllTitlePanes();


        return window;
    }

    /**
     * removes all title panes from all intl. frames
     * -> makes it un-movable
     */
    protected void removeAllTitlePanes () {
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
        if (runningView != null) {
            disposeView();
        }
        listView = tree;
        // TODO: setting up list scrollpane
        JScrollPane spList = new JScrollPane();
        spList.add(listView);
        spList.setViewportView(listView);
        spList.setBounds(Bounds.RIGHT);
        spList.setBackground(DEFAULT_BG_COLOR);
        // TODO: adding list scrollpane to list pane
        pList.add(spList);
        flist.show();
        // revalidate window -> making the tree visible
        window.revalidate();
        // setting viewRunning to TRUE
        runningView = listView;
    }

    /**
     *
     */
    public void disposeView () {
        if (listView != null) {
            if (runningView == listView) {
                listView.setVisible(false);
                listView = null;
                flist.hide();
            } else if (runningView == mapViewer) {
                mapViewer.setVisible(false);
                mapViewer = null;
                fmap.hide();
            }
        }
        runningView = null;
        window.revalidate();
    }

    /**
     *
     */
    public void recieveMap (JMapViewer map) {
        if (runningView != null) {
            disposeView();
        }
        mapViewer = map;
        // TODO: adding MapViewer to panel
        pMap.add(mapViewer);
        fmap.show();
        // revalidating window frame to refresh everything
        window.revalidate();
        // setting mapViewer as the running View
        runningView = mapViewer;
    }

    /**
     *
     */
    public JMapViewer createMap () {
        if (runningView != null) {
            disposeView();
        }
        mapViewer = new JMapViewer();
        // TODO: trying to set up JMapViewer
        mapViewer = new JMapViewer(new MemoryTileCache());
        mapViewer.setBounds(Bounds.RIGHT);
        mapViewer.setBorder(LINE_BORDER);
        new DefaultMapController(mapViewer);
        mapViewer.setTileSource(new BingAerialTileSource());
        mapViewer.setVisible(true);
        mapViewer.addKeyListener(this);
        mapViewer.setMinimumSize(Bounds.RIGHT.getSize());
        mapViewer.addMapMarker(new MapMarkerDot(new Coordinate(50.9, 7.0))); //Köln
        // TODO: adding MapViewer to panel
        pMap.add(mapViewer);
        fmap.show();
        // revalidating window frame to refresh everything
        window.revalidate();
        // setting mapViewer as the running View
        //runningView = mapViewer;

        return mapViewer;
    }

    /**
     * enters the text in the textfield (use for key listener)
     */
    protected void enterText () {
        String text = search.getText().toLowerCase();
        if (!text.isBlank()) {
            switch (text) {
                case "exit":
                    Controller.exit();
                    break;
                case "loadlist":
                    Controller.createDataView(ViewType.LIST_FLIGHT);
                    break;
                case "loadmap":
                    Controller.createDataView(ViewType.MAP);
                    break;
                default:

            }
        }
        search.setText("");
    }

    /**
     *
     */
    public void loadView (ViewType type) {
        this.progressbarVisible(true);
        for (int i = 0; i <= 100; i++) progressbarPP();
        this.progressbarVisible(false);
        switch (type) {
            case LIST_FLIGHT:
                createFlightTree();
                break;
            case LIST_PLANE:
                break;
            case LIST_AIRLINE:
                break;
            case LIST_AIRPORT:
                break;
            case MAP:
                new MapManager(this).createAllFlightsMap();
                window.revalidate();
                break;
            default:
        }
    }

    /**
     * creates flight tree in GUI
     * sets tree to GUI.listView
     * TODO: add param class -> switch case -> to create every tree wanted in one method ?inController?
     */
    protected void createFlightTree () {
        // laeuft noch nicht, zu viele Daten
        try {
            //Airline air = new DBOut().getAirlineByTag("RYR");
            //List<Airline> list = new ArrayList<>();
            //list.add(air);
            List<Flight> list = new DBOut().getAllFlights();
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
        } else if (src == btMap) {
            Controller.createDataView(ViewType.MAP);
        } else if (src == closeView) {
            disposeView();
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {
    }


    @Override
    public void keyPressed(KeyEvent e) {
        Object src = e.getSource();
        int key = e.getKeyCode();
        if (src == search) {
            if (key == KeyEvent.VK_ENTER) {
                if (search.hasFocus())
                    enterText();
            }
        } else if (src == mapViewer) {
            switch (key) {
                case KeyEvent.VK_PAGE_UP:
                    mapViewer.moveMap(0, 2);
                    window.revalidate();
                    break;
                case KeyEvent.VK_HOME:
                    mapViewer.moveMap(-2, 0);
                    window.revalidate();
                    break;
                case KeyEvent.VK_END:
                    mapViewer.moveMap(2, 0);
                    window.revalidate();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    mapViewer.moveMap(0, -2);
                    window.revalidate();
                    break;
                default:
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }



}

package planespotter.display;


import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import planespotter.constants.Bounds;
import planespotter.constants.ViewType;
import planespotter.controller.Controller;
import planespotter.dataclasses.Flight;
import planespotter.model.DBOut;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

import static planespotter.constants.GUIConstants.DEFAULT_BG_COLOR;
import static planespotter.constants.GUIConstants.LINE_BORDER;

/**
 * @name GUI
 * @author jml04
 * @version 1.1
 */
public class GUI implements ActionListener, KeyListener, JMapViewerEventListener,
                            ComponentListener, Runnable {

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
    protected JTextField search, settings_iFrame_maxLoad;
    protected JRadioButton rbFlight, rbAirline;
    protected JProgressBar progressbar;
    protected JMenuBar menubar;
    protected JButton datei, settings, search_settings, btList, btMap, closeView;
    protected JInternalFrame settings_intlFrame;
    protected JScrollPane spList;

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
        JFrame window = this.initialize();
        window.setVisible(true);
    }

    /**
     * GUI run method (?)
     */
    @Override
    public void run() {
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
        // TODO: initializing mainpanel
        mainpanel = PanelModels.mainPanel(window);
        mainpanel.addComponentListener(this);

        // TODO: setting up right desktop pane
        dpright = new JDesktopPane();
        dpright.setBorder(LINE_BORDER);
        dpright.setBackground(DEFAULT_BG_COLOR);
        dpright.setDesktopManager(new DefaultDesktopManager());
        dpright.setBounds(280, 70, mainpanel.getWidth()-280, mainpanel.getHeight()-135);
        dpright.addComponentListener(this);
        // TODO: setting up left desktop pane
        dpleft = new JDesktopPane();
        dpleft.setBorder(LINE_BORDER);
        dpleft.setBackground(DEFAULT_BG_COLOR);
        dpleft.setDesktopManager(new DefaultDesktopManager());
        dpleft.setBounds(0, 70, 280, mainpanel.getHeight()-135);
        dpleft.addComponentListener(this);

            // TODO: initializing title panel
            pTitle = PanelModels.titlePanel(mainpanel);
            pTitle.addComponentListener(this);
            // TODO: initializing list panel
            pList = PanelModels.listPanel(dpright);
            pList.addComponentListener(this);
            // TODO: initializing map panel
            pMap = PanelModels.mapPanel(dpright);
            pMap.addComponentListener(this);
            // TODO: initializing menu panel
            pMenu = PanelModels.menuPanel(dpleft);
            pMenu.addComponentListener(this);
            // TODO: initializing info panel
            pInfo = PanelModels.infoPanel(dpleft);
            pInfo.addComponentListener(this);
            // TODO: initializing background label
            bground = PanelModels.backgroundLabel();

            // TODO: initializing pTitle
            menubar = MenuModels.menuBar(pMenu);
            menubar.addComponentListener(this);

                // TODO: initializing buttons
                datei = MenuModels.fileButton();
                datei.addActionListener(this);
                btList = MenuModels.listButton();
                btList.addActionListener(this);
                btMap = MenuModels.mapButton();
                btMap.addActionListener(this);
                settings = MenuModels.settingsButton();
                settings.addActionListener(this);
                search = MenuModels.searchTextField(menubar);
                search.addKeyListener(this);
                search_settings = MenuModels.searchFilterButton(menubar);
                search_settings.addActionListener(this);
                search_settings.addComponentListener(this);
                progressbar = MenuModels.progressBar(menubar);
                progressbar.addComponentListener(this);
                settings_intlFrame = MenuModels.settings_intlFrame();
                settings_intlFrame.addComponentListener(this);
                settings_intlFrame.addKeyListener(this);
                settings_iFrame_maxLoad = MenuModels.settingsOP_maxLoadTxtField();
                settings_iFrame_maxLoad.addComponentListener(this);

            // TODO: initializing close view button
            closeView = MenuModels.closeViewButton(dpright);
            closeView.addActionListener(this);

        /**
         * title background img (label)
         */
            // TODO: setting up title backround img
            // ich bekomme nur mit der getRessource methode ein Bild zurückgeliefert
            ImageIcon img = new ImageIcon(this.getClass().getResource("/title_background.jpg"));
            title_bground = new JLabel(img);
            title_bground.setBounds(0, 0, pTitle.getWidth(), pTitle.getHeight());
            title_bground.setBorder(LINE_BORDER);
            title = PanelModels.titleTxtLabel(pTitle);

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
            dpleft.add(pMenu);
            // TODO: adding everything to internal finfo frame
            dpleft.add(pInfo);
            // TODO: adding everything to internal map frame
            dpright.add(pMap);
            // TODO: adding everything to internal list frame
            dpright.add(pList);

                // TODO: adding to pTitle
                pTitle.add(PanelModels.titleTxtLabel(pTitle));
                pTitle.add(title_bground);

        // TODO: adding title panel to frame
        mainpanel.add(pTitle);
            settings_intlFrame.add(settings_iFrame_maxLoad);
        mainpanel.add(settings_intlFrame);
        // TODO: moving flist and fmenu to front
        dpright.setVisible(true);
        dpleft.setVisible(true);

        // TODO: adding desktop panes to frame
        mainpanel.add(dpright);
        mainpanel.add(dpleft);
        
        // TODO: adding mainpanel to frame
        window.add(mainpanel);

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
        //fmenu
        titlePane =(BasicInternalFrameTitlePane)((BasicInternalFrameUI)fmenu.getUI()).getNorthPane();
        fmenu.remove(titlePane);
        //finfo
        titlePane =(BasicInternalFrameTitlePane)((BasicInternalFrameUI)finfo.getUI()).getNorthPane();
        finfo.remove(titlePane);
    }

    /**
     *
     */
    public void progressbarStart () {
        progressbarVisible(true);
        progressbar.setIndeterminate(true);
    }
    /**
     *
     */
    public void progressbarVisible (boolean v) {
        progressbar.setVisible(v);
        window.revalidate();
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
        listView.add(closeView);
        // TODO: setting up list scrollpane
        spList = new JScrollPane();
        spList.add(listView);
        spList.setViewportView(listView);
        //spList.setBounds(Bounds.RIGHT);
        spList.setBackground(DEFAULT_BG_COLOR);
        spList.setBounds(pList.getBounds());
        spList.addComponentListener(this);
        spList.setBorder(LINE_BORDER);
        // TODO: adding list scrollpane to list pane
        pList.add(spList);
        dpright.moveToFront(pList);
        //flist.show();
        // revalidate window -> making the tree visible
        window.revalidate();
        // setting viewRunning to TRUE
        runningView = listView;
    }

    /**
     *
     */
    public void disposeView () {
        if (listView != null || mapViewer != null) { // braucht man das
            if (runningView == listView) {
                listView.setVisible(false);
                listView = null;
                pList.setVisible(false);
            } else if (runningView == mapViewer) {
                mapViewer.setVisible(false);
                mapViewer = null;
                pMap.setVisible(false);
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
        pMap.show();
        //fmap.show();
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
        //mapViewer.setBounds(Bounds.RIGHT);
        mapViewer.setBorder(LINE_BORDER);
        new DefaultMapController(mapViewer);
        mapViewer.setTileSource(new BingAerialTileSource());
        mapViewer.setVisible(true);
        mapViewer.setBounds(pMap.getBounds());
        mapViewer.addKeyListener(this);
        mapViewer.addComponentListener(this);
        //mapViewer.setMinimumSize(Bounds.RIGHT.getSize());
        mapViewer.addJMVListener(this);
        // TODO: adding MapViewer to panel
        pMap.add(mapViewer);
        pMap.setVisible(true);
        mapViewer.add(closeView);
        // revalidating window frame to refresh everything
        window.revalidate();
        // setting mapViewer as the running View
        //runningView = mapViewer;

        return mapViewer;
    }

    /**
     *
     */
    public void loadView (ViewType type, List<Flight> list) {
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
            case MAP_ALL:
                try {           // TODO in DBOut catchen
                    new MapManager(this).createAllFlightsMap(list);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                window.revalidate();
                break;
            case MAP_FLIGHTROUTE:
                try {
                    new MapManager(this).createFlightRoute(list);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            this.recieveTree(new TreePlantation().createTree(TreePlantation.createFlightTreeNode(list), this));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * enters the text in the textfield (use for key listener)
     */
    protected void enterText () {
        String text = search.getText().toLowerCase();
        if (!text.isBlank()) {
            if (text.startsWith("exit")) {
                Controller.exit();
            }
            if (text.startsWith("loadlist")) {
                Controller.createDataView(ViewType.LIST_FLIGHT, "");
            }
            if (text.startsWith("loadmap")) {
                Controller.createDataView(ViewType.MAP_ALL, "");
            }
            if (text.startsWith("maxload")) {
                String[] args = text.split(" ");
                try {
                    if (Integer.parseInt(args[1]) <= 10000) {
                        DBOut.maxLoadedFlights = Integer.parseInt(args[1]);
                        System.out.println("maxload changed to " + args[1] + " !");
                    } else {
                        System.out.println("Failed! Maximum is 10000!");
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                if (runningView == null && !text.contains(" ")) {
                    Controller.createDataView(ViewType.MAP_FLIGHTROUTE, text);
                }
            }
        }
        search.setText("");
    }

    /********************************************
     ********************************************
     *                listeners                 *
     *******************************************+
     ********************************************
     */

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == datei) {

        } else if (src == btList) {
            progressbarStart();
            runBackgroundTask(ViewType.LIST_FLIGHT);
        } else if (src == btMap) {
            progressbarStart();
            runBackgroundTask(ViewType.MAP_ALL);
        } else if (src == closeView) {
            disposeView();
        } else if (src == settings) {
            settings_intlFrame.show();
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
        } else if (src == settings_iFrame_maxLoad) {
            if (key == KeyEvent.VK_ENTER) {

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

    @Override
    public void processCommand(JMVCommandEvent commandEvent) {
        if (    commandEvent.getCommand() == JMVCommandEvent.COMMAND.ZOOM
                && commandEvent.getSource() instanceof MapMarkerDot ) {
            System.out.println("es hat geklappt");
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Component comp = e.getComponent();
        if (comp == window) {
            mainpanel.setBounds(window.getBounds());
        } else if (comp == mainpanel) {
            pTitle.setBounds(mainpanel.getX(), mainpanel.getY(), mainpanel.getWidth(), 70);
            dpright.setBounds(280, 70, mainpanel.getWidth()-280, mainpanel.getHeight()-70);
            dpleft.setBounds(0, 70, 280, mainpanel.getHeight()-70);
        } else if (comp == dpleft) {
            pMenu.setBounds(0, 0, dpleft.getWidth(), dpleft.getHeight());
            pInfo.setBounds(0, 0, dpleft.getWidth(), dpleft.getHeight());
        } else if (comp == dpright) {
            pList.setBounds(0, 0, dpright.getWidth(), dpright.getHeight());
            pMap.setBounds(0, 0, dpright.getWidth(), dpright.getHeight());
            closeView.setBounds(dpright.getWidth() - 95, dpright.getHeight() - 45, 80, 30);
        } else if (comp == pTitle) {
                title_bground.setBounds(pTitle.getBounds());
                title.setBounds(pTitle.getWidth()/2-200, 0, 400, 70);
        } else if (comp == menubar) {
            search.setBounds(10, menubar.getHeight()-80, 255, 25);
            search_settings.setBounds(10, menubar.getHeight()-40, 255, 25);
        } else {
            try {
                if (comp == pList) {
                    listView.setBounds(pList.getBounds());
                } else if (comp == pMap) {
                    mapViewer.setBounds(pMap.getBounds());
                } else if (comp == pMenu) {
                    menubar.setBounds(pMenu.getBounds());
                } else if (comp == pInfo) {
                }
            } catch (Exception ex) {
            }
        }
        window.revalidate();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        Component comp = e.getComponent();
        if (comp == pList) {
            pList.setBounds(0, 0, dpright.getWidth(), dpright.getHeight());
            listView.setBounds(flist.getBounds());
            spList.setBounds(flist.getBounds());
        } else if (comp == pMap) {
            pMap.setBounds(0, 0, dpright.getWidth(), dpright.getHeight());
            mapViewer.setBounds(pMap.getBounds());
        } else if (comp == listView) {
            pList.setBounds(0, 0, dpright.getWidth(), dpright.getHeight());
            listView.setBounds(pList.getBounds());
        } else if (comp == mapViewer) {
            pMap.setBounds(0, 0, dpright.getWidth(), dpright.getHeight());
            mapViewer.setBounds(pMap.getBounds());
        } else if (comp == spList) {
            pList.setBounds(0, 0, dpright.getWidth(), dpright.getHeight());
            spList.setBounds(pList.getBounds());
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * runs background task
     */
    private void runBackgroundTask (ViewType type) {
        BackgroundWorker.actionViewType = type;
        new BackgroundWorker().execute();
    }

    private class BackgroundWorker extends SwingWorker<String, Void> {

        public static ViewType actionViewType;

        /**
         *
         */
        @Override
        protected String doInBackground() throws Exception {
            switch (actionViewType) {
                case LIST_FLIGHT:
                    Controller.createDataView(ViewType.LIST_FLIGHT, "");
                    return "[GUI] backround tast started!";
                case LIST_AIRPORT:
                case LIST_AIRLINE:
                case LIST_PLANE:
                case MAP_ALL:
                    Controller.createDataView(ViewType.MAP_ALL, "");
                    return "[GUI] background task started!";
                case MAP_FLIGHTROUTE:
            }
            return "";
        }

        @Override
        protected void done () {
            progressbar.setVisible(false);
            System.out.println("[GUI] db-data loaded!");
        }
    }

}

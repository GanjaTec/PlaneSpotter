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
import planespotter.exceptions.SemaphorError;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
    public JFrame window;
    protected JDesktopPane dpleft, dpright;
    protected JInternalFrame flist, fmap, fmenu, finfo;
    protected JPanel mainpanel, pTitle, pList, pMap, pMenu, pInfo, pStartScreen;
    protected JLabel title, bground, title_bground, lblStartScreen;
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
    protected Semaphor view_SEM = new Semaphor((byte) 0, (byte) 1, (byte) 0);

    /**
     * constructor for GUI
     */
    public GUI() {
    }

    /**
     * GUI run method (?)
     */
    @Override
    public void run() {
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
            // TODO: initializing start screen panel
            pStartScreen = PanelModels.startPanel(dpright);
            pStartScreen.addComponentListener(this);
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
                settings_intlFrame = MenuModels.settings_intlFrame(mainpanel);
                settings_intlFrame.addComponentListener(this);
                settings_iFrame_maxLoad = MenuModels.settingsOP_maxLoadTxtField();
                settings_iFrame_maxLoad.addComponentListener(this);
                settings_iFrame_maxLoad.addKeyListener(this);
            // TODO: initializing close view button
            closeView = MenuModels.closeViewButton(dpright);
            closeView.addActionListener(this);
            // TODO: setting up title backround img
            // ich bekomme nur mit der getRessource methode ein Bild zurückgeliefert
            ImageIcon img = new ImageIcon(this.getClass().getResource("/title_background.jpg"));
            title_bground = new JLabel(img);
            title_bground.setBounds(pTitle.getBounds());
            title_bground.setBorder(LINE_BORDER);
            // title text (might be replaced through one image)
            title = PanelModels.titleTxtLabel(pTitle);
            // TODO: setting up start screen
            ImageIcon start_image = new ImageIcon(this.getClass().getResource("/start_image.png"));
            lblStartScreen = new JLabel(start_image);
            lblStartScreen.setBounds(pStartScreen.getBounds());
            lblStartScreen.setBorder(LINE_BORDER);

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
            // TODO: adding label to start screen panel
            pStartScreen.add(lblStartScreen);
            // TODO: adding everything to right desktop pane
            dpright.add(pList);
            dpright.add(pMap);
            dpright.add(pStartScreen);
            // TODO: adding everything to left desktop pane
            dpleft.add(pMenu);
            dpleft.add(pInfo);
                // TODO: adding to pTitle
                pTitle.add(PanelModels.titleTxtLabel(pTitle));
                pTitle.add(title_bground);
                // TODO: adding textfield to internal settings frame
                settings_iFrame_maxLoad.setText(Controller.getMaxLoadedData() + "");
                settings_intlFrame.add(settings_iFrame_maxLoad);
        // TODO: adding title panel to frame
        mainpanel.add(pTitle);
        // TODO: adding settings internal frame to mainpanel
        mainpanel.add(settings_intlFrame);
        // TODO: moving flist and fmenu to front
        dpright.setVisible(true);
        dpleft.setVisible(true);
        // TODO: adding desktop panes to frame
        mainpanel.add(dpright);
        mainpanel.add(dpleft);
        // TODO: adding mainpanel to frame
        window.add(mainpanel);
        // TODO: setting list and map panel invisible, start screen visible
        pList.setVisible(false);
        pMap.setVisible(false);
        pStartScreen.setVisible(true);
        runningView = pStartScreen;

        return window;
    }

    /**
     * starts a indeterminate progressBar
     */
    public void progressbarStart () {
        progressbarVisible(true);
        progressbar.setIndeterminate(true);
    }
    /**
     * sets the vivibility of the progressBar
     *
     * @param v is the visible-boolean
     */
    public void progressbarVisible (boolean v) {
        progressbar.setVisible(v);
        window.revalidate();
    }
    /**
     * @return progressBar value
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
     * sets the JTree in listView and makes it visible
     *
     * @param tree is the tree to set
     */
    public void recieveTree (JTree tree) {
        if (runningView != null) {
            disposeView();
        }
        if (view_SEM.value() == 1) {
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
        pList.setVisible(true);
        //flist.show();
        // revalidate window -> making the tree visible
        window.revalidate();
        // setting viewRunning to TRUE
    }

    /**
     * disposes all views (and opens the start screen)
     * if no other view is opened, nothing is done
     */
    public void disposeView () {
        if (listView != null || mapViewer != null || pStartScreen != null) { // braucht man das
            if (runningView == listView && mapViewer != null) {
                listView.setVisible(false);
                listView = null;
                pList.setVisible(false);
                pStartScreen.setVisible(true);
                runningView = null;
            } else if (runningView == mapViewer && mapViewer != null) {
                mapViewer.setVisible(false);
                mapViewer = null;
                pMap.setVisible(false);
                pStartScreen.setVisible(true);
                runningView = null;
            } else if (runningView == pStartScreen && pStartScreen != null) {
                pStartScreen.setVisible(false);
            }

            if (view_SEM.value() == 1) {
                listView = null;
                pList.setVisible(false);
                mapViewer = null;
                pMap.setVisible(false);
                pStartScreen.setVisible(true);

                view_SEM.decrease();
            } else {
                pStartScreen.setVisible(false);
            }
        }
        window.revalidate();
    }

    /**
     * sets the JMapViewer in mapViewer
     *
     * @param map is the map to be set
     */
    public void recieveMap (JMapViewer map) {
        if (runningView != null) {
            disposeView();
        }
        if (view_SEM.value() == 1) {
            disposeView();
        }
        mapViewer = map;
        // TODO: adding MapViewer to panel
        pMap.add(mapViewer);
        pMap.setVisible(true);
        //fmap.show();
        // revalidating window frame to refresh everything
        window.revalidate();
        // setting mapViewer as the running View
    }

    /**
     * @return a map prototype (JMapViewer)
     */
    public JMapViewer createMap () {
        if (runningView != null) {
            disposeView();
        }
        if (view_SEM.value() == 1) {
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
                        Controller.setMaxLoadedData(Integer.parseInt(args[1]));
                        System.out.println("maxload changed to " + args[1] + " !");
                    } else {
                        System.out.println("Failed! Maximum is 10000!");
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else if (text.startsWith("flightroute")) {
                if (view_SEM.value() == 0 && !text.contains(" ")) {
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
            pList.setVisible(false);
            listView = null;
            pMap.setVisible(false);
            mapViewer = null;
        } else if (src == settings) {
            settings_intlFrame.show();
            settings_iFrame_maxLoad.setCaretColor(Color.YELLOW);
            settings_iFrame_maxLoad.setCaretPosition(settings_iFrame_maxLoad.getText().length());
            settings_iFrame_maxLoad.grabFocus();
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
                try {
                    Controller.setMaxLoadedData(Integer.parseInt(settings_iFrame_maxLoad.getText()));
                    settings_iFrame_maxLoad.setText("");
                    settings_intlFrame.dispose();
                } catch (NumberFormatException ex) {
                    settings_iFrame_maxLoad.setText("Error");
                }

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
            pStartScreen.setBounds(0, 0, dpright.getWidth(), dpright.getHeight());
            closeView.setBounds(dpright.getWidth() - 95, dpright.getHeight() - 45, 80, 30);
        } else if (comp == pTitle) {
            title_bground.setBounds(pTitle.getBounds());
            title.setBounds(pTitle.getWidth()/2-200, 0, 400, 70);
        } else if (comp == pStartScreen) {
            lblStartScreen.setBounds(pStartScreen.getBounds());
        } else if (comp == menubar) {
            search.setBounds(10, menubar.getHeight()-80, 255, 25);
            search_settings.setBounds(10, menubar.getHeight()-40, 255, 25);
        } else {
            try {
                if (comp == pList) {
                    spList.setBounds(pList.getBounds());
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
            listView.setBounds(pList.getBounds());
            spList.setBounds(pList.getBounds());
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
        BackgroundWorker bgworker = new BackgroundWorker();
        synchronized (this) {
            BackgroundWorker.actionViewType = type;
            bgworker.execute();
        }
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
                    // TODO controller zum Thread machen der die anderen (DBOut) ausführt
                    Controller.createDataView(ViewType.LIST_FLIGHT, "");
                    runningView = listView;
                    view_SEM.increase();
                    return "[GUI] backround tast started!";
                case LIST_AIRPORT:
                case LIST_AIRLINE:
                case LIST_PLANE:
                case MAP_ALL:
                    Controller.createDataView(ViewType.MAP_ALL, "");
                    runningView = mapViewer;
                    view_SEM.increase();
                    return "[GUI] background task started!";
                case MAP_FLIGHTROUTE:
            }
            return "";
        }

        @Override
        protected void done () {
            progressbar.setVisible(false);
            //System.out.println("[GUI] DB-data loaded!");
        }
    }


    /**
     * private semaphor class represents a semaphor with methods
     */
    protected class Semaphor {
        // semaphor, minimum and maximum value
        private byte SEM, min, max;

        /**
         * Semaphor constructor
         */
        public Semaphor (byte min, byte max, byte beginAt) {
            this.min = min;
            this.max = max;
            if (beginAt <= max && beginAt >= min) {
                this.SEM = beginAt;
            } else {
                SEM = min;
            }
        }

        /**
         * increases the semaphor, if its value is 1, SemaphorError is thrown
         * @throws SemaphorError
         */
        public void increase () throws SemaphorError {
            if (SEM < max) {
                SEM++;
            } else {
                throw new SemaphorError();
            }
        }

        /**
         * decreases the semaphor, if its value is 0, SemaphorError is thrown
         * @throws SemaphorError
         */
        public void decrease () throws SemaphorError {
            if (SEM > min) {
                SEM--;
            } else {
                throw new SemaphorError();
            }
        }

        /**
         * @return SEM value
         */
        public int value () throws SemaphorError {
            return SEM;
        }
    }

}

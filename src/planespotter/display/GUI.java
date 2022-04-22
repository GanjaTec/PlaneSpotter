package planespotter.display;


import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.constants.ViewType;
import planespotter.controller.Controller;
import planespotter.dataclasses.CustomMapMarker;
import planespotter.dataclasses.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.ViewType.*;

/**
 * @name GUI
 * @author jml04
 * @version 1.1
 */
public class GUI implements ActionListener, KeyListener, JMapViewerEventListener,
        ComponentListener, Runnable, MouseListener {

    /**
     * components // unsorted TODO sort
     */
    public JFrame window, loadingScreen;
    protected JDesktopPane dpleft, dpright;
    protected JPanel mainpanel, pTitle, pViewHead, pList, pMap, pMenu, pInfo, pStartScreen, pSearch;
    protected JLabel title, bground, title_bground, lblStartScreen, lblLoading, viewHeadText;
    protected JTextField tfSearch, settings_iFrame_maxLoad;
    protected JRadioButton rbFlight, rbAirline;
    protected JProgressBar progressbar;
    protected JMenuBar menubar;
    protected JButton datei, settings, search_settings, btList, btMap, closeView;
    protected JInternalFrame settings_intlFrame;
    protected JScrollPane spList;
    protected JComboBox<JMenuItem> searchFor_cmbBox;

    protected volatile JTree listView, flightInfo;
    // TODO fix ConcurrentModificationException on mapViewer
    protected volatile JMapViewer mapViewer;

    // alternative test path: "C:\\Users\\jml04\\Desktop\\loading.gif"
    private final ImageIcon loading_gif = new ImageIcon(this.getClass().getResource("/loading.gif"));
    // contains all map marker coords with datapoints, if mapViewer != null
    //public HashMap<Coordinate, DataPoint> mapPoints; //@unused

    // controller instance
    private Controller controller = Controller.getInstance();

    /**
     * view semaphor
     * can be:
     *  null -> no view opened
     *  not null -> view opened
     *
     * @deprecated -> should be deleted to prevent conflicts
     */
    protected static Component runningView = null;
    /**
     * constructor for GUI
     */
    public GUI() {
        JFrame window = this.initialize();
    }

    /**
     * GUI run method (?)
     */
    @Override
    public void run() {
        Thread.currentThread().setName("planespotter-gui");
        JFrame loading = this.loadingScreen();
        loading.setVisible(true);

    }

    /**
     * loading screen method, creates a loading screen
     *
     * @return loading screen JFrame
     */
    public JFrame loadingScreen () {
        loadingScreen = new JFrame();
        loadingScreen.setSize(333, 243);
        loadingScreen.setLocationRelativeTo(null);
        loadingScreen.setLayout(null);
        loadingScreen.setOpacity(1f);
        loadingScreen.setUndecorated(true);
            lblLoading = new JLabel(loading_gif);
            lblLoading.setBounds(0, 0, 333, 243);
        loadingScreen.add(lblLoading);
        return loadingScreen;
    }

    /**
     * initialize method
     * creates new GUI window
     */
    protected JFrame initialize () {
        // TODO: setting up window
        this.window = new JFrame("PlaneSpotter");
        this.window.setSize(1280, 720);
        this.window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.window.setLocationRelativeTo(null);
        // TODO: initializing mainpanel
        this.mainpanel = PanelModels.mainPanel(this.window);
        this.mainpanel.addComponentListener(this);
        // TODO: setting up right desktop pane
        this.dpright = new JDesktopPane();
        this.dpright.setBorder(LINE_BORDER);
        this.dpright.setBackground(DEFAULT_BG_COLOR);
        this.dpright.setDesktopManager(new DefaultDesktopManager());
        this.dpright.setBounds(280, 70, this.mainpanel.getWidth()-280, this.mainpanel.getHeight()-135);
        this.dpright.addComponentListener(this);
        // TODO: setting up left desktop pane
        dpleft = new JDesktopPane();
        dpleft.setBorder(LINE_BORDER);
        dpleft.setBackground(DEFAULT_BG_COLOR);
        dpleft.setDesktopManager(new DefaultDesktopManager());
        dpleft.setBounds(0, 70, 280, this.mainpanel.getHeight()-135);
        dpleft.addComponentListener(this);
            // TODO: initializing title panel
            pTitle = PanelModels.titlePanel(this.mainpanel);
            pTitle.addComponentListener(this);
            // TODO: initializing view head panel
            this.pViewHead = PanelModels.viewHeadPanel(this.dpright);
            this.pViewHead.addComponentListener(this);
            // TODO: initializing list panel
            this.pList = PanelModels.listPanel(this.dpright);
            this.pList.addComponentListener(this);
            // TODO: initializing map panel
            this.pMap = PanelModels.mapPanel(this.dpright);
            this.pMap.addComponentListener(this);
                // TODO: initializing map viewer
                this.mapViewer = BlackBeardsNavigator.defaultMapViewer(pMap);
                mapViewer.addKeyListener(this);
                mapViewer.addComponentListener(this);
                mapViewer.addMouseListener(this);
                mapViewer.addJMVListener(this);
            // TODO: initializing menu panel
            this.pMenu = PanelModels.menuPanel(dpleft);
            this.pMenu.addComponentListener(this);
            // TODO: initializing info panel
            this.pInfo = PanelModels.infoPanel(dpleft);
            this.pInfo.addComponentListener(this);
            // TODO: initializing start screen panel
            this.pStartScreen = PanelModels.startPanel(this.dpright);
            this.pStartScreen.addComponentListener(this);
                // TODO: initializing search panel components
                searchFor_cmbBox = MenuModels.searchFor_cmbBox();
                searchFor_cmbBox.addActionListener(this);     // TODO evtl Enum EventType, dann event klasse für even
                                                                // TODO aktionen die von den listenern aufgerugfen wird
            // TODO: initializing search panel
            pSearch = PanelModels.searchPanel(this.pMenu, this);
            pSearch.addComponentListener(this);
            // TODO: initializing background label
            bground = PanelModels.backgroundLabel(this.mainpanel);
            // TODO: initializing pTitle
            this.menubar = MenuModels.menuBar(this.pMenu);
            this.menubar.addComponentListener(this);
                // TODO: initializing buttons
                datei = MenuModels.fileButton();
                datei.addActionListener(this);
                btList = MenuModels.listButton();
                btList.addActionListener(this);
                btMap = MenuModels.mapButton();
                btMap.addActionListener(this);
                settings = MenuModels.settingsButton();
                settings.addActionListener(this);
                tfSearch = MenuModels.searchTextField(this.menubar);
                tfSearch.addKeyListener(this);
                search_settings = MenuModels.searchFilterButton(this.menubar);
                search_settings.addActionListener(this);
                search_settings.addComponentListener(this);
                progressbar = MenuModels.progressBar(this.menubar);
                progressbar.addComponentListener(this);
                this.settings_intlFrame = MenuModels.settings_intlFrame(this.mainpanel);
                this.settings_intlFrame.addComponentListener(this);
                this.settings_iFrame_maxLoad = MenuModels.settingsOP_maxLoadTxtField();
                this.settings_iFrame_maxLoad.addComponentListener(this);
                this.settings_iFrame_maxLoad.addKeyListener(this);
            // TODO: initializing view head text label
            viewHeadText = PanelModels.headLabel(this.pViewHead, "PlaneSpotter");
            // TODO: initializing close view button
            closeView = MenuModels.closeViewButton(this.dpright);
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
            lblStartScreen.setBounds(0, 0, this.pStartScreen.getWidth(), this.pStartScreen.getHeight());
            lblStartScreen.setBorder(LINE_BORDER);
            // TODO: adding test bground image
            ImageIcon test_img = new ImageIcon(this.getClass().getResource("/ttowers.png"));
            lblStartScreen.setIcon(test_img);

        // TODO: adding all generated components to window
        addAllToWinow();

        // TODO: setting list and map panel invisible, start screen visible
        this.pList.setVisible(false);
        this.pMap.setVisible(false);
        this.pStartScreen.setVisible(true);
        runningView = this.pStartScreen;
        this.controller.log(ANSI_GREEN + "GUI initialized sucsessfully!" + ANSI_RESET);
        return this.window;
    }

    /**
     * adds all components to the window frame
     * -> called by initialize()
     */
    private void addAllToWinow() {
        // Adding to Window
        // TODO: adding everything to menubar
        this.menubar.add(datei);
        this.menubar.add(btList);
        this.menubar.add(btMap);
        this.menubar.add(settings);
        this.menubar.add(tfSearch);
        this.menubar.add(search_settings);
        this.menubar.add(progressbar);
        // TODO: adding menubar to menu panel
        this.pMenu.add(this.menubar);
        // TODO: adding mapViewer to map panel
        this.pMap.add(this.mapViewer);
        // TODO: adding label to start screen panel
        this.pStartScreen.add(lblStartScreen);
        // TODO: adding everything to pViewHead
        this.pViewHead.add(viewHeadText);
        this.pViewHead.add(closeView);
        // TODO: adding everything to right desktop pane
        this.dpright.add(this.pViewHead);
        this.dpright.add(this.pList);
        this.dpright.add(this.pMap);
        this.dpright.add(this.pStartScreen);
        // TODO: adding everything to left desktop pane
        dpleft.add(this.pMenu);
        dpleft.add(this.pInfo);
        // TODO: adding to pTitle
        pTitle.add(PanelModels.titleTxtLabel(pTitle));
        pTitle.add(title_bground);
        // TODO: adding textfield to internal settings frame
        this.settings_iFrame_maxLoad.setText(UserSettings.getMaxLoadedFlights() + "");
        this.settings_intlFrame.add(this.settings_iFrame_maxLoad);
        // TODO: adding title panel to frame
        this.mainpanel.add(pTitle);
        // TODO: adding settings internal frame to mainpanel
        this.mainpanel.add(this.settings_intlFrame);
        // TODO: moving flist and fmenu to front
        this.dpright.setVisible(true);
        dpleft.setVisible(true);
        // TODO: adding desktop panes to frame
        this.mainpanel.add(this.dpright);
        this.mainpanel.add(dpleft);
        // TODO: adding mainpanel to frame
        this.window.add(this.mainpanel);
    }

    /**
     * this method is executed when pre-loading is done
     */
    public void donePreLoading () {
        this.playSound(SOUND_DEFAULT);
        loadingScreen.setVisible(false);
        this.window.setVisible(true);
        this.window.requestFocus();
    }

    /**
     * plays a sound from the default toolkit
     * @param sound is the sound to be played (see: GUIConstants)
     *              TODO move to EventWizard
     */
    private void playSound (String sound) {
        Runnable sound2 = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty(sound);
        if (sound2 != null) sound2.run();
    }

    /**
     * revalidates all swing components
     */
    public void revalidateAll () {
        this.window.revalidate();
    }

    /**
     * starts a indeterminate progressBar
     */
    public void progressbarStart () {
        progressbarVisible(true);
        progressbar.setIndeterminate(true);
        progressbar.setString("Loading data...");
        progressbar.setStringPainted(true);
    }

    /**
     * sets the visibility of the progressBar
     *
     * @param v is the visible-boolean
     */
    public void progressbarVisible (boolean v) {
        progressbar.setVisible(v);
        revalidateAll();
    }

    /**
     * @return progressBar value
     *
     * @deprecated
     */
    public int progressbarValue () {
        return progressbar.getValue();
    }

    /**
     * progressbar-plus-plus
     * progressbar value goes +1
     *
     * @deprecated
     */
    public void progressbarPP () {
        progressbar.setValue(progressbar.getValue() + 1);
    }

    /**
     * requests focus for a specific component
     *
     * @param comp is the component that requests the focus
     */
    public void requestComponentFocus (Component comp) {
        comp.requestFocus();
    }

    /**
     *
     * @param tree is the tree to set
     */
    public void recieveInfoTree (JTree tree) {
        flightInfo = tree;
        flightInfo.setBounds(this.pInfo.getBounds());
        flightInfo.setMaximumSize(this.pInfo.getSize());
        flightInfo.setBorder(LINE_BORDER);
        flightInfo.setFont(FONT_MENU.deriveFont(12f));
        this.pInfo.add(flightInfo);
        dpleft.moveToFront(this.pInfo);
        this.pInfo.setVisible(true);
        revalidateAll();
    }

    /**
     * sets the JTree in listView and makes it visible
     *
     * @param tree is the tree to set
     */
    public void recieveTree (JTree tree) {
        listView = tree;
        this.spList = listScrollPane(listView);
        // TODO: adding list scrollpane to list pane
        this.pList.add(this.spList);
        this.dpright.moveToFront(this.pList);
        this.pList.setVisible(true);
        viewHeadText.setText(DEFAULT_HEAD_TEXT + "Flight-List");
        // revalidate window -> making the tree visible
        revalidateAll();
        requestComponentFocus(listView);
    }

    /**
     * creates a JScrollPane with the given Component and a specific layout
     * @param inScrollPane is the JTree or whatever, which is displayed in the JScrollPane
     * @return sp, the JScrollPane
     */
    private JScrollPane listScrollPane(JTree inScrollPane) {
        JScrollPane sp = new JScrollPane(inScrollPane);
        sp.setViewportView(inScrollPane);
        sp.setBackground(DEFAULT_BG_COLOR);
        sp.setForeground(DEFAULT_BORDER_COLOR);
        sp.setBounds(this.pList.getBounds());
        sp.addComponentListener(this);
        sp.setBorder(LINE_BORDER);
        JScrollBar verticalScrollBar = sp.getVerticalScrollBar();
        verticalScrollBar.setBackground(DEFAULT_BG_COLOR);
        verticalScrollBar.setForeground(DEFAULT_ACCENT_COLOR);
        verticalScrollBar.setBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR));
        sp.setVerticalScrollBar(verticalScrollBar);
        return sp;
    }

    /**
     * disposes all views (and opens the start screen)
     * if no other view is opened, nothing is done
     */
    public void disposeView () {
        if (this.pStartScreen != null) {
            this.pStartScreen.setVisible(false);
        } if (listView != null) {
            this.pList.remove(listView);
            listView.setVisible(false);
            listView = null;
            this.pList.setVisible(false);
        } if (this.mapViewer != null) {
            this.pMap.remove(this.mapViewer);
            this.mapViewer.setVisible(false);
            this.pMap.setVisible(false);
        } if (flightInfo != null) {
            flightInfo.setVisible(false);
            flightInfo = null;
            this.pInfo.setVisible(false);
                this.pMenu.setVisible(true);
                dpleft.moveToFront(this.pMenu);
        }
        viewHeadText.setText(DEFAULT_HEAD_TEXT);
        revalidateAll();
        requestComponentFocus(tfSearch);
        Controller.garbageCollector();
    }

    /**
     * sets the JMapViewer in mapViewer
     *
     * @param map is the map to be set
     * @throws IOException, if a map tile has not yet been loaded, but keeps working
     *                      (therefore: no thorws statements) -> exception will be ignored
     */
    public void recieveMap (JMapViewer map) {
        this.mapViewer = map;
        // TODO: adding MapViewer to panel
        this.pMap.add(this.mapViewer);
        viewHeadText.setText(DEFAULT_HEAD_TEXT + "Map-Viewer");
        // revalidating window frame to refresh everything
        this.pMap.setVisible(true);
        this.mapViewer.setVisible(true);
        requestComponentFocus(this.mapViewer);
        revalidateAll();
    }



    /**
     * enters the text in the textfield (use for key listener)
     */
    protected void enterText () {
        String text = tfSearch.getText().toLowerCase();
        if (!text.isBlank()) {
            if (text.startsWith("exit")) {
                Controller.exit();
            } else if (text.startsWith("loadlist")) {
                this.controller.createDataView(LIST_FLIGHT, "");
            } else if (text.startsWith("loadmap")) {
                this.controller.createDataView(MAP_ALL, "");
            } else if (text.startsWith("maxload")) {
                String[] args = text.split(" ");
                try {
                    int max = Integer.parseInt(args[1]);
                    if (max <= 10000) {
                        UserSettings.setMaxLoadedFlights(max);
                        this.controller.log("maxload changed to " + args[1] + " !");
                    } else {
                        this.controller.log("Failed! Maximum is 10000!");
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else if (text.startsWith("flightroute") || text.startsWith("fl")) {
                disposeView();
                String[] args = text.split(" ");
                if (args.length > 1) {
                    String id = args[1];
                    this.controller.createDataView(MAP_FLIGHTROUTE, id);
                } else {
                    this.controller.createDataView(MAP_FLIGHTROUTE, "");
                }
            } else if (text.startsWith("closeall")) { // unsafe TODO delete
                System.err.println("'closeall' is unsafe!, destroys the GUI in the most cases");
                disposeView();
            }
        }
        tfSearch.setText("");
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
            this.controller.createDataView(LIST_FLIGHT, "");
        } else if (src == btMap) {
            progressbarStart();
            this.controller.createDataView(MAP_ALL, "");
        } else if (src == closeView) {
            disposeView();
            this.pStartScreen.setVisible(true);
            dpright.moveToFront(this.pStartScreen);
        } else if (src == settings) {
            this.settings_intlFrame.show();
            this.settings_iFrame_maxLoad.setCaretColor(Color.YELLOW);
            this.settings_iFrame_maxLoad.setCaretPosition(this.settings_iFrame_maxLoad.getText().length());
            this.settings_iFrame_maxLoad.grabFocus();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }


    @Override
    public void keyPressed(KeyEvent e) {
        Object src = e.getSource();
        int key = e.getKeyCode();
        if (src == tfSearch) {
            if (key == KeyEvent.VK_ENTER) {
                if (tfSearch.hasFocus())
                    enterText();
            }
        } else if (src == this.settings_iFrame_maxLoad) {
            if (key == KeyEvent.VK_ENTER) {
                try {
                    // TODO fixen: settings fenster schließt erst nach loading
                    if (Integer.parseInt(this.settings_iFrame_maxLoad.getText()) >= 4) {
                        progressbarStart();
                        UserSettings.setMaxLoadedFlights(Integer.parseInt(this.settings_iFrame_maxLoad.getText()));
                        this.settings_iFrame_maxLoad.setText("");
                        this.settings_intlFrame.setVisible(false);
                        // work with background worker?
                        this.controller.loadData();
                    }
                } catch (NumberFormatException ex) {
                    this.settings_iFrame_maxLoad.setText("Error");
                }

            }
        } else if (src == this.mapViewer) {
            switch (key) {
                case KeyEvent.VK_PAGE_UP:
                    this.mapViewer.moveMap(0, 2);
                    revalidateAll();
                    break;
                case KeyEvent.VK_HOME:
                    this.mapViewer.moveMap(-2, 0);
                    revalidateAll();
                    break;
                case KeyEvent.VK_END:
                    this.mapViewer.moveMap(2, 0);
                    revalidateAll();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    this.mapViewer.moveMap(0, -2);
                    revalidateAll();
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
            System.out.println("jmv listener!!! test");
        }
    }

    /**
     * component listener: fits the component sizes if the window is resized
     */
    @Override
    public void componentResized(ComponentEvent e) {
        Component comp = e.getComponent();
        if (comp == this.window) {
            this.mainpanel.setBounds(this.window.getBounds());
        } else if (comp == this.mainpanel) {
            pTitle.setBounds(this.mainpanel.getX(), this.mainpanel.getY(), this.mainpanel.getWidth(), 70);
            this.dpright.setBounds(280, 70, this.mainpanel.getWidth()-280, this.mainpanel.getHeight()-70);
            dpleft.setBounds(0, 70, 280, this.mainpanel.getHeight()-70);
        } else if (comp == dpleft) {
            this.pMenu.setBounds(0, 0, dpleft.getWidth(), dpleft.getHeight());
            this.pInfo.setBounds(0, 0, dpleft.getWidth(), dpleft.getHeight());
        } else if (comp == this.dpright) {
            this.pViewHead.setBounds(0, 0, this.dpright.getWidth(), this.dpright.getHeight()-24);
            this.pList.setBounds(0, 24, this.dpright.getWidth(), this.dpright.getHeight()-24);
            this.pMap.setBounds(0, 24, this.dpright.getWidth(), this.dpright.getHeight()-24);
            this.pStartScreen.setBounds(0, 24, this.dpright.getWidth(), this.dpright.getHeight()-24);
        } else if (comp == pTitle) {
            title_bground.setBounds(pTitle.getBounds());
            title.setBounds(pTitle.getWidth()/2-200, 0, 400, 70);
        } else if (comp == this.pViewHead) {
            closeView.setBounds(this.pViewHead.getWidth()-85, 4, 80, 16);
        } else if (comp == this.pStartScreen) {
            lblStartScreen.setBounds(0, 0, this.pStartScreen.getWidth(), this.pStartScreen.getHeight());
        } else if (comp == this.menubar) {
            tfSearch.setBounds(10, this.menubar.getHeight()-80, 255, 25);
            search_settings.setBounds(10, this.menubar.getHeight()-40, 255, 25);
        } else if (comp == this.pList) {
            if (this.spList != null && listView != null) {
                this.spList.setBounds(0, 0, this.pList.getWidth(), this.pList.getHeight());
                listView.setBounds(0, 0, this.pList.getWidth(), this.pList.getHeight());
            }
        } else if (comp == this.pMap) {
            if (this.mapViewer != null) {
                this.mapViewer.setBounds(0, 0, this.pMap.getWidth(), this.pMap.getHeight());
            }
        } else if (comp == this.pMenu) {
            this.menubar.setBounds(this.pMenu.getBounds());
        } else if (comp == this.pInfo) {
        }
        revalidateAll();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        Component comp = e.getComponent();
        if (comp == this.pList) {
            this.pList.setBounds(0, 24, this.dpright.getWidth(), this.dpright.getHeight()-24);
            listView.setBounds(0, 0, this.pList.getWidth(), this.pList.getHeight());
            this.spList.setBounds(0, 0, this.pList.getWidth(), this.pList.getHeight());
        } else if (comp == this.pMap) {
            this.pMap.setBounds(0, 24, this.dpright.getWidth(), this.dpright.getHeight() - 24);
            this.mapViewer.setBounds(0, 0, this.pMap.getWidth(), this.pMap.getHeight());
            this.pViewHead.setBounds(0, 0, this.dpright.getWidth(), this.dpright.getHeight()-24);
        } else if (comp == this.settings_iFrame_maxLoad) {
            this.settings_iFrame_maxLoad.setText(UserSettings.getMaxLoadedFlights() + "");
        }
        revalidateAll();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }
    // MouseListener
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point point = e.getPoint();
        ICoordinate clickedCoord = this.mapViewer.getPosition(point);
        List<? extends MapMarker> mapMarkerList = this.mapViewer.getMapMarkerList();
        Iterator<? extends MapMarker> it = mapMarkerList.iterator(); //FIXME setting map marker list while going through it
        CustomMapMarker next = null;
        ICoordinate markerCoord = null;
        int counter = 0;
        while (it.hasNext()) {
            next = (CustomMapMarker) it.next();
            markerCoord = next.getCoordinate();
            // Positionsabfrage mit leichter Toleranz, damit man den Punkt auch trifft
            if (    clickedCoord.getLat() < markerCoord.getLat() + 0.02 &&
                    clickedCoord.getLat() > markerCoord.getLat() - 0.02 &&
                    clickedCoord.getLon() < markerCoord.getLon() + 0.02 &&
                    clickedCoord.getLon() > markerCoord.getLon() - 0.02) {
                break;
            } else {
                next = null;
                markerCoord = null;
            }
            counter++;
        } // TODO alles nach WHILE schleife
        if (next != null && markerCoord != null) {
            CustomMapMarker newMarker = new CustomMapMarker((Coordinate) markerCoord, next.getFlight());
            newMarker.setBackColor(Color.RED);
            this.mapViewer.removeMapMarker(next); // könnte schwierig sein mit concurrency
            this.mapViewer.addMapMarker(newMarker);
            this.pMenu.setVisible(false);
            this.pInfo.setVisible(true);
            this.dpright.moveToFront(this.pInfo);
            Position flightPos = new Position(markerCoord.getLat(), markerCoord.getLon());
            //FIXME test: next.getFlight is null
            try {
                System.out.println("nextFlightID " + next.getFlight().getID());
            } catch (Exception etest) {
            }
            //System.out.println(ANSI_ORANGE + BlackBeardsNavigator.shownFlights.size());
            int flightID = -1;// = newMarker.getFlight().getID(); // FIXME: why is getFlight == null
            try {
                flightID = BlackBeardsNavigator.allMapMarkers.get(counter).getFlight().getID();
            } catch (Exception ex) {
                ex.printStackTrace();
            } // short */
            if (flightID != -1)
                recieveInfoTree(new TreePlantation().createTree(TreePlantation.oneFlightTreeNode(flightID), this));
            else recieveInfoTree(new TreePlantation().createTree(TreePlantation.oneFlightTreeNode(876), this));
            this.resetMapViewer(next);
        }
    }

    /**
     * resets all map markers
     */
    // TODO richtig machen, ConcurrentModificationException, irgendwas läuft nichts
    public void resetMapViewer (MapMarker doNotReset) {
        JMapViewer viewer = this.mapViewer;
        this.mapViewer = null;
        List<MapMarker> markersIn = viewer.getMapMarkerList();
        viewer.removeAllMapMarkers();
        List<MapMarker> markersOut = new ArrayList<>();
        for (MapMarker m : markersIn) {
            if (m != doNotReset) {
                ICoordinate markerPos = m.getCoordinate();
                CustomMapMarker newMarker = new CustomMapMarker((Coordinate) markerPos, null);
                newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR);
                markersOut.add(newMarker);
            } else {
                markersOut.add(m);
            }
        }
        viewer.setMapMarkerList(markersOut);
        this.revalidateAll();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }



}

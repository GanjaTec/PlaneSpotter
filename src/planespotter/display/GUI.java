package planespotter.display;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.controller.Controller;
import planespotter.dataclasses.CustomMapMarker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import static java.awt.event.KeyEvent.*;
import static planespotter.constants.ComponentType.*;
import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.ViewType.*;

/**
 * @name GUI
 * @author jml04
 * @version 1.1
 *
 * GUI class is the main gui class, it implements all the listeners
 * and has all components -> it contains window one sees
 */
public class GUI implements ActionListener, KeyListener, JMapViewerEventListener,
        ComponentListener, Runnable, MouseListener, ItemListener {

    /**
     * components // unsorted TODO sort
     */
    protected JFrame        window, loadingScreen;
    protected JDesktopPane  dpleft, dpright;
    protected JPanel        mainpanel, pTitle, pViewHead, pList, pMap, pMenu, pInfo, pStartScreen, pSearch;
    protected JLabel        title, bground, title_bground, lblStartScreen, lblLoading,
                            viewHeadText, searchForLabel;
    protected List<JComponent> flightSearch, planeSearch, airlineSearch, airportSearch, areaSearch;
    protected JTextArea     searchMessage;
    protected JTextField    tfSearch, settings_iFrame_maxLoad;
    protected JRadioButton  rbFlight, rbAirline;
    protected JProgressBar  progressbar;
    protected JMenuBar      menubar;
    protected JButton btFile, settings, searchFilter, btList, btMap, closeView;
    protected JInternalFrame settings_intlFrame;
    protected JScrollPane   spList;
    protected JComboBox<String> searchFor_cmbBox;
    protected JSeparator    searchSeperator;

    protected volatile JTree listView, flightInfo;
    // TODO fix ConcurrentModificationException on mapViewer
    protected volatile JMapViewer mapViewer;

    // alternative test path: "C:\\Users\\jml04\\Desktop\\loading.gif"
    private final ImageIcon loading_gif = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/loading.gif")));

    // controller instance
    private final Controller controller = Controller.getInstance();

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
        window = this.initialize();
    }

    /**
     * GUI run method (?)
     */
    @Override
    public void run() {
        Thread.currentThread().setName("planespotter-gui");
        var loading = this.loadingScreen();
        loading.setVisible(true);

    }

    /**
     * loading screen method, creates a loading screen
     *
     * @return loading screen JFrame
     */
    private JFrame loadingScreen () {
        this.loadingScreen = new JFrame();
        this.loadingScreen.setSize(333, 243);
        this.loadingScreen.setLocationRelativeTo(null);
        this.loadingScreen.setLayout(null);
        this.loadingScreen.setOpacity(1f);
        this.loadingScreen.setUndecorated(true);
            this.lblLoading = new JLabel(this.loading_gif);
            this.lblLoading.setBounds(0, 0, 333, 243);
        this.loadingScreen.add(this.lblLoading);
        return this.loadingScreen;
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
        this.dpleft = new JDesktopPane();
        this.dpleft.setBorder(LINE_BORDER);
        this.dpleft.setBackground(DEFAULT_BG_COLOR);
        this.dpleft.setDesktopManager(new DefaultDesktopManager());
        this.dpleft.setBounds(0, 70, 280, this.mainpanel.getHeight()-135);
        this.dpleft.addComponentListener(this);
            // TODO: initializing title panel
            this.pTitle = PanelModels.titlePanel(this.mainpanel);
            this.pTitle.addComponentListener(this);
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
                this.mapViewer = BlackBeardsNavigator.defaultMapViewer(this.pMap);
                this.mapViewer.addKeyListener(this);
                this.mapViewer.addComponentListener(this);
                this.mapViewer.addMouseListener(this);
                this.mapViewer.addJMVListener(this);
            // TODO: initializing menu panel
            this.pMenu = PanelModels.menuPanel(this.dpleft);
            this.pMenu.addComponentListener(this);
            // TODO: initializing info panel
            this.pInfo = PanelModels.infoPanel(this.dpleft);
            this.pInfo.addComponentListener(this);
            // TODO: initializing start screen panel
            this.pStartScreen = PanelModels.startPanel(this.dpright);
            this.pStartScreen.addComponentListener(this);
            // TODO: initializing search panel
            this.pSearch = PanelModels.searchPanel(this.pMenu, this);
            this.pSearch.addComponentListener(this);
                // TODO: initializing search panel components
                this.searchForLabel = MenuModels.cmbBoxLabel(this.pSearch);
                this.searchFor_cmbBox = MenuModels.searchFor_cmbBox(this.pSearch);
                this.searchFor_cmbBox.addItemListener(this);
                this.searchSeperator = MenuModels.searchSeperator(this.pSearch);
                this.searchMessage = MenuModels.searchMessage(this.pSearch);
                this.flightSearch = MenuModels.flightSearch(pSearch);
            // TODO: initializing background label
            this.bground = PanelModels.backgroundLabel(this.mainpanel);
            // TODO: initializing pTitle
            this.menubar = MenuModels.menuBar(this.pMenu);
            this.menubar.addComponentListener(this);
                // TODO: initializing buttons
                this.btFile = MenuModels.fileButton(menubar);
                this.btFile.addActionListener(this);
                this.btList = MenuModels.listButton(menubar);
                this.btList.addActionListener(this);
                this.btMap = MenuModels.mapButton(menubar);
                this.btMap.addActionListener(this);
                this.settings = MenuModels.settingsButton(menubar);
                this.settings.addActionListener(this);
                this.tfSearch = MenuModels.searchTextField(this.menubar);
                this.tfSearch.addKeyListener(this);
                this.searchFilter = MenuModels.searchButton(this.menubar);
                this.searchFilter.addActionListener(this);
                this.searchFilter.addComponentListener(this);
                this.progressbar = MenuModels.progressBar(this.menubar);
                this.progressbar.addComponentListener(this);
                this.settings_intlFrame = MenuModels.settings_intlFrame(this.mainpanel);
                this.settings_intlFrame.addComponentListener(this);
                this.settings_iFrame_maxLoad = MenuModels.settingsOP_maxLoadTxtField();
                this.settings_iFrame_maxLoad.addComponentListener(this);
                this.settings_iFrame_maxLoad.addKeyListener(this);
            // TODO: initializing view head text label
            this.viewHeadText = PanelModels.headLabel("PlaneSpotter");
            // TODO: initializing close view button
            this.closeView = MenuModels.closeViewButton(this.dpright);
            this.closeView.addActionListener(this);
            // TODO: setting up title backround img
            // ich bekomme nur mit der getRessource methode ein Bild zurückgeliefert
            var img = new ImageIcon(Objects.requireNonNull(
                    this.getClass().getResource("/title_background.jpg")));
            this.title_bground = new JLabel(img);
            this.title_bground.setBounds(this.pTitle.getBounds());
            this.title_bground.setBorder(LINE_BORDER);
            // title text (might be replaced through one image)
            this.title = PanelModels.titleTxtLabel(this.pTitle);
            // TODO: setting up start screen
            var start_image = new ImageIcon(Objects.requireNonNull(
                    this.getClass().getResource("/start_image.png")));
            this.lblStartScreen = new JLabel(start_image);
            this.lblStartScreen.setBounds(0, 0, this.pStartScreen.getWidth(), this.pStartScreen.getHeight());
            this.lblStartScreen.setBorder(LINE_BORDER);
            // TODO: adding test bground image
            var test_img = new ImageIcon(Objects.requireNonNull(
                    this.getClass().getResource("/ttowers.png")));
            this.lblStartScreen.setIcon(test_img);

        // TODO: adding all generated components to window
        this.addAllToWinow();

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
        this.menubar.add(this.btFile);
        this.menubar.add(this.btList);
        this.menubar.add(this.btMap);
        this.menubar.add(this.settings);
        this.menubar.add(this.tfSearch);
        this.menubar.add(this.searchFilter);
        this.menubar.add(this.progressbar);
        // TODO: adding everything to search panel
        this.pSearch.add(this.searchForLabel);
        this.pSearch.add(this.searchFor_cmbBox);
        this.pSearch.add(this.searchSeperator);
        this.pSearch.add(this.searchMessage);
        for (var c : this.flightSearch) {
            if (c instanceof JButton) {
                ((JButton) c).addActionListener(this);
            }
            this.pSearch.add(c);
        }
        // TODO: adding menubar to menu panel
        this.pMenu.add(this.pSearch);
        this.pMenu.add(this.menubar);
        // TODO: adding mapViewer to map panel
        this.pMap.add(this.mapViewer);
        // TODO: adding label to start screen panel
        this.pStartScreen.add(this.lblStartScreen);
        // TODO: adding everything to pViewHead
        this.pViewHead.add(this.viewHeadText);
        this.pViewHead.add(this.closeView);
        // TODO: adding everything to right desktop pane
        this.dpright.add(this.pViewHead);
        this.dpright.add(this.pList);
        this.dpright.add(this.pMap);
        this.dpright.add(this.pStartScreen);
        // TODO: adding everything to left desktop pane
        this.dpleft.add(this.pMenu);
        this.dpleft.add(this.pInfo);
        // TODO: adding to pTitle
        this.pTitle.add(PanelModels.titleTxtLabel(this.pTitle));
        this.pTitle.add(this.title_bground);
        // TODO: adding textfield to internal settings frame
        this.settings_iFrame_maxLoad.setText(UserSettings.getMaxLoadedFlights() + "");
        this.settings_intlFrame.add(this.settings_iFrame_maxLoad);
        // TODO: adding title panel to frame
        this.mainpanel.add(this.pTitle);
        // TODO: adding settings internal frame to mainpanel
        this.mainpanel.add(this.settings_intlFrame);
        // TODO: moving flist and fmenu to front
        this.dpright.setVisible(true);
        this.dpleft.setVisible(true);
        // TODO: adding desktop panes to frame
        this.mainpanel.add(this.dpright);
        this.mainpanel.add(this.dpleft);
        // TODO: adding mainpanel to frame
        this.window.add(this.mainpanel);
    }

    /**
     * creates a JScrollPane with the given Component and a specific layout
     * @param inside is the JTree or whatever, which is displayed in the JScrollPane
     * @return sp, the JScrollPane
     */
    JScrollPane listScrollPane(JTree inside) {
        var sp = new JScrollPane(inside);
        sp.setViewportView(inside);
        sp.setBackground(DEFAULT_BG_COLOR);
        sp.setForeground(DEFAULT_BORDER_COLOR);
        sp.setBounds(this.pList.getBounds());
        sp.addComponentListener(this);
        sp.setBorder(LINE_BORDER);
        var verticalScrollBar = sp.getVerticalScrollBar();
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
    public void disposeView() {
        if (this.pStartScreen != null) {
            this.pStartScreen.setVisible(false);
        } if (this.listView != null) {
            this.pList.remove(this.listView);
            this.listView.setVisible(false);
            this.listView = null;
            this.pList.setVisible(false);
        } if (this.mapViewer != null) {
            this.pMap.remove(this.mapViewer);
            this.mapViewer.setVisible(false);
            this.pMap.setVisible(false);
        } if (this.flightInfo != null) {
            this.flightInfo.setVisible(false);
            this.flightInfo = null;
            this.pInfo.setVisible(false);
        }
        this.pMenu.setVisible(true);
        this.dpleft.moveToFront(this.pMenu);
        this.viewHeadText.setText(DEFAULT_HEAD_TEXT);
        GUISlave.revalidateAll();
        GUISlave.requestComponentFocus(this.tfSearch);
    }

    /**
     * enters the text in the textfield (use for key listener)
     */
    protected void enterText () {
        var text = this.tfSearch.getText().toLowerCase();
        if (!text.isBlank()) {
            if (text.startsWith("exit")) {
                Controller.exit();
            } else if (text.startsWith("loadlist")) {
                this.controller.createDataView(LIST_FLIGHT, "");
            } else if (text.startsWith("loadmap")) {
                this.controller.createDataView(MAP_ALL, "");
            } else if (text.startsWith("maxload")) {
                var args = text.split(" ");
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
                var args = text.split(" ");
                if (args.length > 1) {
                    var id = args[1];
                    this.controller.createDataView(MAP_FLIGHTROUTE, id);
                }
            }
        }
        this.tfSearch.setText("");
    }

    /********************************************
     ********************************************
     *                listeners                 *
     *******************************************+
     ********************************************
     */

    @Override
    public void actionPerformed(ActionEvent e) {
        var src = e.getSource();
        if (src == this.btFile) {
            // gonna be filled
        } else if (src == this.btList) {
            GUISlave.progressbarStart();
            this.controller.createDataView(LIST_FLIGHT, "");
        } else if (src == this.btMap) {
            GUISlave.progressbarStart();
            this.controller.createDataView(MAP_ALL, "");
        } else if (src == this.closeView) {
            this.disposeView();
            this.pStartScreen.setVisible(true);
            dpright.moveToFront(this.pStartScreen);
        } else if (src == this.settings) {
            this.settings_intlFrame.show();
            this.settings_iFrame_maxLoad.setCaretColor(Color.YELLOW);
            this.settings_iFrame_maxLoad.setCaretPosition(this.settings_iFrame_maxLoad.getText().length());
            this.settings_iFrame_maxLoad.grabFocus();
        } else if (src == this.searchFilter) {
            this.pSearch.setVisible(!this.pSearch.isVisible());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }


    @Override
    public void keyPressed(KeyEvent e) {
        var src = e.getSource();
        int key = e.getKeyCode();
        if (src == this.tfSearch) {
            if (key == KeyEvent.VK_ENTER) {
                if (this.tfSearch.hasFocus())
                    this.enterText();
            }
        } else if (src == this.settings_iFrame_maxLoad) {
            if (key == KeyEvent.VK_ENTER) {
                try {
                    // TODO fixen: settings fenster schließt erst nach loading
                    if (Integer.parseInt(this.settings_iFrame_maxLoad.getText()) >= 4) {
                        GUISlave.progressbarStart();
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
                case VK_PAGE_UP -> this.mapViewer.moveMap(0, 2);
                case VK_HOME -> this.mapViewer.moveMap(-2, 0);
                case VK_END -> this.mapViewer.moveMap(2, 0);
                case VK_PAGE_DOWN -> this.mapViewer.moveMap(0, -2);
            }
            GUISlave.revalidateAll();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void processCommand(JMVCommandEvent commandEvent) {
        if (    commandEvent.getCommand() == JMVCommandEvent.COMMAND.ZOOM
                && commandEvent.getSource() instanceof MapMarkerDot ) {
            System.out.println("jmv listener!!! test"); // test
        }
    }

    /**
     * component listener: fits the component sizes if the window is resized
     */
    @Override
    public void componentResized(ComponentEvent e) {
        var comp = e.getComponent();
             if (comp == this.window) GUISlave.windowResized(WINDOW);
        else if (comp == this.mainpanel) GUISlave.windowResized(MAINPANEL);
        else if (comp == this.dpleft) GUISlave.windowResized(DPLEFT);
        else if (comp == this.dpright)GUISlave.windowResized(DPRIGHT);
        else if (comp == this.pTitle) GUISlave.windowResized(TITLE_PANEL);
        else if (comp == this.pViewHead) GUISlave.windowResized(VIEW_HEAD);
        else if (comp == this.pStartScreen) GUISlave.windowResized(START_SCREEN);
        else if (comp == this.menubar) GUISlave.windowResized(MENUBAR);
        else if (comp == this.pMap) GUISlave.windowResized(MAP_PANEL);
        else if (comp == this.pMenu) GUISlave.windowResized(MENU_PANEL);
        else if ( comp == this.pList && this.spList != null && this.listView != null) {
            GUISlave.windowResized(LIST_PANEL);
        } else if (comp == this.pInfo && this.flightInfo != null) {
            GUISlave.windowResized(INFO_PANEL);
        }

        GUISlave.revalidateAll();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        var comp = e.getComponent();
        if (comp == this.pList) GUISlave.windowResized(LIST_PANEL);
        else if (comp == this.pMap) GUISlave.windowResized(MAP_PANEL);
        else if (comp == this.settings_iFrame_maxLoad) {
            this.settings_iFrame_maxLoad.setText(UserSettings.getMaxLoadedFlights() + "");
        }
        GUISlave.revalidateAll();
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
        var point = e.getPoint();
        var clickedCoord = this.mapViewer.getPosition(point);
        var mapMarkerList = this.mapViewer.getMapMarkerList();
        var it = mapMarkerList.iterator(); //FIXME setting map marker list while going through it
        var newMarkerList = new ArrayList<MapMarker>();
        CustomMapMarker next, newMarker;
        Coordinate markerCoord;
        int counter = 0; // still needed?
        int zoom = this.mapViewer.getZoom();
        while (it.hasNext()) {
            next = (CustomMapMarker) it.next();
            markerCoord = next.getCoordinate();
            newMarker = new CustomMapMarker(markerCoord, next.getFlight());
            // Positionsabfrage mit leichter Toleranz, damit man den Punkt auch trifft
            double tolerance = 0.1/zoom; // // FIXME: 23.04.2022 falsche formel (exponential?)
            if (    clickedCoord.getLat() < markerCoord.getLat() + tolerance &&
                    clickedCoord.getLat() > markerCoord.getLat() - tolerance &&
                    clickedCoord.getLon() < markerCoord.getLon() + tolerance &&
                    clickedCoord.getLon() > markerCoord.getLon() - tolerance) {
                newMarker.setBackColor(Color.RED);
            } else {
                newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR);
            }
            newMarkerList.add(newMarker);
            counter++;
        } // TODO alles nach WHILE schleife
        //if (next != null) {
            // TODO alle anderen auch removen/color ändern/problem gelöst?
            //this.mapViewer.addMapMarker(newMarker);
        this.mapViewer.setMapMarkerList(newMarkerList);
            this.pMenu.setVisible(false);
            this.pInfo.setVisible(true);
            this.dpleft.moveToFront(this.pInfo);
            /*Position flightPos = new Position(markerCoord.getLat(), markerCoord.getLon());
            //FIXME test: next.getFlight is null
            try {
                System.out.println("nextFlightID " + next.getFlight().getID());
            } catch (Exception etest) {
            }
            int flightID = -1;// = newMarker.getFlight().getID(); // FIXME: why is getFlight == null
            try {
                flightID = BlackBeardsNavigator.allMapMarkers.get(counter).getFlight().getID();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (flightID != -1) {
                recieveInfoTree(TreePlantation.createTree(TreePlantation.oneFlightTreeNode(flightID), this));
            } else {
                recieveInfoTree(TreePlantation.createTree(TreePlantation.oneFlightTreeNode(876), this));
            }
            this.resetMapViewer(next);*/
        //}
    }

    /**
     * resets all map markers
     */
    // TODO richtig machen, ConcurrentModificationException, irgendwas läuft nichts
    public void resetMapViewer (MapMarker doNotReset) {
        var viewer = this.mapViewer;
        this.mapViewer = null;
        var markersIn = viewer.getMapMarkerList();
        viewer.removeAllMapMarkers();
        var markersOut = new ArrayList<MapMarker>();
        for (MapMarker m : markersIn) {
            if (m != doNotReset) {
                var markerPos = m.getCoordinate();
                var newMarker = new CustomMapMarker(markerPos, null);
                newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR);
                markersOut.add(newMarker);
            } else {
                markersOut.add(m);
            }
        }
        viewer.setMapMarkerList(markersOut);
        GUISlave.revalidateAll();
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
    // item listener (combo-box)
    @Override
    public void itemStateChanged(ItemEvent e) {
        var item = e.getItem();
        GUISlave.loadSearch((String) item);
    }
}

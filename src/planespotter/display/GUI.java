package planespotter.display;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import planespotter.constants.Paths;
import planespotter.controller.Controller;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.GUIConstants.DefaultColor.*;
import static planespotter.constants.GUIConstants.Images.FLYING_PLANE_ICON;

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
    public JFrame        window;
    public JFrame loadingScreen;
    public JDesktopPane  dpleft;
    protected JDesktopPane dpright;
    protected JPanel        mainpanel;
    protected JPanel pTitle;
    protected JPanel pViewHead;
    protected JPanel pList;
    protected JPanel pMap;
    public JPanel pMenu;
    public JPanel pInfo;
    protected JPanel pStartScreen;
    protected JPanel pSearch;
    protected JLabel        title, title_bground, lblStartScreen, lblLoading, viewHeadText, searchForLabel;
    protected List<JComponent> flightSearch, planeSearch, airlineSearch, airportSearch, areaSearch;
    protected JTextArea     searchMessage;
    protected JTextField    tfSearch, settings_maxLoadTf;
    protected JRadioButton  rbFlight, rbAirline;
    protected JProgressBar  progressbar;
    protected JMenuBar      menubar;
    protected JButton btFile, settings, searchButton, btList, btMap, closeView;
    protected JDialog settingsDialog;
    protected JScrollPane   spList;
    protected JComboBox<String> searchFor_cmbBox, settings_mapTypeCmbBox;
    protected JSeparator    searchSeperator;
    // search components
    protected JTextField search_flightID;
    protected JTextField search_callsign;
    public JTextField search_planeID;
    protected JTextField search_planetype;
    protected JTextField search_icao;
    protected JTextField search_tailNr;
    protected JTextField search_airpName;
    protected JTextField search_airpTag;
    protected JTextField search_airpID;
    // image labels
    protected JLabel bground, menu_bground;

    protected JButton[] fileMenu, settingsButtons;

    protected volatile JTree listView, infoTree, dpInfoTree;
    // TODO fix ConcurrentModificationException on mapViewer
    public volatile JMapViewer mapViewer;

    // alternative test path: "C:\\Users\\jml04\\Desktop\\loading.gif"
    private final ImageIcon loading_gif = new ImageIcon(Paths.RESSOURCE_PATH + "loading.gif");

    /**
     * constructor for GUI
     */
    public GUI() {
        this.loadingScreen = this.loadingScreen();
        this.window = this.initialize();
    }

    /**
     * GUI run method (?)
     */
    @Override
    public void run() {
        this.loadingScreen.setVisible(true);
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
        var menuModels = new MenuModels();
        var panelModels = new PanelModels();
        var searchModels = new SearchModels();
        // setting up window
        this.window = new JFrame("PlaneSpotter v0.1");
        this.window.setSize(1280, 720);
        this.window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.window.setLocationRelativeTo(null);
        this.window.addComponentListener(this);
        // initializing mainpanel
        this.mainpanel = panelModels.mainPanel(this.window);
        // setting up right desktop pane
        this.dpright = new JDesktopPane();
        this.dpright.setBorder(LINE_BORDER);
        this.dpright.setBackground(DEFAULT_BG_COLOR.get());
        this.dpright.setDesktopManager(new DefaultDesktopManager());
        this.dpright.setBounds(280, 70, this.mainpanel.getWidth()-280, this.mainpanel.getHeight()-70);
        this.dpright.setOpaque(false);
        // setting up left desktop pane
        this.dpleft = new JDesktopPane();
        this.dpleft.setBorder(LINE_BORDER);
        this.dpleft.setBackground(DEFAULT_BG_COLOR.get());
        this.dpleft.setDesktopManager(new DefaultDesktopManager());
        this.dpleft.setBounds(0, 70, 280, this.mainpanel.getHeight()-70);
        this.dpleft.setOpaque(false);
            // initializing title panel
            this.pTitle = panelModels.titlePanel(this.mainpanel);
            // initializing view head panel
            this.pViewHead = panelModels.viewHeadPanel(this.dpright);
            this.pViewHead.setOpaque(false);
            // initializing list panel
            this.pList = panelModels.listPanel(this.dpright);
            this.pList.setOpaque(false);
            // initializing map panel
            this.pMap = panelModels.mapPanel(this.dpright);
            this.pMap.setOpaque(false);
                // initializing map viewer
                this.mapViewer = new BlackBeardsNavigator().defaultMapViewer(this.pMap);
                this.mapViewer.addKeyListener(this);
                this.mapViewer.addMouseListener(this);
                this.mapViewer.addJMVListener(this);
            // initializing menu panel
            this.pMenu = panelModels.menuPanel(this.dpleft);
            this.pMenu.setOpaque(false);
            // initializing info panel
            this.pInfo = panelModels.infoPanel(this.dpleft);
            this.pInfo.setOpaque(false);
            // initializing src screen panel
            this.pStartScreen = panelModels.startPanel(this.dpright);
            this.pStartScreen.setOpaque(false);
            // initializing search panel
            this.pSearch = panelModels.searchPanel(this.pMenu);
                // initializing search panel components
                this.searchForLabel = searchModels.cmbBoxLabel(this.pSearch);
                this.searchForLabel.setOpaque(false);
                this.searchFor_cmbBox = searchModels.searchFor_cmbBox(this.pSearch);
                this.searchFor_cmbBox.addItemListener(this);
                this.searchSeperator = searchModels.searchSeperator(this.pSearch);
                this.searchMessage = searchModels.searchMessage(this.pSearch);
                this.searchMessage.setOpaque(false);
                this.flightSearch = searchModels.flightSearch(pSearch, this);
                this.planeSearch = searchModels.planeSearch(pSearch, this);
                this.airportSearch = searchModels.airportSearch(this.pSearch, this);
            // initializing background labels
            this.bground = panelModels.backgroundLabel(this.dpright);
            this.menu_bground = panelModels.menuBgLabel(this.dpleft);
            // initializing pTitle
            this.menubar = menuModels.menuBar(this.pMenu);
            this.menubar.setOpaque(false);
                // initializing buttons
                this.btList = menuModels.listButton(this.menubar);
                this.btList.addActionListener(this);
                this.btMap = menuModels.mapButton(this.menubar);
                this.btMap.addActionListener(this);
                this.settings = menuModels.settingsButton(this.menubar);
                this.settings.addActionListener(this);
                this.tfSearch = menuModels.searchTextField(this.menubar);
                this.tfSearch.addKeyListener(this);
                this.searchButton = menuModels.searchButton(this.menubar);
                this.searchButton.addActionListener(this);
                this.progressbar = menuModels.progressBar(this.menubar);
                this.settingsDialog = menuModels.settingsDialog(this.window);
                this.settings_maxLoadTf = menuModels.settings_maxLoadTxtField();
                this.settings_maxLoadTf.addKeyListener(this);
                this.settings_mapTypeCmbBox = menuModels.settings_mapTypeCmbBox();
                this.settings_mapTypeCmbBox.addItemListener(this);
                this.settingsButtons = menuModels.settingsButtons(this.settingsDialog);
                for (var bt : this.settingsButtons) {
                    bt.addActionListener(this);
                }
            // initializing view head text label
            this.viewHeadText = panelModels.headLabel();
            this.viewHeadText.setOpaque(false);
            // initializing file button
            this.btFile = menuModels.fileButton(this.dpright);
            this.btFile.addActionListener(this);
            // initializing file menu buttons
            this.fileMenu = menuModels.fileMenu(this.pViewHead);
            // initializing close view button
            this.closeView = menuModels.closeViewButton(this.dpright);
            this.closeView.addActionListener(this);
            // setting up title backround img
            this.title_bground = new JLabel();
            this.title_bground.setBounds(this.pTitle.getBounds());
            this.title_bground.setBorder(LINE_BORDER);
            var img = Images.TITLE.get();
            int width = this.title_bground.getWidth();
            int height = this.title_bground.getHeight();
            var bgroundImg = new ImageIcon(img.getImage().getScaledInstance(width, height, 4));
            this.title_bground.setIcon(bgroundImg);
            // title text (might be replaced through one image)
            this.title = panelModels.titleTxtLabel(this.pTitle);
            // setting up src screen
            var start_image = new ImageIcon(Paths.RESSOURCE_PATH + "start_image.png"); // 15.05.2022
            this.lblStartScreen = new JLabel(start_image);
            this.lblStartScreen.setBounds(0, 0, this.pStartScreen.getWidth(), this.pStartScreen.getHeight());
            this.lblStartScreen.setBorder(LINE_BORDER);
            // adding test bground image
            var test_img = new ImageIcon(Paths.RESSOURCE_PATH + "ttowers.png"); // 15.05.2022
            this.lblStartScreen.setIcon(test_img);
            this.lblStartScreen.setOpaque(false);

            this.window.setIconImage(FLYING_PLANE_ICON.get().getImage());

        // adding all generated components to window
        this.addAllToWinow();

        // setting list and map panel invisible, src screen visible
        this.pList.setVisible(false);
        this.pMap.setVisible(false);
        this.pStartScreen.setVisible(true);
        return this.window;
    }

    /**
     * adds all components to the window frame
     * -> called by initialize()
     */
    private void addAllToWinow() {
        // Adding to Window
        // adding everything to menubar
        this.menubar.add(this.btList);
        this.menubar.add(this.btMap);
        this.menubar.add(this.settings);
        this.menubar.add(this.tfSearch);
        this.menubar.add(this.searchButton);
        this.menubar.add(this.progressbar);
        // adding everything to search panel
        this.pSearch.add(this.searchForLabel);
        this.pSearch.add(this.searchFor_cmbBox);
        this.pSearch.add(this.searchSeperator);
        this.pSearch.add(this.searchMessage);
        for (var comps : this.allSearchModels()) {
            if (comps != null) {
                for (var c : comps) {
                    if (c instanceof JLabel) {
                        c.setOpaque(false);
                    } else if (c instanceof JButton bt) {
                        bt.addActionListener(this);
                    } else if (c instanceof JTextField) {
                        c.addKeyListener(this);
                    }
                    this.pSearch.add(c);
                }
            }
        }
        // adding menubar to menu panel
        this.pMenu.add(this.pSearch);
        this.pMenu.add(this.menubar);
        // adding mapViewer to map panel
        this.pMap.add(this.mapViewer);
        // adding label to src screen panel
        this.pStartScreen.add(this.lblStartScreen);
        for (var bt : this.fileMenu) {
            bt.addActionListener(this);
            this.pViewHead.add(bt);
        }
        // adding everything to pViewHead
        this.pViewHead.add(this.viewHeadText);
        this.pViewHead.add(this.closeView);
        this.pViewHead.add(this.btFile);
        // adding everything to right desktop pane
        this.dpright.add(this.pViewHead);
        this.dpright.add(this.pList);
        this.dpright.add(this.pMap);
        this.dpright.add(this.pStartScreen);
        this.dpright.add(this.bground); // background 1
        // adding everything to left desktop pane
        this.dpleft.add(this.pMenu);
        this.dpleft.add(this.pInfo);
        this.dpleft.add(this.menu_bground);
        // adding to pTitle
        //this.pTitle.add(new PanelModels().titleTxtLabel(this.pTitle));
        this.pTitle.add(this.title_bground);
        // adding textfield to internal settings frame
        this.settings_maxLoadTf.setText(UserSettings.getMaxLoadedData() + "");
        this.settingsDialog.add(this.settings_maxLoadTf);
        this.settingsDialog.add(this.settings_mapTypeCmbBox);
        this.settingsDialog.add(this.settingsButtons[0]);
        this.settingsDialog.add(this.settingsButtons[1]);
        // setting desktopPanes visible
        this.dpright.setVisible(true);
        this.dpleft.setVisible(true);
        // adding title panel to mainpanel
        this.mainpanel.add(this.pTitle);
        // adding desktop panes to mainpanel
        this.mainpanel.add(this.dpright);
        this.mainpanel.add(this.dpleft);
        // adding mainpanel to frame
        this.window.add(this.mainpanel);
    }

    /**
     * @return all search models in a list
     */
    ArrayList<List<JComponent>> allSearchModels () {
        var allSearchComps = new ArrayList<List<JComponent>>();
        allSearchComps.add(this.flightSearch);
        allSearchComps.add(this.planeSearch);
        allSearchComps.add(this.airlineSearch);
        allSearchComps.add(this.airportSearch);
        allSearchComps.add(this.areaSearch);
        return allSearchComps;
    }

    /**
     * creates a JScrollPane with the given Component and a specific layout
     * @param inside is the JTree or whatever, which is displayed in the JScrollPane
     * @return sp, the JScrollPane
     */
    JScrollPane listScrollPane(JTree inside) {
        var sp = new JScrollPane(inside);
        sp.setViewportView(inside);
        sp.setBackground(DEFAULT_BG_COLOR.get());
        sp.setForeground(DEFAULT_BORDER_COLOR.get());
        sp.setBounds(0, 0, this.pList.getWidth(), this.pList.getHeight());
        sp.setBorder(LINE_BORDER);
        var verticalScrollBar = sp.getVerticalScrollBar();
        verticalScrollBar.setBackground(DEFAULT_BG_COLOR.get());
        verticalScrollBar.setForeground(DEFAULT_ACCENT_COLOR.get());
        verticalScrollBar.setBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR.get()));
        sp.setVerticalScrollBar(verticalScrollBar);
        sp.setOpaque(false);
        return sp;
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
        if (src instanceof JButton bt) { // TODO: 22.05.2022 in Controller auslagern, dort den thread erzeugen (oder Listener Klasse)
            var gsl = new GUISlave();
            Controller.getScheduler().exec(() -> gsl.buttonClicked(bt), "GUI-EventThread", false, 5, true);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        new GUISlave().keyPressed(e);
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
        var gsl = new GUISlave();
        gsl.windowResized();
        gsl.update();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        this.componentResized(e);
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
        int button = e.getButton();
        var ctrl = Controller.getInstance();
        if (button == MouseEvent.BUTTON1 && ctrl.currentViewType != null) {
            ctrl.mapClicked(e.getPoint());
        }
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
        if (e.getSource() == this.searchFor_cmbBox) {
            var gsl = new GUISlave();
            gsl.clearSearch();
            gsl.loadSearch((String) item);
        } else if (e.getSource() == this.settings_mapTypeCmbBox) {
            var usrSettings = new UserSettings();
            if (item.equals("Bing Map")) {
                usrSettings.setCurrentMapSource(usrSettings.bingMap);
            } else if (item.equals("Default Map")) {
                usrSettings.setCurrentMapSource(usrSettings.tmstMap);
            } else if (item.equals("Transport Map")) {
                usrSettings.setCurrentMapSource(usrSettings.transportMap);
            }
        }
    }

}

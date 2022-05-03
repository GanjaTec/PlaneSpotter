package planespotter.display;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import planespotter.controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

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
    protected JLabel        title, title_bground, lblStartScreen, lblLoading, viewHeadText, searchForLabel;
    protected List<JComponent> flightSearch, planeSearch, airlineSearch, airportSearch, areaSearch;
    protected JTextArea     searchMessage;
    protected JTextField    tfSearch, settings_iFrame_maxLoad;
    protected JRadioButton  rbFlight, rbAirline;
    protected JProgressBar  progressbar;
    protected JMenuBar      menubar;
    protected JButton btFile, settings, searchButton, btList, btMap, closeView;
    protected JInternalFrame settings_intlFrame;
    protected JScrollPane   spList;
    protected JComboBox<String> searchFor_cmbBox;
    protected JSeparator    searchSeperator;
    // search components
    protected JTextField search_flightID, search_callsign;
    // image labels
    protected JLabel bground, menu_bground;

    protected volatile JTree listView, infoTree;
    // TODO fix ConcurrentModificationException on mapViewer
    protected volatile JMapViewer mapViewer;

    // alternative test path: "C:\\Users\\jml04\\Desktop\\loading.gif"
    private final ImageIcon loading_gif = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/planespotter/images/loading.gif")));

    // controller instance
    final Controller controller = Controller.getInstance();

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
        this.window.addComponentListener(this);
        // TODO: initializing mainpanel
        this.mainpanel = PanelModels.mainPanel(this.window);
        // TODO: setting up right desktop pane
        this.dpright = new JDesktopPane();
        this.dpright.setBorder(LINE_BORDER);
        this.dpright.setBackground(DEFAULT_BG_COLOR);
        this.dpright.setDesktopManager(new DefaultDesktopManager());
        this.dpright.setBounds(280, 70, this.mainpanel.getWidth()-280, this.mainpanel.getHeight()-70);
        this.dpright.setOpaque(false);
        // TODO: setting up left desktop pane
        this.dpleft = new JDesktopPane();
        this.dpleft.setBorder(LINE_BORDER);
        this.dpleft.setBackground(DEFAULT_BG_COLOR);
        this.dpleft.setDesktopManager(new DefaultDesktopManager());
        this.dpleft.setBounds(0, 70, 280, this.mainpanel.getHeight()-70);
        this.dpleft.setOpaque(false);
            // TODO: initializing title panel
            this.pTitle = PanelModels.titlePanel(this.mainpanel);
            // TODO: initializing view head panel
            this.pViewHead = PanelModels.viewHeadPanel(this.dpright);
            this.pViewHead.setOpaque(false);
            // TODO: initializing list panel
            this.pList = PanelModels.listPanel(this.dpright);
            this.pList.setOpaque(false);
            // TODO: initializing map panel
            this.pMap = PanelModels.mapPanel(this.dpright);
            this.pMap.setOpaque(false);
                // TODO: initializing map viewer
                this.mapViewer = BlackBeardsNavigator.defaultMapViewer(this.pMap);
                this.mapViewer.addKeyListener(this);
                this.mapViewer.addMouseListener(this);
                this.mapViewer.addJMVListener(this);
            // TODO: initializing menu panel
            this.pMenu = PanelModels.menuPanel(this.dpleft);
            this.pMenu.setOpaque(false);
            // TODO: initializing info panel
            this.pInfo = PanelModels.infoPanel(this.dpleft);
            this.pInfo.setOpaque(false);
            // TODO: initializing start screen panel
            this.pStartScreen = PanelModels.startPanel(this.dpright);
            this.pStartScreen.setOpaque(false);
            // TODO: initializing search panel
            this.pSearch = PanelModels.searchPanel(this.pMenu);
                // TODO: initializing search panel components
                this.searchForLabel = SearchModels.cmbBoxLabel(this.pSearch);
                this.searchForLabel.setOpaque(false);
                this.searchFor_cmbBox = SearchModels.searchFor_cmbBox(this.pSearch);
                this.searchFor_cmbBox.addItemListener(this);
                this.searchSeperator = SearchModels.searchSeperator(this.pSearch);
                this.searchMessage = SearchModels.searchMessage(this.pSearch);
                this.searchMessage.setOpaque(false);
                this.flightSearch = SearchModels.flightSearch(pSearch, this);
            // TODO: initializing background labels
            this.bground = PanelModels.backgroundLabel(this.dpright);
            this.menu_bground = PanelModels.menuBgLabel(this.dpleft);
            // TODO: initializing pTitle
            this.menubar = MenuModels.menuBar(this.pMenu);
            this.menubar.setOpaque(false);
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
                this.searchButton = MenuModels.searchButton(this.menubar);
                this.searchButton.addActionListener(this);
                this.progressbar = MenuModels.progressBar(this.menubar);
                this.settings_intlFrame = MenuModels.settings_intlFrame(this.mainpanel);
                this.settings_iFrame_maxLoad = MenuModels.settingsOP_maxLoadTxtField();
                this.settings_iFrame_maxLoad.addKeyListener(this);
            // TODO: initializing view head text label
            this.viewHeadText = PanelModels.headLabel("PlaneSpotter");
            this.viewHeadText.setOpaque(false);
            // TODO: initializing close view button
            this.closeView = MenuModels.closeViewButton(this.dpright);
            this.closeView.addActionListener(this);
            // TODO: setting up title backround img
            // ich bekomme nur mit der getRessource methode ein Bild zurückgeliefert
            var img = new ImageIcon(Objects.requireNonNull(
                    this.getClass().getResource("/planespotter/images/title_background.jpg"))); // // FIXME: 27.04.2022 in Constants auslagern!
            this.title_bground = new JLabel(img);
            this.title_bground.setBounds(this.pTitle.getBounds());
            this.title_bground.setBorder(LINE_BORDER);
            // title text (might be replaced through one image)
            this.title = PanelModels.titleTxtLabel(this.pTitle);
            // TODO: setting up start screen
            var start_image = new ImageIcon(Objects.requireNonNull(
                    this.getClass().getResource("/planespotter/images/start_image.png")));
            this.lblStartScreen = new JLabel(start_image);
            this.lblStartScreen.setBounds(0, 0, this.pStartScreen.getWidth(), this.pStartScreen.getHeight());
            this.lblStartScreen.setBorder(LINE_BORDER);
            // TODO: adding test bground image
            var test_img = new ImageIcon(Objects.requireNonNull(
                    this.getClass().getResource("/planespotter/images/ttowers.png")));
            this.lblStartScreen.setIcon(test_img);
            this.lblStartScreen.setOpaque(false);

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
        this.menubar.add(this.searchButton);
        this.menubar.add(this.progressbar);
        // TODO: adding everything to search panel
        this.pSearch.add(this.searchForLabel);
        this.pSearch.add(this.searchFor_cmbBox);
        this.pSearch.add(this.searchSeperator);
        this.pSearch.add(this.searchMessage);
        for (var c : this.flightSearch) {
            if (c instanceof JLabel) {
                c.setOpaque(false);
            } else if (c instanceof JButton) {
                ((JButton) c).addActionListener(this);
            } else if (c instanceof JTextField) {
                c.addKeyListener(this);
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
        this.dpright.add(this.bground); // background 1
        // TODO: adding everything to left desktop pane
        this.dpleft.add(this.pMenu);
        this.dpleft.add(this.pInfo);
        this.dpleft.add(this.menu_bground);
        // TODO: adding to pTitle
        this.pTitle.add(PanelModels.titleTxtLabel(this.pTitle));
        this.pTitle.add(this.title_bground);
        // TODO: adding textfield to internal settings frame
        this.settings_iFrame_maxLoad.setText(UserSettings.getMaxLoadedFlights() + "");
        this.settings_intlFrame.add(this.settings_iFrame_maxLoad);
        // setting desktopPanes visible
        this.dpright.setVisible(true);
        this.dpleft.setVisible(true);
        // TODO: adding title panel to mainpanel
        this.mainpanel.add(this.pTitle);
        // TODO: adding settings internal frame to mainpanel
        this.mainpanel.add(this.settings_intlFrame);
        // TODO: adding desktop panes to mainpanel
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
        sp.setBounds(0, 0, this.pList.getWidth(), this.pList.getHeight());
        sp.setBorder(LINE_BORDER);
        var verticalScrollBar = sp.getVerticalScrollBar();
        verticalScrollBar.setBackground(DEFAULT_BG_COLOR);
        verticalScrollBar.setForeground(DEFAULT_ACCENT_COLOR);
        verticalScrollBar.setBorder(BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR));
        sp.setVerticalScrollBar(verticalScrollBar);
        return sp;
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
                    this.controller.createDataView(MAP_TRACKING, id);
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
        if (src instanceof JButton) {
            if (((JButton) src).getName() != null && ((JButton) src).getName().equals("loadMap")) {
                GUISlave.progressbarStart();
            }
            GUISlave.buttonClicked((JButton) src);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        GUISlave.keyPressed(e);
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
        GUISlave.windowResized();
        GUISlave.revalidateAll();
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
        if (button == MouseEvent.BUTTON1) {
            BlackBeardsNavigator.markerClicked(e.getPoint());
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
        GUISlave.loadSearch((String) item);
    }
}

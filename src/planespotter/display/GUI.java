package planespotter.display;

import org.jfree.chart.ChartPanel;
import planespotter.constants.Paths;
import planespotter.constants.SearchType;
import planespotter.constants.UserSettings;
import planespotter.constants.ViewType;
import planespotter.controller.ActionHandler;
import planespotter.display.models.MenuModels;
import planespotter.display.models.PaneModels;
import planespotter.display.models.SearchModels;
import planespotter.throwables.NoSuchContainerException;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @name GUI
 * @author jml04
 * @version 1.1
 *
 * GUI class is the main gui class, it implements all the listeners
 * and has all components -> it contains window that the user sees
 */
public class GUI {
    // action  handler
    private final ActionHandler actionHandler;
    // map manager
    private MapManager mapManager;
    // tree plantation
    private TreePlantation treePlantation;
    // current loaded search
    private SearchType currentSearchType;
    // current view type ( in action )
    private ViewType currentViewType;

    private Rectangle currentVisibleRect;

    // all components TODO Component name (String) enum that one doesn't have to search for the right comp name
    private final HashMap<String, Container> components;

    public JFrame loadingScreen;
    protected List<JComponent> flightSearch, planeSearch, airlineSearch, airportSearch, areaSearch;
    // search components
    // TODO: 30.06.2022 move to components hash map
    public JTextField search_flightID;
    public JTextField search_callsign;
    public JTextField search_planeID;
    public JTextField search_planetype;
    public JTextField search_icao;
    public JTextField search_tailNr;
    public JTextField search_airpName;
    public JTextField search_airpTag;
    public JTextField search_airpID;
    // file menu
    protected JButton[] fileMenu;
    // data trees
    protected JTree listView, infoTree, dpInfoTree;

    public ChartPanel chartPanel;

    /**
     * constructor for GUI
     */
    public GUI(final ActionHandler actionHandler) {
        this.components = new HashMap<>();
        this.actionHandler = actionHandler;
        this.loadingScreen = new PaneModels().loadingScreen();
        this.addContainer("window", this.initialize());
        this.getContainer("window").add(this.getContainer("mainPanel"));
        this.currentViewType = null;
        this.currentSearchType = SearchType.FLIGHT;
    }

    /**
     * GUI run method (?)
     */
    public void startLoadingScreen() {
        this.loadingScreen.setVisible(true);
    }

    /**
     * initialize method
     * creates new GUI window
     */
    protected JFrame initialize() {
        var menuModels = new MenuModels();
        var panelModels = new PaneModels();
        var searchModels = new SearchModels();
        // setting up window
        var window = panelModels.windowFrame(this.actionHandler);
        var mainPanel = panelModels.mainPanel(window);
        this.addContainer("mainPanel", mainPanel);
        var desktopPanes = panelModels.desktopPanes(mainPanel);
        this.addContainer("leftDP", desktopPanes[0]);
        this.addContainer("rightDP", desktopPanes[1]);
        var titlePanel = panelModels.titlePanel(mainPanel);
        this.addContainer("titlePanel", titlePanel);
        var viewHeadPanel = panelModels.viewHeadPanel((JDesktopPane) this.getContainer("rightDP"));
        this.addContainer("viewHeadPanel", viewHeadPanel);
        var listPanel = panelModels.listPanel((JDesktopPane) this.getContainer("rightDP"));
        this.addContainer("listPanel", listPanel);
        var mapPanel = panelModels.mapPanel((JDesktopPane) this.getContainer("rightDP"));
        this.addContainer("mapPanel", mapPanel);

        // initializing map viewer
        this.mapManager = new MapManager(this, mapPanel, this.actionHandler);
        this.treePlantation = new TreePlantation();

        var menuPanel = panelModels.menuPanel((JDesktopPane) this.getContainer("leftDP"));
        this.addContainer("menuPanel", menuPanel);
        var infoPanel = panelModels.infoPanel((JDesktopPane) this.getContainer("leftDP"));
        this.addContainer("infoPanel", infoPanel);
        var startPanel = panelModels.startPanel((JDesktopPane) this.getContainer("rightDP"));
        this.addContainer("startPanel", startPanel);
        var searchPanel = panelModels.searchPanel(menuPanel);
        this.addContainer("searchPanel", searchPanel);
                // initializing search panel components
                var searchForLabel = searchModels.cmbBoxLabel(searchPanel);
                this.addContainer("searchForLabel", searchForLabel);
                var searchForCmbBox = searchModels.searchFor_cmbBox(searchPanel, this.actionHandler);
                this.addContainer("searchForCmbBox", searchForCmbBox);
                //this.searchFor_cmbBox.addItemListener(this.actionHandler);
                var searchSeperator = searchModels.searchSeperator(searchPanel);
                this.addContainer("searchSeperator", searchSeperator);
                var searchMessage = searchModels.searchMessage(searchPanel);
                this.addContainer("searchMessage", searchMessage);
                this.flightSearch = searchModels.flightSearch(searchPanel, this, this.actionHandler);
                this.planeSearch = searchModels.planeSearch(searchPanel, this, this.actionHandler);
                this.airportSearch = searchModels.airportSearch(searchPanel, this, this.actionHandler);
            var bgLabel = panelModels.backgroundLabel((JDesktopPane) this.getContainer("rightDP"));
            this.addContainer("bgLabel", bgLabel);
            var menuBgLabel = panelModels.menuBgLabel((JDesktopPane) this.getContainer("leftDP"));
            this.addContainer("menuBgLabel", menuBgLabel);
            var menuBar = menuModels.menuBar(menuPanel);
            this.addContainer("menuBar", menuBar);
                // initializing buttons
                var listButton = menuModels.listButton(menuBar, this.actionHandler);
                this.addContainer("listButton", listButton);
                var mapButton = menuModels.mapButton(menuBar, this.actionHandler);
                this.addContainer("mapButton", mapButton);
                var statsButton = menuModels.statisticsButton(menuBar, this.actionHandler);
                this.addContainer("statsButton", statsButton);
                var supplierButton = menuModels.supplierButton(menuBar, this.actionHandler);
                this.addContainer("supplierButton", supplierButton);
                var settingsButton = menuModels.settingsButton(menuBar, this.actionHandler);
                this.addContainer("settingsButton", settingsButton);
                var searchButton = menuModels.searchButton(menuBar, this.actionHandler);
                this.addContainer("searchButton", searchButton);

                var progressBar = menuModels.progressBar(viewHeadPanel);
                this.addContainer("progressBar", progressBar);
                var settingsDialog = menuModels.settingsDialog(window);
                this.addContainer("settingsDialog", settingsDialog);
                var settings_maxLoadTxtField = menuModels.settings_maxLoadTxtField(this.actionHandler);
                this.addContainer("settingsMaxLoadTxtField", settings_maxLoadTxtField);
                var settings_mapTypeCmbBox = menuModels.settings_mapTypeCmbBox(this.actionHandler);
                this.addContainer("settingsMapTypeCmbBox", settings_mapTypeCmbBox);
                var settingsButtons = menuModels.settingsButtons(settingsDialog, this.actionHandler);
                this.addContainer("settingsCancelButton", settingsButtons[0]);
                this.addContainer("settingsConfirmButton", settingsButtons[1]);

            var viewHeadTxtLabel = panelModels.headTxtLabel();
            this.addContainer("viewHeadTxtLabel", viewHeadTxtLabel);
            var fileButton = menuModels.fileButton((JDesktopPane) this.getContainer("rightDP"), this.actionHandler);
            this.addContainer("fileButton", fileButton);
            //this.btFile.addActionListener(this.actionHandler);
            var fileMenu = menuModels.fileMenu(viewHeadPanel, this.actionHandler);
            this.fileMenu = fileMenu;
            this.addContainer("fileBackButton", fileMenu[0]);
            this.addContainer("fileSaveButton", fileMenu[1]);
            this.addContainer("fileOpenButton", fileMenu[2]);
            var closeViewButton = menuModels.closeViewButton((JDesktopPane) this.getContainer("rightDP"), this.actionHandler);
            this.addContainer("closeViewButton", closeViewButton);
            //this.closeView.addActionListener(this.actionHandler);
            var titleBackgroundLabel = panelModels.titleBackgroundLabel(titlePanel);
            this.addContainer("titleBgLabel", titleBackgroundLabel);

            var test_img = new ImageIcon(Paths.IMAGE_PATH + "ttowers.png"); // FIXME: 28.05.2022 auslagern und anderes bild!
                                                                                       // (richtiger start screen mit kurzem text oder so)
            var startLabel = panelModels.startScreenLabel(startPanel, test_img);
            this.addContainer("startLabel", startLabel);

        // adding all generated components to window
        this.addAllToWinow();

        this.getContainer("mapPanel").setVisible(false);
        this.getContainer("startPanel").setVisible(true);

        return window;
    }

    /**
     * adds all components to the window fr24Frame
     * -> called by initialize()
     */
    private void addAllToWinow() { // TODO: 28.05.2022 (evtl.) Components als paremeter
        // TODO: 28.05.2022 HashMap mit key verwenden, statt Klassenvariablen
        // Adding to Window
        // adding everything to menubar
        var menubar = this.getContainer("menuBar");
        menubar.add(this.getContainer("listButton"));
        menubar.add(this.getContainer("mapButton"));
        menubar.add(this.getContainer("statsButton"));
        menubar.add(this.getContainer("supplierButton"));
        menubar.add(this.getContainer("settingsButton")); // TODO gehört das nicht alles in SearchPanel
        menubar.add(this.getContainer("searchButton"));
        // adding everything to search panel
        var searchPanel = this.getContainer("searchPanel");
        searchPanel.add(this.getContainer("searchForLabel"));
        searchPanel.add(this.getContainer("searchForCmbBox"));
        searchPanel.add(this.getContainer("searchSeperator"));
        searchPanel.add(this.getContainer("searchMessage"));
        searchPanel.addKeyListener(this.actionHandler);
        for (var comps : this.allSearchModels()) {
            if (comps != null) {
                for (var c : comps) {
                    if (c instanceof JLabel) {
                        c.setOpaque(false);
                    }
                    searchPanel.add(c);
                }
            }
        }
        var menuPanel = this.getContainer("menuPanel");
        menuPanel.add(searchPanel);
        menuPanel.add(menubar);
        this.getContainer("startPanel").add(this.getContainer("startLabel"));
        var viewHeadPanel = this.getContainer("viewHeadPanel");
        viewHeadPanel.add(this.getContainer("fileBackButton"));
        viewHeadPanel.add(this.getContainer("fileSaveButton"));
        viewHeadPanel.add(this.getContainer("fileOpenButton"));
        viewHeadPanel.add(this.getContainer("viewHeadTxtLabel"));
        viewHeadPanel.add(this.getContainer("closeViewButton"));
        viewHeadPanel.add(this.getContainer("fileButton"));
        viewHeadPanel.add(this.getContainer("progressBar"));
        // adding everything to right desktop pane
        var rightDP = this.getContainer("rightDP");
        rightDP.add(viewHeadPanel);
        rightDP.add(this.getContainer("listPanel"));
        rightDP.add(this.getContainer("mapPanel"));
        rightDP.add(this.getContainer("startPanel"));
        rightDP.add(this.getContainer("bgLabel"));
        var leftDP = this.getContainer("leftDP");
        leftDP.add(menuPanel);
        leftDP.add(this.getContainer("infoPanel"));
        leftDP.add(this.getContainer("menuBgLabel"));
        var titlePanel = this.getContainer("titlePanel");
        titlePanel.add(this.getContainer("titleBgLabel"));
        var settingsMaxLoadTxtField = (JTextField) this.getContainer("settingsMaxLoadTxtField");
        settingsMaxLoadTxtField.setText(UserSettings.getMaxLoadedData() + "");
        var settingsDialog = this.getContainer("settingsDialog");
        settingsDialog.add(settingsMaxLoadTxtField);
        settingsDialog.add(this.getContainer("settingsMapTypeCmbBox"));
        settingsDialog.add(this.getContainer("settingsCancelButton"));
        settingsDialog.add(this.getContainer("settingsConfirmButton"));
        var mainPanel = this.getContainer("mainPanel");
        mainPanel.add(titlePanel);
        // adding desktop panes to mainpanel
        mainPanel.add(rightDP);
        mainPanel.add(leftDP);
        // adding mainpanel to fr24Frame
    }

    public boolean hasContainer(String withName) {
        return this.components.containsKey(withName);
    }

    void addContainer(final String key, final Container c) {
        this.components.put(key, c);
    }

    public final Container getContainer(final String name) {
        final var comp = this.components.getOrDefault(name, null);
        if (comp == null) {
            throw new NoSuchContainerException();
        }
        return comp;
    }

    /**
     * @return all search models in a list
     */
     Collection<List<JComponent>> allSearchModels() {
        var allSearchComps = new LinkedList<List<JComponent>>();
        allSearchComps.add(this.flightSearch);
        allSearchComps.add(this.planeSearch);
        allSearchComps.add(this.airlineSearch);
        allSearchComps.add(this.airportSearch);
        allSearchComps.add(this.areaSearch);
        return allSearchComps;
    }

    public TreasureMap getMap() {
        assert this.mapManager != null;
        return this.mapManager.getMapViewer();
    }

    public MapManager getMapManager() {
        assert this.mapManager != null;
        return this.mapManager;
    }

    public TreePlantation getTreePlantation() {
        assert this.treePlantation != null;
        return this.treePlantation;
    }

    public SearchType getCurrentSearchType() {
        assert this.currentSearchType != null;
        return this.currentSearchType;
    }

    public void setCurrentSearchType(SearchType searchType) {
        this.currentSearchType = searchType;
    }

    public ViewType getCurrentViewType() {
        assert this.currentViewType != null;
        return this.currentViewType;
    }

    public void setCurrentViewType(ViewType viewType) {
        this.currentViewType = viewType;
    }

    public Rectangle getCurrentVisibleRect() {
        assert this.currentVisibleRect != null;
        return this.currentVisibleRect;
    }

    public void setCurrentVisibleRect(Rectangle visibleRect) {
        this.currentVisibleRect = visibleRect;
    }

    public JTree getListView() {
        return (this.listView != null) ? this.listView : null;
    }

    public JButton[] getFileMenu() {
        assert this.fileMenu != null;
        return this.fileMenu;
    }
}

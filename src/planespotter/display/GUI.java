package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartPanel;
import planespotter.constants.*;
import planespotter.controller.ActionHandler;
import planespotter.display.models.MenuModels;
import planespotter.display.models.PaneModels;
import planespotter.display.models.SearchModels;
import planespotter.model.nio.LiveLoader;
import planespotter.throwables.IllegalInputException;
import planespotter.throwables.NoSuchComponentException;
import planespotter.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.GUIConstants.DEFAULT_HEAD_TEXT;
import static planespotter.constants.Sound.SOUND_DEFAULT;

/**
 * @name GUI
 * @author jml04
 * @version 1.1
 *
 * GUI class is the main gui class, it implements all the listeners
 * and has all components -> it contains window that the user sees
 */
@Deprecated(since = "new UserInterface class"/* ,forRemoval = true*/)
public class GUI {
    // 'warning shown' flag
    private static boolean warningShown = false;
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

    public final JFrame loadingScreen;

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
    public JTextField search_airlID;
    public JTextField search_airlTag;
    public JTextField search_airlName;
    public JTextField search_airlCountry;
    // file menu
    protected JButton[] fileMenu;
    // data trees
    protected JTree listView, infoTree, dpInfoTree;
    // chartPanel
    public ChartPanel chartPanel;

    /**
     * constructor for GUI
     */
    public GUI(final ActionHandler actionHandler) {
        this.components = new HashMap<>();
        this.actionHandler = actionHandler;
        this.loadingScreen = new PaneModels().loadingScreen();
        this.addContainer("window", this.initialize());
        this.getComponent("window").add(this.getComponent("mainPanel"));
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
        /*MenuModels menuModels = new MenuModels();
        PaneModels panelModels = new PaneModels();
        SearchModels searchModels = new SearchModels();
        // setting up window
        JFrame window = panelModels.windowFrame(this.actionHandler);
        JPanel mainPanel = panelModels.mainPanel(window);
        this.addContainer("mainPanel", mainPanel);
        JDesktopPane[] desktopPanes = panelModels.desktopPanes(mainPanel);
        this.addContainer("leftDP", desktopPanes[0]);
        this.addContainer("rightDP", desktopPanes[1]);
        JPanel titlePanel = panelModels.titlePanel(mainPanel);
        this.addContainer("titlePanel", titlePanel);
        JPanel viewHeadPanel = panelModels.viewHeadPanel((JDesktopPane) this.getComponent("rightDP"));
        this.addContainer("viewHeadPanel", viewHeadPanel);
        JPanel listPanel = panelModels.listPanel((JDesktopPane) this.getComponent("rightDP"));
        this.addContainer("listPanel", listPanel);
        JPanel mapPanel = panelModels.mapPanel((JDesktopPane) this.getComponent("rightDP"));
        this.addContainer("mapPanel", mapPanel);

        // initializing map viewer
        this.mapManager = *//*new MapManager(this, mapPanel, this.actionHandler)*//*null;
        this.treePlantation = new TreePlantation();

        JPanel menuPanel = panelModels.menuPanel((JDesktopPane) this.getComponent("leftDP"));
        this.addContainer("menuPanel", menuPanel);
        JPanel infoPanel = panelModels.infoPanel((JDesktopPane) this.getComponent("leftDP"));
        this.addContainer("infoPanel", infoPanel);
        JPanel startPanel = panelModels.startPanel((JDesktopPane) this.getComponent("rightDP"));
        this.addContainer("startPanel", startPanel);
        JPanel searchPanel = panelModels.searchPanel(menuPanel);
        this.addContainer("searchPanel", searchPanel);
                // initializing search panel components
                JLabel searchForLabel = searchModels.cmbBoxLabel(searchPanel);
                this.addContainer("searchForLabel", searchForLabel);
                JComboBox<String> searchForCmbBox = searchModels.searchFor_cmbBox(searchPanel, this.actionHandler);
                this.addContainer("searchForCmbBox", searchForCmbBox);
                //this.searchFor_cmbBox.addItemListener(this.actionHandler);
                JSeparator searchSeperator = searchModels.searchSeperator(searchPanel);
                this.addContainer("searchSeperator", searchSeperator);
                JTextArea searchMessage = searchModels.searchMessage(searchPanel);
                this.addContainer("searchMessage", searchMessage);
                this.flightSearch = searchModels.flightSearch(searchPanel, this, this.actionHandler);
                this.planeSearch = searchModels.planeSearch(searchPanel, this, this.actionHandler);
                this.airportSearch = searchModels.airportSearch(searchPanel, this, this.actionHandler);
                this.airlineSearch = searchModels.airlineSearch(searchPanel, this, this.actionHandler);
            JLabel bgLabel = panelModels.backgroundLabel((JDesktopPane) this.getComponent("rightDP"));
            this.addContainer("bgLabel", bgLabel);
            var menuBgLabel = panelModels.menuBgLabel((JDesktopPane) this.getComponent("leftDP"));
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
                var settingsLivePeriodSlider = menuModels.settingsLivePeriodSlider();
                this.addContainer("settingsLivePeriodSlider", settingsLivePeriodSlider);
                var settingsFilterButton = menuModels.settingsFilterButton(this.actionHandler);
                this.addContainer("settingsFilterButton", settingsFilterButton);
                var settingsButtons = menuModels.settingsButtons(settingsDialog, this.actionHandler);
                this.addContainer("settingsCancelButton", settingsButtons[0]);
                this.addContainer("settingsConfirmButton", settingsButtons[1]);

            var viewHeadTxtLabel = panelModels.headTxtLabel();
            this.addContainer("viewHeadTxtLabel", viewHeadTxtLabel);
            var fileButton = menuModels.fileButton((JDesktopPane) this.getComponent("rightDP"), this.actionHandler);
            this.addContainer("fileButton", fileButton);
            //this.btFile.addActionListener(this.actionHandler);
            var fileMenu = menuModels.fileMenu(viewHeadPanel, this.actionHandler);
            this.fileMenu = fileMenu;
            this.addContainer("fileBackButton", fileMenu[0]);
            this.addContainer("fileSaveButton", fileMenu[1]);
            this.addContainer("fileOpenButton", fileMenu[2]);
            var closeViewButton = menuModels.closeViewButton((JDesktopPane) this.getComponent("rightDP"), this.actionHandler);
            this.addContainer("closeViewButton", closeViewButton);
            //this.closeView.addActionListener(this.actionHandler);
            var titleBackgroundLabel = panelModels.titleBackgroundLabel(titlePanel);
            this.addContainer("titleBgLabel", titleBackgroundLabel);
            // TODO: 03.07.2022 remove or improve
            var startLabel = panelModels.startScreenLabel(startPanel, Images.BGROUND_IMG.get());
            this.addContainer("startLabel", startLabel);

        // adding all generated components to window
        this.addAllToWinow();

        this.getComponent("mapPanel").setVisible(false);
        this.getComponent("startPanel").setVisible(true);
        // TODO: 04.08.2022 move
        window.setJMenuBar(menuModels.topMenuBar(this.actionHandler));

        return window;*/return null;
    }

    /**
     * adds all components to the window fr24Frame
     * -> called by initialize()
     */
    private void addAllToWinow() { // TODO: 28.05.2022 (evtl.) Components als paremeter
        // Adding to Window
        // adding everything to menubar
        var menubar = this.getComponent("menuBar");
        menubar.add(this.getComponent("listButton"));
        menubar.add(this.getComponent("mapButton"));
        menubar.add(this.getComponent("statsButton"));
        menubar.add(this.getComponent("supplierButton"));
        menubar.add(this.getComponent("settingsButton")); // TODO gehört das nicht alles in SearchPanel
        menubar.add(this.getComponent("searchButton"));
        // adding everything to search panel
        var searchPanel = this.getComponent("searchPanel");
        searchPanel.add(this.getComponent("searchForLabel"));
        searchPanel.add(this.getComponent("searchForCmbBox"));
        searchPanel.add(this.getComponent("searchSeperator"));
        searchPanel.add(this.getComponent("searchMessage"));
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
        var menuPanel = this.getComponent("menuPanel");
        menuPanel.add(searchPanel);
        menuPanel.add(menubar);
        this.getComponent("startPanel").add(this.getComponent("startLabel"));
        var viewHeadPanel = this.getComponent("viewHeadPanel");
        viewHeadPanel.add(this.getComponent("fileBackButton"));
        viewHeadPanel.add(this.getComponent("fileSaveButton"));
        viewHeadPanel.add(this.getComponent("fileOpenButton"));
        viewHeadPanel.add(this.getComponent("viewHeadTxtLabel"));
        viewHeadPanel.add(this.getComponent("closeViewButton"));
        viewHeadPanel.add(this.getComponent("fileButton"));
        viewHeadPanel.add(this.getComponent("progressBar"));
        // adding everything to right desktop pane
        var rightDP = this.getComponent("rightDP");
        rightDP.add(viewHeadPanel);
        rightDP.add(this.getComponent("listPanel"));
        rightDP.add(this.getComponent("mapPanel"));
        rightDP.add(this.getComponent("startPanel"));
        rightDP.add(this.getComponent("bgLabel"));
        var leftDP = this.getComponent("leftDP");
        leftDP.add(menuPanel);
        leftDP.add(this.getComponent("infoPanel"));
        leftDP.add(this.getComponent("menuBgLabel"));
        var titlePanel = this.getComponent("titlePanel");
        titlePanel.add(this.getComponent("titleBgLabel"));
        var settingsMaxLoadTxtField = (JTextField) this.getComponent("settingsMaxLoadTxtField");
        settingsMaxLoadTxtField.setText(UserSettings.getMaxLoadedData() + "");
        var settingsDialog = this.getComponent("settingsDialog");
        settingsDialog.add(settingsMaxLoadTxtField);
        settingsDialog.add(this.getComponent("settingsMapTypeCmbBox"));
        settingsDialog.add(this.getComponent("settingsLivePeriodSlider"));
        settingsDialog.add(this.getComponent("settingsFilterButton"));
        settingsDialog.add(this.getComponent("settingsCancelButton"));
        settingsDialog.add(this.getComponent("settingsConfirmButton"));
        var mainPanel = this.getComponent("mainPanel");
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

    public final Container getComponent(final String name) {
        final var comp = this.components.getOrDefault(name, null);
        if (comp == null) {
            throw new NoSuchComponentException();
        }
        return comp;
    }

    /**
     * @return all search models in a list
     */
    public Collection<List<JComponent>> allSearchModels() {
        var allSearchComps = new LinkedList<List<JComponent>>();
        allSearchComps.add(this.flightSearch);
        allSearchComps.add(this.planeSearch);
        allSearchComps.add(this.airlineSearch);
        allSearchComps.add(this.airportSearch);
        allSearchComps.add(this.areaSearch);
        return allSearchComps;
    }

    /**
     * shows a specific warning dialog
     *
     * @param type is the warning type which contains the warning message
     */
    public void showWarning(Warning type) {
        this.showWarning(type, null);
    }

    /**
     * shows a specific warning dialog
     *
     * @param type is the warning type which contains the warning message
     */
    public void showWarning(Warning type, @Nullable String addTxt) {
        if (!warningShown) {
            var message = type.message();
            if (addTxt != null) {
                message += "\n" + addTxt;
            }
            warningShown = true;
            try {
                JOptionPane.showOptionDialog(
                        this.getComponent("window"),
                        message,
                        "Warning",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, null, null); // TODO: 30.06.2022 warning icon
            } finally {
                warningShown = false;
            }
        }
    }

    /**
     * this method is executed when pre-loading is done
     */
    public void onInitFinish() {
        Utilities.playSound(SOUND_DEFAULT.get());
        this.loadingScreen.dispose();
        var window = this.getComponent("window");
        window.setVisible(true);
        window.requestFocus();
    }

    public void receiveChart(ChartPanel chartPanel) {
        this.disposeView();

        var rightDP = (JDesktopPane) this.getComponent("rightDP");
        rightDP.add(chartPanel);
        rightDP.moveToFront(chartPanel);
        chartPanel.setVisible(true);

        this.chartPanel = chartPanel;
    }

    /**
     * sets the JTree in listView and makes it visible
     *
     * @param tree is the tree to set
     */
    public void receiveTree(JTree tree) {
        var listPanel = (JPanel) this.getComponent("listPanel");
        tree.setBounds(0, 0, listPanel.getWidth(), listPanel.getHeight());
        this.addContainer("listScrollPane", new PaneModels().listScrollPane(tree, listPanel));
        var listScrollPane = this.getComponent("listScrollPane");
        this.getComponent("listPanel").add(listScrollPane);
        var rightDP = (JDesktopPane) this.getComponent("rightDP");
        rightDP.moveToFront(listPanel);
        listPanel.setVisible(true);
        var viewHeadText = (JTextField) this.getComponent("viewHeadText");
        viewHeadText.setText(DEFAULT_HEAD_TEXT + "Flight-List"); // TODO: 21.05.2022 add text
        // revalidate window -> making the tree visible
        this.requestComponentFocus(this.listView);
    }

    /**
     *
     * @param flightTree is the flight tree to set
     * @param dpInfoTree is the @Nullable data point info tree
     */
    public void receiveInfoTree(@NotNull final JTree flightTree,
                                @Nullable final JTree dpInfoTree) {
        var infoPanel = this.getComponent("infoPanel");
        infoPanel.removeAll();
        int width = infoPanel.getWidth();
        int height = infoPanel.getHeight() / 2;
        this.getComponent("menuPanel").setVisible(false);
        flightTree.setBounds(0, 0, width, height + 50);
        flightTree.setMaximumSize(infoPanel.getSize());
        flightTree.setBorder(LINE_BORDER);
        flightTree.setFont(FONT_MENU.deriveFont(12f));
        infoPanel.add(flightTree);
        this.addContainer("flightInfoTree", flightTree);
        if (dpInfoTree != null) {
            this.receiveDataPointInfoTree(dpInfoTree, width, height);
        }
        var leftDP = (JDesktopPane) this.getComponent("leftDP");
        leftDP.moveToFront(infoPanel);
        infoPanel.setVisible(true);
    }

    private void receiveDataPointInfoTree(@NotNull JTree dpInfoTree, int width, int height) {
        dpInfoTree.setBounds(0, height + 50, width, height - 50);
        dpInfoTree.setBorder(LINE_BORDER);
        dpInfoTree.setFont(FONT_MENU.deriveFont(12f));
        this.getComponent("infoPanel").add(dpInfoTree);
        this.addContainer("dpInfoTree", dpInfoTree);
    }

    public void showSettings() {
        this.getComponent("settingsDialog").setVisible(true);
        JTextField settingsMaxLoadTxtField = (JTextField) this.getComponent("settingsMaxLoadTxtField");
        settingsMaxLoadTxtField.setCaretColor(Color.YELLOW);
        this.requestComponentFocus(settingsMaxLoadTxtField);
    }

    /**
     * starts a indeterminate progressBar
     */
    public void startProgressBar() {
        var progressBar = (JProgressBar) this.getComponent("progressBar");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Loading data...");
        progressBar.setStringPainted(true);
    }

    /**
     * sets the visibility of the progressBar
     *
     */
    public void stopProgressBar() {
        this.getComponent("progressBar").setVisible(false);
    }

    /**
     * revalidates all swing components
     */
    public void update() {
        this.getComponent("window").revalidate();
    }

    /**
     * requests focus for a specific component
     *
     * @param comp is the component that requests the focus
     */
    public void requestComponentFocus(JComponent comp) {
        comp.requestFocus();
    }

    /**
     * loads all search components for a certain search type
     *
     * @param type is the search type
     */
    public void loadSearch(SearchType type) {
        this.setCurrentSearchType(type);
        switch (type) {
            case FLIGHT -> this.showSearch(this.flightSearch);
            case PLANE -> this.showSearch(this.planeSearch);
            case AIRLINE -> this.showSearch(this.airlineSearch);
            case AIRPORT -> this.showSearch(this.airportSearch);
            // @deprecated, TODO remove
            case AREA -> this.showSearch(this.areaSearch);
        }
    }

    /**
     * sets every component from the given search visible
     *
     * @param search is the given list of search components
     */
    private void showSearch(List<JComponent> search) {
        var searchModels = this.allSearchModels();
        for (var comps : searchModels) {
            var equals = (comps == search);
            if (comps != null) {
                for (var c : comps) {
                    c.setVisible(equals);
                }
            }
        }
    }

    /**
     * disposes all views (and opens the src screen)
     * if no other view is opened, nothing is done
     */
    public synchronized void disposeView() {
        if (this.chartPanel != null) {
            var rightDP = (JDesktopPane) this.getComponent("rightDP");
            rightDP.remove(this.chartPanel);
        }
        if (this.hasContainer("startPanel")) {
            this.getComponent("startPanel").setVisible(false);
        } if (this.hasContainer("listView")) {
            final var listPanel = this.getComponent("listPanel");
            var listView = this.getComponent("listView");
            listPanel.remove(listView);
            listView.setVisible(false);
            listPanel.setVisible(false);
        } if (this.getMap() != null) {
            final var viewer = this.getMap();
            final var mapPanel = this.getComponent("mapPanel");
            viewer.removeAllMapMarkers();
            viewer.removeAllMapPolygons();
            viewer.setVisible(false);
            mapPanel.remove(viewer);
            mapPanel.setVisible(false);
        } if (this.hasContainer("flightInfoTree")) {
            var flightInfo = this.getComponent("flightInfoTree");
            flightInfo.setVisible(false);
            this.getComponent("infoPanel").setVisible(false);
        }
        var menuPanel = this.getComponent("menuPanel");
        menuPanel.setVisible(true);
        var leftDP = (JDesktopPane) this.getComponent("leftDP");
        leftDP.moveToFront(menuPanel);
        var viewHeadTxtLabel = (JLabel) this.getComponent("viewHeadTxtLabel");
        viewHeadTxtLabel.setText(DEFAULT_HEAD_TEXT); // TODO EXTRA methode
        this.setCurrentViewType(null);
        this.getMap().setHeatMap(null);
        //LiveMap.close();
        LiveLoader.setLive(false);
    }

    public void setViewHeadBtVisible(boolean b) {
        this.getComponent("fileButton").setVisible(b);
        this.getComponent("closeViewButton").setVisible(b);
    }

    public void setFileMenuVisible(boolean b) {
        assert this.fileMenu != null;
        for (var bt : this.fileMenu) {
            bt.setVisible(b);
        }
    }

    public String[] searchInput() throws IllegalInputException {
        SearchType currentSearchType = this.getCurrentSearchType();
        String[] inputFields = switch (currentSearchType) {
            case FLIGHT -> new String[] {
                    this.search_flightID.getText(),
                    this.search_callsign.getText()
            };
            case PLANE -> new String[] {
                    this.search_planeID.getText(),
                    this.search_planetype.getText(),
                    this.search_icao.getText(),
                    this.search_tailNr.getText()
            };
            case AIRPORT -> new String[] {
                    this.search_airpID.getText(),
                    this.search_airpTag.getText(),
                    this.search_airpName.getText()
            };
            case AIRLINE -> new String[] {
                    this.search_airlID.getText(),
                    this.search_airlTag.getText(),
                    this.search_airlName.getText(),
                    this.search_airlCountry.getText()
            };
            default -> null;
        };
        if (inputFields == null) {
            return null;
        }

        int length = inputFields.length;
        for (int i = 0; i < length; i++) {
            // checking strings for illegal characters
            // or expressions before returning them
            inputFields[i] = Utilities.checkString(inputFields[i]);
        }
        return inputFields;
    }

    public void clearSearch() {
        final String blank = "";
        this.allSearchModels().stream()
                .filter(Objects::nonNull)
                .forEach(models -> models.forEach(m -> {
                    if (m instanceof JTextField jtf) {
                        jtf.setText(blank);
                    }
                }));
    }

    @Nullable
    public File getSelectedFile() {
        JFileChooser fileChooser = MenuModels.fileLoader((JFrame) this.getComponent("window"));
        return fileChooser.getSelectedFile();
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

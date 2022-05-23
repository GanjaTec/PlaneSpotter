package planespotter.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import planespotter.constants.UserSettings;
import planespotter.controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.KeyEvent.VK_PAGE_DOWN;
import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.SearchType.*;
import static planespotter.constants.ViewType.*;

/**
 * @name GUIWorker
 * @author jml04
 * @version 1.0
 *
 * class GUIWorker contains methods to do some actions for the GUI (that the GUI class is not too full)
 */
public final class GUIAdapter {

    // only gui instance
    private final GUI gui;
    // controller instance
    private final Controller ctrl;

    /**
     * empty constructor
     */
    public GUIAdapter() {
        this.ctrl = Controller.getInstance();
        this.gui = this.ctrl.gui();
    }

    /**
     * sets the JMapViewer in mapViewer
     *
     * @param map is the map to be set
     */
    public void recieveMap (JMapViewer map, String text) {
        gui.mapViewer = map;
        // TODO: adding MapViewer to panel
        gui.pMap.add(gui.mapViewer);
        gui.viewHeadText.setText(DEFAULT_HEAD_TEXT + "Map-Viewer > " + text);
        // revalidating window frame to refresh everything
        gui.pMap.setVisible(true);
        gui.mapViewer.setVisible(true);
        this.requestComponentFocus(gui.mapViewer);
    }

    /**
     * sets the JTree in listView and makes it visible
     *
     * @param tree is the tree to set
     */
    public void recieveTree (JTree tree) {
        tree.setBounds(0, 0, gui.pList.getWidth(), gui.pList.getHeight());
        gui.listView = tree;
        gui.spList = gui.listScrollPane(gui.listView);
        gui.spList.setOpaque(false);
        gui.pList.add(gui.spList);
        gui.dpright.moveToFront(gui.pList);
        gui.pList.setVisible(true);
        gui.viewHeadText.setText(DEFAULT_HEAD_TEXT + "Flight-List"); // TODO: 21.05.2022 add text
        // revalidate window -> making the tree visible
        this.requestComponentFocus(gui.listView);
    }

    /**
     *
     * @param flightTree is the flight tree to set
     * @param dpInfoTree is the @Nullable data point info tree
     */
    public void recieveInfoTree(JTree flightTree, @Nullable JTree dpInfoTree) {
        int width = gui.pInfo.getWidth();
        int height = (gui.pInfo.getHeight() / 2);
        gui.pMenu.setVisible(false);
        gui.infoTree = flightTree;
        gui.infoTree.setBounds(0, 0, width, height + 50);
        gui.infoTree.setMaximumSize(gui.pInfo.getSize());
        gui.infoTree.setBorder(LINE_BORDER);
        gui.infoTree.setFont(FONT_MENU.deriveFont(12f));
        gui.pInfo.add(gui.infoTree);
        if (dpInfoTree != null) {
            this.recieveDataPointInfoTree(dpInfoTree, width, height);
        }
        gui.dpleft.moveToFront(gui.pInfo);
        gui.pInfo.setVisible(true);
    }

    private void recieveDataPointInfoTree(@NotNull JTree dpInfoTree, int width, int height) {
        gui.dpInfoTree = dpInfoTree;
        gui.dpInfoTree.setBounds(0, height + 50, width, height - 50);
        gui.dpInfoTree.setBorder(LINE_BORDER);
        gui.dpInfoTree.setFont(FONT_MENU.deriveFont(12f));
        gui.pInfo.add(gui.dpInfoTree);
    }

    /**
     * starts a indeterminate progressBar
     */
    public void startProgressBar() {
        this.stopProgressBar();
        gui.progressbar.setIndeterminate(true);
        gui.progressbar.setString("Loading data...");
        gui.progressbar.setStringPainted(true);
    }

    /**
     * sets the visibility of the progressBar
     *
     */
    public void stopProgressBar() {
        gui.progressbar.setVisible(false);
    }

    /**
     * revalidates all swing components
     */
    public void update() {
        gui.window.revalidate();
    }

    /**
     * requests focus for a specific component
     *
     * @param comp is the component that requests the focus
     */
    void requestComponentFocus(Component comp) {
        comp.requestFocus();
    }

    /**
     * loads all search components for a certain search type
     *
     * @param forItem is the search type
     */
    public void loadSearch(String forItem) {
        switch (forItem) {
            case "Flight" -> {
                this.showSearch(gui.flightSearch);
                ctrl.currentSearchType = FLIGHT;
            }
            case "Plane" -> {
                this.showSearch(gui.planeSearch);
                ctrl.currentSearchType = PLANE;
            }
            case "Airline" -> {
                this.showSearch(gui.airlineSearch);
                ctrl.currentSearchType = AIRLINE;
            }
            case "Airport" -> {
                this.showSearch(gui.airportSearch);
                ctrl.currentSearchType = AIRPORT;
            }
            case "Area" -> {
                this.showSearch(gui.areaSearch);
                ctrl.currentSearchType = AREA;
            }
        }
    }

    /**
     * sets every component from the given search visible
     *
     * @param search is the given list of search components
     */
    private void showSearch(List<JComponent> search) {
        var searchModels = gui.allSearchModels();
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
        if (gui.pStartScreen != null) {
            gui.pStartScreen.setVisible(false);
        } if (gui.listView != null) {
            gui.pList.remove(gui.listView);
            gui.listView.setVisible(false);
            gui.listView = null;
            gui.pList.setVisible(false);
        } if (this.mapViewer() != null) {
            this.mapViewer().removeAllMapMarkers();
            this.mapViewer().removeAllMapPolygons();
            gui.pMap.remove(this.mapViewer());
            this.mapViewer().setVisible(false);
            gui.pMap.setVisible(false);
            this.mapViewer().removeAllMapMarkers();
        } if (gui.infoTree != null) {
            gui.infoTree.setVisible(false);
            gui.infoTree = null;
            gui.pInfo.setVisible(false);
        }
        gui.pMenu.setVisible(true);
        gui.dpleft.moveToFront(gui.pMenu);
        gui.viewHeadText.setText(DEFAULT_HEAD_TEXT);
        ctrl.currentViewType = null;
        this.update();
    }

    void windowResized() {
        gui.mainpanel.setBounds(0, 0, gui.window.getWidth()-14, gui.window.getHeight()-37);
        gui.pTitle.setBounds(0, 0, gui.mainpanel.getWidth(), 70);

        int width = gui.pTitle.getWidth();
        int height = gui.pTitle.getHeight();
        gui.title_bground.setBounds(0, 0, width, height);
        var img = Images.TITLE.get();
        gui.title_bground.setIcon(this.scaledImage(img, width, height));

        gui.dpright.setBounds(280, 70, gui.mainpanel.getWidth() - 280, gui.mainpanel.getHeight() - 70);
        gui.dpleft.setBounds(0, 70, 280, gui.mainpanel.getHeight() - 70);

        gui.pViewHead.setBounds(0, 0, gui.dpright.getWidth(), 24);
        gui.pStartScreen.setBounds(0, 24, gui.dpright.getWidth(), gui.dpright.getHeight() - 24);
        gui.pList.setBounds(0, 24, gui.dpright.getWidth(), gui.dpright.getHeight() - 24);
        gui.pMap.setBounds(0, 24, gui.dpright.getWidth(), gui.dpright.getHeight() - 24);
        gui.pMenu.setBounds(0, 0, gui.dpleft.getWidth(), gui.dpleft.getHeight());
        gui.pInfo.setBounds(0, 0, gui.dpleft.getWidth(), gui.dpleft.getHeight());

        gui.bground.setBounds(0, 0, gui.dpright.getWidth(), gui.dpright.getHeight());
        gui.menu_bground.setBounds(0, 0, gui.dpleft.getWidth(), gui.dpleft.getHeight());

        gui.title_bground.setBounds(gui.pTitle.getBounds());
        gui.title.setBounds(gui.pTitle.getWidth() / 2 - 200, 0, 400, 70);
        gui.closeView.setBounds(gui.pViewHead.getWidth() - 85, 4, 80, 16);
        gui.btFile.setBounds(gui.pViewHead.getWidth() - 168, 4, 80, 16);
        gui.lblStartScreen.setBounds(0, 0, gui.pStartScreen.getWidth(), gui.pStartScreen.getHeight());
        gui.mapViewer.setBounds(0, 0, gui.pMap.getWidth(), gui.pMap.getHeight());
        gui.menubar.setBounds(gui.pMenu.getBounds());
        gui.tfSearch.setBounds(10, gui.menubar.getHeight() - 80, 255, 25);
        gui.searchButton.setBounds(10, gui.menubar.getHeight() - 40, 255, 25);
        if (gui.spList != null && gui.listView != null) {
            gui.spList.setBounds(0, 0, gui.pList.getWidth(), gui.pList.getHeight());
            gui.listView.setBounds(gui.spList.getBounds());
        }
        if (gui.infoTree != null) {
            gui.infoTree.setBounds(gui.pInfo.getBounds());
        }
        if (gui.fileMenu != null) {
            int minus = 84;
            var fileMenu = gui.fileMenu;
            for (var bt : fileMenu) {
                bt.setBounds(gui.pViewHead.getWidth() - minus, 4, 80, 16);
                minus += 84;
            }
        }
    }

    /**
     * executed when a button is clicked
     * 
     * @param button is the clicked button
     */
    public synchronized void buttonClicked(JButton button) {
        Controller.getScheduler().exec(() -> {
            if (button == gui.btFile) {
                this.setViewHeadBtVisible(false);
                this.setFileMenuVisible(true);
            } else if (button == gui.btList) {
                this.startProgressBar();
                this.ctrl.show(LIST_FLIGHT, "");
            } else if (button == gui.btMap) {
                this.startProgressBar();
                this.ctrl.show(MAP_ALL, "");
            } else if (button == gui.closeView) {
                this.disposeView();
                ctrl.loadedData = null;
                this.gui.pStartScreen.setVisible(true);
                this.gui.dpright.moveToFront(gui.pStartScreen);
            } else if (button == gui.settings) {
                this.gui.settingsDialog.setVisible(true);
                this.gui.settings_maxLoadTf.setCaretColor(Color.YELLOW);
                this.requestComponentFocus(this.gui.settings_maxLoadTf);
            } else if (button == gui.searchButton) {
                this.gui.pSearch.setVisible(!gui.pSearch.isVisible());
                this.gui.tfSearch.setVisible(!gui.pSearch.isVisible());
                this.loadSearch("Plane");
            } else if (button == gui.settingsButtons[0]) {
                this.gui.settingsDialog.setVisible(false);
            } else if (button == gui.settingsButtons[1]) {
                this.ctrl.confirmSettings(gui.settings_maxLoadTf.getText(), (String) gui.settings_mapTypeCmbBox.getSelectedItem());
                this.gui.settingsDialog.setVisible(false);
            } else if (button.getName().equals("loadList")) {
                // future
                this.ctrl.show(MAP_SIGNIFICANCE, "Significance Map");
            } else if (button.getName().equals("loadMap")) {
                // TODO search type abfragen, bzw. ComboBox SelectedItem
                var inputs = this.searchInput();
                this.ctrl.search(inputs, 1);
            } else if (button.getName().equals("open")) {
                Controller.getInstance().loadFile();
            } else if (button.getName().equals("save")) {
                Controller.getInstance().saveFile();
            } else if (button.getName().equals("back")) {
                this.setFileMenuVisible(false);
                this.setViewHeadBtVisible(true);
            }
        }, "GUI-Adapter", false, 5, true);
    }

    public void mapClicked(MouseEvent clickEvent) {
        int button = clickEvent.getButton();
        var ctrl = Controller.getInstance();
        if (button == MouseEvent.BUTTON1 && ctrl.currentViewType != null) {
            ctrl.mapClicked(clickEvent.getPoint());
        }
    }

    private void setViewHeadBtVisible(boolean b) {
        gui.btFile.setVisible(b);
        gui.closeView.setVisible(b);
    }

    private void setFileMenuVisible (boolean b) {
        var fileMenu = gui.fileMenu;
        for (var bt : fileMenu) {
            bt.setVisible(b);
        }
    }

    /**
     * executed when a key is pressed
     */
    void keyPressed (KeyEvent e) {
        var src = e.getSource();
        int key = e.getKeyCode();
        try {
            if (src == gui.tfSearch) {
                if (key == KeyEvent.VK_ENTER) {
                    if (gui.tfSearch.hasFocus())
                        new GUIAdapter().enterText();
                }
            } else if (src == gui.settings_maxLoadTf) {
                if (key == KeyEvent.VK_ENTER) {
                    // TODO fixen: settings fenster schließt erst nach loading
                    if (Integer.parseInt(gui.settings_maxLoadTf.getText()) >= 4) {
                        this.startProgressBar();
                        new UserSettings().setMaxLoadedData(Integer.parseInt(gui.settings_maxLoadTf.getText()));
                        gui.settings_maxLoadTf.setText("");
                        gui.settingsDialog.setVisible(false);
                    }
                }
            } else if (src == this.mapViewer()) {
                switch (key) {
                    case VK_PAGE_UP -> this.mapViewer().moveMap(0, 10);
                    case VK_HOME -> this.mapViewer().moveMap(-10, 0);
                    case VK_END -> this.mapViewer().moveMap(10, 0);
                    case VK_PAGE_DOWN -> this.mapViewer().moveMap(0, -10);
                }
            }
        } catch (NumberFormatException ex) {
                    gui.settings_maxLoadTf.setText("Error");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public JMapViewer mapViewer () {
        return gui.mapViewer;
    }

    private String[] searchInput () {
        switch (ctrl.currentSearchType) {
            case FLIGHT -> {
                return new String[] {
                        gui.search_flightID.getText(),
                        gui.search_callsign.getText()
                };
            } case PLANE -> {
                return new String[] {
                        gui.search_planeID.getText(),
                        gui.search_planetype.getText(),
                        gui.search_icao.getText(),
                        gui.search_tailNr.getText()
                };
            } case AIRPORT -> {
                return new String[] {
                        gui.search_airpID.getText(),
                        gui.search_airpTag.getText(),
                        gui.search_airpName.getText()
                };
            }
        }
        return null;
    }

    void clearSearch () {
        var comps = new JTextField[] {
                gui.search_planetype,
                gui.search_tailNr,
                gui.search_planeID,
                gui.search_callsign,
                gui.search_flightID,
                gui.search_icao
        };
        for (var c : comps) {
            c.setText("");
        }

    }

    /**
     * enters the text in the textfield (use for key listener)
     */
    private void enterText() {
        var text = gui.tfSearch.getText().toLowerCase();
        if (!text.isBlank()) {
            if (text.startsWith("exit")) {
                ctrl.exit();
            } else if (text.startsWith("loadlist")) {
                ctrl.show(LIST_FLIGHT, "");
            } else if (text.startsWith("loadmap")) {
                ctrl.show(MAP_ALL, "");
            } else if (text.startsWith("maxload")) {
                var args = text.split(" ");
                try {
                    int max = Integer.parseInt(args[1]);
                    if (max <= 10000) {
                        new UserSettings().setMaxLoadedData(max);
                        Controller.getLogger().log("maxload changed to " + args[1] + " !", gui);
                    } else {
                        Controller.getLogger().log("Failed! Maximum is 10000!", gui);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else if (text.startsWith("flightroute") || text.startsWith("fl")) {
                var args = text.split(" ");
                if (args.length > 1) {
                    var id = args[1];
                    ctrl.show(MAP_TRACKING, id);
                }
            }
        }
        gui.tfSearch.setText("");
    }

    public ImageIcon scaledImage(ImageIcon input, int width, int height) {
        var scaled = input.getImage().getScaledInstance(width, height, 4);
        return new ImageIcon(scaled);
    }

}

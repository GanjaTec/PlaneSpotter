package planespotter.display;

import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import planespotter.controller.Controller;
import planespotter.model.Utilities;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.KeyEvent.VK_PAGE_DOWN;
import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.SearchType.*;
import static planespotter.constants.ViewType.LIST_FLIGHT;
import static planespotter.constants.ViewType.MAP_ALL;

/**
 * @name GUIWorker
 * @author jml04
 * @version 1.0
 *
 * class GUIWorker contains methods to do some actions for the GUI (that the GUI class is not too full)
 */
public final class GUISlave {

    // only gui instance
    private final GUI gui;
    // controller instance
    private final Controller controller;

    /**
     * empty constructor
     */
    public GUISlave() {
        this.controller = Controller.getInstance();
        this.gui = this.controller.gui();
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
        this.revalidateAll();
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
        // TODO: adding list scrollpane to list pane
        gui.pList.add(gui.spList);
        gui.dpright.moveToFront(gui.pList);
        gui.pList.setVisible(true);
        gui.viewHeadText.setText(DEFAULT_HEAD_TEXT + "Flight-List");
        // revalidate window -> making the tree visible
        this.revalidateAll();
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
            gui.dpInfoTree = dpInfoTree;
            gui.dpInfoTree.setBounds(0, height + 50, width, height - 50);
            gui.dpInfoTree.setBorder(LINE_BORDER);
            gui.dpInfoTree.setFont(FONT_MENU.deriveFont(12f));
            gui.pInfo.add(gui.dpInfoTree);
        }
        gui.dpleft.moveToFront(gui.pInfo);
        gui.pInfo.setVisible(true);
        this.revalidateAll();
    }

    /**
     * starts a indeterminate progressBar
     */
    public void progressbarStart () {
        this.progressbarVisible(true);
        gui.progressbar.setIndeterminate(true);
        gui.progressbar.setString("Loading data...");
        gui.progressbar.setStringPainted(true);
    }

    /**
     * sets the visibility of the progressBar
     *
     * @param v is the visible-boolean
     */
    public void progressbarVisible (boolean v) {
        gui.progressbar.setVisible(v);
        this.revalidateAll();
    }

    /**
     * revalidates all swing components
     */
    public void revalidateAll () {
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
    public void loadSearch (String forItem) {
        switch (forItem) {
            case "Flight" -> {
                this.showSearch(gui.flightSearch);
                controller.currentSearchType = FLIGHT;
            }
            case "Plane" -> {
                this.showSearch(gui.planeSearch);
                controller.currentSearchType = PLANE;
            }
            case "Airline" -> {
                this.showSearch(gui.airlineSearch);
                controller.currentSearchType = AIRLINE;
            }
            case "Airport" -> {
                this.showSearch(gui.airportSearch);
                controller.currentSearchType = AIRPORT;
            }
            case "Area" -> {
                this.showSearch(gui.areaSearch);
                controller.currentSearchType = AREA;
            }
        }
    }

    /**
     * sets every component from the given search visible
     *
     * @param search is the given list of search components
     */
    private void showSearch (List<JComponent> search) {
        for (var comps : gui.allSearchModels()) {
            var equals = (comps == search);
            if (comps != null) {
                for (JComponent c : comps) {
                    c.setVisible(equals);
                }
            }
        }
    }

    /**
     * disposes all views (and opens the start screen)
     * if no other view is opened, nothing is done
     */
    public void disposeView() {
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
            gui.pMap.remove(gui.mapViewer);
            gui.mapViewer.setVisible(false);
            gui.pMap.setVisible(false);
            gui.mapViewer.removeAllMapMarkers();
        } if (gui.infoTree != null) {
            gui.infoTree.setVisible(false);
            gui.infoTree = null;
            gui.pInfo.setVisible(false);
        }
        gui.pMenu.setVisible(true);
        gui.dpleft.moveToFront(gui.pMenu);
        gui.viewHeadText.setText(DEFAULT_HEAD_TEXT);
        BlackBeardsNavigator.currentViewType = null;
        this.revalidateAll();
    }

    void windowResized () {
        gui.mainpanel.setBounds(0, 0, gui.window.getWidth()-14, gui.window.getHeight()-37);
        gui.pTitle.setBounds(0, 0, gui.mainpanel.getWidth(), 70);

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
            for (var bt : gui.fileMenu) {
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
    public void buttonClicked (JButton button) {
        if (button == gui.btFile) {
            this.setViewHeadBtVisible(false);
            this.setFileMenuVisible(true);
        } else if (button == gui.btList) {
            this.progressbarStart();
            this.controller.show(LIST_FLIGHT, "");
        } else if (button == gui.btMap) {
            this.progressbarStart();
            this.controller.show(MAP_ALL, "");
        } else if (button == gui.closeView) {
            this.disposeView();
            var ctrl = Controller.getInstance(); // muss das sein
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
            gui.settingsDialog.setVisible(false);
        } else if (button == gui.settingsButtons[1]) {
            new UserSettings().confirm(gui.settings_maxLoadTf.getText(), (String) gui.settings_mapTypeCmbBox.getSelectedItem());
            gui.settingsDialog.setVisible(false);
        } else if (button.getName().equals("loadList")) {
            // future
        } else if (button.getName().equals("loadMap")) {
            // TODO search type abfragen, bzw. ComboBox SelectedItem
            var inputs = this.searchInput();
            this.controller.search(inputs, 1);
        } else if (button.getName().equals("open")) {
            try {
                new MenuModels().fileLoader(gui.window);
            } catch (DataNotFoundException e) {
                this.controller.getLogger().errorLog(e.getMessage(), this);
            }
        } else if (button.getName().equals("save")) {
            new MenuModels().fileSaver(gui.window);
        } else if (button.getName().equals("back")) {
            this.setFileMenuVisible(false);
            this.setViewHeadBtVisible(true);
        }
    }

    private void setViewHeadBtVisible (boolean b) {
        gui.btFile.setVisible(b);
        gui.closeView.setVisible(b);
    }

    private void setFileMenuVisible (boolean b) {
        for (var bt : gui.fileMenu) {
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
                        gui.enterText();
                }
            } else if (src == gui.settings_maxLoadTf) {
                if (key == KeyEvent.VK_ENTER) {
                    // TODO fixen: settings fenster schließt erst nach loading
                    if (Integer.parseInt(gui.settings_maxLoadTf.getText()) >= 4) {
                        this.progressbarStart();
                        new UserSettings().setMaxLoadedData(Integer.parseInt(gui.settings_maxLoadTf.getText()));
                        gui.settings_maxLoadTf.setText("");
                        gui.settingsDialog.setVisible(false);
                        // work with background worker?
                        //controller.loadLiveData();
                    }
                }
            } else if (src == gui.mapViewer) {
                switch (key) {
                    case VK_PAGE_UP -> gui.mapViewer.moveMap(0, 2);
                    case VK_HOME -> gui.mapViewer.moveMap(-2, 0);
                    case VK_END -> gui.mapViewer.moveMap(2, 0);
                    case VK_PAGE_DOWN -> gui.mapViewer.moveMap(0, -2);
                }
                this.revalidateAll();
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
        switch (controller.currentSearchType) {
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

}

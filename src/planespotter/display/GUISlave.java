package planespotter.display;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import planespotter.controller.Controller;
import planespotter.model.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.KeyEvent.VK_PAGE_DOWN;
import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.GUIConstants.FONT_MENU;
import static planespotter.constants.SearchType.FLIGHT;
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
    private static GUI gui;
    private static int currentFileChooser;

    /**
     * empty constructor
     */
    public GUISlave() {
    }

    /**
     * initializes GUISlave class
     */
    public static void initialize () {
        gui = Controller.gui();
    }

    /**
     * this method is executed when pre-loading is done
     */
    public static void donePreLoading () {
        Utilities.playSound(SOUND_DEFAULT);
        gui.loadingScreen.setVisible(false);
        gui.window.setVisible(true);
        gui.window.requestFocus();
    }

    /**
     * sets the JMapViewer in mapViewer
     *
     * @param map is the map to be set
     */
    public static void recieveMap (JMapViewer map) {
        gui.mapViewer = map;
        // TODO: adding MapViewer to panel
        gui.pMap.add(gui.mapViewer);
        gui.viewHeadText.setText(DEFAULT_HEAD_TEXT + "Map-Viewer > Live-Map");
        // revalidating window frame to refresh everything
        gui.pMap.setVisible(true);
        gui.mapViewer.setVisible(true);
        GUISlave.requestComponentFocus(gui.mapViewer);
        GUISlave.revalidateAll();
    }

    /**
     * sets the JTree in listView and makes it visible
     *
     * @param tree is the tree to set
     */
    public static void recieveTree (JTree tree) {
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
        GUISlave.revalidateAll();
        GUISlave.requestComponentFocus(gui.listView);
    }

    /**
     *
     * @param tree is the tree to set
     */
    public static void recieveInfoTree(JTree tree) {
        gui.pMenu.setVisible(false);
        gui.infoTree = tree;
        gui.infoTree.setBounds(gui.pInfo.getBounds());
        gui.infoTree.setMaximumSize(gui.pInfo.getSize());
        gui.infoTree.setBorder(LINE_BORDER);
        gui.infoTree.setFont(FONT_MENU.deriveFont(12f));
        gui.pInfo.add(gui.infoTree);
        gui.dpleft.moveToFront(gui.pInfo);
        gui.pInfo.setVisible(true);
        GUISlave.revalidateAll();
    }

    /**
     * starts a indeterminate progressBar
     */
    public static void progressbarStart () {
        GUISlave.progressbarVisible(true);
        gui.progressbar.setIndeterminate(true);
        gui.progressbar.setString("Loading data...");
        gui.progressbar.setStringPainted(true);
    }

    /**
     * sets the visibility of the progressBar
     *
     * @param v is the visible-boolean
     */
    public static void progressbarVisible (boolean v) {
        gui.progressbar.setVisible(v);
        GUISlave.revalidateAll();
    }

    /**
     * revalidates all swing components
     */
    public static void revalidateAll () {
        gui.window.revalidate();
    }

    /**
     * requests focus for a specific component
     *
     * @param comp is the component that requests the focus
     */
    static void requestComponentFocus(Component comp) {
        comp.requestFocus();
    }

    /**
     * loads all search components for a certain search type
     *
     * @param forItem is the search type
     */
    public static void loadSearch (String forItem) {
        switch (forItem) {
            case "Flight" -> GUISlave.showSearch(gui.flightSearch);
            case "Plane" -> GUISlave.showSearch(gui.planeSearch);
            case "Airline" -> GUISlave.showSearch(gui.airlineSearch);
            case "Airport" -> GUISlave.showSearch(gui.airportSearch);
            case "Area" -> GUISlave.showSearch(gui.areaSearch);
        }
    }

    /**
     * sets every component from the given search visible
     *
     * @param search is the given list of search components
     */
    private static void showSearch (List<JComponent> search) {
        var allSearchComps = new ArrayList<List<JComponent>>();
        allSearchComps.add(gui.flightSearch);
        allSearchComps.add(gui.planeSearch);
        allSearchComps.add(gui.airlineSearch);
        allSearchComps.add(gui.airportSearch);
        allSearchComps.add(gui.areaSearch);
        for (var comps : allSearchComps) {
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
    public static void disposeView() {
        if (gui.pStartScreen != null) {
            gui.pStartScreen.setVisible(false);
        } if (gui.listView != null) {
            gui.pList.remove(gui.listView);
            gui.listView.setVisible(false);
            gui.listView = null;
            gui.pList.setVisible(false);
        } if (gui.mapViewer != null) {
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
        GUISlave.revalidateAll();
        GUISlave.requestComponentFocus(gui.tfSearch);
    }

    static void windowResized () {
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
    }

    /**
     * executed when a button is clicked
     * 
     * @param button is the clicked button
     */
    public static void buttonClicked (JButton button) {
        if (button == gui.btFile) {
            MenuModels.fileSaver(gui.window);
            GUISlave.currentFileChooser++;
        } else if (button == gui.btList) {
            GUISlave.progressbarStart();
            gui.controller.createDataView(LIST_FLIGHT, "");
        } else if (button == gui.btMap) {
            GUISlave.progressbarStart();
            gui.controller.createDataView(MAP_ALL, "");
        } else if (button == gui.closeView) {
            GUISlave.disposeView();
            gui.pStartScreen.setVisible(true);
            gui.dpright.moveToFront(gui.pStartScreen);
        } else if (button == gui.settings) {
            gui.settings_intlFrame.show();
            gui.settings_iFrame_maxLoad.setCaretColor(Color.YELLOW);
            gui.settings_iFrame_maxLoad.setCaretPosition(gui.settings_iFrame_maxLoad.getText().length());
            gui.settings_iFrame_maxLoad.grabFocus();
        } else if (button == gui.searchButton) {
            gui.pSearch.setVisible(!gui.pSearch.isVisible());
            gui.tfSearch.setVisible(!gui.pSearch.isVisible());
        } else if (button.getName().equals("loadList")) {
            // future
        } else if (button.getName().equals("loadMap")) {
            // TODO search type abfragen, bzw. ComboBox SelectedItem
            String[] inputs = {
                    gui.search_flightID.getText(),
                    gui.search_callsign.getText()
            };
            gui.controller.search(FLIGHT, inputs);
        }
    }

    /**
     * executed when a key is pressed
     */
    static void keyPressed (KeyEvent e) {
        var src = e.getSource();
        int key = e.getKeyCode();
        try {
            if (src == gui.tfSearch) {
                if (key == KeyEvent.VK_ENTER) {
                    if (gui.tfSearch.hasFocus())
                        gui.enterText();
                }
            } else if (src == gui.settings_iFrame_maxLoad) {
                if (key == KeyEvent.VK_ENTER) {
                    // TODO fixen: settings fenster schließt erst nach loading
                    if (Integer.parseInt(gui.settings_iFrame_maxLoad.getText()) >= 4) {
                        GUISlave.progressbarStart();
                        UserSettings.setMaxLoadedFlights(Integer.parseInt(gui.settings_iFrame_maxLoad.getText()));
                        gui.settings_iFrame_maxLoad.setText("");
                        gui.settings_intlFrame.setVisible(false);
                        // work with background worker?
                        gui.controller.loadData();
                    }
                }
            } else if (src == gui.mapViewer) {
                switch (key) {
                    case VK_PAGE_UP -> gui.mapViewer.moveMap(0, 2);
                    case VK_HOME -> gui.mapViewer.moveMap(-2, 0);
                    case VK_END -> gui.mapViewer.moveMap(2, 0);
                    case VK_PAGE_DOWN -> gui.mapViewer.moveMap(0, -2);
                }
                GUISlave.revalidateAll();
            }
        } catch (NumberFormatException ex) {
                    gui.settings_iFrame_maxLoad.setText("Error");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static JMapViewer mapViewer () {
        return gui.mapViewer;
    }

}

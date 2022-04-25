package planespotter.display;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import planespotter.constants.ComponentType;
import planespotter.controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.GUIConstants.FONT_MENU;

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
        GUISlave.playSound(SOUND_DEFAULT);
        gui.loadingScreen.setVisible(false);
        gui.window.setVisible(true);
        gui.window.requestFocus();
    }

    /**
     * plays a sound from the default toolkit
     * @param sound is the sound to be played (see: GUIConstants)
     *              TODO move to EventWizard
     */
    private static void playSound (String sound) {
        var sound2 = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty(sound);
        if (sound2 != null) sound2.run();
    }

    /**
     * sets the JMapViewer in mapViewer
     *
     * @param map is the map to be set
     */
    static void recieveMap (JMapViewer map) {
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
    static void recieveTree (JTree tree) {
        gui.listView = tree;
        gui.spList = gui.listScrollPane(gui.listView);
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
    static void recieveInfoTree(JTree tree) {
        gui.flightInfo = tree;
        gui.flightInfo.setBounds(gui.pInfo.getBounds());
        gui.flightInfo.setMaximumSize(gui.pInfo.getSize());
        gui.flightInfo.setBorder(LINE_BORDER);
        gui.flightInfo.setFont(FONT_MENU.deriveFont(12f));
        gui.pInfo.add(gui.flightInfo);
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

    static void windowResized (ComponentType type) {
        switch (type) {
            case WINDOW -> gui.mainpanel.setBounds(gui.window.getBounds());
            case MAP_PANEL -> gui.mapViewer.setBounds(gui.pMap.getBounds());
            case MENU_PANEL -> gui.menubar.setBounds(gui.pMenu.getBounds());
            case VIEW_HEAD -> gui.closeView.setBounds(gui.pViewHead.getWidth() - 85, 4, 80, 16);
            case START_SCREEN -> gui.lblStartScreen.setBounds(gui.pStartScreen.getBounds());
            case MAINPANEL -> {
                gui.pTitle.setBounds(0, 0, gui.mainpanel.getWidth(), 70);
                gui.dpright.setBounds(280, 70, gui.mainpanel.getWidth() - 280, gui.mainpanel.getHeight() - 70);
                gui.dpleft.setBounds(0, 70, 280, gui.mainpanel.getHeight() - 70);
            }
            case DPLEFT -> {
                gui.pMenu.setBounds(0, 0, gui.dpleft.getWidth(), gui.dpleft.getHeight());
                gui.pInfo.setBounds(0, 0, gui.dpleft.getWidth(), gui.dpleft.getHeight());
            }
            case DPRIGHT -> {
                gui.pViewHead.setBounds(0, 0, gui.dpright.getWidth(), 24);
                gui.pList.setBounds(0, 24, gui.dpright.getWidth(), gui.dpright.getHeight() - 24);
                gui.pMap.setBounds(0, 24, gui.dpright.getWidth(), gui.dpright.getHeight() - 24);
                gui.pStartScreen.setBounds(0, 24, gui.dpright.getWidth(), gui.dpright.getHeight() - 24);
            }
            case TITLE_PANEL -> {
                gui.title_bground.setBounds(gui.pTitle.getBounds());
                gui.title.setBounds(gui.pTitle.getWidth() / 2 - 200, 0, 400, 70);
            }
            case MENUBAR -> {
                gui.tfSearch.setBounds(10, gui.menubar.getHeight() - 80, 255, 25);
                gui.searchFilter.setBounds(10, gui.menubar.getHeight() - 40, 255, 25);
            }
            case LIST_PANEL -> {
                if (gui.spList != null && gui.listView != null) {
                    gui.spList.setBounds(gui.pList.getBounds());
                    gui.listView.setBounds(gui.pList.getBounds());
                }
            }
            case INFO_PANEL -> {
                if (gui.flightInfo != null) {
                    gui.flightInfo.setBounds(gui.pInfo.getBounds());
                }
            }
        }
        GUISlave.revalidateAll();
    }

}

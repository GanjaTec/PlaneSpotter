package planespotter.controller;

import org.jetbrains.annotations.NotNull;

import planespotter.constants.*;
import planespotter.display.Diagrams;
import planespotter.display.GUI;
import planespotter.display.TreasureMap;
import planespotter.display.UserInterface;
import planespotter.display.models.InfoPane;
import planespotter.display.models.LayerPane;
import planespotter.display.models.SearchPane;
import planespotter.display.models.SettingsPane;
import planespotter.model.nio.LiveLoader;
import planespotter.statistics.Statistics;
import planespotter.throwables.IllegalInputException;
import planespotter.util.Utilities;
import planespotter.util.math.MathUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static java.awt.event.KeyEvent.*;
import static planespotter.constants.ViewType.MAP_LIVE;

/**
 * @name ActionHandler
 * @author jml04
 * @author Bennet
 * @version 1.0
 *
 * record ActionHandler handles GUI-User-Interactions and does further action
 * @see GUI
 * @see Controller
 * @see Scheduler
 */
public final class ActionHandler implements ActionListener, KeyListener,
        ComponentListener, MouseListener,
        ItemListener, WindowListener {

    private static final ActionHandler INSTANCE;
    private static final KeyEventDispatcher globalEventDispatcher;

    static {
        INSTANCE = new ActionHandler();
        globalEventDispatcher = initGlobalEventDispatcher();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(globalEventDispatcher);
    }

    private static KeyEventDispatcher initGlobalEventDispatcher() {
        return keyEvent -> {
            GUI gui;
            JButton searchButton;
            int eventID = keyEvent.getID();
            if (eventID == KEY_PRESSED) {
                // search hotkey
                if (keyEvent.isShiftDown() && keyEvent.getKeyCode() == VK_S) {
                    /*gui = Controller.getInstance().getGUI();
                    searchButton = (JButton) gui.getComponent("searchButton");
                    getActionHandler().buttonClicked(searchButton, Controller.getInstance(), gui);*/
                    getActionHandler().menuClicked(new JMenu("Search"));
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * @return main ActionHandler
     */
    public static ActionHandler getActionHandler() {
        return INSTANCE;
    }

    // ActionHandler hash code
    private final int hashCode = System.identityHashCode(this);

    /**
     * private ActionHandler constructor for main instance
     */
    private ActionHandler() {
        // nothing to do
    }

    /**
     * executed when a button is clicked,
     * executes certain actions for different
     * buttons (in a new thread)
     *
     * @param button is the clicked button
     * @param ctrl is the main controller instance
     * @param ui is the main GUI instance
     */
    public synchronized void buttonClicked(JButton button, Controller ctrl, UserInterface ui) {
        ctrl.getScheduler().exec(() -> {

            if (button.getText().equals("Cancel")) {
                ui.getSettings().setVisible(false);

            } else if (button.getText().equals("Confirm")) {
                SettingsPane settings = ui.getSettings();
                ctrl.confirmSettings(settings.getValues());
                settings.setVisible(false);

            } else if (button.getName().equals("loadList")) {
                // future
                ctrl.show(ViewType.MAP_HEATMAP);

            } else if (button.getName().equals("loadMap")) {
                try {
                    //var inputs = gui.searchInput();
                    String[] inputs;
                    if ((inputs = ctrl.getUI().getSearchPanel().searchInput()) != null) {
                        ctrl.search(inputs, 1);
                    }
                } catch (IllegalInputException e) {
                    ctrl.handleException(e);
                }
            }/* else if (button.getName().equals("open")) {
                Controller.getInstance().loadFile();

            } else if (button.getName().equals("save")) {
                Controller.getInstance().saveFile();

            } else if (button.getName().equals("back")) {
                ui.setFileMenuVisible(false);
                ui.setViewHeadBtVisible(true);

            }*/
        }, "Action Handler", false, Scheduler.MID_PRIO, true);
    }

    /**
     * executed when the map is clicked,
     * used for showing information about flights
     *
     * @param clickEvent is the MouseEvent, produced by the click
     * @param ctrl is the Controller instance (given by Controller.getInstance())
     * @param ui is the {@link UserInterface} instance (given by Controller.getUI())
     */
    public synchronized void mapClicked(MouseEvent clickEvent, Controller ctrl, UserInterface ui) {
        // TODO evtl threaded
        int button = clickEvent.getButton();
        if (button == MouseEvent.BUTTON1) {
            var clicked = ui.getMap().getPosition(clickEvent.getPoint());
            boolean hit = switch (ui.getCurrentViewType()) {
                case MAP_LIVE -> ctrl.onLiveClick(clicked);
                case MAP_FROMSEARCH -> ctrl.onClick_all(clicked);
                case MAP_TRACKING -> ctrl.onTrackingClick(clicked);
                default -> false;
            };
            if (!hit) {
                ui.getLayerPane().removeTop();
            }
        }
    }

    /**
     * executed when a key is entered on a certain component
     *
     * @param event is the KeyEvent
     * @param ui is the GUI instance
     */
    public void keyEntered(KeyEvent event, UserInterface ui) {
        var source = event.getSource();
        int key = event.getKeyCode();
        try {
            /*if (source == gui.getComponent("settingsMaxLoadTxtField")) {
                if (key == VK_ENTER) {
                    var settingsMaxLoadTxtField = (JTextField) gui.getComponent("settingsMaxLoadTxtField");
                    // TODO fixen: settings fenster schließt erst nach loading
                    int newMax = Integer.parseInt(settingsMaxLoadTxtField.getText());
                    if (newMax > 0) {
                        gui.startProgressBar();
                        UserSettings.setMaxLoadedData(newMax);
                        settingsMaxLoadTxtField.setText("");
                        gui.getComponent("settingsDialog").setVisible(false);
                    } else {
                        settingsMaxLoadTxtField.setText("[x > 0]");
                    }
                }
            } else*/ if (source instanceof JTextField && key == VK_ENTER) {
                var jButton = new JButton();
                jButton.setName("loadMap");
                this.buttonClicked(jButton, Controller.getInstance(), ui);
            } else /*if (source == gui.getComponent("window"))*/ {
                final var viewer = ui.getMap();
                switch (key) { // FIXME läuft noch nicht
                    case VK_PAGE_UP -> viewer.moveMap(0, 10);
                    case VK_HOME -> viewer.moveMap(-10, 0);
                    case VK_END -> viewer.moveMap(10, 0);
                    case VK_PAGE_DOWN -> viewer.moveMap(0, -10);
                }
            }
        } catch (NumberFormatException ex) {
            System.err.println("NumberFormatException in ActionHandler.keyEntered");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * executed when the window is resized
     *
     * @param ui is the GUI instance that owns all components
     */
    public void windowResized(UserInterface ui) {
        JFrame window = ui.getWindow();
        LayerPane layerPane = ui.getLayerPane();
        TreasureMap map = ui.getMap();

        layerPane.setBounds(0, 0, window.getWidth(), window.getHeight());
        map.setBounds(layerPane.getBounds());
        Component top = layerPane.getTop();
        if (top instanceof SearchPane) {
            top.setBounds(10, 10, 250, layerPane.getHeight());
        } else if (top instanceof InfoPane ip) {
            top.setBounds(0, 0, 270, layerPane.getHeight());
            ip.getInfoList().setBounds(10, 10, 250, MathUtils.divide(layerPane.getHeight(), 2));
        }

    }

    /**
     *
     *
     * @param source is the source component, on which the change occurred
     * @param item is the item String
     * @param ui is the GUI instance
     */
    private void itemChanged(Object source, String item, UserInterface ui) {
        SearchPane searchPanel = ui.getSearchPanel();
        if (source == searchPanel.getSearchCmbBox()) {
            searchPanel.clearSearch();
            ui.showSearch(SearchType.byItemString(item));
        }/* else if (source == ui.getComponent("settingsMapTypeCmbBox")) {
            switch (item) {
                case "Bing Map" -> UserSettings.setCurrentMapSource(UserSettings.BING_MAP);
                case "Default Map" -> UserSettings.setCurrentMapSource(UserSettings.DEFAULT_MAP);
                case "Transport Map" -> UserSettings.setCurrentMapSource(UserSettings.TRANSPORT_MAP);
            }
        }*/
    }

    /**
     * overwritten actionPerformed() method is executed when an action is performed by
     * a component which has this ActionHandler as its ActionListener
     *
     * @param e is the ActionEvent which is performed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        var src = e.getSource();
        if (src instanceof JButton bt) {
            var ctrl = Controller.getInstance();
            var ui = ctrl.getUI();
            if (e.getID() != KEY_TYPED) {
                this.buttonClicked(bt, ctrl, ui);
            }
        }
    }

    /**
     * MouseListener-method,
     * executed when the mouse is pressed
     *
     * @param e is the MouseEvent which contains the button
     */
    @Override
    public void mousePressed(@NotNull MouseEvent e) {
        Controller ctrl = Controller.getInstance();
        UserInterface ui = ctrl.getUI();
        Component component = e.getComponent();
        if (component == ui.getMap()) {
            this.mapClicked(e, ctrl, ui);
        } else if (component instanceof JMenu menu) {
            this.menuClicked(menu);
        } else if (component instanceof JMenuItem item) {
            this.menuItemClicked(ctrl, item);
        }
    }

    private void menuItemClicked(@NotNull Controller ctrl, @NotNull JMenuItem item) {
        final String text = item.getText();
        final UserInterface ui = ctrl.getUI();
        switch (text) {
            case "Open" -> ctrl.loadFile();
            case "Save As" -> ctrl.saveFile();
            case "Exit" -> ctrl.shutdown(false);
            case "Fr24-Supplier" -> ctrl.runFr24Collector();
            case "Top-Airports" -> Diagrams.showTopAirports(ui, new Statistics());
            case "Top-Airlines" -> Diagrams.showTopAirlines(ui, new Statistics());

            case "ADSB-Supplier", "Antenna", "Position-HeatMap" -> ui.showWarning(Warning.NOT_SUPPORTED_YET);
        }
    }

    public void menuClicked(@NotNull JMenu menu) {
        String text = menu.getText();
        Controller ctrl = Controller.getInstance();
        UserInterface ui = ctrl.getUI();
        switch (text) {
            case "Live-Map" -> ctrl.show(MAP_LIVE);
            case "Search" -> ui.showSearch(SearchType.FLIGHT);
            case "Settings" -> ui.showSettings();
            case "Close View" -> {
                ctrl.loadedData = null;
                LiveLoader.setLive(false);
                ui.getMapManager().clearMap();
                LayerPane layerPane = ui.getLayerPane();
                layerPane.removeTop();
                layerPane.setBottomDefault();
            }
        }
    }

    /**
     * KeyListener-method,
     * executed when a key is pressed
     *
     * @param e is the Key event which contains the Key-Code and Key-Char
     */
    @Override
    public void keyPressed(KeyEvent e) {
        var ui = Controller.getInstance().getUI();
        this.keyEntered(e, ui);
    }

    /**
     * ComponentListener-method,
     * executed when a Component is resized
     *
     * @param e is the ComponentEvent
     */
    @Override
    public void componentResized(ComponentEvent e) {
        UserInterface ui = Controller.getInstance().getUI();
        this.windowResized(ui);
    }

    /**
     * ComponentListener-method,
     * executed when a Component is shown
     *
     * @param e is the ComponentEvent
     */
    @Override
    public void componentShown(ComponentEvent e) {
        this.componentResized(e);
    }

    /**
     * ItemListener-method,
     * executed when a JComboBox-item is changed
     *
     * @param e is the ItemEvent
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        var src = e.getSource();
        var item = (String) e.getItem();
        var ui = Controller.getInstance().getUI();
        this.itemChanged(src, item, ui);
    }

    /**
     * WindowListener-method,
     * executed on window-closing (X - button)
     *
     * @param e is the WindowEvent
     */
    @Override
    public void windowClosing(WindowEvent e) {
        Controller.getInstance().shutdown(false);
    }

    /*
     * the following listener-methods are unused
     */

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}
    @Override public void componentMoved(ComponentEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void windowOpened(WindowEvent e) {}
    @Override public void windowClosed(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "ActionHandler";
    }
}

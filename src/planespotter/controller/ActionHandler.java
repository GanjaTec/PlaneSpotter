package planespotter.controller;

import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import planespotter.constants.*;
import planespotter.dataclasses.Hotkey;
import planespotter.display.Diagrams;
import planespotter.model.Scheduler;
import planespotter.display.TreasureMap;
import planespotter.display.UserInterface;
import planespotter.display.models.InfoPane;
import planespotter.display.models.LayerPane;
import planespotter.display.models.SearchPane;
import planespotter.display.models.SettingsPane;
import planespotter.statistics.Statistics;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.awt.event.KeyEvent.*;
import static planespotter.constants.ViewType.MAP_LIVE;

/**
 * @name ActionHandler
 * @author jml04
 * @author Bennet
 * @version 1.0
 *
 * record ActionHandler handles user interactions in the UI and does further action
 * @see planespotter.display.UserInterface
 * @see planespotter.controller.Controller
 * @see planespotter.model.Scheduler
 */
public abstract class ActionHandler
        implements  ActionListener, KeyListener,
                    ComponentListener, MouseListener,
                    ItemListener, WindowListener {

    // ActionHandler instance, we need only one instance here,
    // because we don't want parallel listeners who listen to the same actions
    @NotNull private static final ActionHandler INSTANCE;

    // global event dispatcher, events come here before going to the components listeners,
    // used for global hotkeys
    @NotNull private static final KeyEventDispatcher GLOBAL_EVENT_DISPATCHER;

    // map for all global hotkeys,
    // key is the Hotkey object with the key data
    // value is the (Runnable) action for the specific hotkey
    @NotNull private static final Map<Hotkey, Runnable> HOTKEYS;

    // initializing all static members
    static {
        INSTANCE = new ActionHandler() {};

        GLOBAL_EVENT_DISPATCHER = initGlobalEventDispatcher();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(GLOBAL_EVENT_DISPATCHER);

        HOTKEYS = new HashMap<>(1);
        initHotkeys(null);
    }

    // ActionHandler hash code
    private final int hashCode = System.identityHashCode(this);


    // instance //

    /**
     * private ActionHandler constructor for main instance
     */
    private ActionHandler() {
        // nothing to do
    }

    /**
     * hot key event dispatcher,
     * listens to global key events
     *
     * @return {@link KeyEventDispatcher}, which listens to global hotkeys
     */
    @NotNull
    private static KeyEventDispatcher initGlobalEventDispatcher() {
        return keyEvent -> {
            int eventID = keyEvent.getID();
            if (eventID == KEY_PRESSED) {
                // getting pressed key
                int keyCode = keyEvent.getKeyCode();
                boolean shiftDown = keyEvent.isShiftDown(),
                        ctrlDown = keyEvent.isControlDown(),
                        altDown = keyEvent.isAltDown();
                Hotkey hotkey = new Hotkey(keyCode, shiftDown, ctrlDown, altDown);
                Runnable action;
                // checking hotkeys
                Set<Hotkey> hotkeys = HOTKEYS.keySet();
                for (Hotkey key : hotkeys) {
                    if (key.equals(hotkey)) {
                        action = HOTKEYS.get(key);
                        action.run();
                        return true;
                    }
                }
            }
            return false;
        };
    }

    /**
     * initializes all hotkeys with a Hotkey object (key)
     * and a Runnable action (value)
     *
     * @param additionals are additional {@link Hotkey}s, paired with actions (in a {@link Map}) that should be initialized
     */
    private static void initHotkeys(@Nullable Map<Hotkey, Runnable> additionals) {
        Runnable shiftSAction = () -> getActionHandler().menuClicked(new JMenu("Search"));

        // shift S / default search hotkey
        HOTKEYS.put(new Hotkey(VK_S, true, false, false), shiftSAction);

        if (additionals == null) {
            return;
        }
        // variable hotkeys
        Hotkey key;
        Runnable value;
        for (Map.Entry<Hotkey, Runnable> add : additionals.entrySet()) {
            key = add.getKey();
            value = add.getValue();
            if (key == null || value == null) {
                continue;
            }
            HOTKEYS.put(key, value);
        }
    }

    /**
     * adds a hotkey to the hotkey hash map
     *
     * @param hotkey is the {@link Hotkey} object (key)
     * @param action is the action ({@link Runnable}) that is executed when the hotkey is pressed
     * @return true if the hotkey was added, else false
     */
    public static boolean addHotkey(@NotNull Hotkey hotkey, @NotNull Runnable action) {
        if (HOTKEYS.containsKey(hotkey)) {
            return false;
        }
        HOTKEYS.put(hotkey, action);
        return true;
    }

    /**
     * removes a hotkey from the hotkey map, if present
     *
     * @param hotkey is the {@link Hotkey} object (key) to remove
     * @return true if the hotkey was removed, else false (e.g. when no hotkey was found)
     */
    public static boolean removeHotkey(@NotNull Hotkey hotkey) {
        return HOTKEYS.remove(hotkey) != null;
    }

    /**
     * getter for main instance of {@link ActionHandler}
     *
     * @return main ActionHandler
     */
    @NotNull
    public static ActionHandler getActionHandler() {
        return INSTANCE;
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
    public synchronized void buttonClicked(@NotNull JButton button, @NotNull Controller ctrl, @NotNull UserInterface ui) {
        ctrl.getScheduler().exec(() -> {

            if (button.getText().equals("Cancel")) {
                ui.getSettings().setVisible(false);

            } else if (button.getText().equals("Confirm")) {
                SettingsPane settings = ui.getSettings();
                ctrl.confirmSettings(settings.getValues());
                ui.showSettings(false);

            } else if (button.getName().equals("loadList")) {
                // future
                ctrl.show(ViewType.MAP_HEATMAP);

            } else if (button.getName().equals("loadMap")) {
                //var inputs = gui.searchInput();
                String[] inputs;
                if ((inputs = ctrl.getUI().getSearchPanel().searchInput()) != null) {
                    ctrl.search(inputs, 1);
                }
            }
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
    public synchronized void mapClicked(@NotNull MouseEvent clickEvent, @NotNull Controller ctrl, UserInterface ui) {
        int button = clickEvent.getButton();
        if (button == MouseEvent.BUTTON1) {
            ICoordinate clicked = ui.getMap().getPosition(clickEvent.getPoint());
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
     * executed when a key is entered on a certain component,
     * not executed when global hotkeys are pressed
     *
     * @param event is the KeyEvent
     * @param ui is the GUI instance
     */
    public void keyEntered(@NotNull KeyEvent event, @NotNull UserInterface ui) {
        Object source = event.getSource();
        int key = event.getKeyCode();
        try {
            if (source instanceof JTextField && key == VK_ENTER) {
                JButton jButton = new JButton();
                jButton.setName("loadMap");
                this.buttonClicked(jButton, Controller.getInstance(), ui);
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
     * @param ui is the UI instance that owns all components
     */
    public void windowResized(@NotNull UserInterface ui) {
        JFrame window = ui.getWindow();
        LayerPane layerPane = ui.getLayerPane();
        //TreasureMap map = ui.getMap();

        layerPane.setBounds(0, 0, window.getWidth(), window.getHeight());

        Component bottom = layerPane.getBottom();
        if (bottom != null) {
            bottom.setBounds(layerPane.getBounds());
        }
        //map.setBounds(layerPane.getBounds());
        Component top = layerPane.getTop();
        if (top instanceof SearchPane) {
            top.setBounds(10, 10, top.getWidth(), top.getHeight());
        } else if (top instanceof InfoPane ip) {
            ip.setBounds(0, 0, 270, layerPane.getHeight());
            JList<String> infoList = ip.getInfoList();
            infoList.setBounds(10, 10, infoList.getWidth(), infoList.getHeight());
        }

    }

    /**
     * this method is called when an {@link JComboBox}-item is changed
     *
     * @param source is the source component, on which the change occurred
     * @param item is the item String
     * @param ui is the GUI instance
     */
    private void itemChanged(@NotNull Object source, @NotNull String item, @NotNull UserInterface ui) {
        SearchPane searchPanel = ui.getSearchPanel();
        if (source == searchPanel.getSearchCmbBox()) {
            searchPanel.clearSearch();
            ui.showSearch(SearchType.byItemString(item));
        } else if (source == ui.getSettings().getMapTypeCmbBox()) {
            switch (item) {
                case "Bing Map" -> UserSettings.setCurrentMapSource(UserSettings.BING_MAP);
                case "Default Map" -> UserSettings.setCurrentMapSource(UserSettings.DEFAULT_MAP);
                case "Transport Map" -> UserSettings.setCurrentMapSource(UserSettings.TRANSPORT_MAP);
            }
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
            // TODO: 25.08.2022 ctrl.showStats(ViewType type)
            case "Top-Airports" -> {
                try {
                    Diagrams.showTopAirports(ui, new Statistics());
                } catch (DataNotFoundException e) {
                    ctrl.handleException(e);
                }
            }
            case "Top-Airlines" -> {
                try {
                    Diagrams.showTopAirlines(ui, new Statistics());
                } catch (DataNotFoundException e) {
                    ctrl.handleException(e);
                }
            }
            case "Position-HeatMap" -> ctrl.showBitmap();

            case "ADSB-Supplier", "Antenna" -> ui.showWarning(Warning.NOT_SUPPORTED_YET);
        }
    }

    public void menuClicked(@NotNull JMenu menu) {
        String text = menu.getText();
        Controller ctrl = Controller.getInstance();
        UserInterface ui = ctrl.getUI();
        switch (text) {
            case "Live-Map" -> ctrl.show(MAP_LIVE);
            case "Search" -> ui.showSearch(SearchType.FLIGHT);
            case "Settings" -> ui.showSettings(true);
            case "Close View" -> {
                ctrl.loadedData = null;
                ctrl.getLiveLoader().setLive(false);
                ui.getMapManager().clearMap();
                LayerPane layerPane = ui.getLayerPane();
                layerPane.removeTop();
                layerPane.setBottomDefault();
            }
        }
    }


    // overwritten listener methods

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

}

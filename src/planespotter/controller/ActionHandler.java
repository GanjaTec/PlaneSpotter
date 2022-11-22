package planespotter.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import planespotter.constants.SearchType;
import planespotter.constants.ViewType;
import planespotter.constants.Warning;
import planespotter.dataclasses.Hotkey;
import planespotter.display.StatsView;
import planespotter.display.UserInterface;
import planespotter.display.models.*;
import planespotter.model.ConnectionManager;
import planespotter.model.Scheduler;
import planespotter.statistics.Statistics;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
 * Singleton class {@link ActionHandler} handles user interactions in the UI and does further action
 * (usually calls the {@link Controller} to do further action)
 * @see planespotter.display.UserInterface
 * @see planespotter.controller.Controller
 * @see planespotter.model.Scheduler
 */
// TODO: 15.11.2022 HOTKEY MASK instead of 3 different booleans
public final class ActionHandler
        implements  ActionListener, KeyListener, ComponentListener, MouseListener,
                    ItemListener, WindowListener, ListSelectionListener {

    // ActionHandler instance, we need only one instance here,
    // because we don't want parallel listeners who listen to the same actions
    private static final ActionHandler INSTANCE;

    // initializing all static members
    static {
        INSTANCE = new ActionHandler();
    }

    // hotkey manager instance
    private final HotkeyManager hotkeyManager;

    // ActionHandler hash code
    private final int hashCode;


    // instance //

    /**
     * private {@link ActionHandler} constructor for main instance
     */
    private ActionHandler() {
        Runnable shiftSAction, f11Action;
        Map<Hotkey, Runnable> initKeys = new HashMap<>();

        // shift S / default search hotkey
        shiftSAction = () -> getActionHandler().menuClicked(new JMenu("Search"));
        initKeys.put(new Hotkey(VK_S, true, false, false), shiftSAction);
        // F11 / default fullscreen hotkey
        f11Action = () -> getActionHandler().menuItemClicked(Controller.getInstance(), new JMenuItem("Fullscreen"));
        initKeys.put(new Hotkey(VK_F11, false, false, false), f11Action);

        this.hotkeyManager = new HotkeyManager(initKeys);
        this.hashCode = System.identityHashCode(this);
    }

    /**
     * static getter for singleton {@link ActionHandler} instance
     *
     * @return static {@link ActionHandler} singleton instance
     */
    @NotNull
    public static ActionHandler getActionHandler() {
        return INSTANCE;
    }

    /**
     * getter for {@link HotkeyManager} instance
     *
     * @return {@link HotkeyManager} instance
     */
    @NotNull
    public HotkeyManager getHotkeyManager() {
        return this.hotkeyManager;
    }

    /**
     * executed when a button is clicked,
     * executes certain actions for different
     * buttons (in a new thread)
     *
     * @param button is the clicked button
     * @param ctrl is the main {@link Controller} instance
     * @param ui is the main {@link UserInterface} instance
     */
    public synchronized void buttonClicked(@NotNull JButton button, @NotNull Controller ctrl, @NotNull UserInterface ui) {
        ctrl.getScheduler().exec(() -> {

            String text = button.getText();
            switch (text) {
                case "Cancel" -> ui.getSettings().setVisible(false);
                case "Confirm" -> {
                    SettingsPane settings = ui.getSettings();
                    ctrl.confirmSettings(settings.getValues());
                    ui.showSettings(false);
                }
                case "Search" -> {
                    String[] inputs;
                    if ((inputs = ctrl.getUI().getSearchPane().searchInput()) != null) {
                        ctrl.search(inputs, 1);
                    }
                }
                case "Add" -> {
                    ConnectionPane connPane = ctrl.getUI().getConnectionPane();
                    JList<String> connList = connPane.getConnectionList();
                    connPane.showAddDialog(e -> ctrl.addConnection(connPane, connList, connList.getModel()));
                }
                case "Remove" -> {
                    ConnectionPane connPane = ctrl.getUI().getConnectionPane();
                    JList<String> connList = connPane.getConnectionList();
                    ctrl.removeConnection(connList, connList.getSelectedValuesList(), connList.getModel());
                    connPane.showConnection(null, e -> {});
                }
                case "Connect", "Disconnect" -> {
                    boolean connect = text.equals("Connect");
                    ConnectionPane connPane = ctrl.getUI().getConnectionPane();
                    ctrl.setConnection(connect, connect ? connPane.getMixData() : ctrl.isFr24Enabled());
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
     * @param ui is the {@link UserInterface} instance
     */
    public void keyEntered(@NotNull KeyEvent event, @NotNull UserInterface ui) {
        Object source = event.getSource();
        int key = event.getKeyCode();
        try {
            if (source instanceof JTextField && key == VK_ENTER) {
                JButton jButton = new JButton("Search");
                buttonClicked(jButton, Controller.getInstance(), ui);
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

        layerPane.setBounds(0, 0, window.getWidth(), window.getHeight());

        Component bottom = layerPane.getBottom();
        if (bottom != null) {
            bottom.setBounds(layerPane.getBounds());
        }
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
     * @param ctrl is the Controller instance
     */
    private void itemChanged(@NotNull Object source, @NotNull String item, @NotNull Controller ctrl) {
        UserInterface ui = ctrl.getUI();
        SearchPane searchPanel = ui.getSearchPane();
        if (source == searchPanel.getSearchCmbBox()) {
            searchPanel.clearSearch();
            ui.showSearch(SearchType.byItemString(item));
        }
    }

    /**
     * executed when a {@link JMenuItem} is clicked, action depends on the menu item text
     *
     * @param ctrl is the {@link Controller} instance
     * @param item is the clicked {@link JMenuItem}
     */
    private void menuItemClicked(@NotNull Controller ctrl, @NotNull JMenuItem item) {
        final String text = item.getText();
        final UserInterface ui = ctrl.getUI();
        switch (text) {
            case "Open" -> ctrl.loadFile();
            case "Save As" -> ctrl.saveFile();
            case "Fullscreen" -> ui.setFullScreen(!ui.isFullscreen());
            case "Exit" -> ctrl.shutdown(false);
            case "Fr24-Supplier" -> {
                int mode = JOptionPane.showOptionDialog(ctrl.getUI().getWindow(),
                        "Do you want insert custom Antenna-Data? \nClick YES for ADSB-Data and NO for Fr24-Data",
                        "Insert Mode", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, new String[] {"ADSB-Data", "Fr24-Data", "Mixed-Data"}, null);
                ctrl.runCollector(mode);
            }
            case "ADSB-Supplier" -> ctrl.showConnectionManager();
            // TODO: 25.08.2022 ctrl.showStats(ViewType type)
            case "Top-Airports" -> {
                try {
                    StatsView.showTopAirports(ui, new Statistics());
                } catch (DataNotFoundException e) {
                    ctrl.handleException(e);
                }
            }
            case "Top-Airlines" -> {
                try {
                    StatsView.showTopAirlines(ui, new Statistics());
                } catch (DataNotFoundException e) {
                    ctrl.handleException(e);
                }
            }
            case "Position-HeatMap" -> ctrl.show(ViewType.MAP_HEATMAP);

            case "Antenna" -> ui.showWarning(Warning.NOT_SUPPORTED_YET);
        }
    }

    /**
     * executed when a {@link JMenu} is clicked, action depends on the menu text
     *
     * @param menu is the clicked {@link JMenu}
     */
    public void menuClicked(@NotNull JMenu menu) {
        String text = menu.getText();
        Controller ctrl = Controller.getInstance();
        UserInterface ui = ctrl.getUI();
        switch (text) {
            case "Live-Map" -> {
                ctrl.setAdsbEnabled(false);
                ctrl.setFr24Enabled(true);
                ctrl.show(MAP_LIVE);
            }
            case "Search" -> ui.showSearch(SearchType.FLIGHT);
            case "Settings" -> ui.showSettings(true);
            case "Close View" -> {
                ctrl.setAdsbEnabled(false);
                ctrl.setDataList(null);
                ctrl.getDataLoader().setLive(false);
                ui.clearView();
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
        Object src = e.getSource();
        if (src instanceof JButton bt) {
            Controller ctrl = Controller.getInstance();
            UserInterface ui = ctrl.getUI();
            int eventID = e.getID();
            if (eventID != KEY_TYPED) {
                buttonClicked(bt, ctrl, ui);
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
            mapClicked(e, ctrl, ui);
        } else if (component instanceof JMenu menu) {
            menuClicked(menu);
            ui.unselectMenuBar();
        } else if (component instanceof JMenuItem item) {
            menuItemClicked(ctrl, item);
            ui.unselectMenuBar();
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
        UserInterface ui = Controller.getInstance().getUI();
        keyEntered(e, ui);
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
        windowResized(ui);
    }

    /**
     * ComponentListener-method,
     * executed when a Component is shown
     *
     * @param e is the ComponentEvent
     */
    @Override
    public void componentShown(ComponentEvent e) {
        componentResized(e);
    }

    /**
     * ItemListener-method,
     * executed when a JComboBox-item is changed
     *
     * @param e is the ItemEvent
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        Object src = e.getSource();
        String item = (String) e.getItem();
        Controller ctrl = Controller.getInstance();
        itemChanged(src, item, ctrl);
    }

    /**
     * executed when a JList-value changes, e.g. by selection or deselection
     *
     * @param e is the {@link ListSelectionEvent}
     */
    @Override
    public synchronized void valueChanged(ListSelectionEvent e) {
        Controller ctrl = Controller.getInstance();
        ConnectionManager.Connection conn; ConnectionManager connMngr;
        if (e.getSource() instanceof JList<?> jlist) {
                String selectedItem = (String) jlist.getSelectedValue();
                connMngr = ctrl.getConnectionManager();
                connMngr.setSelectedConn(selectedItem);
                if ((conn = connMngr.getSelectedConn()) != null) {
                    ctrl.getUI().getConnectionPane().showConnection(conn, this);
                }
        }
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

    /**
     * overwritten equals method
     *
     * @param obj is the object to compare with this
     * @return true if the object is equal with this {@link ActionHandler}
     */
    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj.getClass() == this.getClass();
    }


    /**
     * overwritten hash code method
     *
     * @return ActionHandler hash code
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * overwritten toString method
     *
     * @return 'ActionHandler' String
     */
    @Override
    public String toString() {
        return "ActionHandler";
    }


    /**
     * @name HotkeyManager
     * @version 1.0
     *
     * @description
     * The {@link HotkeyManager} represents a manager for all {@link Hotkey}s (initial- and user-hotkeys).
     * Contains functions to add/remove {@link Hotkey}s + action
     */
    public static class HotkeyManager {

        // map for all global hotkeys,
        // key is the Hotkey object with the key data
        // value is the (Runnable) action for the specific hotkey
        @NotNull private final Map<Hotkey, Runnable> hotkeys;

        /**
         * constructs a {@link HotkeyManager}
         * initializing global listener and hotkey map
          */
        private HotkeyManager(@Nullable Map<Hotkey, Runnable> initKeys) {
            // global event dispatcher, events come here before going to the components listeners,
            // used for global hotkeys
            @NotNull KeyEventDispatcher globalEventDispatcher = initGlobalEventDispatcher();
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(globalEventDispatcher);

            this.hotkeys = new HashMap<>(2);
            initHotkeys(initKeys);
        }

        /**
         * hot key event dispatcher,
         * listens to global key events
         *
         * @return {@link KeyEventDispatcher}, which listens to global hotkeys
         */
        @NotNull
        private KeyEventDispatcher initGlobalEventDispatcher() {
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
                    Set<Hotkey> hotkeys = this.hotkeys.keySet();
                    for (Hotkey key : hotkeys) {
                        if (key.equals(hotkey)) {
                            action = this.hotkeys.get(key);
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
        private void initHotkeys(@Nullable Map<Hotkey, Runnable> additionals) {
            if (additionals == null) {
                return;
            }
            // variable (optional) hotkeys
            Hotkey key;
            Runnable value;
            for (Map.Entry<Hotkey, Runnable> add : additionals.entrySet()) {
                key = add.getKey();
                value = add.getValue();
                if (key == null || value == null) {
                    continue;
                }
                hotkeys.put(key, value);
            }
        }

        /**
         * adds a hotkey to the hotkey hash map
         *
         * @param hotkey is the {@link Hotkey} object (key)
         * @param action is the action ({@link Runnable}) that is executed when the hotkey is pressed
         * @return true if the hotkey was added, else false
         */
        public boolean addHotkey(@NotNull Hotkey hotkey, @NotNull Runnable action) {
            if (hotkeys.containsKey(hotkey)) {
                return false;
            }
            hotkeys.put(hotkey, action);
            return true;
        }

        /**
         * removes a hotkey from the hotkey map, if present
         *
         * @param hotkey is the {@link Hotkey} object (key) to remove
         * @return true if the hotkey was removed, else false (e.g. when no hotkey was found)
         */
        public boolean removeHotkey(@NotNull Hotkey hotkey) {
            return hotkeys.remove(hotkey) != null;
        }

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

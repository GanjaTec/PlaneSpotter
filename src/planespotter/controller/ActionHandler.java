package planespotter.controller;

import planespotter.constants.Images;
import planespotter.constants.SearchType;
import planespotter.constants.UserSettings;
import planespotter.constants.ViewType;
import planespotter.display.GUI;
import planespotter.display.GUIAdapter;
import planespotter.display.models.Menus;
import planespotter.throwables.IllegalInputException;
import planespotter.util.Utilities;

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
public record ActionHandler()
        implements ActionListener, KeyListener,
                   ComponentListener, MouseListener,
                   ItemListener, WindowListener {

    /**
     * executed when a button is clicked,
     * executes certain actions for different
     * buttons (in a new thread)
     *
     * @param button is the clicked button
     * @param ctrl is the main controller instance
     * @param gui is the main GUI instance
     * @param guiAdapter is the main GUIAdapter instance
     */
    public synchronized void buttonClicked(JButton button, Controller ctrl, GUI gui, GUIAdapter guiAdapter) {
        Controller.getScheduler().exec(() -> {

            if (button == gui.getContainer("fileButton")) {
                guiAdapter.setViewHeadBtVisible(false);
                guiAdapter.setFileMenuVisible(true);

            } else if (button == gui.getContainer("listButton")) {
                guiAdapter.startProgressBar();

            } else if (button == gui.getContainer("mapButton")) {
                guiAdapter.startProgressBar();
                ctrl.show(MAP_LIVE, "Live-Map");

            } else if (button == gui.getContainer("statsButton")) {
                Menus.show(Menus.TYPE_STATS, (Window) gui.getContainer("window"));

            } else if (button == gui.getContainer("supplierButton")) {
                Menus.show(Menus.TYPE_SUPPLIER, (Window) gui.getContainer("window"));

            } else if (button == gui.getContainer("closeViewButton")) {
                guiAdapter.disposeView();
                ctrl.loadedData = null;
                var startPanel = gui.getContainer("startPanel");
                var rightDP = (JDesktopPane) gui.getContainer("rightDP");
                startPanel.setVisible(true);
                rightDP.moveToFront(startPanel);

            } else if (button == gui.getContainer("settingsButton")) {
                gui.getContainer("settingsDialog").setVisible(true);
                var settingsMaxLoadTxtField = (JTextField) gui.getContainer("settingsMaxLoadTxtField");
                settingsMaxLoadTxtField.setCaretColor(Color.YELLOW);
                guiAdapter.requestComponentFocus(settingsMaxLoadTxtField);

            } else if (button == gui.getContainer("searchButton")) {
                var searchPanel = gui.getContainer("searchPanel");
                searchPanel.setVisible(!searchPanel.isVisible());
                gui.getContainer("searchTxtField").setVisible(!searchPanel.isVisible());
                guiAdapter.loadSearch(SearchType.FLIGHT);

            } else if (button == gui.getContainer("settingsCancelButton")) {
                gui.getContainer("settingsDialog").setVisible(false);

            } else if (button == gui.getContainer("settingsConfirmButton")) {
                var settingsMaxLoadTxtField = (JTextField) gui.getContainer("settingsMaxLoadTxtField");
                var settingsMapTypeCmbBox = (JComboBox<String>) gui.getContainer("settingsMapTypeCmbBox");
                ctrl.confirmSettings(settingsMaxLoadTxtField.getText(), (String) settingsMapTypeCmbBox.getSelectedItem());
                gui.getContainer("settingsDialog").setVisible(false);

            } else if (button.getName().equals("loadList")) {
                // future
                ctrl.show(ViewType.MAP_HEATMAP, "Heat Map");

            } else if (button.getName().equals("loadMap")) {
                try {
                    var inputs = guiAdapter.searchInput();
                    ctrl.search(inputs, 1);
                } catch (IllegalInputException e) {
                    ctrl.handleException(e);
                }
            } else if (button.getName().equals("open")) {
                Controller.getInstance().loadFile();

            } else if (button.getName().equals("save")) {
                Controller.getInstance().saveFile();

            } else if (button.getName().equals("back")) {
                guiAdapter.setFileMenuVisible(false);
                guiAdapter.setViewHeadBtVisible(true);

            }
        }, "Action Handler", false, Scheduler.MID_PRIO, true);
    }

    /**
     * executed when the map is clicked,
     * used for showing information about flights
     *
     * @param clickEvent is the MouseEvent, produced by the click
     * @param ctrl is the Controller instance (given by Controller.getInstance())
     * @param gui is the GUI instance (given by Controller.getGUI())
     */
    public synchronized void mapClicked(MouseEvent clickEvent, Controller ctrl, GUI gui) {
        // TODO evtl threaded
        int button = clickEvent.getButton();
        if (button == MouseEvent.BUTTON1 && gui.getCurrentViewType() != null) {
            var clicked = gui.getMap().getPosition(clickEvent.getPoint());
            switch (gui.getCurrentViewType()) {
                case MAP_LIVE -> ctrl.onLiveClick(clicked);
                case MAP_FROMSEARCH -> ctrl.onClick_all(clicked);
                case MAP_TRACKING -> ctrl.onTrackingClick(clicked);
            }
        }
    }

    /**
     * executed when a key is entered on a certain component
     *
     * @param source is the source component where the key was entered on
     * @param key is the key code from the entered key
     * @param gui is the GUI instance
     * @param guiAdapter is the GUIAdapter instance
     */
    public void keyEntered(Object source, int key, GUI gui, GUIAdapter guiAdapter) {
        try {
            if (source == gui.getContainer("settingsMaxLoadTxtField")) {
                if (key == KeyEvent.VK_ENTER) {
                    var settingsMaxLoadTxtField = (JTextField) gui.getContainer("settingsMaxLoadTxtField");
                    // TODO fixen: settings fenster schließt erst nach loading
                    int newMax = Integer.parseInt(settingsMaxLoadTxtField.getText());
                    if (newMax > 0) {
                        guiAdapter.startProgressBar();
                        UserSettings.setMaxLoadedData(newMax);
                        settingsMaxLoadTxtField.setText("");
                        gui.getContainer("settingsDialog").setVisible(false);
                    } else {
                        settingsMaxLoadTxtField.setText("[x > 0]");
                    }
                }
            } else if (source == gui.getContainer("window") /*&& gui.getCurrentViewType() == MAP_LIVE*/) {
                System.out.println(key);
                final var viewer = gui.getMap();
                switch (key) { // FIXME läuft noch nicht
                    case VK_PAGE_UP, VK_W -> viewer.moveMap(0, 10);
                    case VK_HOME, VK_A -> viewer.moveMap(-10, 0);
                    case VK_END, VK_D -> viewer.moveMap(10, 0);
                    case VK_PAGE_DOWN, VK_S -> viewer.moveMap(0, -10);
                }
            } else if (source instanceof JTextField && key == VK_ENTER) {
                var jButton = new JButton();
                jButton.setName("loadMap");
                this.buttonClicked(jButton, Controller.getInstance(), gui, guiAdapter);
            }
        } catch (NumberFormatException ex) {
            var settingsMaxLoadTxtField = (JTextField) gui.getContainer("settingsMaxLoadTxtField");
            settingsMaxLoadTxtField.setText("Error");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * executed when the window is resized
     *
     * @param gui is the GUI instance that owns all components
     */
    public void windowResized(GUI gui) {
        var window = gui.getContainer("window");
        var mainPanel = gui.getContainer("mainPanel");
        var titlePanel = gui.getContainer("titlePanel");
        var titleBgLabel = (JLabel) gui.getContainer("titleBgLabel");
        var leftDP = gui.getContainer("leftDP");
        var rightDP = gui.getContainer("rightDP");
        var viewHeadPanel = gui.getContainer("viewHeadPanel");
        var startPanel = gui.getContainer("startPanel");
        var listPanel = gui.getContainer("listPanel");
        var mapPanel = gui.getContainer("mapPanel");
        var menuPanel = gui.getContainer("menuPanel");
        var infoPanel = gui.getContainer("infoPanel");
        var menuBar = gui.getContainer("menuBar"); // wird die überhaupt benötigt

        mainPanel.setBounds(0, 0, window.getWidth()-14, window.getHeight()-37);
        titlePanel.setBounds(0, 0, mainPanel.getWidth(), 70);

        int width = titlePanel.getWidth();
        int height = titlePanel.getHeight();
        titleBgLabel.setBounds(0, 0, width, height);
        var img = Utilities.scaledImage(Images.TITLE.get(), width, height);
        titleBgLabel.setIcon(img);

        rightDP.setBounds(280, 70, mainPanel.getWidth() - 280, mainPanel.getHeight() - 70);
        leftDP.setBounds(0, 70, 280, mainPanel.getHeight() - 70);

        viewHeadPanel.setBounds(0, 0, rightDP.getWidth(), 24);
        startPanel.setBounds(0, 24, rightDP.getWidth(), rightDP.getHeight() - 24);
        listPanel.setBounds(0, 24, rightDP.getWidth(), rightDP.getHeight() - 24);
        mapPanel.setBounds(0, 24, rightDP.getWidth(), rightDP.getHeight() - 24);
        menuPanel.setBounds(0, 0, leftDP.getWidth(), leftDP.getHeight());
        infoPanel.setBounds(0, 0, leftDP.getWidth(), leftDP.getHeight());

        gui.getContainer("bgLabel").setBounds(0, 0, rightDP.getWidth(), rightDP.getHeight());
        gui.getContainer("menuBgLabel").setBounds(0, 0, leftDP.getWidth(), leftDP.getHeight());

        gui.getContainer("closeViewButton").setBounds(viewHeadPanel.getWidth() - 85, 4, 80, 16);
        gui.getContainer("fileButton").setBounds(viewHeadPanel.getWidth() - 168, 4, 80, 16);
        gui.getContainer("startLabel").setBounds(0, 0, startPanel.getWidth(), startPanel.getHeight());
        gui.getMap().setBounds(0, 0, mapPanel.getWidth(), mapPanel.getHeight());
        menuBar.setBounds(menuPanel.getBounds());
        gui.getContainer("searchTxtField").setBounds(10, menuBar.getHeight() - 80, 255, 25);
        gui.getContainer("searchButton").setBounds(10, menuBar.getHeight() - 40, 255, 25);
        if (gui.hasContainer("listScrollPane") && gui.getListView() != null) {
            var listScrollPane = gui.getContainer("listScrollPane");
            listScrollPane.setBounds(0, 0, listPanel.getWidth(), listPanel.getHeight());
            gui.getListView().setBounds(listScrollPane.getBounds());
        }
        if (gui.hasContainer("flightInfoTree")) {
            gui.getContainer("flightInfoTree").setBounds(infoPanel.getBounds());
        }
        int minus = 84;
        for (var bt : gui.getFileMenu()) {
            if (bt != null) {
                bt.setBounds(viewHeadPanel.getWidth() - minus, 4, 80, 16);
                minus += 84;
            }
        }
    }

    /**
     *
     *
     * @param source is the source component, on which the change occurred
     * @param item is the item String
     * @param gui is the GUI instance
     */
    private void itemChanged(Object source, String item, GUI gui) {
        if (source == gui.getContainer("searchForCmbBox")) {
            var guiAdapter = new GUIAdapter(gui);
            guiAdapter.clearSearch();
            guiAdapter.loadSearch(SearchType.byItemString(item));
        } else if (source == gui.getContainer("settingsMapTypeCmbBox")) {
            switch (item) {
                case "Bing Map" -> UserSettings.setCurrentMapSource(UserSettings.BING_MAP);
                case "Default Map" -> UserSettings.setCurrentMapSource(UserSettings.DEFAULT_MAP);
                case "Transport Map" ->  UserSettings.setCurrentMapSource(UserSettings.TRANSPORT_MAP);
            }
        }
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
            var gui = Controller.getGUI();
            var guiAdapter = new GUIAdapter(gui);
            this.buttonClicked(bt, ctrl, gui, guiAdapter);
        }
    }

    /**
     * MouseListener-method,
     * executed when the mouse is pressed
     *
     * @param e is the MouseEvent which contains the button
     */
    @Override
    public void mousePressed(MouseEvent e) {
        var ctrl = Controller.getInstance();
        var gui = Controller.getGUI();
        this.mapClicked(e, ctrl, gui);
    }

    /**
     * KeyListener-method,
     * executed when a key is pressed
     *
     * @param e is the Key event which contains the Key-Code and Key-Char
     */
    @Override
    public void keyPressed(KeyEvent e) {
        var src = e.getSource();
        var gui = Controller.getGUI();
        if (src instanceof JTextField jtf) {
            int key = e.getKeyCode();
            var guiAdapter = new GUIAdapter(gui);
            this.keyEntered(jtf, key, gui, guiAdapter);
        }
    }

    /**
     * ComponentListener-method,
     * executed when a Component is resized
     *
     * @param e is the ComponentEvent
     */
    @Override
    public void componentResized(ComponentEvent e) {
        var gui = Controller.getGUI();
        this.windowResized(gui);
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
        var gui = Controller.getGUI();
        this.itemChanged(src, item, gui);
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

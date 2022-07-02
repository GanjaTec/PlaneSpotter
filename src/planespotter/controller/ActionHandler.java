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
import javax.swing.event.ChangeListener;
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

            if (button == gui.getComponent("fileButton")) {
                guiAdapter.setViewHeadBtVisible(false);
                guiAdapter.setFileMenuVisible(true);

            } else if (button == gui.getComponent("listButton")) {
                guiAdapter.startProgressBar();

            } else if (button == gui.getComponent("mapButton")) {
                guiAdapter.startProgressBar();
                ctrl.show(MAP_LIVE, "Live-Map");

            } else if (button == gui.getComponent("statsButton")) {
                Menus.show(Menus.TYPE_STATS, (Window) gui.getComponent("window"));

            } else if (button == gui.getComponent("supplierButton")) {
                Menus.show(Menus.TYPE_SUPPLIER, (Window) gui.getComponent("window"));

            } else if (button == gui.getComponent("closeViewButton")) {
                guiAdapter.disposeView();
                ctrl.loadedData = null;
                var startPanel = gui.getComponent("startPanel");
                var rightDP = (JDesktopPane) gui.getComponent("rightDP");
                startPanel.setVisible(true);
                rightDP.moveToFront(startPanel);

            } else if (button == gui.getComponent("settingsButton")) {
                gui.getComponent("settingsDialog").setVisible(true);
                var settingsMaxLoadTxtField = (JTextField) gui.getComponent("settingsMaxLoadTxtField");
                settingsMaxLoadTxtField.setCaretColor(Color.YELLOW);
                guiAdapter.requestComponentFocus(settingsMaxLoadTxtField);

            } else if (button == gui.getComponent("searchButton")) {
                var searchPanel = gui.getComponent("searchPanel");
                searchPanel.setVisible(!searchPanel.isVisible());
                guiAdapter.loadSearch(SearchType.FLIGHT);

            } else if (button == gui.getComponent("settingsCancelButton")) {
                gui.getComponent("settingsDialog").setVisible(false);

            } else if (button == gui.getComponent("settingsConfirmButton")) {
                var settingsMaxLoadTxtField = (JTextField) gui.getComponent("settingsMaxLoadTxtField");
                var settingsMapTypeCmbBox = (JComboBox<?>) gui.getComponent("settingsMapTypeCmbBox");
                var settingsLivePeriodSlider = (JSlider) gui.getComponent("settingsLivePeriodSlider");
                ctrl.confirmSettings(
                        settingsMaxLoadTxtField.getText(), // max loaded data
                        (String) settingsMapTypeCmbBox.getSelectedItem(), // map type
                        String.valueOf(settingsLivePeriodSlider.getValue()) // live data loading period
                        );
                gui.getComponent("settingsDialog").setVisible(false);

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
     * @param event is the KeyEvent
     * @param gui is the GUI instance
     * @param guiAdapter is the GUIAdapter instance
     */
    public void keyEntered(KeyEvent event, GUI gui, GUIAdapter guiAdapter) {
        var source = event.getSource();
        int key = event.getKeyCode();
        try {
            if (source == gui.getComponent("settingsMaxLoadTxtField")) {
                if (key == KeyEvent.VK_ENTER) {
                    var settingsMaxLoadTxtField = (JTextField) gui.getComponent("settingsMaxLoadTxtField");
                    // TODO fixen: settings fenster schließt erst nach loading
                    int newMax = Integer.parseInt(settingsMaxLoadTxtField.getText());
                    if (newMax > 0) {
                        guiAdapter.startProgressBar();
                        UserSettings.setMaxLoadedData(newMax);
                        settingsMaxLoadTxtField.setText("");
                        gui.getComponent("settingsDialog").setVisible(false);
                    } else {
                        settingsMaxLoadTxtField.setText("[x > 0]");
                    }
                }
            } else if (source == gui.getComponent("window")) {
                final var viewer = gui.getMap();
                switch (key) { // FIXME läuft noch nicht
                    case VK_PAGE_UP -> viewer.moveMap(0, 10);
                    case VK_HOME -> viewer.moveMap(-10, 0);
                    case VK_END -> viewer.moveMap(10, 0);
                    case VK_PAGE_DOWN -> viewer.moveMap(0, -10);
                    case VK_S -> {
                        if (event.isShiftDown()) {
                            var searchButton = gui.getComponent("searchButton");
                            this.buttonClicked((JButton) searchButton, Controller.getInstance(), gui, guiAdapter);
                        }
                    }

                }
            } else if (source instanceof JTextField && key == VK_ENTER) {
                var jButton = new JButton();
                jButton.setName("loadMap");
                this.buttonClicked(jButton, Controller.getInstance(), gui, guiAdapter);
            }
        } catch (NumberFormatException ex) {
            var settingsMaxLoadTxtField = (JTextField) gui.getComponent("settingsMaxLoadTxtField");
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
        var window = gui.getComponent("window");
        var mainPanel = gui.getComponent("mainPanel");
        var titlePanel = gui.getComponent("titlePanel");
        var titleBgLabel = (JLabel) gui.getComponent("titleBgLabel");
        var leftDP = gui.getComponent("leftDP");
        var rightDP = gui.getComponent("rightDP");
        var viewHeadPanel = gui.getComponent("viewHeadPanel");
        var startPanel = gui.getComponent("startPanel");
        var listPanel = gui.getComponent("listPanel");
        var mapPanel = gui.getComponent("mapPanel");
        var menuPanel = gui.getComponent("menuPanel");
        var infoPanel = gui.getComponent("infoPanel");
        var menuBar = gui.getComponent("menuBar"); // wird die überhaupt benötigt

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

        gui.getComponent("bgLabel").setBounds(0, 0, rightDP.getWidth(), rightDP.getHeight());
        gui.getComponent("menuBgLabel").setBounds(0, 0, leftDP.getWidth(), leftDP.getHeight());

        gui.getComponent("closeViewButton").setBounds(viewHeadPanel.getWidth() - 85, 4, 80, 16);
        gui.getComponent("fileButton").setBounds(viewHeadPanel.getWidth() - 168, 4, 80, 16);
        gui.getComponent("startLabel").setBounds(0, 0, startPanel.getWidth(), startPanel.getHeight());
        gui.getMap().setBounds(0, 0, mapPanel.getWidth(), mapPanel.getHeight());
        menuBar.setBounds(menuPanel.getBounds());
        gui.getComponent("searchButton").setBounds(10, menuBar.getHeight() - 40, 255, 25);
        if (gui.hasContainer("listScrollPane") && gui.getListView() != null) {
            var listScrollPane = gui.getComponent("listScrollPane");
            listScrollPane.setBounds(0, 0, listPanel.getWidth(), listPanel.getHeight());
            gui.getListView().setBounds(listScrollPane.getBounds());
        }
        if (gui.hasContainer("flightInfoTree")) {
            gui.getComponent("flightInfoTree").setBounds(infoPanel.getBounds());
        }
        if (gui.chartPanel != null) {
            gui.chartPanel.setBounds(0, 24, rightDP.getWidth(), rightDP.getHeight() - 24);
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
        if (source == gui.getComponent("searchForCmbBox")) {
            var guiAdapter = new GUIAdapter(gui);
            guiAdapter.clearSearch();
            guiAdapter.loadSearch(SearchType.byItemString(item));
        } else if (source == gui.getComponent("settingsMapTypeCmbBox")) {
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
        var gui = Controller.getGUI();
        var guiAdapter = new GUIAdapter(gui);
        this.keyEntered(e, gui, guiAdapter);
        /*if (src instanceof JTextField jtf) {

        } else if (src instanceof JFrame wnd) {
            this.keyEntered(wnd, key, gui, guiA);
        }*/
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

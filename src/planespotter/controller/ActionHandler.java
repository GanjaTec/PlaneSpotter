package planespotter.controller;

import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import planespotter.constants.Images;
import planespotter.constants.SearchType;
import planespotter.constants.UserSettings;
import planespotter.constants.ViewType;
import planespotter.display.GUI;
import planespotter.display.GUIAdapter;
import planespotter.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.KeyEvent.VK_PAGE_DOWN;
import static planespotter.constants.ViewType.LIST_FLIGHT;
import static planespotter.constants.ViewType.MAP_ALL;

public record ActionHandler()
        implements ActionListener, KeyListener, JMapViewerEventListener,
                   ComponentListener, MouseListener, ItemListener, WindowListener {

    /**
     * executed when a button is clicked
     *
     * @param button is the clicked button
     */
    public synchronized void buttonClicked(JButton button, Controller ctrl, GUI gui, GUIAdapter guiAdapter) {
        if (button == gui.getContainer("fileButton")) {
            guiAdapter.setViewHeadBtVisible(false);
            guiAdapter.setFileMenuVisible(true);
        } else if (button == gui.getContainer("listButton")) {
            guiAdapter.startProgressBar();
            ctrl.show(LIST_FLIGHT, "");
        } else if (button == gui.getContainer("mapButton")) {
            guiAdapter.startProgressBar();
            ctrl.show(MAP_ALL, "Live-Map");
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
            //this.guiAdapter.ctrl.show(MAP_SIGNIFICANCE, "Significance Map");
        } else if (button.getName().equals("loadMap")) {
            // TODO search type abfragen, bzw. ComboBox SelectedItem
            var inputs = guiAdapter.searchInput();
            ctrl.search(inputs, 1);
        } else if (button.getName().equals("open")) {
            Controller.getInstance().loadFile();
        } else if (button.getName().equals("save")) {
            Controller.getInstance().saveFile();
        } else if (button.getName().equals("back")) {
            guiAdapter.setFileMenuVisible(false);
            guiAdapter.setViewHeadBtVisible(true);
        }
    }

    public void mapClicked(MouseEvent clickEvent, Controller ctrl, GUI gui) {
        int button = clickEvent.getButton();
        if (button == MouseEvent.BUTTON1 && gui.getCurrentViewType() != null) {
            ctrl.mapClicked(clickEvent.getPoint());
        }
    }

    /**
     * executed when a key is pressed
     */
    public void keyEntered(Object source, int key, GUI gui, GUIAdapter guiAdapter) {
        try {
            if (source == gui.getContainer("searchTxtField")) {
                if (key == KeyEvent.VK_ENTER) {
                    var searchTxtField = (JTextField) gui.getContainer("searchTxtField");
                    if (searchTxtField.hasFocus()) {
                        var txt = searchTxtField.getText().toLowerCase();
                        Controller.getInstance().enterText(txt);
                    }
                }
            } else if (source == gui.getContainer("settingsMaxLoadTxtField")) {
                if (key == KeyEvent.VK_ENTER) {
                    var settingsMaxLoadTxtField = (JTextField) gui.getContainer("settingsMaxLoadTxtField");
                    // TODO fixen: settings fenster schließt erst nach loading
                    if (Integer.parseInt(settingsMaxLoadTxtField.getText()) >= 4) {
                        guiAdapter.startProgressBar();
                        new UserSettings().setMaxLoadedData(Integer.parseInt(settingsMaxLoadTxtField.getText()));
                        settingsMaxLoadTxtField.setText("");
                        gui.getContainer("settingsDialog").setVisible(false);
                    }
                }
            } else if (source == gui.getMap()) {
                final var viewer = gui.getMap();
                switch (key) { // FIXME läuft noch nicht
                    case VK_PAGE_UP -> viewer.moveMap(0, 10);
                    case VK_HOME -> viewer.moveMap(-10, 0);
                    case VK_END -> viewer.moveMap(10, 0);
                    case VK_PAGE_DOWN -> viewer.moveMap(0, -10);
                }
            }
        } catch (NumberFormatException ex) {
            var settingsMaxLoadTxtField = (JTextField) gui.getContainer("settingsMaxLoadTxtField");
            settingsMaxLoadTxtField.setText("Error");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

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

    private void itemChanged(Object source, String item, GUI gui) {
        if (source == gui.getContainer("searchForCmbBox")) {
            var guiAdapter = new GUIAdapter(gui);
            guiAdapter.clearSearch();
            guiAdapter.loadSearch(SearchType.byItem(item));
        } else if (source == gui.getContainer("settingsMapTypeCmbBox")) {
            var usrSettings = new UserSettings();
            if (item.equals("Bing Map")) {
                usrSettings.setCurrentMapSource(usrSettings.bingMap);
            } else if (item.equals("Default Map")) {
                usrSettings.setCurrentMapSource(usrSettings.tmstMap);
            } else if (item.equals("Transport Map")) {
                usrSettings.setCurrentMapSource(usrSettings.transportMap);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        var src = e.getSource();
        if (src instanceof JButton bt) {
            var ctrl = Controller.getInstance();
            var gui = Controller.getGUI();
            var guiAdapter = new GUIAdapter(gui);
            Controller.getScheduler().exec(() -> this.buttonClicked(bt, ctrl,
                            gui, guiAdapter), "Action Handler",
                    false, 5, true);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        var ctrl = Controller.getInstance();
        var gui = Controller.getGUI();
        this.mapClicked(e, ctrl, gui);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        var src = e.getSource();
        int key = e.getKeyCode();
        var gui = Controller.getGUI();
        var guiAdapter = new GUIAdapter(gui);
        this.keyEntered(src, key, gui, guiAdapter);
    }

    /**
     * component listener: fits the component sizes if the window is resized
     */
    @Override
    public void componentResized(ComponentEvent e) {
        var gui = Controller.getGUI();
        this.windowResized(gui);
    }

    @Override
    public void componentShown(ComponentEvent e) {
        this.componentResized(e);
    }

    // item listener (combo-box)
    @Override
    // TODO: 06.06.2022 falsches item
    public void itemStateChanged(ItemEvent e) {
        var src = e.getSource();
        var item = (String) e.getItem();
        var gui = Controller.getGUI();
        this.itemChanged(src, item, gui);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int option = JOptionPane.showConfirmDialog(e.getWindow(), "Do you really want to exit PlaneSpotter?",
                                               "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }


    // unused listeners
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void processCommand(JMVCommandEvent e) {}
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

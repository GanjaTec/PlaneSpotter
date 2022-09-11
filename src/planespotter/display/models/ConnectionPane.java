package planespotter.display.models;

import libs.UWPButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.constants.DefaultColor;
import planespotter.model.ConnectionManager;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionPane extends JDialog {

    @NotNull private final JList<String> connectionList;

    @NotNull private JPanel connectionPanel;

    private JTextField nameTextField, uriTextField, hostTextField, portTextField, pathTextField;

    @Nullable private JDialog addDialog;

    private boolean mixWithFr24;

    @NotNull private final AtomicBoolean uriMode;

    public ConnectionPane(@NotNull Frame owner, @NotNull ListSelectionListener onListSelect, @NotNull ActionListener onButtonClick, @NotNull ActionListener onConnectClick, @NotNull ConnectionManager cManager) {
        super(owner, "Connection Manager");
        setLocationRelativeTo(owner);
        setSize(500, 400);
        setLocation((owner.getWidth()/2) - (getWidth()/2), (owner.getHeight()/2) - (getWidth()/2));
        setLayout(null);
        setResizable(false);
        setType(Type.POPUP);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.uriMode = new AtomicBoolean(true);

        this.connectionList = connectionList(0, 0, 287, 315, onListSelect, cManager);
        this.connectionPanel = connectionPanel(0, 0, 200, 365, null, onConnectClick);
        JPanel listOptions = listButtonPanel(200, 0, 287, 50, onButtonClick);

        JScrollPane scrollPane = new JScrollPane(this.connectionList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(200, 50, 287, 315);

        add(scrollPane);
        add(listOptions);
        add(connectionPanel);
    }

    public void showConnection(@Nullable ConnectionManager.Connection conn, @NotNull ActionListener onConnectClick) {
        remove(connectionPanel);
        repaint();
        connectionPanel = connectionPanel(0, 0, 200, 365, conn, onConnectClick);
        add(connectionPanel);

    }

    public void closeAddDialog() {
        if (addDialog != null) {
            addDialog.setVisible(false);
            addDialog = null;
        }
    }

    public void showAddDialog(@NotNull ActionListener onAdd) {
        addDialog = new JDialog(this, "Add Connection");
        addDialog.setSize(300, 200);
        addDialog.setLayout(null);
        addDialog.setLocationRelativeTo(null);

        JLabel[] lbls = new JLabel[] {
                new JLabel("Name:"), new JLabel("URI:"), new JLabel("Host:"), new JLabel("  Port:"), new JLabel("Path: *")
        };

        JTextField[] tfs = new JTextField[] {
                new JTextField(), new JTextField(), new JTextField(), new JTextField(), new JTextField()
        };

        lbls[0].setBounds(10, 20, 40, 20);
        lbls[1].setBounds(10, 50, 40, 20);
        lbls[2].setBounds(10, 50, 40, 20);
        lbls[3].setBounds(170, 50, 40, 20);
        lbls[4].setBounds(10, 80, 40, 20);
        tfs[0].setBounds(50, 20, 220, 20);
        tfs[1].setBounds(50, 50, 220, 20);
        tfs[2].setBounds(50, 50, 120, 20);
        tfs[3].setBounds(210, 50, 60, 20);
        tfs[4].setBounds(50, 80, 220, 20);

        lbls[0].setVisible(true);
        lbls[1].setVisible(true);
        lbls[2].setVisible(false);
        lbls[3].setVisible(false);
        lbls[4].setVisible(false);
        tfs[0].setVisible(true);
        tfs[1].setVisible(true);
        tfs[2].setVisible(false);
        tfs[3].setVisible(false);
        tfs[4].setVisible(false);

        tfs[1].setText("http://"); // initial uri value, must be completed
        tfs[3].setText("8080"); // initial default port

        this.nameTextField = tfs[0];
        this.uriTextField = tfs[1];
        this.hostTextField = tfs[2];
        this.portTextField = tfs[3];
        this.pathTextField = tfs[4];
        JToggleButton option = new JToggleButton("Mode: URI");
        option.setBounds(0, 0, 150, 15);
        option.setBackground(DefaultColor.DEFAULT_SEARCH_ACCENT_COLOR.get());
        option.setForeground(DefaultColor.DEFAULT_FONT_COLOR.get());
        option.setMultiClickThreshhold(200);
        option.addChangeListener(e -> {
            Arrays.stream(lbls).forEach(lbl -> {
                if (!lbl.getText().equals("Name:")) {
                    lbl.setVisible(!lbl.isVisible());
                }
            });
            Arrays.stream(tfs).forEach(tf -> {
                if (tf != nameTextField) {
                    tf.setVisible(!tf.isVisible());
                }
            });
            uriMode.set(!uriMode.get());
            if (e.getSource() instanceof JToggleButton jtb) {
                jtb.setText("Mode: " + (uriMode.get() ? "URI" : "Host/Port"));
            }
            option.setBackground(DefaultColor.DEFAULT_SEARCH_ACCENT_COLOR.get());
        });
        UWPButton confirm = new UWPButton("Add");
        confirm.setBounds(100, 120, 100, 20);
        confirm.setBackground(DefaultColor.DEFAULT_SEARCH_ACCENT_COLOR.get());
        confirm.setEffectColor(DefaultColor.DEFAULT_FONT_COLOR.get());
        confirm.setSelectedColor(DefaultColor.DEFAULT_MAP_ICON_COLOR.get());
        confirm.addActionListener(onAdd);
        for (JLabel lbl : lbls) {
            addDialog.add(lbl);
        }
        for (JTextField tf : tfs) {
            addDialog.add(tf);
        }
        addDialog.add(option);
        addDialog.add(confirm);

        addDialog.setVisible(true);
    }

    @NotNull
    private JPanel connectionPanel(int x, int y, int width, int height, @Nullable ConnectionManager.Connection conn, @NotNull ActionListener onConnectClick) {
        mixWithFr24 = false;

        JPanel panel = new JPanel(null);
        panel.setBounds(x, y, width, height);
        Color foreground = DefaultColor.DEFAULT_SEARCH_ACCENT_COLOR.get();
        panel.setBorder(BorderFactory.createLineBorder(foreground));

        JLabel nameLbl, uriLbl, noConnection; JSeparator[] sps; UWPButton connectButton; JCheckBox mixDataCheck;
        if (conn != null) {
            sps = new JSeparator[] { new JSeparator(), new JSeparator(), new JSeparator() };
            int sY = 50;
            for (JSeparator s : sps) {
                s.setForeground(foreground);
                s.setBounds(10, sY, width-20, 2);
                sY += 40;
                panel.add(s);
            }
            nameLbl = new JLabel("Name: " + conn.name);
            nameLbl.setBounds(10, 20, width-20, 20);
            nameLbl.setForeground(foreground);

            uriLbl = new JLabel("URI: " + conn.uri);
            uriLbl.setBounds(10, 60, width-20, 20);
            uriLbl.setForeground(foreground);

            mixDataCheck = new JCheckBox("Mix with Fr24-Data");
            mixDataCheck.setBounds(10, 100, width-20, 20);
            mixDataCheck.setForeground(foreground);
            mixDataCheck.addChangeListener(e -> mixWithFr24 = !mixWithFr24);

            connectButton = new UWPButton(conn.isConnected() ? "Disconnect" : "Connect");
            connectButton.setBackground(foreground);
            connectButton.setForeground(DefaultColor.DEFAULT_FONT_COLOR.get());
            connectButton.setEffectColor(DefaultColor.DEFAULT_FONT_COLOR.get());
            connectButton.setSelectedColor(DefaultColor.DEFAULT_MAP_ICON_COLOR.get());
            connectButton.setBounds(width/4, height-40, width/2, 30);
            connectButton.addActionListener(onConnectClick);

            panel.add(nameLbl);
            panel.add(uriLbl);
            panel.add(mixDataCheck);
            panel.add(connectButton);

        } else {
            noConnection = new JLabel("No connection selected!");
            noConnection.setBounds(0, 190, width, 20);
            noConnection.setForeground(foreground);

            panel.add(noConnection);
        }
        panel.setVisible(true);

        return panel;
    }

    private JList<String> connectionList(int x, int y, int width, int height, @NotNull ListSelectionListener onListChange, @NotNull ConnectionManager cMngr) {
        JList<String> list = new JList<>(cMngr.getConnections()
                .stream()
                .map(con -> con.name)
                .toArray(String[]::new));
        list.addListSelectionListener(onListChange);
        UIManager.put("ToggleButton.select", DefaultColor.DEFAULT_SEARCH_ACCENT_COLOR.get());
        list.setBounds(x, y, width, height);
        ConnectionManager.Connection selected = cMngr.getSelectedConn();
        if (selected != null) {
            list.setSelectedValue(selected.name, true);
        }
        list.setForeground(DefaultColor.DEFAULT_SEARCH_ACCENT_COLOR.get());

        return list;
    }

    private JPanel listButtonPanel(int x, int y, int width, int height, @NotNull ActionListener onButtonClick) {
        JPanel panel = new JPanel(null);
        panel.setBounds(x, y, width, height);

        UWPButton[] buttons = new UWPButton[] {
                new UWPButton("Add"),
                new UWPButton("Remove")
        };

        int bX = 0;
        for (UWPButton button : buttons) {
            button.setEffectColor(DefaultColor.DEFAULT_FONT_COLOR.get());
            button.setSelectedColor(DefaultColor.DEFAULT_MAP_ICON_COLOR.get());
            button.setBounds(bX += 80, 10, 60, 30);
            button.setBackground(DefaultColor.DEFAULT_SEARCH_ACCENT_COLOR.get());
            button.setForeground(DefaultColor.DEFAULT_FONT_COLOR.get());
            button.addActionListener(onButtonClick);
            panel.add(button);
        }

        return panel;
    }

    @NotNull
    public JList<String> getConnectionList() {
        return connectionList;
    }

    public @Nullable String @NotNull [] getInput() {
        try {
            return uriMode.get()
                    ? new String[] {
                    nameTextField.getText(), uriTextField.getText()
            }
                    : new String[] {
                    nameTextField.getText(), hostTextField.getText(), portTextField.getText(), pathTextField.getText()
            };
        } catch (NullPointerException npe) {
            return uriMode.get()
                    ? new String[] { null, null }
                    : new String[] { null, null, null, null };
        }
    };

    public boolean getMixData() {
        return mixWithFr24;
    }

}

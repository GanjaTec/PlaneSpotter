package planespotter.display.models;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.DefaultColor;
import planespotter.constants.Images;
import planespotter.controller.ActionHandler;

import javax.swing.*;
import java.awt.*;

public class DevToolsView {

    private static DevToolsView instance = new DevToolsView(ActionHandler.getActionHandler());

    private JFrame frame;
    private JMenuBar menuBar;
    private JPanel contentPane;

    private DevToolsView(@NotNull ActionHandler handler) {
        this.frame = new JFrame("DevTools Window");
        this.menuBar = new JMenuBar();
        this.contentPane = new JPanel(null);

        JMenu dbToCSVMenu = new JMenu("DB to CSV");
        JMenuItem sep = new JMenuItem("table-separated CSV's");
        JMenuItem comb = new JMenuItem("table-combined CSV");
        sep.addMouseListener(handler);
        comb.addMouseListener(handler);
        dbToCSVMenu.add(sep);
        dbToCSVMenu.add(comb);
        menuBar.add(dbToCSVMenu);

        contentPane.setBounds(0, 15, 695, 470);
        contentPane.setBackground(DefaultColor.DEFAULT_FONT_COLOR.get());

        frame.setSize(700, 500);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setJMenuBar(menuBar);
        frame.add(contentPane);

        frame.setVisible(false);
    }

    public static DevToolsView getInstance() {
        return instance;
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public void setLoading(boolean b) {
        if (b) {
            contentPane.add(loadingLbl(contentPane));
        } else {
            contentPane.removeAll();
        }
        contentPane.repaint();
    }

    private JLabel loadingLbl(Component parent) {
        ImageIcon icon = Images.LOADING_CYCLE_GIF.get();
        JLabel lbl = new JLabel(icon);
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        lbl.setBounds(0, 0, w, h);
        //lbl.setBounds(parent.getWidth() / 2 - w / 2, parent.getHeight() / 2 - h / 2, w, h);
        lbl.setOpaque(false);
        lbl.setLayout(null);
        lbl.setVisible(true);
        return lbl;
    }

    private JLabel loadingLbl2(Component parent) {
        JLabel lbl = new JLabel();
        lbl.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        lbl.setOpaque(false);
        lbl.setLayout(null);

        JProgressBar progressBar = new JProgressBar(0, 1);
        int w = 100;
        int h = 30;
        progressBar.setBounds(parent.getWidth() / 2 - w / 2, parent.getHeight() / 2 - h / 2, w, h);
        progressBar.setIndeterminate(true);

        lbl.add(progressBar);

        lbl.setVisible(true);
        return lbl;
    }

}

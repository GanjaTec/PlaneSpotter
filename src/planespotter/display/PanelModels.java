package planespotter.display;

import planespotter.constants.Bounds;

import javax.swing.*;
import java.awt.*;

import static planespotter.constants.GUIConstants.*;
import static planespotter.constants.GUIConstants.LINE_BORDER;

public class PanelModels {

    //default desktop width
    static int WIDTH_RIGHT = 1259-280;
    static int WIDTH_LEFT = 1259-WIDTH_RIGHT; // unnötig (=279)
    // large menu item width
    static int WIDTH_MENUITEM = WIDTH_LEFT-25;

    /**
     * main-panel
     */
    public static JPanel mainPanel () {
        // TODO: setting up mainpanel
        JPanel mainpanel = new JPanel();
        mainpanel.setBounds(Bounds.MAINPANEL); // mainpanel width: 1260
        mainpanel.setLayout(null);
        mainpanel.setBackground(DEFAULT_BG_COLOR);
        mainpanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 6));

        return mainpanel;
    }

    /**
     * title panel with bground and title
     */
    public static JPanel titlePanel () {
        // TODO: setting up title panel
        JPanel title = new JPanel();
        title.setBounds(Bounds.TITLE);
        title.setOpaque(true);
        title.setBackground(DEFAULT_ACCENT_COLOR);
        title.setLayout(null);
        title.setBorder(LINE_BORDER);

        return title;
    }

    /**
     * title background img label
     */
    public static JLabel titleBgLabel (ImageIcon img) {
        // TODO: setting up title backround img
        JLabel title_bground = new JLabel(img);
        title_bground.setBounds(Bounds.TITLE);
        title_bground.setBorder(LINE_BORDER);

        return title_bground;
    }

    /**
     * title text label
     */
    public static JLabel titleTxtLabel () {
        // TODO: setting up title label
        JLabel title_text = new JLabel("P l a n e S p o t t e r");
        title_text.setFont(TITLE_FONT);
        title_text.setForeground(DEFAULT_FG_COLOR);
        title_text.setFocusable(false);
        title_text.setBounds(420, 0, 1660, 70); // bounds in Bounds Klasse

        return title_text;
    }

    /**
     * list panel
     */
    public static JPanel listPanel () {
        // TODO: setting up list panel
        JPanel list = new JPanel();
        list.setBounds(0, 0, WIDTH_RIGHT, 615);
        list.setBackground(DEFAULT_BG_COLOR);
        list.setLayout(null);

        return list;
    }

    /**
     * map panel
     */
    public static JPanel mapPanel () {
        // TODO: setting up map panel
        JPanel map = new JPanel();
        map.setBounds(0, 0, WIDTH_RIGHT, 615);
        map.setBackground(DEFAULT_BG_COLOR);
        map.setLayout(null);

        return map;
    }

    /**
     * menu panel
     */
    public static JPanel menuPanel () {
        // TODO: setting up menu panel
        JPanel menu = new JPanel();
        menu.setBounds(0, 0, WIDTH_LEFT, 615);
        menu.setBackground(DEFAULT_BG_COLOR);
        menu.setLayout(null);

        return menu;
    }

    /**
     * info panel
     */
    public static JPanel infoPanel () {
        // TODO: setting up info panel
        JPanel info = new JPanel();
        info.setBounds(0, 0,  WIDTH_LEFT, 615);
        info.setBackground(DEFAULT_BG_COLOR);

        return info;
    }

    /**
     *
     */
    public static JLabel backgroundLabel () {
        // TODO: setting up background image
        JLabel bground = new JLabel(img);
        bground.setSize(Bounds.MAINPANEL.width, Bounds.MAINPANEL.height);

        return bground;
    }


}

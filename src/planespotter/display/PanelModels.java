package planespotter.display;

import javax.swing.*;
import javax.swing.border.Border;

import static planespotter.constants.GUIConstants.*;

/**
 * @name PanelModels
 * @author jml04
 * @version 1.0
 *
 * contains panel models for GUI
 */
final class PanelModels {

    //default desktop width
    static int WIDTH_RIGHT = 1259-280;
    static int WIDTH_LEFT = 1259-WIDTH_RIGHT; // unnötig (=279)
    // large menu item width
    static int WIDTH_MENUITEM = WIDTH_LEFT-25;

    /**
     * main-panel
     */
    static JPanel mainPanel (JFrame parent) {
        // TODO: setting up mainpanel
        JPanel mainpanel = new JPanel();
        mainpanel.setBounds(0, 0, parent.getWidth(), parent.getHeight()); // mainpanel width: 1260
        mainpanel.setLayout(null);
        mainpanel.setBackground(DEFAULT_BG_COLOR);
        return mainpanel;
    }

    /**
     * title panel with bground and title
     */
    static JPanel titlePanel (JPanel parent) {
        // TODO: setting up title panel
        JPanel title = new JPanel();
        title.setBounds(0, 0, parent.getWidth(), 70);
        title.setOpaque(true);
        title.setBackground(DEFAULT_ACCENT_COLOR);
        title.setLayout(null);
        title.setBorder(LINE_BORDER);
        return title;
    }

    /**
     * title text label
     */
    static JLabel titleTxtLabel (JPanel parent) {
        // TODO: setting up title label
        JLabel title_text = new JLabel("P l a n e S p o t t e r");
        title_text.setFont(TITLE_FONT);
        title_text.setForeground(DEFAULT_FG_COLOR);
        title_text.setFocusable(false);
        title_text.setBounds(parent.getWidth()/2-200, 0, 400, 70); // bounds in Bounds Klasse (?)

        return title_text;
    }

    /**
     * @return head panel for view
     */
    static JPanel viewHeadPanel (JDesktopPane parent) {
        // TODO: setting up view head panel
        JPanel viewHead = new JPanel();
        viewHead.setLayout(null);
        viewHead.setBorder(LINE_BORDER);
        viewHead.setBounds(0, 0, parent.getWidth(), 24);
        viewHead.setBackground(DEFAULT_BG_COLOR);

        return viewHead;
    }

        /**
         * @return head label, which is part of viewHeadPanel
         */
        static JLabel headLabel (JPanel parent, String text) {
            // TODO: setting up head label
            JLabel head = new JLabel(text + " > ");
            head.setBounds(5, 0, 400, 24);
            head.setFont(FONT_MENU.deriveFont(18));
            head.setForeground(DEFAULT_FONT_COLOR);

            return head;
        }

    /**
     * @return list panel
     */
    static JPanel listPanel (JDesktopPane parent) {
        // TODO: setting up list panel
        JPanel list = new JPanel();
        list.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
        list.setBackground(DEFAULT_BG_COLOR);
        list.setLayout(null);
        list.setBorder(LINE_BORDER);

        return list;
    }

    /**
     * map panel
     */
    static JPanel mapPanel (JDesktopPane parent) {
        // TODO: setting up map panel
        JPanel map = new JPanel();
        map.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
        map.setBackground(DEFAULT_BG_COLOR);
        map.setLayout(null);
        map.setBorder(LINE_BORDER);

        return map;
    }

    /**
     * menu panel
     */
    static JPanel menuPanel (JDesktopPane parent) {
        // TODO: setting up menu panel
        JPanel menu = new JPanel();
        menu.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        menu.setBackground(DEFAULT_BG_COLOR);
        menu.setLayout(null);

        return menu;
    }

    /**
     * info panel
     */
    static JPanel infoPanel (JDesktopPane parent) {
        // TODO: setting up info panel
        JPanel info = new JPanel();
        info.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        info.setBackground(DEFAULT_ACCENT_COLOR);
        info.setLayout(null);

        return info;
    }

    /**
     * search panel // TODO radioButtons, TxtField, 2 Buttons, JLabels -> aber extern
     */
    static JPanel searchPanel (JPanel parent, GUI gui) {
        // TODO: setting up search panel
        JPanel search = new JPanel();
        search.setBounds(10, 150, parent.getWidth()-20, parent.getHeight()-225);
        search.setBackground(DEFAULT_BG_COLOR);
        search.setBorder(LINE_BORDER);
        search.setLayout(null);
        search.setVisible(false);

        return search;
    }

    /**
     * start panel
     */
    static JPanel startPanel (JDesktopPane parent) {
        // TODO setting up start screen panel
        JPanel start = new JPanel();
        start.setBounds(0, 24, parent.getWidth(), parent.getHeight()-24);
        start.setBackground(DEFAULT_BG_COLOR);
        start.setLayout(null);
        start.setBorder(LINE_BORDER);

        return start;
    }

    /**
     * background label
     */
    static JLabel backgroundLabel (JPanel parent) {
        // TODO: setting up background image
        JLabel bground = new JLabel(img);
        bground.setSize(parent.getWidth(), parent.getHeight());

        return bground;
    }




}

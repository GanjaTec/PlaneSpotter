package planespotter.display;


import planespotter.Controller;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

import static planespotter.dataclasses.GUIConstants.*;

/**
 * @name
 * @author
 * @version
 */
public class GUI implements     ActionListener, KeyListener, ListSelectionListener,
                                MouseListener, MouseWheelListener, ChangeListener {

    /**
     * components
     */
    private JFrame frame;
    //private JDesktopPane dpleft, dpright;
    private JInternalFrame flist, fmap, fmenu, finfo;
    private JPanel pTitle, pList, pMap, pMenu, pSearch, pfinfo;
    private JLabel title, bground;
    private JMenu datei, view, settings, search_settings;
    private static JTree listView;
    private static DefaultMutableTreeNode tree;
    protected JTextField search;
    protected JRadioButton rbFlight, rbAirline;
    protected JButton exit;

    /**
     * class constants
     */
    // icons / images
    private final ImageIcon img = new ImageIcon(this.getClass().getResource("/background.jpg")),
                            img_exit = new ImageIcon(this.getClass().getResource("/img_exit.png")),
                            img_exit_selected = new ImageIcon(this.getClass().getResource("/img_exit_selected.png"));
    // line border
    private final Border    LINE_BORDER = BorderFactory.createLineBorder(DEFAULT_FG_COLOR, 1),
                            MENU_BORDER = BorderFactory.createLineBorder(DEFAULT_FG_COLOR, 2);


    /**
     * constructor for NeueGUI
     *
     */
    public GUI() {

        JFrame frame = this.initialize();
        frame.setVisible(true);


    }

    /**
     * initialize method
     * creates new GUI frame
     */
    private JFrame initialize () {

        // TODO: setting up frame
        frame = new JFrame("PlaneSpotter");
        frame.setSize(1280, 720);
        frame.setUndecorated(true);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setBackground(DEFAULT_BG_COLOR);

        /*
        // TODO: setting up right desktop pane
        dpright = new JDesktopPane();
        dpright.setBorder(LINE_BORDER);
        dpright.setBackground(DEFAULT_BG_COLOR);
        // TODO: setting up left desktop pane
        dpleft = new JDesktopPane();
        dpleft.setBorder(LINE_BORDER);
        dpleft.setBackground(DEFAULT_BG_COLOR);
         */

        // TODO: setting up internal list frame
        flist = new JInternalFrame("List-Ansicht", false);
        flist.setBounds(280, 70, 1000, 650);
        flist.setClosable(false);
        flist.setLayout(null);
        flist.setBorder(BorderFactory.createEmptyBorder());
        flist.setBackground(DEFAULT_BG_COLOR);
        flist.setFocusable(false);
        flist.setBorder(LINE_BORDER);

        //flist.hide();

        // TODO: setting up internal map frame
        fmap = new JInternalFrame("Map-Ansicht", false);
        fmap.setBounds(280, 50, 1000, 650);
        fmap.setClosable(false);
        fmap.setLayout(null);
        fmap.setBorder(BorderFactory.createEmptyBorder());
        fmap.setBackground(DEFAULT_BG_COLOR);
        fmap.setFocusable(false);
        //fmenu.setBorder(LINE_BORDER);
        //fmap.hide();

        // internal frames //

        // TODO: setting up internal menu frame
        fmenu = new JInternalFrame("Menu", false);
        fmenu.setBounds(0, 70, 280, 650);
        fmenu.setBackground(DEFAULT_BG_COLOR);
        fmenu.setFocusable(false);
        fmenu.setBorder(LINE_BORDER);
        fmenu.setLayout(null);

        // TODO: setting up internal finfo frame
        finfo = new JInternalFrame("finfo", false);
        finfo.setBounds(0, 70, 280, 650);
        finfo.setBackground(DEFAULT_BG_COLOR);
        finfo.setFocusable(false);
        //finfo.setBorder(LINE_BORDER);

        // panels //

        // TODO: setting up title panel
        pTitle = new JPanel();
        pTitle.setBounds(0, 0, 1280, 70);
        pTitle.setOpaque(true);
        pTitle.setBackground(DEFAULT_BG_COLOR);
        pTitle.setLayout(null);
        pTitle.setBorder(LINE_BORDER);

        // TODO: setting up list panel
        pList = new JPanel();
        pList.setBounds(0, 0, 1000, 650);
        pList.setBackground(DEFAULT_BG_COLOR);
        pList.setLayout(null);

        // TODO: setting up map panel
        pMap = new JPanel();
        pMap.setBounds(0, 0, 1000, 650);
        pMap.setBackground(DEFAULT_BG_COLOR);

        // TODO: setting up menu panel
        pMenu = new JPanel();
        pMenu.setBounds(0, 0, 280, 650);
        pMenu.setBackground(DEFAULT_BG_COLOR);
        pMenu.setLayout(null);

        // TODO: setting up info panel
        pfinfo = new JPanel();
        pfinfo.setBounds(0, 0, 280, 650);
        pfinfo.setBackground(DEFAULT_BG_COLOR);

        // text field //

        // TODO: setting up search text field
        search = new JTextField();
        search.setToolTipText("Search");
        search.setBounds(10, 535, 260, 25);
        search.setBackground(Color.LIGHT_GRAY);
        search.setFont(FONT_MENU);
        search.setBorder(MENU_BORDER);

        // TODO: setting up exit button
        exit = new JButton(img_exit);
        exit.setOpaque(true);
        exit.setBackground(DEFAULT_BG_COLOR);
        exit.setBounds(1247, 12, 25, 25);
        exit.addActionListener(this);
        exit.setBorder(BorderFactory.createEmptyBorder());
        exit.setSelectedIcon(img_exit_selected);

        // TODO: setting up title label
        title = new JLabel("PlaneSpotter");
        title.setFont(TITLE_FONT.deriveFont(65f));
        title.setForeground(DEFAULT_FG_COLOR);
        title.setFocusable(false);
        title.setBounds((pTitle.getWidth() / 2) - (pTitle.getWidth() / 6), 0, 1280, 70);

        // TODO: setting up background image
        bground = new JLabel(img);
        bground.setSize(frame.getSize());

        // TODO: setting up datei menu
        datei = new JMenu("File");
        datei.setFont(FONT_MENU);
        datei.setBorder(MENU_BORDER);
        datei.setBackground(DEFAULT_BG_COLOR);
        datei.setForeground(DEFAULT_FG_COLOR);
        datei.setBounds(0, 0, 280,  25);

        // TODO: setting up view menu
        view = new JMenu("View");
        view.setFont(FONT_MENU);
        view.setBorder(MENU_BORDER);
        view.setBackground(DEFAULT_BG_COLOR);
        view.setForeground(DEFAULT_FG_COLOR);
        view.setBounds(0, 40, 280, 25);

        // TODO: setting up settings menu
        settings = new JMenu("Settings");
        settings.setFont(FONT_MENU);
        settings.setBorder(MENU_BORDER);
        settings.setBackground(DEFAULT_BG_COLOR);
        settings.setForeground(DEFAULT_FG_COLOR);
        settings.setBounds(0, 80, 280,  25);

        // TODO: setting up search-settings menu
        search_settings = new JMenu("Search-Settings");
        search_settings.setFont(FONT_MENU);
        search_settings.setBorder(MENU_BORDER);
        search_settings.setBackground(DEFAULT_BG_COLOR);
        search_settings.setForeground(DEFAULT_FG_COLOR);
        search_settings.setBounds(0, 575, 280, 25);


        // TODO: setting up radio button: "search for airline"
        rbAirline = new JRadioButton();
        rbAirline.addChangeListener(this);

        // TODO: setting up radio button: "search for flight"
        rbFlight = new JRadioButton();
        rbFlight.addChangeListener(this);

        // TODO: adding everything to menu panel
        pMenu.add(datei);
        pMenu.add(view);
        pMenu.add(settings);
        pMenu.add(search_settings);
        pMenu.add(search);

        // TODO: adding everything to title panel
        pTitle.add(title);
        pTitle.add(exit);
        // TODO: adding everything to list panel
        try {
            Controller.createFlightTree();
        } catch (SQLException e) {
            e.printStackTrace();
        } // kommt noch in einen ActionListener oder so
        pList.add(listView);

        // TODO: adding everything to internal menu frame
        fmenu.add(pMenu);
        // TODO: adding everything to internal finfo frame
        finfo.add(pfinfo);
        // TODO: adding everything to internal map frame
        fmap.add(pMap);
        // TODO: adding everything to internal list frame
        flist.add(pList);
        // removing mouselisteners // klappt nicht sow wie es soll
        for (MouseListener l : flist.getContentPane().getMouseListeners()) {
            flist.removeMouseListener(l);

        }

        /*
        // TODO: adding internal frames to dpright
        dpright.add(flist);
        dpright.add(fmap);
        // TODO: adding internal frames to dpleft
        dpleft.add(fmenu);
        dpleft.add(finfo);
         */

        // TODO: adding title panel to frame
        frame.add(pTitle);
        /*
        // TODO: adding desktop panes to frame
        frame.add(dpright);
        frame.add(dpleft);

        dpright.setVisible(true);
        dpleft.setVisible(true);
        dpright.moveToFront(flist);
        dpleft.moveToFront(fmenu);
         */
        frame.add(flist);
        frame.add(fmap);
        frame.add(fmenu);
        frame.add(finfo);

        flist.show();
        //fmap.show();
        fmenu.show();
        //finfo.show();


        return frame;
    }




    /**
     * static, damit der controller die methode nutzen kann, ohne eine neue gui zu oefnen
     * @param node is the tree node to recieve
     */
    public static void recieveTree (DefaultMutableTreeNode node) {
        tree = node;
    }

    /**
     *
     * @return listView Tree
     */
    public static JTree getListView () {
        return listView;
    }

    /**
     *
     * @param tree is the tree to set
     */
    public static void setListView (JTree tree) {
        listView = tree;
    }


    // listeners //

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == exit)  Controller.exit();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object src = e.getSource();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        Object src = e.getSource();
    }
}

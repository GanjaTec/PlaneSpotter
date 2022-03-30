package planespotter.display;

import planespotter.display.listeners.KeyListener;
import planespotter.display.listeners.ListListener;
import planespotter.display.listeners.RadioButtonListener;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @name
 * @author
 * @version
 */
public class GUI {

    /**
     * components
     */
    private JFrame frame;
    private JInternalFrame flist, fmap, fmenu, info;
    private JPanel pTitle, pList, pMap, pMenu, pSearch, pInfo;
    private JLabel title;
    private JLabel bground;
    private JList menulist;
    private JTextField search;
    private JRadioButton rbFlight;
    private JRadioButton rbAirline;

    /**
     * global constants
     */
    private final ImageIcon img = new ImageIcon(this.getClass().getResource("/background.jpg"));
    private final Font font = new Font("Broadway", Font.BOLD, 20);
    private final Border LINE_BORDER = BorderFactory.createLineBorder(Color.CYAN, 1);

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
        frame.setBackground(Color.DARK_GRAY);

        // TODO: setting up internal list frame
        flist = new JInternalFrame("List-Ansicht", false);
        flist.setBounds(100, 50, 1180, 670);
        flist.setClosable(false);
        flist.setLayout(null);
        flist.setBorder(BorderFactory.createEmptyBorder());
        flist.hide();

        // TODO: setting up internal map frame
        fmap = new JInternalFrame("Map-Ansicht", false);
        fmap.setBounds(100, 50, 1180, 670);
        fmap.setClosable(false);
        fmap.setLayout(null);
        fmap.setBorder(BorderFactory.createEmptyBorder());
        fmap.hide();

        // TODO: setting up internal menu frame
        fmenu = new JInternalFrame("Menu", false);
        fmenu.setBounds(0, 50, 100, 670);

        // TODO: setting up title pane
        pTitle = new JPanel();
        pTitle.setBounds(0, 0, 1280, 50);
        pTitle.setOpaque(true);

        // TODO: setting up list panel
        pList = new JPanel();
        pList.setBounds(100, 50, 1180, 670);

        // TODO: setting up map panel
        pMap = new JPanel();
        pMap.setBounds(100, 50, 1180, 670);

        // TODO: setting up menu panel
        pMenu = new JPanel();
        pMenu.setBounds(0, 50, 100, 670);

        // TODO: setting up search panel
        pSearch = new JPanel();
        pSearch.setBounds(0, 50, 100, 270);
        pSearch.addKeyListener(new KeyListener());

        // TODO: setting up search text field
        search = new JTextField();
        search.setToolTipText("Search");
        search.setBounds(0, 50, 100, 30);
        search.setBackground(Color.WHITE);
        search.setFont(font);
        search.setBorder(LINE_BORDER);

        // TODO: setting up title label
        title = new JLabel("PlaneSpotter");
        title.setFont(font.deriveFont(35f));
        title.setForeground(Color.CYAN);
        title.setFocusable(false);

        // TODO: setting up background image
        bground = new JLabel(img);
        bground.setSize(frame.getSize());

        // TODO: setting up menu list
        String[] items = {"Datei", "View", "Exit"};
        menulist = new JList(items);
        menulist.addListSelectionListener(new ListListener());

        // TODO: setting up radio button: "search for airline"
        rbAirline = new JRadioButton();
        rbAirline.addChangeListener(new RadioButtonListener());

        // TODO: setting up radio button: "search for flight"
        rbFlight = new JRadioButton();
        rbFlight.addChangeListener(new RadioButtonListener());

        // TODO: setting up

        // TODO: adding everything together


        // TODO: adding internal frames to frame
        frame.add(flist);
        frame.add(fmap);
        frame.add(fmenu);
        frame.add(info);
        //TODO: adding panels to internal frames
        flist.add(pList);
        fmap.add(pMap);
        fmenu.add(pMenu);
        info.add(pInfo);


        return frame;
    }

}

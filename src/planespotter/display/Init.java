package planespotter.display;

import planespotter.Controller;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Init implements ActionListener {

    /**
     * Components
     */
    private final JFrame frame;
    private final ImageIcon img =   new ImageIcon(this.getClass().getResource("/background.jpg")),
                                    listIcon = new ImageIcon(this.getClass().getResource("/background.jpg"));
    private final JLabel title, backround;
    private final JButton btMapView, btListView, btExit;

    private final Font font = new Font("garamond", 1, 20);

    /**
     * Init-constructor
     *
     * creates a new Init Frame
     */
    public Init () {    // frame 800 * 500

        // TODO: new Frame
        frame = new JFrame("PlaneSpotter");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.setResizable(false);
        frame.setUndecorated(true);

        // TODO: new title Component
        title = new JLabel("PlaneSpotter");
        Font f2 = new Font("Atlantico", 1, 60);
        title.setFont(f2);
        title.setFocusable(false);
        title.setBounds(200, 60, 400, 60);
        title.setForeground(Color.CYAN);
        title.setVisible(true);

        // default button border and colors
        Border border = BorderFactory.createLineBorder(Color.BLACK, 4);
        Color bg = Color.CYAN;
        Color fg = Color.DARK_GRAY;

        // TODO: new 'List View' Button
        btListView = new JButton("List View");
        btListView.setBounds(300, 200, 200, 50);
        btListView.setBackground(bg);
        btListView.setForeground(fg);
        btListView.setFont(font);
        btListView.setBorder(border);
        btListView.addActionListener(this);
        btListView.setVisible(true);

        // TODO: new 'Map View' Button
        btMapView = new JButton("Map View");
        btMapView.setBounds(300, 270, 200, 50);
        btMapView.setBackground(bg);
        btMapView.setForeground(fg);
        btMapView.setFont(font);
        btMapView.setBorder(border);
        btMapView.addActionListener(this);
        btMapView.setVisible(true);

        // TODO: new 'Exit' Button
        btExit = new JButton("Exit");
        btExit.setBounds(350, 340, 100, 50);
        btExit.setBackground(bg);
        btExit.setForeground(fg);
        btExit.setFont(font);
        btExit.setBorder(border);
        btExit.addActionListener(this);
        btExit.setVisible(true);

        // TODO: new backround image (label)
        backround = new JLabel(img);
        backround.setSize(frame.getSize());
        backround.setFocusable(false);
        backround.setVisible(true);

        // TODO: adding components to frame
        frame.add(title);
        frame.add(btListView);
        frame.add(btMapView);
        frame.add(btExit);
        frame.add(backround);

        frame.setVisible(true);
        frame.requestFocus();


    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btExit) System.exit(0);
        else if (src == btListView) {
            Controller.openWindow(ListView.class, frame);
        }
        else if (src == btMapView) {
            //frame.setVisible(false);
            new MapView(frame);
        }
    }
}

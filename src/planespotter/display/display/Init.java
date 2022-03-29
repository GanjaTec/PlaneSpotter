package planespotter.display.display;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Init implements ActionListener {

    /**
     * Components
     */
    private final JFrame frame;
    private final ImageIcon img = new ImageIcon(this.getClass().getResource("/background.jpg"));
    private final JLabel title, backround;
    private final JButton btMapView, btListView, btExit;
    private final Font font = new Font(Font.SANS_SERIF, 1, 20);

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

        // TODO: new title Component
        title = new JLabel("PlaneSpotter");
        Font f2 = font.deriveFont(60f);
        title.setFont(f2);
        title.setFocusable(false);
        title.setBounds(200, 60, 400, 60);
        title.setForeground(Color.DARK_GRAY);
        title.setVisible(true);

        // default button border
        Border border = BorderFactory.createLineBorder(Color.GRAY, 4);

        // TODO: new 'List View' Button
        btListView = new JButton("List View");
        btListView.setBounds(320, 180, 160, 40);
        btListView.setBackground(Color.DARK_GRAY);
        btListView.setForeground(Color.WHITE);
        btListView.setFont(font);
        btListView.setBorder(border);
        btListView.addActionListener(this);
        btListView.setVisible(true);

        // TODO: new 'Map View' Button
        btMapView = new JButton("Map View");
        btMapView.setBounds(320, 250, 160, 40);
        btMapView.setBackground(Color.DARK_GRAY);
        btMapView.setForeground(Color.WHITE);
        btMapView.setFont(font);
        btMapView.setBorder(border);
        btMapView.addActionListener(this);
        btMapView.setVisible(true);

        // TODO: new 'Exit' Button
        btExit = new JButton("Exit");
        btExit.setBounds(360, 320, 80, 40);
        btExit.setBackground(Color.DARK_GRAY);
        btExit.setForeground(Color.WHITE);
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
            frame.setVisible(false);
            new ListView(frame);
        }
        else if (src == btMapView) {
            frame.setVisible(false);
            new MapView();
        }
    }
}

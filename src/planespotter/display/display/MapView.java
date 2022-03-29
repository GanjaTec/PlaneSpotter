package planespotter.display.display;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletionStage;

import com.bbn.openmap.app.OpenMap;
import org.lodgon.openmapfx.core.*;
import org.lodgon.openmapfx.service.MapViewPane;
import org.lodgon.openmapfx.service.OpenMapFXService;

public class MapView {

    //private JFrame frame, owner;
    //private JMenuBar menuBar;
    //private JMenu datei, settings;

    // Map Struktur
    private OpenMap map;


    public MapView (JFrame owner) {
        map = new OpenMap();




        this.owner = owner;

        /*frame = new JFrame("Map-View");
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.setResizable(false);
        //frame.add((Component) map);
        frame.setVisible(true);*/
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

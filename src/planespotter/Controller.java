package planespotter;

import planespotter.display.*;

import javax.swing.*;

public class Controller {

    public Controller () {}

    /**
     * * * * * * * * * * * * * * *
     * static controller methods *
     * * * * * * * * * * * * * * *
     *
     * openFrame() opens a frame
     * @param c is the Frame-Class to be opened
     * @param opener is the JFrame who opens the new Window
     */
    public static void openWindow (Class c, JFrame opener) {
        if (c == Init.class) new Init();
        else if (c == ListView.class) new ListView(opener);
        else if (c == MapView.class) new MapView(opener);
    }
}

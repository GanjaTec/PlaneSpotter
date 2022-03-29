package planespotter;

import planespotter.display.*;

import javax.swing.*;
import java.util.HashMap;

public class Controller {

    /**
     * HashMap containing all working frames
     */
    private static HashMap<Class, Boolean> framesworking = new HashMap<>();

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
        if (opener != null) opener.setVisible(false);
        if (c == Init.class) {
            new Init();
            framesworking.put(c, true);
        } else if (c == ListView.class) {
            new ListView(opener);
            framesworking.put(c, true);
        } else if (c == MapView.class) {
            new MapView(opener);
            framesworking.put(c, true);
        }
    }

    /**
     *
     */
    public void addDataToList (Frame frame) {

    }

    protected static void initialize () {
        framesworking.put(Init.class, false);
        framesworking.put(ListView.class, false);
        framesworking.put(MapView.class, false);
    }

    public static void setFrameWorking (Class key, boolean working) throws FrameNotFoundException {
        if (framesworking.containsKey(key))
            framesworking.replace(key, working);
        else throw new FrameNotFoundException();
    }

    public static boolean getFrameWorking (Class key) throws FrameNotFoundException {
        if (framesworking.containsKey(key))
            return framesworking.get(key);
        else throw new FrameNotFoundException();
    }


}

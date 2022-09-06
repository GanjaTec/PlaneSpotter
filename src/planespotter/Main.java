package planespotter;

import planespotter.controller.ActionHandler;
import planespotter.controller.Controller;
import planespotter.dataclasses.Hotkey;

import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * @name Main
 * @author jml04
 * @version 1.0
 *
 * Main is the main class which contains the main()-methods to start the program
 */
public abstract class Main {

    /**
     * Project Main-Method
     * @param args -> can be ignored
     */
    public static void main(String[] args) {
        // starting program with controller
        final Controller control = Controller.getInstance();
        control.start();
    }

}

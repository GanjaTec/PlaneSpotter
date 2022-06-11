package planespotter;

import planespotter.controller.Controller;

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

    public static void main (String[] args) {

        final var control = Controller.getInstance();

        // starting program
        control.start();
    }

}

package planespotter;

import planespotter.controller.Controller;

/**
 * @name Main
 * @author jml04
 * @version 1.0
 */
public class Main {

    /**
     * Project Main-Method
     * @param args -> can be ignored
     */
    public static void main (String[] args) {

        final var control = Controller.getInstance();

        // TODO: starting program
        control.start();
    }

}

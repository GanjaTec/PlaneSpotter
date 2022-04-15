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
     * @param args -> null
     */
    public static void main (String[] args) {

        final Controller control = new Controller();

        // TODO: initializing ThreadPoolExecutor
        Controller.init();

        // TODO: new GUI frame
        control.openWindow();
    }

}

package planespotter;

import planespotter.controller.Controller;
import planespotter.model.io.BufferedCSVWriter;
import planespotter.model.io.DBToCSV;

/**
 * @name Main
 * @author jml04
 * @version 1.0
 *
 * Main is the main class which contains the main()-methods to start the program
 */
public final class Main {

    /**
     * Project Main-Method
     * @param args -> can be ignored
     */
    public static void main(String[] args) {
        DBToCSV.setDebugMode(true);
        BufferedCSVWriter.setDebugMode(true);

        // starting program with controller
        final Controller control = Controller.getInstance();
        control.start();
    }

}

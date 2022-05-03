package planespotter.model;

import planespotter.constants.Paths;
import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.display.UserSettings;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import static planespotter.constants.Configuration.*;

/**
 * file manager -> loads and saves files
 */
public class FileMaster {

    // controller instance
    private Controller controller = Controller.getInstance();
    // saving / loading at the monment ?
    private static boolean processing;

    /**
     * constructor
     */
    private FileMaster() {

    }

    /**
     *
     */
    public void savePspFile (JFileChooser chooser) {
        try {
            var file = chooser.getSelectedFile();
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * saves the config as a .cfg file
     */
    public static void saveConfig () {
        var fileWizard = new FileMaster();
        try {
            processing = true;
            fileWizard.controller.log("saving config...");
            var config = new File(Paths.SRC_PATH + "configuration.cfg");
            if (!config.exists()) { // creating new file if there is no existing one
                config.createNewFile();
            }
            var writer = new FileWriter(config);
            writer.write("maxThreadPoolSize: " + MAX_THREADPOOL_SIZE + "\n");
            writer.write("maxLoadedFlights: " + UserSettings.getMaxLoadedFlights() + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fileWizard.controller.sucsessLog("configuration.cfg saved sucsessfully!");
            processing = false;
        }
    }

    /**
     *
     */
    private void writeFlightRoute (File toWrite, HashMap<Integer, DataPoint> route)
            throws IOException {
        // TODO how to write Hashmap to File?
        //  Serializable? new Class? Gson? (must not be readable, just loadable)
        var writer = new FileWriter(toWrite);
        //writer.write(route);
    }

}

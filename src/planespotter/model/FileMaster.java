package planespotter.model;

import planespotter.constants.Paths;
import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.display.UserSettings;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;

import static planespotter.constants.Configuration.*;

/**
 * file manager -> loads and saves files
 */
public class FileMaster {

    // controller instance
    private final Controller controller = Controller.getInstance();

    /**
     * constructor
     */
    public FileMaster() {

    }

    /**
     * saves the config as a .cfg file
     */
    public static void saveConfig () {
        var fileWizard = new FileMaster();
        // saving / loading at the monment ?
        try {
            fileWizard.controller.log("saving config...");
            var config = new File(Paths.SRC_PATH + "configuration.cfg");
            if (!config.exists()) { // creating new file if there is no existing one
                config.createNewFile();
            }
            var writer = new FileWriter(config);
            writer.write("maxThreadPoolSize: " + MAX_THREADPOOL_SIZE + "\n");
            writer.write("maxLoadedFlights: " + UserSettings.getMaxLoadedData() + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fileWizard.controller.sucsessLog("configuration.cfg saved sucsessfully!");
        }
    }

    /**
     * saves a flight route in a .psp (.planespotter) file
     * @return
     */
    public HashMap<Integer, DataPoint> loadPlsFile (JFileChooser chooser) throws DataNotFoundException {
        var file = chooser.getSelectedFile();
        if (file.exists()) {
            try {
                return this.readFlightRoute(file);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        throw new DataNotFoundException("file couldn't be found!");
    }

    /**
     * saves a flight route in a .psp (.planespotter) file
     */
    public void savePlsFile (JFileChooser chooser) {
        try {
            var file = chooser.getSelectedFile();
            if (!file.exists()) {
                file.createNewFile();
            }
            if (!Controller.allMapData.isEmpty()) {
                this.writeFlightRoute(file, Controller.allMapData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param file is the file to read from
     * @return the flight route hash map
     */
    private HashMap<Integer, DataPoint> readFlightRoute(File file)
            throws ClassCastException, IOException, ClassNotFoundException {
        var fis = new FileInputStream(file);
        var ois = new ObjectInputStream(fis);
        HashMap<Integer, DataPoint> route = (HashMap<Integer, DataPoint>) ois.readObject();
        ois.close();
        return route;
    }

    /**
     *
     */
    private void writeFlightRoute (File toWrite, HashMap<Integer, DataPoint> route)
            throws IOException {
        // TODO how to write Hashmap to File?
        //  Serializable? new Class? Gson? (must not be readable, just loadable)
        var fos = new FileOutputStream(toWrite);
        var oos = new ObjectOutputStream(fos);
        oos.writeObject(route);
        oos.close();
        fos.close();
    }

}

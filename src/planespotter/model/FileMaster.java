package planespotter.model;

import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import planespotter.constants.Paths;
import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.display.UserSettings;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import java.io.*;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;

import static planespotter.constants.Configuration.*;

/**
 * file manager -> loads and saves files
 */
public class FileMaster {

    // controller instance
    private final Controller controller;

    /**
     * constructor
     */
    public FileMaster() {
        controller = Controller.getInstance();
    }

    /**
     * saves the config as a .cfg file
     */
    public Runnable saveConfig () {
        var fileWizard = new FileMaster();
        // saving / loading at the monment ?
        try {
            fileWizard.controller.getLogger().log("saving config...", fileWizard);
            var config = new File(Paths.SRC_PATH + "configuration.cfg");
            if (!config.exists()) { // creating new file if there is no existing one
                config.createNewFile();
            }
            var writer = new FileWriter(config);
            writer.write("maxThreadPoolSize: " + MAX_THREADPOOL_SIZE + "\n");
            writer.write("maxLoadedFlights: " + new UserSettings().getMaxLoadedData() + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fileWizard.controller.getLogger().sucsessLog("configuration.cfg saved sucsessfully!", fileWizard);
        }
        return this::saveConfig;
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
            var ctrl = Controller.getInstance();
            if (!ctrl.allMapData.isEmpty()) {
                this.writeFlightRoute(file, ctrl.allMapData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO will be replaced with log4j (-> saving logs)
    public void saveLogFile (String logged) {
        try {
            if (logged == null) {
                throw new IllegalArgumentException("logged data might not be null!");
            }
            var filename = "log_" + (this.hashCode()/2 + logged.hashCode()/2) + ".log";
            var file = new File("logs\\" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            this.writeLog(file, logged);
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

    private void writeLog (File file, String text) {
        try {
            var writer = new FileWriter(file);
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

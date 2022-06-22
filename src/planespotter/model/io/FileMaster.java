package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.Paths;
import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.MapData;
import planespotter.constants.UserSettings;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.Vector;

import static planespotter.constants.Configuration.*;

/**
 * file manager -> loads and saves files
 */
public class FileMaster {

    /**
     * constructor
     */
    public FileMaster() {
    }

    /**
     * saves the config as a .cfg file
     */
    public static void saveConfig() {
        var fileWizard = new FileMaster();
        // saving / loading at the monment ?
        try {
            Controller.getLogger().log("saving config...", fileWizard);
            var config = new File(Paths.SRC_PATH + "planespotter/ressources/configuration.cfg");
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
            Controller.getLogger().sucsessLog("configuration.cfg saved sucsessfully!", fileWizard);
        }
    }

    /**
     * saves a flight route in a .psp (.planespotter) file
     * @return the loaded route as a data point vector
     */
    public Vector<DataPoint> loadPlsFile(JFileChooser chooser) throws DataNotFoundException {
        var ctrl = Controller.getInstance();
        var file = chooser.getSelectedFile();
        if (file.exists()) {
            try {
                var route = this.readMapData(file).data();
                ctrl.done();
                return route;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        throw new DataNotFoundException("file couldn't be found!");
    }

    /**
     * saves a flight route in a .psp (.planespotter) file
     */
    public void savePlsFile(@NotNull MapData mapData, File file, Controller ctrl)
            throws DataNotFoundException, FileAlreadyExistsException {

        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new FileAlreadyExistsException("");
                }
            }
            if (!mapData.data().isEmpty()) {
                this.writeMapData(file, mapData);
            } else {
                throw new DataNotFoundException("Couldn't save flight route, loaded data is empty!");
            }
        } catch (FileAlreadyExistsException fae) {
            throw new FileAlreadyExistsException("File already exists");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            ctrl.done();
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
    private MapData readMapData(File file)
            throws ClassCastException, IOException, ClassNotFoundException {
        var fis = new FileInputStream(file);
        var ois = new ObjectInputStream(fis);
        var mapData = (MapData) ois.readObject();
        ois.close();
        fis.close();
        return mapData;
    }

    /**
     *
     */
    private void writeMapData(File toWrite, MapData mapData)
            throws IOException {
        // TODO how to write Hashmap to File?
        //  Serializable? new Class? Gson? (must not be readable, just loadable)
        var fos = new FileOutputStream(toWrite);
        var oos = new ObjectOutputStream(fos);
        oos.writeObject(mapData);
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

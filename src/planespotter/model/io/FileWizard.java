package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
 * @name FileMaster
 * @author jml04
 * @version 1.0
 *
 * class FileMaster is a file manager that loads and saves files
 */
public class FileWizard {

    private static final FileWizard fileWizard = new FileWizard();

    /**
     * constructor
     */
    private FileWizard() {
    }

    /**
     * saves the config as a .cfg file
     */
    public synchronized void saveConfig() {
        File config;
        Writer writer;
        try {
            Controller.getLogger().log("saving config...", fileWizard);
            config = new File(Paths.RESOURCE_PATH + "config.psc");
            if (!config.exists() && config.createNewFile()) { // creating new file if there is no existing one
                writer = new FileWriter(config);
                writer.write("maxThreadPoolSize: " + MAX_THREADPOOL_SIZE + "\n");
                writer.write("maxLoadedFlights: " + UserSettings.getMaxLoadedData() + "\n");
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Controller.getLogger().successLog("configuration saved sucsessfully!", fileWizard);
        }
    }

    /**
     * saves a flight route in a .psp (.planespotter) file
     * @return the loaded route as a data point vector
     */
    public synchronized Vector<DataPoint> loadPlsFile(JFileChooser chooser) throws DataNotFoundException {
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
    public synchronized void savePlsFile(@NotNull MapData mapData, File file, Controller ctrl)
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
    public synchronized void saveLogFile(@Nullable String prefixName, String logged) {
        try {
            if (logged == null) {
                throw new IllegalArgumentException("logged data might not be null!");
            }
            var prefix = (prefixName == null) ? "log_" : prefixName + "_";
            var filename = prefix + (this.hashCode()/2 + logged.hashCode()/2) + ".log";
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
    private synchronized MapData readMapData(File file)
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
    private synchronized void writeMapData(File toWrite, MapData mapData)
            throws IOException {

        var fos = new FileOutputStream(toWrite);
        var oos = new ObjectOutputStream(fos);
        oos.writeObject(mapData);
        oos.close();
        fos.close();
    }

    private synchronized void writeLog(File file, String text)
            throws IOException {

        var writer = new FileWriter(file);
        writer.write(text);
        writer.close();
    }

    public static FileWizard getFileWizard() {
        return fileWizard;
    }

}

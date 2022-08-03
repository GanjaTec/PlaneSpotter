package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.constants.Paths;
import planespotter.controller.Controller;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.MapData;
import planespotter.constants.UserSettings;
import planespotter.throwables.DataNotFoundException;
import planespotter.util.Logger;
import planespotter.util.Time;

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
     * saves the config as a .psc file
     * uses a FileWriter to write the config file,
     * no special file format, just '.psc' ending
     */
    public synchronized void saveConfig() {
        File config;
        Logger log = Controller.getInstance().getLogger();
        log.log("saving config...", fileWizard);
        config = new File(Paths.RESOURCE_PATH + "config.psc");
        if (!config.exists()) {// creating new file if there is no existing one
            try {
                config.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }
        try (Writer writer = new FileWriter(config)) {

                writer.write("maxThreadPoolSize: " + MAX_THREADPOOL_SIZE + "\n");
                writer.write("maxLoadedFlights: " + UserSettings.getMaxLoadedData() + "\n");
                log.successLog("configuration saved successfully!", fileWizard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * reads a flight route from a '.pls' file
     * takes the route data from a MapData object,
     * which is loaded from file
     *
     * @param selected is the selected file from the file chooser
     * @return the loaded route as a data point vector
     */
    @NotNull
    public synchronized MapData loadPlsFile(@NotNull File selected) throws DataNotFoundException {
        if (selected.exists()) {
            try {
                return this.readMapData(selected);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        throw new DataNotFoundException("Error! File not found or invalid MapData!");
    }

    /**
     * saves a flight route in a .pls file,
     * uses a MapData object to store the data
     */
    public synchronized void savePlsFile(@NotNull MapData mapData, @NotNull File file)
            throws DataNotFoundException, FileAlreadyExistsException {

        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new FileAlreadyExistsException("File already exists!");
                }
            }
            if (mapData.data().isEmpty()) {
                throw new DataNotFoundException("Couldn't save flight route, loaded data is empty!");
            }
            this.writeMapData(file, mapData);

        } catch (FileAlreadyExistsException fae) {
            throw new FileAlreadyExistsException("File already exists");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // TODO will be replaced with log4j (-> saving logs)
    public synchronized void saveLogFile(@Nullable String prefixName, @NotNull String logged) {
        try {
            String prefix = (prefixName == null) ? "log_" : prefixName + "_";
            String filename = prefix + Time.nowMillis() + ".log";
            File file = new File("logs\\" + filename);
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
    @NotNull
    private synchronized MapData readMapData(@NotNull File file)
            throws ClassCastException, IOException, ClassNotFoundException {

        // replaced default try-finally block with try-with-resource block,
        //  which has automatic resource-management
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            return (MapData) ois.readObject();
        }
    }

    /**
     *
     */
    private synchronized void writeMapData(@NotNull File toWrite, @NotNull MapData mapData)
            throws IOException {
        try (FileOutputStream fos = new FileOutputStream(toWrite);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(mapData);
        }
    }

    private synchronized void writeLog(@NotNull File file, @NotNull String text)
            throws IOException {

        try (FileWriter writer = new FileWriter(file)) {

            writer.write(text);
        }
    }

    @NotNull
    public static FileWizard getFileWizard() {
        return fileWizard;
    }

}

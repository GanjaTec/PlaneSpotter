package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.constants.Configuration;
import planespotter.controller.Controller;
import planespotter.dataclasses.MapData;
import planespotter.constants.UserSettings;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.InvalidDataException;
import planespotter.util.Logger;
import planespotter.util.Time;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;

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
     * saves the config as a .psc file using
     * UserSettings.write() for writing the configuration values.
     * Only non-final User-Settings are saved, static final fields
     * are ignored, because they are never updated
     */
    public synchronized boolean saveConfig() {
        File config;
        config = new File(Configuration.CONFIG_FILENAME);
        if (!config.exists()) { // creating new file if there is no existing one
            try {
                config.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return UserSettings.write(Configuration.CONFIG_FILENAME);
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
    public synchronized MapData loadPlsFile(@NotNull File selected)
            throws DataNotFoundException, InvalidDataException {

        if (selected.exists()) {
            try {
                return this.readMapData(selected);
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                throw new InvalidDataException("Map data is invalid, try another file!");
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

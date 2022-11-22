package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import planespotter.constants.Configuration;
import planespotter.dataclasses.MapData;
import planespotter.display.TreasureMap;
import planespotter.model.ConnectionManager;
import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.ExtensionException;
import planespotter.throwables.InvalidDataException;
import planespotter.util.Time;
import planespotter.util.Utilities;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @name FileWizard
 * @author jml04
 * @version 1.0
 *
 * @description
 * class FileWizard is a file manager that contains functions
 * to write and read different files with specific data
 */
public class FileWizard {

    // static FileWizard singleton instance
    private static final FileWizard fileWizard = new FileWizard();

    /**
     * private constructor,
     * used for static singleton instance
     */
    private FileWizard() {
    }

    /**
     * getter for static {@link FileWizard} singleton instance
     *
     * @return the {@link FileWizard} instance
     */
    @NotNull
    public static FileWizard getFileWizard() {
        return fileWizard;
    }

    /**
     * writes all {@link planespotter.model.ConnectionManager.Connection}s from a given {@link ConnectionManager}
     * to {@link File} with specific name
     *
     * @param filename is the filename for the connections-file (must end with '.psc')
     * @param cManager is the {@link ConnectionManager} where the
     *                 {@link planespotter.model.ConnectionManager.Connection}s are from
     * @throws IOException if an error occurs during the write operation
     * @throws ExtensionException if the file name has the wrong file extension
     */
    public void writeConnections(@NotNull String filename, @NotNull ConnectionManager cManager)
            throws IOException, ExtensionException {

        writeConnections(new File(filename), cManager);
    }

    /**
     * writes all {@link planespotter.model.ConnectionManager.Connection}s from a given {@link ConnectionManager}
     * to {@link File} with specific name
     *
     * @param file is the connections-{@link File} (name must end with '.psc')
     * @param cManager is the {@link ConnectionManager} where the
     *                 {@link planespotter.model.ConnectionManager.Connection}s are from
     * @throws IOException if an error occurs during the write operation
     * @throws ExtensionException if the file name has the wrong file extension
     */
    public void writeConnections(@NotNull File file, @NotNull ConnectionManager cManager)
            throws IOException, ExtensionException {

        if (!file.getName().endsWith(".psc")) {
            throw new ExtensionException("Only '.psc' extension allowed for connection file!");
        }
        try (Writer fw = new FileWriter(file);
             BufferedWriter buf = new BufferedWriter(fw)) {
            for (ConnectionManager.Connection conn : cManager.getConnections()) {
                buf.write(conn.name + ": " + conn.uri + ": " + conn.isMixWithFr24() + "\n");
            }
        }
    }

    /**
     * reads a connection-{@link File} to a map of {@link planespotter.model.ConnectionManager.Connection} names,
     * paired with the {@link planespotter.model.ConnectionManager.Connection}s itself
     *
     * @param filename is the filename for the connections-{@link File} (must end with '.psc')
     * @return {@link Map} of Connection names and Connections
     * @throws IOException if an error occurs during the writing process
     */
    public Map<String, ConnectionManager.Connection> readConnections(@NotNull String filename) throws IOException {
        return readConnections(new File(filename));
    }

    /**
     * reads a connection-{@link File} to a map of {@link planespotter.model.ConnectionManager.Connection} names,
     * paired with the {@link planespotter.model.ConnectionManager.Connection}s itself
     *
     * @param file is the connections-{@link File} (name must end with '.psc')
     * @return {@link Map} of Connection names and Connections
     * @throws IOException if an error occurs during the writing process
     */
    public Map<String, ConnectionManager.Connection> readConnections(@NotNull File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File '" + file.getName() + "' not found!");
        }
        try (Reader fr = new FileReader(file);
             BufferedReader buf = new BufferedReader(fr)) {
            return buf.lines()
                    .filter(line -> !line.isBlank() && line.contains(": "))
                    .map(line -> line.split(": "))
                    .filter(arr -> arr.length == 3)
                    .collect(Collectors.toMap(arr -> arr[0], arr -> new ConnectionManager.Connection(arr[0], arr[1], Boolean.parseBoolean(arr[2]))));
        } catch (ArrayIndexOutOfBoundsException aioob) {
            throw new InvalidDataException("Couldn't read config file!");
        }
    }

    /**
     * writes any type of image to a specific '.bmp' (bitmap) file
     *
     * @param img is the {@link Image} to be written
     * @param imgType is the {@link Image} type constant from {@link java.awt.image.BufferedImage}.'...'
     * @param file is the {@link File} where the {@link Image} is saved in
     * @throws IOException if an error occurs the writing process
     */
    public void writeBitmapImg(@NotNull Image img, int imgType, @NotNull File file) throws IOException {
        ImageIO.write(Utilities.createBufferedImage(img, imgType), "BMP", file);
    }

    /**
     * writes a {@link Configuration} to a specific {@link File}
     * {@see Configuration.CONFIG_FILENAME}
     *
     * @param config is the {@link Configuration} to be written
     * @param filename is the filename of the configuration file (must end with '.psc')
     * @throws IOException if an error occurs the writing process
     */
    public void writeConfig(@NotNull Configuration config, @NotNull String filename) throws IOException {
        if (filename.isBlank()) {
            throw new InvalidDataException("File name must not be blank!");
        }
        File file = new File(filename);
        if (!file.exists() && !file.createNewFile()) {
            file.createNewFile(); // result ignored
        }
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            String key; Object val;
            for (Map.Entry<String, Object> property : config.getUserProperties()) {
                key = property.getKey();
                val = property.getValue();
                if (!val.getClass().isPrimitive() && !(val instanceof String)) {
                    throw new NotSerializableException("Cannot serialize the current value: " + val);
                }
                bw.write(key + ": " + val);
            }
        }
    }

    /**
     * reads a {@link Configuration} from specific {@link File}
     *
     * @param filename is the {@link File} name of the configuration file (must end with '.psc')
     * @return array of {@link Object}s, the config values
     * @throws ExtensionException if the file name does not end with '.psc'
     * @throws FileNotFoundException if the {@link File} was not found
     */
    @NotNull
    public Object[] readConfig(@NotNull String filename)
            throws ExtensionException, FileNotFoundException, InvalidDataException {

        System.out.println("Reading configuration file...");
        if (!filename.endsWith(".psc")) {
            throw new ExtensionException("File must end with '.psc'");
        }
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException("Configuration file not found!");
        }
        try (Reader fileReader = new FileReader(file);
             BufferedReader buf = new BufferedReader(fileReader)) {
            return buf.lines()
                    .filter(line -> !line.startsWith("#") && line.contains(":"))
                    .map(line -> line.split(": "))
                    .filter(vals -> vals.length > 1)
                    .map(entry -> {
                        String key = entry[0],
                                value = entry[1];
                        switch (key) {
                            case "dataLimit", "gridSizeLat", "gridSizeLon" -> {
                                try {
                                    return Integer.parseInt(value);
                                } catch (NumberFormatException nfe) {
                                    throw new InvalidDataException("Couldn't parse String to Int!");
                                }
                            }
                            case "currentMapSource" -> {
                                return translateSource(value);
                            }
                            default -> throw new InvalidDataException("Couldn't read settings data!");
                        }
                    })
                    .toArray(Object[]::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new InvalidDataException("Couldn't read settings data!");
    }

    /**
     * translates a {@link String} to {@link TileSource}
     *
     * @param name is the source name
     * @return {@link TileSource} constant, coonverted from {@link String}
     * @throws InvalidDataException if the input {@link String} does not match a {@link TileSource} constant
     */
    @NotNull
    public TileSource translateSource(@NotNull String name) {
        return switch (name) {
            case "OSM" -> TreasureMap.OPEN_STREET_MAP;
            case "Public Transport" -> TreasureMap.TRANSPORT_MAP;
            case "Bing" -> TreasureMap.BING_MAP;
            default -> throw new InvalidDataException("Couldn't read map source data!");
        };
    }

    /**
     * reads a flight route from a '.pls' file
     * takes the route data from a MapData object,
     * which is loaded from file
     *
     * @param selected is the selected file from the file chooser (must end with '.psc')
     * @return the loaded route as a data point vector
     */
    @NotNull
    public synchronized MapData loadPlsFile(@NotNull File selected)
            throws InvalidDataException, FileNotFoundException {

        if (selected.exists()) {
            try {
                return this.readMapData(selected);
            } catch (IOException ioe) {
                throw new InvalidDataException("Map data is invalid, try another file!");
            }
        }
        throw new FileNotFoundException("Error! File not found!");
    }

    /**
     * saves a flight route in a .pls file,
     * uses a MapData object to store the data
     *
     * @param mapData is the {@link MapData} to be saved
     * @param file is the {@link File} where the map data is saved in (name must end with '.psc')
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

    /**
     * saves a log in a '.log' {@link File}
     *
     * @param prefixName is the log name prefix, may be null
     * @param logged is the logged text
     */
    // TODO will be replaced with log4j (-> saving logs)
    public synchronized void saveLogFile(@Nullable String prefixName, @NotNull String logged) {
        try {
            String prefix = (prefixName == null) ? "log_" : prefixName + "_";
            String filename = prefix + Time.nowMillis() + ".log";
            File file = new File("logs\\" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            writeLog(file, logged);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * reads a {@link MapData} object from specific {@link File}
     *
     * @param file is the file to read from (name must end with '.psc')
     * @return the flight route hash map
     * @throws IOException if an error occurs during the reading process
     */
    @NotNull
    private synchronized MapData readMapData(@NotNull File file)
            throws IOException {

        // replaced default try-finally block with try-with-resource block,
        //  which has automatic resource-management
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            try {
                return (MapData) ois.readObject();
            } catch (ClassNotFoundException | ClassCastException e) {
                throw new IOException("MapData object is invalid!", e);
            }
        }
    }

    /**
     *  writes a certain {@link MapData} object to {@link File}
     *
     * @param toWrite is the {@link File} where the {@link MapData} is written to (name must end with '.psc')
     * @param mapData is the {@link MapData} to be written
     * @throws IOException if an error occurs during the writing process
     */
    private synchronized void writeMapData(@NotNull File toWrite, @NotNull MapData mapData)
            throws IOException {
        try (FileOutputStream fos = new FileOutputStream(toWrite);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(mapData);
        }
    }

    /**
     * writes a log {@link String} to a certain {@link File}
     *
     * @param file is the {@link File} where the log is written to (name must end with '.psc')
     * @param text is the logged text to be written
     * @throws IOException if an error occurs during the writing process
     */
    private synchronized void writeLog(@NotNull File file, @NotNull String text)
            throws IOException {

        try (FileWriter writer = new FileWriter(file)) {

            writer.write(text);
        }
    }

}

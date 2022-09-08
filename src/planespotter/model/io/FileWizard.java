package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import planespotter.constants.Configuration;
import planespotter.controller.Controller;
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @name FileWizard
 * @author jml04
 * @version 1.0
 *
 * @description
 * class FileMaster is a file manager that contains functions
 * to write and read (save and load) files
 */
public class FileWizard {

    private static final FileWizard fileWizard = new FileWizard();

    /**
     * private constructor,
     * used for static instance
     */
    private FileWizard() {
    }

    public void writeConnections(@NotNull String filename, @NotNull ConnectionManager cManager) throws IOException {
        writeConnections(new File(filename), cManager);
    }

    public void writeConnections(@NotNull File file, @NotNull ConnectionManager cManager) throws IOException {
        try (Writer fw = new FileWriter(file);
             BufferedWriter buf = new BufferedWriter(fw)) {
            for (ConnectionManager.Connection conn : cManager.getConnections()) {
                buf.write(conn.name + ": " + conn.uri + "\n");
            }
        }
    }

    public Map<String, ConnectionManager.Connection> readConnections(@NotNull String filename) throws IOException {
        return readConnections(new File(filename));
    }

    public Map<String, ConnectionManager.Connection> readConnections(@NotNull File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File '" + file.getName() + "' not found!");
        }
        try (Reader fr = new FileReader(file);
             BufferedReader buf = new BufferedReader(fr)) {
            return buf.lines()
                    .filter(line -> !line.isBlank() && line.contains(": "))
                    .map(line -> line.split(": "))
                    .filter(arr -> arr.length == 2)
                    .collect(Collectors.toMap(arr -> arr[0], arr -> new ConnectionManager.Connection(arr[0], arr[1])));
        }
    }

    /**
     * writes any type of image to a specific '.bmp' (bitmap) file
     *
     * @param img is the {@link Image} to be written
     * @param imgType is the {@link Image} type constant from {@link java.awt.image.BufferedImage}.'...'
     * @param file is the {@link File} where the {@link Image} is saved in
     * @throws IOException
     */
    public void writeBitmapImg(@NotNull Image img, int imgType, @NotNull File file) throws IOException {
        ImageIO.write(Utilities.createBufferedImage(img, imgType), "BMP", file);
    }

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
     *
     *
     * @param filename
     * @return
     * @throws ExtensionException
     * @throws FileNotFoundException
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
     *
     *
     * @param name
     * @return
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
     * @param selected is the selected file from the file chooser
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
     *
     */
    private synchronized void writeMapData(@NotNull File toWrite, @NotNull MapData mapData)
            throws IOException {
        try (FileOutputStream fos = new FileOutputStream(toWrite);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(mapData);
        }
    }

    private synchronized void writeLog(@NotNull File file, OutputStream os) {

    }

    private synchronized void writeLog(@NotNull File file, PrintWriter writer) {
        // try to write System.out (PrintStream) into file

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

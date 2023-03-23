package planespotter.model.io;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import planespotter.constants.props.Configuration;
import planespotter.constants.props.Property;
import planespotter.constants.props.UserProperties;
import planespotter.dataclasses.ConnectionSource;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public String readUTF(@NotNull File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {

            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * writes all {@link ConnectionSource}s from a given {@link ConnectionManager}
     * to {@link File} with specific name
     *
     * @param filename is the filename for the connections-file (must be '.json' type)
     * @param cmng is the {@link ConnectionManager} where the {@link ConnectionSource}s are from
     * @throws IOException if an error occurs during the write operation
     * @throws ExtensionException if the file name has the wrong file extension
     */
    public void writeConnections(@NotNull String filename, @NotNull ConnectionManager cmng) throws IOException, ExtensionException {
        writeConnections(new File(filename), cmng);
    }

    /**
     * writes all {@link ConnectionSource}s from a given {@link ConnectionManager}
     * to {@link File} with specific name
     *
     * @param file is the connections file (must be '.json' type)
     * @param cmng is the {@link ConnectionManager} where the {@link ConnectionSource}s are from
     * @throws IOException if an error occurs during the write operation
     * @throws ExtensionException if the file name has the wrong file extension
     */
    public void writeConnections(@NotNull File file, @NotNull ConnectionManager cmng) throws IOException, ExtensionException {
        if (!file.getName().endsWith(".json")) {
            throw new ExtensionException("Only '.json' files allowed!");
        }
        Gson gson = new Gson();
        Collection<ConnectionSource> cons = cmng.getConnections();
        try (JsonWriter writer = gson.newJsonWriter(new FileWriter(file))) {
            writer.beginArray();
            for (ConnectionSource src : cons) {
                writer.beginObject()
                        .name("name").value(src.name)
                        .name("uri").value(src.uri.toString())
                        .name("connected").value(false)
                        .name("mixWithFr24").value(src.isMixWithFr24())
                        .endObject();
            }
            writer.endArray();
        }
    }

    /**
     * reads a connection-{@link File} to a map of {@link ConnectionSource} names,
     * paired with the {@link ConnectionSource}s itself
     *
     * @param filename is the filename for the connections-{@link File} (must end with '.psc')
     * @return {@link Map} of Connection names and Connections
     * @throws IOException if an error occurs during the writing process
     */
    public Map<String, ConnectionSource> readConnections(@NotNull String filename) throws IOException, ExtensionException {
        return readConnections(new File(filename));
    }

    /**
     * reads a connection-{@link File} to a map of {@link ConnectionSource} names,
     * paired with the {@link ConnectionSource}s itself
     *
     * @param file is the connections-{@link File} (name must end with '.psc')
     * @return {@link Map} of Connection names and Connections
     * @throws IOException if an error occurs during the writing process
     */
    public Map<String, ConnectionSource> readConnections(@NotNull File file) throws IOException, ExtensionException {
        if (!file.getName().endsWith(".json")) {
            throw new ExtensionException("Only '.json' files allowed!");
        }
        ConnectionSource src;
        Gson gson = new Gson();
        Map<String, ConnectionSource> map = new HashMap<>();
tc:     try (JsonReader reader = new JsonReader(new FileReader(file))) {
            reader.beginArray();
            while (reader.hasNext()) {
                try {
                    src = gson.fromJson(reader, ConnectionSource.class);
                } catch (JsonSyntaxException je) {
                    reader.endArray();
                    System.err.println("Reached end of JsonArray!");
                    break tc; // to skip second reader.endArray()
                }
                map.put(src.name, src);
            }
            reader.endArray();
        }
         return map;
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

    public void writeConfig(@NotNull Configuration config, @NotNull File file) throws IOException, ExtensionException {
        if (!file.getName().endsWith(".json")) {
            throw new ExtensionException("config file must end with '.json'");
        }
        Property[] props = config.getUserProperties().toArray(); // length always 4
        try (FileWriter fw = new FileWriter(file);
             JsonWriter jw = new JsonWriter(fw)) {
            jw.beginObject()
                    .name(props[0].key).value((int) props[0].val)
                    .name(props[1].key).value(props[1].val.toString())
                    .name(props[2].key).value((int) props[2].val)
                    .name(props[3].key).value((int) props[3].val)
                    .endObject();
        }
    }

    public Configuration readConfig(@NotNull File file) throws IOException, ExtensionException {
        if (!file.getName().endsWith(".json")) {
            throw new ExtensionException("config file must end with '.json'");
        }
        //List<Property> props = new ArrayList<>();
        UserProperties props = null;
        Gson gson = new Gson();
        try (FileReader fr = new FileReader(file);
             JsonReader jr = new JsonReader(fr)) {

            if (jr.hasNext()) {
                props = gson.fromJson(jr, UserProperties.class);
            }
        }
        if (props == null) {
            throw new IOException("Could not read UserProperties");
        }
        return new Configuration(props.toArray());
    }

    /**
     * translates a {@link String} to {@link TileSource}
     *
     * @param name is the source name
     * @return {@link TileSource} constant, converted from {@link String}
     * @throws InvalidDataException if the input {@link String} does not match a {@link TileSource} constant
     */
    @NotNull
    public TileSource translateSource(@NotNull String name) {
        return switch (name) {
            case "OSM" -> TreasureMap.OPEN_STREET_MAP;
            case "Transport Map" -> TreasureMap.TRANSPORT_MAP;
            case "Bing Map" -> TreasureMap.BING_MAP;
            default -> TreasureMap.OPEN_STREET_MAP;
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
    public MapData loadPlsFile(@NotNull File selected)
            throws InvalidDataException, FileNotFoundException {

        if (selected.exists()) {
            try {
                return readMapData(selected);
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
    public void savePlsFile(@NotNull MapData mapData, @NotNull File file)
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
            writeMapData(file, mapData);

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
    public void saveLogFile(@Nullable String prefixName, @NotNull String logged) {
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
    private MapData readMapData(@NotNull File file) throws IOException {

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
    private void writeMapData(@NotNull File toWrite, @NotNull MapData mapData)
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

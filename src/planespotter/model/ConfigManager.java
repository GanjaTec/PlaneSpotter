package planespotter.model;

import planespotter.throwables.FileAlreadyExistsException;

import java.io.*;

import static planespotter.constants.Paths.SRC_PATH;

public class ConfigManager {

    private static File config = new File(SRC_PATH + "config.cfg");
    private static FileWriter writer;
    private static FileReader fileReader;
    private static BufferedReader reader;

    public static void createConfig () throws FileAlreadyExistsException, FileNotFoundException {
        if (!config.exists()) {

            try {

                config = new File("/config.cfg");

                writer = new FileWriter(config);
                writer.write("maxFlightsLoaded: 2000");
                writer.close();

                
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {

        }
    }

    /**
     *
     */
    public static void loadCofnig () {
        try {
            createConfig();
            // loading logic / vars
        } catch (FileAlreadyExistsException | NullPointerException | FileNotFoundException e) {
            // loading logic
            // variables
        }
    }

    /**
     *
     */
    public static void saveConfig () {

    }

    /**
     *
     */
    public static File getConfig () {
        return config;
    }

}

package planespotter;

import planespotter.display.FileAlreadyExistsException;

import java.io.*;

public class ConfigManager {

    private static File config = new File("/config.cfg");
    private static FileWriter writer;
    private static FileReader fileReader;
    private static BufferedReader reader;

    public static void createConfig () throws FileAlreadyExistsException, FileNotFoundException {
        if (!config.exists()) {
            /*
            try {

                config = new File("/config.cfg");

                writer = new FileWriter(config);
                writer.write("test-boolean: true");
                writer.write("test-pw: 12345");
                writer.close();

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }*/
        } else throw new FileAlreadyExistsException();
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

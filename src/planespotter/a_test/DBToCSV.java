package planespotter.a_test;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.Paths;
import planespotter.model.io.CSVWriter;
import planespotter.throwables.DataNotFoundException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class DBToCSV {

    public static void dbToCSV(String dirName) throws SQLException {

        String dir = Paths.RESOURCE_PATH + dirName + (dirName.endsWith("\\") ? "" : "\\");
        if (new File(dir).mkdir()) {
            System.out.println("DB-Directory created successfully!");
        }
        try {
            planesToCSV(dir);
            flightsToCSV(dir);
            airlinesToCSV(dir);
            airportsToCSV(dir);
            trackingToCSV(dir);
        } catch (DataNotFoundException | IOException e) {
            throw new SQLException(e);
        }

    }

    private static void planesToCSV(@NotNull String path) throws SQLException, DataNotFoundException, IOException {
        File file = new File(path + "planes.csv");
        CSVWriter csv = new CSVWriter("SELECT * FROM planes");

        String[] header = new String[] {"ID", "tailnr", "icaonr", "registration", "type", "airline"};
        String[] types = new String[] {"int", "string", "string", "string", "string", "int"};
        csv.writeToCSV(file, header, types);
    }

    private static void airportsToCSV(@NotNull String path) throws SQLException, DataNotFoundException, IOException {
        File file = new File(path + "airports.csv");
        CSVWriter csv = new CSVWriter("SELECT * FROM airports");
        String[] header = new String[] {"ID", "iatatag", "name", "country", "lat", "lon"},
                types  = new String[] {"int", "string", "string", "string", "double", "double"};
        csv.writeToCSV(file, header, types);
    }

    private static void trackingToCSV(@NotNull String path) throws SQLException, DataNotFoundException, IOException {
        File file = new File(path + "tracking.csv");
        CSVWriter csv = new CSVWriter("SELECT * FROM tracking");

        String[] header = new String[] {"ID", "flightid", "latitude", "longitude", "altitude", "groundspeed", "heading", "squawk", "timestamp"};
        String[] types = new String[] {"int", "int", "double", "double", "int", "int", "int", "int", "long"};
        csv.writeToCSV(file, header, types);
    }

    private static void flightsToCSV(@NotNull String path) throws SQLException, DataNotFoundException, IOException {
        File file = new File(path + "flights.csv");
        CSVWriter csv = new CSVWriter("SELECT * FROM flights");

        String[] header = new String[] {"ID", "plane", "src", "dest", "flightnr", "callsign", "start", "endTime"},
                 types  = new String[] {"int", "string", "string", "string", "string", "string", "string", "string"};
        csv.writeToCSV(file, header, types);
    }

    private static void airlinesToCSV(@NotNull String path) throws SQLException, DataNotFoundException, IOException {
        File file = new File(path + "airlines.csv");
        CSVWriter csv = new CSVWriter("SELECT * FROM airlines");

        String[] header = new String[] {"ID", "icaotag", "name", "country"},
                 types  = new String[] {"int", "string", "string", "string"};
        csv.writeToCSV(file, header, types);
    }


}

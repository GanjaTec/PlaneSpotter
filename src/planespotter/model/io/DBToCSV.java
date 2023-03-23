package planespotter.model.io;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.Paths;
import planespotter.throwables.DataNotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public class DBToCSV {

    private static boolean debug = false;
    private static final String DBG = "[DBToCSV-DEBUG]: ";


    /*public static void main(String[] args) throws SQLException, FileNotFoundException {
        String arg;
        if (args.length == 0 || (arg = args[0]).isBlank()) {
            arg = "DatabaseCSV";
        }
        dbToCSV(arg);
    }*/

    public static void dbToCSV(@NotNull String dirName, boolean separated) throws SQLException, IOException, DataNotFoundException {
        String dir = dirName + (dirName.endsWith("\\") ? "" : "\\");
        File dirFile = new File(dir);
        if (dirFile.mkdir()) {
            System.out.println("DB-Directory created successfully!");
        }
        if (separated) {
            dbToSeparatedCSVs(dir);
        } else {
            dbToCombinedCSV(dir);
        }
    }

    private static void dbToSeparatedCSVs(String dir) throws SQLException, FileNotFoundException {

        try {
            planesToCSV(dir);
            System.out.println(dir + "planes.csv written successfully!");
            flightsToCSV(dir);
            System.out.println(dir + "flights.csv written successfully!");
            airlinesToCSV(dir);
            System.out.println(dir + "airlines.csv written successfully!");
            airportsToCSV(dir);
            System.out.println(dir + "airports.csv written successfully!");
            trackingToCSV(dir);
            System.out.println(dir + "tracking.csv written successfully!");
        } catch (DataNotFoundException | IOException e) {
            if (e instanceof FileNotFoundException fnf) {
                throw fnf;
            }
            throw new SQLException(e);
        }

    }

    private static void dbToCombinedCSV(String path) throws SQLException, DataNotFoundException, IOException {
        File file = new File(path + "DBDataCombined.csv");
        String sql =
                "SELECT " +
                    "t.ID AS TID, t.flightid AS FID, t.latitude AS lat, t.longitude AS lon, t.altitude AS alt, " +
                    "t.groundspeed AS speed, t.heading AS track, t.squawk, t.timestamp," +
                    "f.plane AS PID, f.src, f.dest, f.flightnr AS FNR, f.callsign, f.start AS startTime, f.endTime," +
                    "p.tailnr AS plane_TNR, p. icaonr AS plane_icao, p.registration AS plane_reg, p.type AS plane_type, p.airline AS ALID," +
                    "ap.ID AS APID, ap.iatatag AS ap_iata, ap.name AS ap_name, ap.country AS ap_country, ap.lat AS ap_lat, ap.lon AS ap_lon," +
                    "al.icaotag AS al_icao, al.name AS al_name, al.country AS al_country " +
                "FROM tracking t, flights f, planes p, airports ap, airlines al " +
                "WHERE " +
                            "t.flightid = f.ID " +
                        "AND f.plane = p.ID " +
                        "AND ((f.src IS ap.iatatag) OR (f.dest IS ap.iatatag)) " +
                        "AND p.airline = al.ID";

        /*String sql2 = "SELECT " +
                "t.ID AS TID, t.flightid AS FID, t.latitude AS lat, t.longitude AS lon, t.altitude AS alt, " +
                "t.groundspeed AS speed, t.heading AS track, t.squawk, t.timestamp," +
                "f.plane AS PID, f.src, f.dest, f.flightnr AS FNR, f.callsign, f.start AS startTime, f.endTime," +
                "p.tailnr AS plane_TNR, p. icaonr AS plane_icao, p.registration AS plane_reg, p.type AS plane_type, p.airlinr AS ALID," +
                "ap.ID AS APID, ap.iatatag AS ap_iata, ap.name AS ap_name, ap.country AS ap_country, ap.lat AS ap_lat, ap.lon AS ap_lon," +
                "al.icaotag AS al_icao, al.name AS al_name, al.country AS al_country " +
                "JOIN flights f ON f.ID = t.flightid " +
                "JOIN planes p ON p.ID = f.plane " +
                "JOIN airports ap ON ((ap.icaotag IS f.src) OR (ap.icaotag IS f.dest)) " +
                "JOIN airlines al ON al.ID = p.airline " +
                "FROM tracking t";*/
        BufferedCSVWriter writer = new BufferedCSVWriter(sql);
        if (inDebugMode())
            System.out.println(DBG + "CSVWriter created");

        //"ID", "icaotag", "name", "country"

        String[] header = new String[] {
            "TID", "FID", "lat", "lon", "alt", "speed", "track", "squawk", "timestamp", "PID", "src", "dest", "FNR",
            "callsign", "startTime", "endTime", "plane_TNR", "plane_icao", "plane_reg", "plane_type", "ALID", "APID",
            "ap_iata", "ap_name", "ap_country", "ap_lat", "ap_lon", "al_icao", "al_name", "al_country"};
        String[] types = new String[] {
            "int", "int", "double", "double", "int", "int", "int", "int", "long", "int", "string", "string", "string",
            "string", "string", "string", "string", "string", "string", "string", "int", "int", "string",
            "string", "string", "double", "double", "string", "string", "string"
        };

        if (inDebugMode())
            System.out.println(DBG + "starting to write");
        writer.writeToCSV(file, header, types);
        if (inDebugMode())
            System.out.println(DBG + "finished write");



    }

    private static void planesToCSV(@NotNull String path) throws SQLException, DataNotFoundException, IOException {
        File file = new File(path + "planes.csv");
        BufferedCSVWriter csv = new BufferedCSVWriter("SELECT * FROM planes");

        String[] header = new String[] {"ID", "tailnr", "icaonr", "registration", "type", "airline"};
        String[] types = new String[] {"int", "string", "string", "string", "string", "int"};
        csv.writeToCSV(file, header, types);
    }

    private static void airportsToCSV(@NotNull String path) throws SQLException, DataNotFoundException, IOException {
        File file = new File(path + "airports.csv");
        BufferedCSVWriter csv = new BufferedCSVWriter("SELECT * FROM airports");
        String[] header = new String[] {"ID", "iatatag", "name", "country", "lat", "lon"},
                types  = new String[] {"int", "string", "string", "string", "double", "double"};
        csv.writeToCSV(file, header, types);
    }

    private static void trackingToCSV(@NotNull String path) throws SQLException, DataNotFoundException, IOException {
        File file = new File(path + "tracking.csv");
        BufferedCSVWriter csv = new BufferedCSVWriter("SELECT * FROM tracking");

        String[] header = new String[] {"ID", "flightid", "latitude", "longitude", "altitude", "groundspeed", "heading", "squawk", "timestamp"};
        String[] types = new String[] {"int", "int", "double", "double", "int", "int", "int", "int", "long"};
        csv.writeToCSV(file, header, types);
    }

    private static void flightsToCSV(@NotNull String path) throws SQLException, DataNotFoundException, IOException {
        File file = new File(path + "flights.csv");
        BufferedCSVWriter csv = new BufferedCSVWriter("SELECT * FROM flights");

        String[] header = new String[] {"ID", "plane", "src", "dest", "flightnr", "callsign", "start", "endTime"},
                 types  = new String[] {"int", "string", "string", "string", "string", "string", "string", "string"};
        csv.writeToCSV(file, header, types);
    }

    private static void airlinesToCSV(@NotNull String path) throws SQLException, DataNotFoundException, IOException {
        File file = new File(path + "airlines.csv");
        BufferedCSVWriter csv = new BufferedCSVWriter("SELECT * FROM airlines");

        String[] header = new String[] {"ID", "icaotag", "name", "country"},
                 types  = new String[] {"int", "string", "string", "string"};
        csv.writeToCSV(file, header, types);
    }

    public static boolean inDebugMode() {
        return debug;
    }

    public static void setDebugMode(boolean mode) {
        debug = mode;
    }
}

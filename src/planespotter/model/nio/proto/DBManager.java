package planespotter.model.nio.proto;

import planespotter.model.io.SupperDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @name DBConnector
 * @author jml04
 * @version 1.0
 *
 * class DBConnector creates a connection to the DB
 */
public abstract class DBManager extends SupperDB {

    // writing boolean, true when writing
    protected static volatile boolean writing = false;

    /**
     * @return DB-Connection or null, if an error occurred
     */
    public static Connection connect() {
        try {
            return getDBConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}

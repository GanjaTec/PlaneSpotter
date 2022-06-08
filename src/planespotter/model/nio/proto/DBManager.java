package planespotter.model.nio.proto;

import planespotter.model.io.SupperDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    public static void executeSQL(Connection conn, PreparedStatement... stmts)
            throws SQLException {

        for (var stmt : stmts) {
            stmt.setFetchSize(10000);
            stmt.executeBatch();
            stmt.closeOnCompletion();
        }
        conn.close();
    }

}

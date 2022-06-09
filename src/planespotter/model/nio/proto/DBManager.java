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

    public static int[] executeQuery(PreparedStatement pstmt)
            throws SQLException {

        var ids = new int[0];
        pstmt.setFetchSize(1000);
        pstmt.executeBatch();
        var generatedKeys = pstmt.getGeneratedKeys();
        int rowID;
        while (generatedKeys.next()) {
            rowID = generatedKeys.getInt(1);
            int length = ids.length;
            ids = Arrays.copyOf(ids, length + 1);
            ids[length] = rowID;
        }
        generatedKeys.close();
        pstmt.close();
        return ids;
    }

    public static void executeSQL(Connection conn, PreparedStatement... stmts)
            throws SQLException {

        for (var stmt : stmts) {
            stmt.setFetchSize(2000); // TODO beste?
            stmt.executeBatch();
            stmt.closeOnCompletion();
        }
        conn.close();
    }

}

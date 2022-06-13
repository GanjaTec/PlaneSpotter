package planespotter.dataclasses;

import planespotter.model.SupperDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @name DBResult
 * @author jml04
 * @version 1.0
 *
 * record DBResult is a DB-Result that contains a ResultSet and a Connection
 */
public record DBResult(ResultSet resultSet, Connection connection) {

    /**
     * closes a db result
     *
     * @throws SQLException if there is an error with sql
     */
    public void close() throws SQLException {
        resultSet.close();
        connection.close();
        SupperDB.sqlReady();
    }

}

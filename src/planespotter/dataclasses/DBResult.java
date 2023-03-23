package planespotter.dataclasses;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @name DBResult
 * @author jml04
 * @version 1.0
 *
 * @description
 * record DBResult is a DB-Result that contains a ResultSet and a Connection
 */
public record DBResult(ResultSet resultSet, Connection connection)
        implements AutoCloseable {

    /**
     * closes a db result
     *
     * @throws SQLException if there is an error with sql
     */
    @Override
    public void close() throws SQLException {
        resultSet.close();
        connection.close();
    }

}

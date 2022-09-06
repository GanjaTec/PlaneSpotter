package planespotter.model.io;

import org.jetbrains.annotations.NotNull;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import planespotter.controller.Controller;
import planespotter.dataclasses.DBResult;
import planespotter.throwables.InvalidDataException;
import planespotter.throwables.NoAccessException;

import java.sql.*;
import java.util.Arrays;

/**
 * @name DBConnector
 * @author Lukas
 * @author jml04
 * @version 1.1
 *
 * @description
 * Class DBConnector represents a Database-Connector,
 * which is able to do general actions on the database.
 * It is used to reduce redundant code in the DB-subclasses.
 * It also prepares you a nice, warm supper.
 *
 */
public abstract sealed class DBConnector
		permits DBIn, DBOut {

	// database monitor object
	@NotNull protected static final Object DB_SYNC;

	// database name
	@NotNull public static final String DB_NAME;

	// database URL
	@NotNull private static final String DB_URL;

	// database Source-Object
	@NotNull private static final SQLiteDataSource DATABASE;

	// initializing Database
	static {
		// setting database monitor object
		DB_SYNC = new Object();
		// setting database name and URL
		DB_NAME = "plane.db";
		DB_URL = "jdbc:sqlite:" + DB_NAME;
		// setting up database source
		DATABASE = new SQLiteDataSource();
		DATABASE.setUrl(DB_URL);
		DATABASE.setDatabaseName(DB_NAME);

		// creating database config
		SQLiteConfig dbConfig = new SQLiteConfig();
		dbConfig.setReadUncommited(true);
		dbConfig.setSharedCache(true);
		dbConfig.setCacheSize(100);
		dbConfig.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
		dbConfig.setLockingMode(SQLiteConfig.LockingMode.NORMAL);

		DATABASE.setConfig(dbConfig);

	}

	/**
	 * opens a database-connection
	 *
	 * @return the database connection
	 * @throws SQLException if the database cannot be accessed
	 */
	@NotNull
	protected static Connection getConnection(boolean readOnly)
			throws SQLException {

		DATABASE.setReadOnly(readOnly);
		return DATABASE.getConnection();
	}

	/**
	 * creates a PreparedStatement with given SQL-statement
	 * in the form 'SELECT ... FROM ... WHERE ID = (?)',
	 * where (?) are placeholders
	 *
	 * @param sql is the SQL-statement
	 * @return PreparedStatement created by the given SQL-statement
	 * @throws SQLException if an error occurs while creating the PreparedStatement
	 */
	@NotNull
	protected PreparedStatement createPreparedStatement(@NotNull String sql, boolean readOnly)
			throws SQLException {

		if (sql.isBlank()) {
			throw new InvalidDataException("SQL-String must not be blank!");
		}
		PreparedStatement pstmt = getConnection(readOnly).prepareStatement(sql);
		pstmt.setPoolable(true);
		return pstmt;
	}
	
	/**
	 * This method is used to query the DB
	 * it takes a String and returns a DBResult,
	 * WARNING: use this method for READ-ONLY queries only!
	 * 
	 * @param query String to use for the Query
	 * @return DBResult containing the queried Data and the DB-connection
	 */
	@NotNull
	protected DBResult queryDB(@NotNull final String query)
			throws NoAccessException {

			try {
				// we won't close these AutoCloseables
				Connection conn = getConnection(true);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				// returning new DBResult Object
				return new DBResult(rs, conn);
				// no auto-close for the used AutoCloseables, because
				// the DBResult is needed after this method invocation
			} catch (SQLException e) {
				// TODO: 05.09.2022 throw here
				Controller.getInstance().handleException(e);
				e.printStackTrace();
			}
			throw new NoAccessException("SupperDB.queryDB: Couldn't find any Data or an error occurred!");
	}

}

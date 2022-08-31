package planespotter.model.io;

import org.jetbrains.annotations.NotNull;

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
	@NotNull private static final SQLiteDataSource database;

	//private static Connection connection;

	// initializing Database
	static {
		// setting database monitor object
		DB_SYNC = new Object();
		// setting database name and URL
		DB_NAME = "plane.db";
		DB_URL = "jdbc:sqlite:" + DB_NAME;
		// setting up database source
		database = new SQLiteDataSource();
		database.setUrl(DB_URL);
		database.setDatabaseName(DB_NAME);
		// allowing the database to accept several read-connections at once
		database.setReadUncommited(true);
		// enabling the shared cache for the database
		database.setSharedCache(true);
		// setting DB synchronous mode to 'normal'
		database.setSynchronous("NORMAL");
		// setting DB locking mode to 'normal'
		database.setLockingMode("NORMAL");

		/*try {
			connection = database.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			connection = null;
		}*/
	}

	/**
	 * opens a database-connection
	 *
	 * @return the database connection
	 * @throws SQLException if the database cannot be accessed
	 */
	@NotNull
	protected static Connection getConnection()
			throws SQLException {

		return database.getConnection();
		//return connection == null ? (connection = database.getConnection()) : connection;
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
	protected PreparedStatement createPreparedStatement(@NotNull String sql)
			throws SQLException {

		if (sql.isBlank()) {
			throw new InvalidDataException("SQL-String must not be blank!");
		}
		return getConnection().prepareStatement(sql);
	}
	
	/**
	 * This method is used to query the DB
	 * it takes a String and returns a DBResult
	 * 
	 * @param query String to use for the Query
	 * @return DBResult containing the queried Data and the DB-connection
	 */
	@NotNull
	protected DBResult queryDB(@NotNull final String query)
			throws NoAccessException {

			try {
				// we won't close these AutoCloseables
				Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				// returning new DBResult Object
				return new DBResult(rs, conn);
				// no auto-close for the used AutoCloseables, because
				// the DBResult is needed after this method invocation
			} catch (SQLException e) {
				Controller.getInstance().handleException(e);
				e.printStackTrace();
			}
			throw new NoAccessException("SupperDB.queryDB: Couldn't find any Data or an error occurred!");
	}

	/**
	 * executes a PreparedStatement
	 *
	 * @param pstmt is the PreparedStatement to execute
	 * @return ID's of the inserted records (generated Keys)
	 * @throws SQLException if there is an error with SQL
	 */
	@Deprecated(since = "SupplierTry, too specific", forRemoval = true)
	protected static int[] executeSQL(@NotNull PreparedStatement pstmt)
			throws SQLException {

		int[] ids = new int[0];
		pstmt.setFetchSize(1000); // TODO beste?
		pstmt.executeBatch();
		ResultSet generatedKeys = pstmt.getGeneratedKeys();
		int rowID, length;
		while (generatedKeys.next()) {
			rowID = generatedKeys.getInt(1);
			length = ids.length;
			ids = Arrays.copyOf(ids, length + 1);
			ids[length] = rowID;
		}
		generatedKeys.close();
		pstmt.close();
		return ids;
	}

	/**
	 * executes multiple PreparedStatements
	 *
	 * @param conn is the database Connection
	 * @param stmts are the PreparedStatements
	 * @throws SQLException if there is an error with SQL
	 */
	@Deprecated(since = "SupplierTry, too specific", forRemoval = true)
	protected static void executeSQL(@NotNull Connection conn, @NotNull PreparedStatement... stmts)
			throws SQLException {

		for (PreparedStatement stmt : stmts) {
			stmt.setFetchSize(2000); // TODO beste?
			stmt.executeBatch();
			stmt.closeOnCompletion();
		}
		conn.close();
	}
}

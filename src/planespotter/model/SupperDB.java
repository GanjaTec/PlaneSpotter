package planespotter.model;

import org.sqlite.SQLiteDataSource;
import planespotter.controller.Controller;
import planespotter.dataclasses.DBResult;
import planespotter.throwables.InvalidDataException;
import planespotter.throwables.NoAccessException;

import java.sql.*;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @name SupperDB
 * @author Lukas
 * @author jml04
 * @version 1.1
 *
 * Class SupperDB represents a Database Manager,
 * which is able to do general actions on the database.
 * It is used to reduce redundant code in the DB-subclasses.
 * It also prepares you a nice, warm supper.
 */
// TODO evtl klasse umbenennen, für einen aussagekräftigen namen, sowas wie Database oder DBManager
public abstract class SupperDB {
	// writing boolean, true when writing
	public static final Object dbLock;
	// database name
	public static final String DB_NAME;
	// database URL
	private static final String DB_URL;
	// database Source-Object
	private static final SQLiteDataSource database;

	static {
		// setting sqlBusy to false
		dbLock = new Object();
		// setting final database Strings
		DB_NAME = "plane.db";
		DB_URL = "jdbc:sqlite:" + DB_NAME;
		// setting up database source
		database = new SQLiteDataSource();
		database.setUrl(DB_URL);
		database.setDatabaseName(DB_NAME);
	}

	protected static Connection getDBConnection()
			throws ClassNotFoundException, SQLException, NoAccessException {

		// TODO: 15.06.2022 sollte es vielleicht im ganzen Programm nur eine dauerhafte Connection geben?
		return database.getConnection();
	}
	
	/**
	 * This method is used to querry the DB
	 * it takes a String and returns a ResultSet
	 * 
	 * @param querry String to use for the Query
	 * @return ResultSet containing the queried Data
	 */
	protected DBResult queryDB(final String querry) throws NoAccessException {
			try {
				Connection conn = getDBConnection();
				Statement stmt = conn.createStatement();
				ResultSet query = stmt.executeQuery(querry);
				// returning new DBResult Object
				return new DBResult(query, conn);
			} catch (SQLException | ClassNotFoundException e) {
				Controller.getInstance().handleException(e);
				e.printStackTrace();
			}
			throw new InvalidDataException("SupperDB.queryDB: Couldn't find any Data or an error occurred!");
	}

	/**
	 * executes a PreparedStatement
	 *
	 * @param pstmt is the PreparedStatement to execute
	 * @return ID's of the inserted records (generated Keys)
	 * @throws SQLException if there is an error with SQL
	 */
	public static int[] executeSQL(PreparedStatement pstmt)
			throws SQLException {

		var ids = new int[0];
		pstmt.setFetchSize(1000); // TODO beste?
		pstmt.executeBatch();
		var generatedKeys = pstmt.getGeneratedKeys();
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

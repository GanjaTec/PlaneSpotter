package planespotter.model;

import planespotter.controller.Controller;
import planespotter.dataclasses.DBResult;
import planespotter.throwables.InvalidDataException;
import planespotter.throwables.NoAccessException;

import java.sql.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used to reduce redundant in the DB subclasses. It also prepares you a nice, warm supper.
 * 
 * @author Lukas
 *
 */
public class SupperDB {

	// writing boolean, true when writing
	protected static volatile AtomicBoolean sqlBusy = new AtomicBoolean(false);

	protected static Connection getDBConnection()
			throws ClassNotFoundException, SQLException, NoAccessException {

		//if (!sqlBusy) {
			sqlBusy.set(true);
			Class.forName("com.mysql.cj.jdbc.Driver");
			String db = "jdbc:sqlite:plane.db";
			return DriverManager.getConnection(db);
		//}
		//throw new NoAccessException("Database is locked, probably writing...");
	}

	/**
	 * sets sqlBusy to false,
	 * executed when a db-connection is closed
	 */
	public static synchronized void sqlReady() {
		sqlBusy.set(false);
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
			Connection conn = getDBConnection(); // TODO close
			Statement stmt = conn.createStatement();
			ResultSet query = stmt.executeQuery(querry);
			return new DBResult(query, conn);
		} catch (SQLException | ClassNotFoundException e) {
			Controller.getInstance().handleException(e);
			e.printStackTrace();
		}
		throw new InvalidDataException("Couldn't find any Data or an error occurred in queryDB()!");
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

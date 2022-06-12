package planespotter.model.io;

import planespotter.throwables.DataNotFoundException;
import planespotter.throwables.InvalidDataException;

import java.sql.*;
import java.util.Arrays;

/**
 * This class is used to reduce redundant in the DB subclasses. It also prepares you a nice, warm supper.
 * 
 * @author Lukas
 *
 */
public class SupperDB {

	// writing boolean, true when writing
	protected static volatile boolean writing = false;

	protected static Connection getDBConnection()
			throws ClassNotFoundException, SQLException {

		Class.forName("com.mysql.cj.jdbc.Driver");
		String db = "jdbc:sqlite:plane.db";
		return DriverManager.getConnection(db);
	}
	
	/**
	 * This method is used to querry the DB
	 * it takes a String and returns a ResultSet
	 * 
	 * @param querry String to use for the Querry
	 * @return ResultSet containing the querried Data
	 */
	protected ResultSet queryDB(final String querry) {

		try {
			Connection conn = getDBConnection();
			Statement stmt = conn.createStatement();
			return stmt.executeQuery(querry);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		throw new InvalidDataException("Couldn't find any Data or an error occured!");
	}
	
	/**
	 * @deprecated
	 * 
	 * @return
	 */
	protected ResultSet executePS() {
		ResultSet rs = null;
		
		
		
		return rs;
	}

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

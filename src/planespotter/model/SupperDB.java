package planespotter.model;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * This class is used to recude redundant in the DB subclasses. It also prepares you a nice, warm supper.
 * 
 * @author Lukas
 *
 */
public class SupperDB {

	protected static Connection getDBConnection() throws Exception {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String db = "jdbc:sqlite:plane.db";
		Connection conn = DriverManager.getConnection(db);
		return conn;
	}
}

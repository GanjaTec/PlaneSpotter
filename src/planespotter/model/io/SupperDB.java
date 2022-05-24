package planespotter.model.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

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
		return DriverManager.getConnection(db);
	}
	
	/**
	 * This method is used to querry the DB
	 * it takes a String and returns a ResultSet
	 * 
	 * @param querry String to use for the Querry
	 * @return ResultSet containing the querried Data
	 */
	protected ResultSet querryDB(String querry) throws Exception {
		ResultSet rs;
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		rs = stmt.executeQuery(querry);

		return rs;
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
}

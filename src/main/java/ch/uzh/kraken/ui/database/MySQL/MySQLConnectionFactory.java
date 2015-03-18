package ch.uzh.kraken.ui.database.MySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.kraken.ui.util.Util;

public class MySQLConnectionFactory {
	private static Logger log = LoggerFactory.getLogger(MySQLConnectionFactory.class);
	
	public static Connection getMySQlConnection(){
		Connection connection = null;
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");

			Properties properties = Util.readProperties("db.properties");

			String dbHost = properties.getProperty("host");
			String dbDatabase = properties.getProperty("database");
			String dbUser = properties.getProperty("user");
			String dbPassword = properties.getProperty("password");
			String dbUseUnicode = properties.getProperty("useUnicode", "true");
			String dbCharacterEncoding = properties.getProperty("characterEncoding", "UTF-8");

			String url = dbHost + "/" + dbDatabase;
			url += "?user=" + dbUser + "&password=" + dbPassword;
			url += "&useUnicode=" + dbUseUnicode + "&characterEncoding=" + dbCharacterEncoding;
			log.debug("Connecting to DB: {}", url);
			connection = DriverManager.getConnection(url);
			log.info("Database connection established");
		}
		catch(ClassNotFoundException e) {
			log.error("MySQL driver class not found!");
			e.printStackTrace();
		}
		catch(SQLException e) {
			log.error("SQL Exception during connection to database");
			e.printStackTrace();
		}
		return connection;
	}
}

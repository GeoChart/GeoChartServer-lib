package ch.uzh.kraken.ui.database.influxDB;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.kraken.ui.util.Util;

public class InfluxDBConnection {
	
	private static Logger log = LoggerFactory.getLogger(InfluxDBConnection.class);
	public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
	
	private InfluxDB db;
	private String databaseName;

	public InfluxDBConnection() {
		this("db.properties");
	}
	
	public InfluxDBConnection(String propertiesFile){
			Properties properties = Util.readProperties(propertiesFile);
			String url = properties.getProperty("host");
			databaseName = properties.getProperty("database");
			String user = properties.getProperty("user");
			String password = properties.getProperty("password");
			log.info("Connecting to influx db {}: {} with username: {} and password: ******", databaseName, url, user);
			db = InfluxDBFactory.connect(url, user, password);
			log.info("Database connection established");
	}
	
	public List<Serie> executeQuery(String query){
		log.debug("Executing query: {}", query);
		long start = System.currentTimeMillis();
		List<Serie> list = db.query(databaseName, query, DEFAULT_TIME_UNIT);
		log.debug("Query executed in: {} ms", System.currentTimeMillis() - start);
		return list;
		
		
	}
}

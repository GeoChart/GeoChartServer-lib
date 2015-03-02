package ch.uzh.kraken.ui.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.kraken.ui.util.Util;

public class TorrentDetails {

	private static final Logger log = LoggerFactory.getLogger(TorrentDetails.class.getName());
	
	private Connection connection = MySQLConnectionFactory.getMySQlConnection();
			
	public TorrentDetails() {
		
	}

	@SuppressWarnings("unchecked")
	public String getTorrentDetailsFromInfoHashAsJson(String infoHash) {
		final ResultSet resultSet;
		final JSONObject json = new JSONObject();
		try {
			PreparedStatement statement = getTitleFromInfoHashStatement(infoHash);
			resultSet = statement.executeQuery();
			resultSet.next();
			json.put("TITLE", resultSet.getString("title"));
			json.put("FILESIZE", Util.beautifyFilesize(resultSet.getLong("filesize")));
			json.put("PUBLISH_DATE", resultSet.getString("publishDate"));
		}
		catch(SQLException e) {
			log.error("SQL Exception occurred");
			e.printStackTrace();
		}
		return json.toString();
	}

	private PreparedStatement getTitleFromInfoHashStatement(String infoHash) throws SQLException {
		String sql = "";
		PreparedStatement statement;

		sql += "SELECT title, publish_date AS publishDate, filesize ";
		sql += "FROM statistics_torrents ";
		sql += "WHERE HEX(info_hash) = ? ;";

		statement = connection.prepareStatement(sql);
		statement.setString(1, infoHash);
		return statement;
	}
}
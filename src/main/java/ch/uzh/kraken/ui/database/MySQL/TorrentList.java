package ch.uzh.kraken.ui.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.kraken.ui.util.JsonErrors;
import ch.uzh.kraken.ui.util.Util;
import ch.uzh.kraken.ui.util.Validation;

public class TorrentList {

	private static final Logger log = LoggerFactory.getLogger(TorrentList.class.getName());

	public TorrentList() {
		super();
	}

	@SuppressWarnings("unchecked")
	public String getTorrentListAsJson(String date) {
		final JSONObject json = new JSONObject();
		final JSONArray list = new JSONArray();
		final ResultSet resultSet;

		if(!Validation.validateDate(date)) {
			return JsonErrors.createStandardJsonError().toString();
		}

		try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
		{
			PreparedStatement statement = getTorrentListHashStatement(date, connection);
			resultSet = statement.executeQuery();

			while(resultSet.next()) {
				JSONObject entry = new JSONObject();
				entry.put("TITLE", resultSet.getString("title"));
				entry.put("INFO_HASH", resultSet.getString("infoHash"));
				entry.put("FILESIZE", Util.beautifyFilesize(resultSet.getLong("filesize")));
				entry.put("PUBLISH_DATE", resultSet.getString("publishDate"));
				entry.put("OBSERVED_PEERS", resultSet.getInt("observedPeers"));
				entry.put("MAX_SWARM_SIZE", resultSet.getInt("maxSwarmSize"));
				list.add(entry);
			}
			json.put("LIST", list);
		}
		catch(SQLException e) {
			log.error("SQL Exception occurred");
			e.printStackTrace();
		}
		return json.toString();
	}

	private PreparedStatement getTorrentListHashStatement(String date, Connection connection) throws SQLException {
		String sql = "";
		PreparedStatement statement;

		sql += "SELECT statistics_torrents.title AS title, ";
		sql += "   HEX(statistics_torrents.info_hash) AS infoHash, ";
		sql += "   statistics_torrents.filesize AS filesize, ";
		sql += "   statistics_torrents.publish_date AS publishDate, ";
		sql += "   statistics_torrentmeta.observed_peers AS observedPeers, ";
		sql += "   statistics_torrentmeta.max_swarm_size AS maxSwarmSize ";
		sql += "FROM statistics_torrents, ";
		sql += "   statistics_torrentmeta ";
		sql += "WHERE statistics_torrentmeta.date = ? ";
		sql += "AND statistics_torrents.info_hash = statistics_torrentmeta.info_hash ";
		sql += "ORDER BY statistics_torrentmeta.observed_peers DESC; ";

		statement = connection.prepareStatement(sql);
		statement.setString(1, date);
		return statement;
	}

	@SuppressWarnings("unchecked")
	public String getDateBoundingAsJson() {
		JSONObject json = new JSONObject();
		ResultSet resultSet;
		try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
		{
			PreparedStatement statement = getDateBoundingStatement(connection);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				json.put("FROM_DATE", resultSet.getString("from_date"));
				json.put("TO_DATE", resultSet.getString("to_date"));
			}
		}
		catch(SQLException e) {
			log.error("SQL Exception occurred");
			e.printStackTrace();
		}
		return json.toString();
	}

	private PreparedStatement getDateBoundingStatement(Connection connection) throws SQLException {
		StringBuffer sql = new StringBuffer();
		PreparedStatement statement;

		sql.append("SELECT MIN(date) AS from_date, ");
		sql.append("   MAX(date) AS to_date ");
		sql.append("FROM statistics_torrentmeta; ");

		statement = connection.prepareStatement(sql.toString());
		return statement;
	}
	
}
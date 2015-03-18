package ch.uzh.geoViz.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributionOverTime {

	private static final Logger log = LoggerFactory.getLogger(DistributionOverTime.class.getName());

	private Connection connection = MySQLConnectionFactory.getMySQlConnection();
	public DistributionOverTime() {
		super();
		
	}

	@SuppressWarnings("unchecked")
	public String getTorrentDateObservedPeersAsJson(String infoHash) {
		// LinkedHashMap instead of JSONObject to preserve order (for c3.js)
		final Map<String, Object> json = new LinkedHashMap<String, Object>();
		final JSONArray date = new JSONArray();
		final JSONArray observedPeers = new JSONArray();
		final JSONArray seeders = new JSONArray();
		final ResultSet resultSet;

		try {
			PreparedStatement statement = getTorrentDateObservedPeersStatement(infoHash);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				date.add(resultSet.getString("date"));
				observedPeers.add(resultSet.getInt("observedPeers"));
				seeders.add(resultSet.getInt("seeders"));
			}
			json.put("AXIS", date);
			json.put("OBSERVED_PEERS", observedPeers);
			json.put("SEEDERS", seeders);
		}
		catch(SQLException e) {
			log.error("SQL Exception occurred");
			e.printStackTrace();
		}
		return JSONValue.toJSONString(json);
	}

	private PreparedStatement getTorrentDateObservedPeersStatement(String infoHash) throws SQLException {
		String sql = "";
		PreparedStatement statement;

		sql += "SELECT total_table.date AS date, ";
		sql += "   total_table.observedPeers AS observedPeers, ";
		sql += "   FLOOR(statistics_torrentmeta.seeder_quota * total_table.observedPeers) AS seeders ";
		sql += "FROM statistics_torrentmeta, ";
		sql += "   (SELECT date, ";
		sql += "      COUNT(*) AS observedPeers ";
		sql += "   FROM statistics_peers ";
		sql += "   WHERE HEX(info_hash) = ? ";
		sql += "   GROUP BY date) AS total_table ";
		sql += "WHERE HEX(statistics_torrentmeta.info_hash) = ? ";
		sql += "AND statistics_torrentmeta.date = total_table.date; ";

		statement = connection.prepareStatement(sql);
		statement.setString(1, infoHash);
		statement.setString(2, infoHash);
		return statement;
	}

}
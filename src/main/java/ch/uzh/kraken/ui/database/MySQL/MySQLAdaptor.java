package ch.uzh.kraken.ui.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import ch.uzh.kraken.ui.database.api.AbstractAdaptor;
import ch.uzh.kraken.ui.database.api.IDataAdaptor;
import ch.uzh.kraken.ui.database.api.IDataAdaptorFactory;
import ch.uzh.kraken.ui.util.JsonErrors;
import ch.uzh.kraken.ui.util.Validation;

public class MySQLAdaptor extends AbstractAdaptor implements IDataAdaptorFactory {

	@Override
	public String getDateBoundsTorrentListAsJSON() {
		TorrentList db = new TorrentList();
		String json = db.getDateBoundingAsJson();
		return json;
	}

	public IDataAdaptor getSpecificAdaptorImpl() {
		return new MySQLAdaptor();
	}
	
	@SuppressWarnings("unchecked")
	public String getSpecificMapDataAsJson(String date, String infoHash) {
		JSONObject json;

		if(infoHash == null || !Validation.validateInfoHash(infoHash)) {
			json = JsonErrors.createStandardJsonError();
		}
		else {
			try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
			{
				if(date == null || !Validation.validateDate(date)) {
					date = getNewestDate();
				}
				PreparedStatement statement = getSpecificMapDataStatement(date, infoHash, connection);
				ResultSet resultSet = statement.executeQuery();
				json = new JSONObject();
				json.put("DATE", date);
				json.put("INFO_HASH", infoHash);
				json = createMapJson(resultSet, json);
			}
			catch(SQLException e) {
				json = JsonErrors.createStandardJsonError();
				e.printStackTrace();
			}
		}
		return json.toJSONString();
	}

	protected String getTorrentTitle(String infoHash) {
		final ResultSet resultSet;
		String title = "";

		try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
		{
			String sql = "";
			PreparedStatement statement;

			sql += "SELECT title ";
			sql += "FROM statistics_torrents ";
			sql += "WHERE HEX(info_hash) = ? ;";

			statement = connection.prepareStatement(sql);
			statement.setString(1, infoHash);
			resultSet = statement.executeQuery();

			while(resultSet.next()) {
				title = resultSet.getString("title");
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return title;
	}

	@SuppressWarnings("unchecked")
	public String getGenericMapDataAsJson(String date) {
		JSONObject json;

		try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
		{
			if(date == null || !Validation.validateDate(date)) {
				date = getNewestDate();
			}
			PreparedStatement statement = getGenericMapDataStatement(date, connection);
			ResultSet resultSet = statement.executeQuery();
			json = new JSONObject();
			json.put("DATE", date);
			json = createMapJson(resultSet, json);
		}
		catch(SQLException e) {
			json = JsonErrors.createStandardJsonError();
			e.printStackTrace();
		}
		return json.toJSONString();
	}

	@SuppressWarnings("unchecked")
	private JSONObject createMapJson(ResultSet resultSet, JSONObject json) throws SQLException {
		JSONArray countryArray = new JSONArray();
		int notLocatablePeers = 0;
		double notLocatablePercentage = 0.0;

		while(resultSet.next()) {
			int observedPeers = resultSet.getInt("observed_peers");
			int maxSwarmSize = resultSet.getInt("max_swarm_size");
			double percentage = resultSet.getDouble("percentage");
			String countryIsoCode = resultSet.getString("country_iso_code");

			if(resultSet.wasNull() || countryIsoCode.equals("")) {
				notLocatablePeers = resultSet.getInt("observed_peers");
				notLocatablePercentage = resultSet.getDouble("percentage");
			}
			else {
				JSONObject country = new JSONObject();
				country.put("COUNTRY_CODE", countryIsoCode);
				country.put("OBSERVED_PEERS", observedPeers);
				country.put("MAX_SWARM_SIZE", maxSwarmSize);
				country.put("PERCENTAGE", percentage);
				countryArray.add(country);
			}
		}
		json.put("COUNTRIES", countryArray);
		json.put("NOT_LOCATABLE_PEERS", notLocatablePeers);
		json.put("NOT_LOCATABLE_PERCENTAGE", notLocatablePercentage);
		return json;
	}

	private PreparedStatement getSpecificMapDataStatement(String date, String infoHash, Connection connection) throws SQLException {
		final StringBuffer sql = new StringBuffer();
		final PreparedStatement statement;
		final int maxSwarmSize = getMaxSwarmSize(date, infoHash, connection);
		final int totalPeersObserved = getTotalPeersObserved(date, infoHash, connection);
		final double factor = ((double) maxSwarmSize) / ((double) totalPeersObserved);

		sql.append("SELECT country_iso_code, ");
		sql.append("   COUNT(*) AS observed_peers, ");
		sql.append("   CEIL(COUNT(*) * ? ) AS max_swarm_size, ");
		sql.append("   ( COUNT(*) / ? ) * 100 AS percentage ");
		sql.append("FROM statistics_peers ");
		sql.append("WHERE date = ? ");
		sql.append("AND HEX(info_hash) = ? ");
		sql.append("GROUP BY country_iso_code ");
		sql.append("ORDER BY observed_peers DESC;");

		statement = connection.prepareStatement(sql.toString());
		statement.setDouble(1, factor);
		statement.setInt(2, totalPeersObserved);
		statement.setString(3, date);
		statement.setString(4, infoHash);
		return statement;
	}

	private PreparedStatement getGenericMapDataStatement(String date, Connection connection) throws SQLException {
		final StringBuffer sql = new StringBuffer();
		final PreparedStatement statement;
		final int maxSwarmSize = getMaxSwarmSize(date, connection);
		final int totalPeersObserved = getTotalPeersObserved(date, connection);
		final double factor = ((double) maxSwarmSize) / ((double) totalPeersObserved);

		sql.append("SELECT country_iso_code, ");
		sql.append("   COUNT(*) AS observed_peers, ");
		sql.append("   CEIL(COUNT(*) * ? ) AS max_swarm_size, ");
		sql.append("   ( COUNT(*) / ? ) * 100 AS percentage ");
		sql.append("FROM ");
		sql.append("   (SELECT country_iso_code, ");
		sql.append("      ip_address, ");
		sql.append("      date ");
		sql.append("   FROM statistics_peers ");
		sql.append("   WHERE date = ? ");
		sql.append("   GROUP BY ip_address) ");
		sql.append("   AS countries_over_all_torrents ");
		sql.append("GROUP BY country_iso_code ");
		sql.append("ORDER BY observed_peers DESC;");

		statement = connection.prepareStatement(sql.toString());
		statement.setDouble(1, factor);
		statement.setInt(2, totalPeersObserved);
		statement.setString(3, date);
		return statement;
	}

	public String getNewestDate() {
		String sql = "SELECT MAX(date) AS max_date FROM statistics_peers LIMIT 1";
		ResultSet resultSet;
		PreparedStatement statement;
		String maxDate = "";

		try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
		{
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			resultSet.next();
			maxDate = resultSet.getString("max_date");
		}
		catch(SQLException e) {
			e.printStackTrace();
		}

		return maxDate;
	}

	private Integer getTotalPeersObserved(String date, String infoHash, Connection connection) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT(*) AS total FROM `statistics_peers` ");
		sql.append("WHERE date = ? ");
		if(infoHash != null) {
			sql.append("AND HEX(info_hash) = ? ;");
		}

		PreparedStatement statement = connection.prepareStatement(sql.toString());
		statement.setString(1, date);
		if(infoHash != null) {
			statement.setString(2, infoHash);
		}
		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		return resultSet.getInt("total");
	}

	private Integer getTotalPeersObserved(String date, Connection connection) throws SQLException {
		return getTotalPeersObserved(date, null, connection);
	}

	private Integer getMaxSwarmSize(String date, String infoHash, Connection connection) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT SUM(max_swarm_size) AS max_swarm_size ");
		sql.append("FROM statistics_torrentmeta ");
		sql.append("WHERE date = ? ");
		if(infoHash != null) {
			sql.append("AND HEX(info_hash) = ? ");
		}

		PreparedStatement statement = connection.prepareStatement(sql.toString());
		statement.setString(1, date);
		if(infoHash != null) {
			statement.setString(2, infoHash);
		}
		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		return resultSet.getInt("max_swarm_size");
	}

	private Integer getMaxSwarmSize(String date, Connection connection) throws SQLException {
		return getMaxSwarmSize(date, null, connection);
	}
	
}

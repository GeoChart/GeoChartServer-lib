package ch.uzh.kraken.ui.database.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class AbstractAdaptor implements IDataAdaptor {

	final static String CSV_SEPARATOR = ";";

	public abstract String getSpecificMapDataAsJson(String date, String infoHash);
	protected abstract String getTorrentTitle(String infoHash); 
	public abstract String getGenericMapDataAsJson(String date); 
	
	public String getSpecificMapDataAsCsv(String date, String infoHash) {
		final StringBuffer csv = new StringBuffer();
		final String jsonString = getSpecificMapDataAsJson(date, infoHash);
		final JSONParser parser = new JSONParser();
		final String torrentTitle = getTorrentTitle(infoHash);

		csv.append("COUNTRY_CODE" + AbstractAdaptor.CSV_SEPARATOR);
		csv.append("OBSERVED_PEERS" + AbstractAdaptor.CSV_SEPARATOR);
		csv.append("MAX_SWARM_SIZE" + AbstractAdaptor.CSV_SEPARATOR);
		csv.append("PERCENTAGE" + AbstractAdaptor.CSV_SEPARATOR);
		csv.append("DATE" + AbstractAdaptor.CSV_SEPARATOR);
		csv.append("TORRENT_TITLE\n");

		try {
			JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
			JSONArray countries = (JSONArray) jsonObject.get("COUNTRIES");
			for(int i = 0; i < countries.size(); i++) {
				JSONObject country = (JSONObject) countries.get(i);
				csv.append(country.get("COUNTRY_CODE"));
				csv.append(AbstractAdaptor.CSV_SEPARATOR);
				csv.append(country.get("OBSERVED_PEERS"));
				csv.append(AbstractAdaptor.CSV_SEPARATOR);
				csv.append(country.get("MAX_SWARM_SIZE"));
				csv.append(AbstractAdaptor.CSV_SEPARATOR);
				csv.append(country.get("PERCENTAGE"));
				csv.append(AbstractAdaptor.CSV_SEPARATOR);
				csv.append(date);
				csv.append(AbstractAdaptor.CSV_SEPARATOR);
				csv.append(torrentTitle);
				csv.append("\n");
			}
			csv.append("NOT LOCATABLE");
			csv.append(AbstractAdaptor.CSV_SEPARATOR);
			csv.append(jsonObject.get("NOT_LOCATABLE_PEERS"));
			csv.append(AbstractAdaptor.CSV_SEPARATOR);
			csv.append("");
			csv.append(AbstractAdaptor.CSV_SEPARATOR);
			csv.append(jsonObject.get("NOT_LOCATABLE_PERCENTAGE"));
			csv.append(AbstractAdaptor.CSV_SEPARATOR);
			csv.append(date);
			csv.append(AbstractAdaptor.CSV_SEPARATOR);
			csv.append(torrentTitle);
			csv.append("\n");
		}
		catch(ParseException e) {
			e.printStackTrace();
		}
		return csv.toString();
	}
	
	public String getGenericMapDataAsCsv(String date) {
		final StringBuffer csv = new StringBuffer();
		final String jsonString = getGenericMapDataAsJson(date);
		final JSONParser parser = new JSONParser();

		csv.append("COUNTRY_CODE" + AbstractAdaptor.CSV_SEPARATOR);
		csv.append("OBSERVED_PEERS" + AbstractAdaptor.CSV_SEPARATOR);
		csv.append("MAX_SWARM_SIZE" + AbstractAdaptor.CSV_SEPARATOR);
		csv.append("PERCENTAGE" + AbstractAdaptor.CSV_SEPARATOR);
		csv.append("DATE\n");

		try {
			JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
			JSONArray countries = (JSONArray) jsonObject.get("COUNTRIES");
			for(int i = 0; i < countries.size(); i++) {
				JSONObject country = (JSONObject) countries.get(i);
				csv.append(country.get("COUNTRY_CODE"));
				csv.append(AbstractAdaptor.CSV_SEPARATOR);
				csv.append(country.get("OBSERVED_PEERS"));
				csv.append(AbstractAdaptor.CSV_SEPARATOR);
				csv.append(country.get("MAX_SWARM_SIZE"));
				csv.append(AbstractAdaptor.CSV_SEPARATOR);
				csv.append(country.get("PERCENTAGE"));
				csv.append(AbstractAdaptor.CSV_SEPARATOR);
				csv.append(date);
				csv.append("\n");
			}
			csv.append("NOT LOCATABLE");
			csv.append(AbstractAdaptor.CSV_SEPARATOR);
			csv.append(jsonObject.get("NOT_LOCATABLE_PEERS"));
			csv.append(AbstractAdaptor.CSV_SEPARATOR);
			csv.append("");
			csv.append(AbstractAdaptor.CSV_SEPARATOR);
			csv.append(jsonObject.get("NOT_LOCATABLE_PERCENTAGE"));
			csv.append(AbstractAdaptor.CSV_SEPARATOR);
			csv.append(date);
			csv.append("\n");
		}
		catch(ParseException e) {
			e.printStackTrace();
		}
		return csv.toString();
	}
}
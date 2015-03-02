package ch.uzh.kraken.ui.database.influxDB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.influxdb.dto.Serie;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.kraken.ui.database.api.AbstractAdaptor;
import ch.uzh.kraken.ui.util.Util;

public class InfluxDBAdaptor extends AbstractAdaptor{

	private static Logger log = LoggerFactory.getLogger(InfluxDBAdaptor.class);

	private Map<String, Map<String, Object>> torrents = DBMaker.newTempTreeMap();
	private double lastUpdate = 0;
	private InfluxDBConnection influx = new InfluxDBConnection();
	SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

	private static String dateBoundsTemplate = "{\"FROM_DATE\":\"%s\",\"TO_DATE\":\"%s\"}";
	@Override
	public String getDateBoundsTorrentListAsJSON() {
		Serie fromSerie = influx.executeQuery("select INFO_HASH from TORRENTS order asc limit 1").get(0);
		long fromTime = (long) (double) fromSerie.getRows().get(0).get("time");

		Serie toSerie = influx.executeQuery("select INFO_HASH from TORRENTS order desc limit 1").get(0);
		long toTime = (long) (double) toSerie.getRows().get(0).get("time");


		return String.format(dateBoundsTemplate, df2.format(new Date(fromTime)), df2.format(new Date(toTime)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getTorrentListAsJson(String date) {

		updateTorrentInfo();
		final JSONObject json = new JSONObject();
		final JSONArray list = new JSONArray();

		List<Serie> results = influx.executeQuery("select max(SWARM_SIZE) as SWARM_SIZE, max(UNIQUE_ADDRESSES) as OBSERVED_PEERS "
				+ "from /^aggregated.returnedpeers.20m.results.*/ "
				+ "where time > '"+date +" 00:00:00' and "
				+ "time < '"+ date + " 23:59:59.999' group by time(1d)");

		for(Serie s : results){
			String hash = s.getName().split("\\.")[4];
			Map<String, Object> map = s.getRows().get(0);
			Map<String, Object> meta = (Map<String, Object>)torrents.get(hash);
			JSONObject entry = new JSONObject();
			entry.put("INFO_HASH", hash.toUpperCase());
			if(meta != null){
				entry.put("TITLE", meta.get("TORRENT_TITLE"));
				entry.put("FILESIZE", Util.beautifyFilesize((long)(double)meta.get("TORRENT_SIZE_KB")));
				entry.put("PUBLISH_DATE", meta.get("PUBLISH_DATE"));
			}else{
				entry.put("TITLE", "N/A");
				entry.put("FILESIZE", "N/A");
				entry.put("PUBLISH_DATE", "N/A");
			}
			entry.put("OBSERVED_PEERS", map.get("SWARM_SIZE"));
			entry.put("MAX_SWARM_SIZE", map.get("UNIQUE_ADDRESSES"));

			list.add(entry);
		}
		json.put("LIST", list);
		log.info("Loaded torrent list with {} torrents", list.size());
		return JSONValue.toJSONString(json);
	}

	/**
	 * {  
	 *	"AXIS":["2014-09-10", ...],
	 *	"OBSERVED_PEERS":[ 7849, ...],
	 *	"SEEDERS":[ 5376, ...]
	 * }
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getTorrentDateObservedPeersAsJson(String infoHash) {
		String query = String.format("select max(SWARM_SIZE) as SWARM_SIZE, max(UNIQUE_ADDRESSES) as UNIQUE_ADDRESSES "
				+ "from aggregated.returnedpeers.20m.results.%s group by time(1d) order asc", infoHash.toLowerCase());
		final Map<String, Object> json = new LinkedHashMap<String, Object>();
		final JSONArray dates = new JSONArray();
		final JSONArray observedPeers = new JSONArray();
		final JSONArray seeders = new JSONArray();
		List<Serie> series = influx.executeQuery(query);
		for(Serie s : series){
			for(Map<String, Object> row : s.getRows()){
				Date date = new Date((long)(double) row.get("time"));
				dates.add(df2.format(date));
				observedPeers.add(row.get("SWARM_SIZE"));
				seeders.add(row.get("UNIQUE_ADDRESSES"));
			}
		}

		json.put("AXIS", dates);
		json.put("OBSERVED_PEERS", observedPeers);
		json.put("SEEDERS", seeders);

		return JSONValue.toJSONString(json);
	}


	/**
	 * @param infoHash
	 * @return expected:
	 * {  
	 *	 "FILESIZE":"1.5 GiB",
	 *	 "TITLE":"Al Filo Del Mañana [BluRay Screener][Español Castellano]",
	 *	 "PUBLISH_DATE":"2014-09-09"
	 * }
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getTorrentDetailsFromInfoHashAsJson(String infoHash) {
		updateTorrentInfo();
		JSONObject entry = new JSONObject();
		Map<String, Object> meta = torrents.get(infoHash.toLowerCase());
		if(meta != null){
			entry.put("TITLE", meta.get("TORRENT_TITLE"));
			entry.put("FILESIZE", Util.beautifyFilesize((long)(double)meta.get("TORRENT_SIZE_KB")));
			entry.put("PUBLISH_DATE", meta.get("PUBLISH_DATE"));
		}else{
			entry.put("TITLE", "N/A");
			entry.put("FILESIZE", "N/A");
			entry.put("PUBLISH_DATE", "N/A");
		}
		return entry.toJSONString();
	}

	@Override
	public String getNewestDate() {
		String query = "select * from /aggregated.returnedpeers.20m.results.*/ order desc limit 1";
		List<Serie> series = influx.executeQuery(query);
		long time = (long) (double) series.get(0).getRows().get(0).get("time");
		return df2.format(new Date(time));
	}


	/**
	 * @param date
	 * @param infoHash
	 * @return expected:{  
	 *	"DATE":"2014-09-12",
	 *	"INFO_HASH":"8600313422E2D8EBC2CDA3CA04718A8A7FE33282",
	 *	"NOT_LOCATABLE_PERCENTAGE":0.0274,
	 *	"COUNTRIES":[  
	 *    {  
	 *       "OBSERVED_PEERS":3235,
	 *       "PERCENTAGE":88.703,
	 *       "COUNTRY_CODE":"ES",
	 *       "MAX_SWARM_SIZE":841
	 *   },
	 *   ...
	 *		],
	 *	"NOT_LOCATABLE_PEERS":1
	 * }
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getSpecificMapDataAsJson(String date, String infoHash) {
		//		Serie.Builder geoBuilder = new Serie.Builder("geo." + infoHash + "." + city.getContinent() + "." + city.getCountry() + "." + city.getCity());
		//		geoBuilder.columns("ip", "continent", "country", "city", "ASN" );
		String baseQuery = "select max(swarm_size) as swarm_size, count(unique(ip)) as observed_peers from /geo." + infoHash + ".*/ where time > '" +date +"' and time < '" + date + " 23:59:59.999'";
		JSONObject obj = new JSONObject();
		JSONArray countries = new JSONArray();
		obj.put("DATE", date);
		obj.put("INFO_HASH", infoHash.toUpperCase());
		List<Serie> series = influx.executeQuery(baseQuery);
		double total = 0d;
		for(Serie s : series){
			JSONObject country = new JSONObject();
			String[] name = s.getName().split(".");
			Map<String, Object> row = s.getRows().get(0);
			total += (double) row.get("observed_peers");
			country.put("COUNTRY_CODE", name[3]);
			country.put("MAX_SWARM_SIZE", row.get("swarm_size"));
			country.put("OBSERVED_PEERS", row.get("observed_peers"));
			countries.add(country);
		}
		for(Object o : countries){
			JSONObject country = (JSONObject) o;
			double d = (double)country.get("OBSERVED_PEERS");
			country.put("PERCENTAGE", d/total);
		}

		//TODO calculate not locatable peers
		obj.put("NOT_LOCATABLE_PEERS", 0);
		obj.put("NOT_LOCATABLE_PERCENTAGE", 0);
		return obj.toJSONString();	
	}

	/**
	 * @param date
	 * @return expected: {  
	 *	"DATE":"2014-09-12",
	 * 	"NOT_LOCATABLE_PERCENTAGE":0.0288,
	 *	"COUNTRIES":[  
	 *    {  
	 *        "OBSERVED_PEERS":3605,
	 *        "PERCENTAGE":20.7303,
	 *        "COUNTRY_CODE":"ES",
	 *        "MAX_SWARM_SIZE":680
	 *    },
	 *    ...
	 *		],
	 *	"NOT_LOCATABLE_PEERS":5
	 * }
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getGenericMapDataAsJson(String date) {
		String baseQuery = "select max(swarm_size) as swarm_size, count(unique(ip)) as observed_peers from /geo.*/ where time > '" +date +"' and time < '" + date + " 23:59:59.999'";
		JSONObject obj = new JSONObject();
		JSONArray countries = new JSONArray();
		obj.put("DATE", date);
		List<Serie> series = influx.executeQuery(baseQuery);
		double total = 0d;
		for(Serie s : series){
			JSONObject country = new JSONObject();
			String[] name = s.getName().split(".");
			Map<String, Object> row = s.getRows().get(0);
			total += (double) row.get("observed_peers");
			country.put("COUNTRY_CODE", name[3]);
			country.put("MAX_SWARM_SIZE", row.get("swarm_size"));
			country.put("OBSERVED_PEERS", row.get("observed_peers"));
			countries.add(country);
		}
		for(Object o : countries){
			JSONObject country = (JSONObject) o;
			double d = (double)country.get("OBSERVED_PEERS");
			country.put("PERCENTAGE", d/total);
		}
		//TODO calculate not locatable peers
		obj.put("NOT_LOCATABLE_PEERS", 0);
		obj.put("NOT_LOCATABLE_PERCENTAGE", 0);
		return obj.toJSONString();	
	}


	@Override
	protected String getTorrentTitle(String infoHash) {
		// TODO Auto-generated method stub
		return null;
	}


	private synchronized void updateTorrentInfo() {
		Serie last = null;
		for(Serie s : influx.executeQuery("select INFO_HASH, TORRENT_TITLE, TORRENT_SIZE_KB, PUBLISH_DATE from TORRENTS where time > " + lastUpdate)){
			for(Map<String, Object> map : s.getRows()){
				torrents.put(map.get("INFO_HASH").toString(), map);
			}
			last = s;
		}
		lastUpdate = (double) last.getRows().get(0).get("time");
	}
}

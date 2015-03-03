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

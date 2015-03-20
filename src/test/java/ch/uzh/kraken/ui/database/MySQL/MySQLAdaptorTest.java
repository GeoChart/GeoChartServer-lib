package ch.uzh.kraken.ui.database.MySQL;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.uzh.geoViz.database.MySQL.MySQLAdaptor;
import ch.uzh.geoViz.util.Util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class MySQLAdaptorTest {

	@Test
	public void test() {
		MySQLAdaptor mysql = new MySQLAdaptor();
		
		Util.setProperties(Util.readProperties("geoViz.properties"));
		
		System.out.println();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser pr = new JsonParser();
		JsonElement el = pr.parse(mysql.getMapData("2015-03-07").toJSONString());
		String prettyJsonString = gson.toJson(el);
		
		System.out.println(prettyJsonString);

		System.out.println(mysql.getDataAsCsv("2015-03-07", "overall_unique_ipaddresses"));

	}

}

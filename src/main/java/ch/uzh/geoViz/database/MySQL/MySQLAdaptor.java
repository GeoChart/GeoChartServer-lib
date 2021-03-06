package ch.uzh.geoViz.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.geoViz.database.api.IDataAdaptor;
import ch.uzh.geoViz.database.api.IDataAdaptorFactory;
import ch.uzh.geoViz.util.JsonErrors;
import ch.uzh.geoViz.util.Validation;

public class MySQLAdaptor implements IDataAdaptor, IDataAdaptorFactory {

	final static String CSV_SEPARATOR = ";";
	
	private static Logger log = LoggerFactory.getLogger(MySQLAdaptor.class); 

	public IDataAdaptor getSpecificAdaptorImpl() {
		return new MySQLAdaptor();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getDateBounds() {
		JSONObject obj = new JSONObject();
		try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
		{
			Statement stm = connection.createStatement();
			stm.execute("SELECT MIN(date) AS date FROM Data");
			ResultSet rs = stm.getResultSet();
			rs.next();
			String start = rs.getString("date");
			
			stm.execute("SELECT MAX(date) AS date FROM Data");
			rs = stm.getResultSet();
			rs.next();
			String end = rs.getString("date");
			
			obj.put("FROM_DATE", start);
			obj.put("TO_DATE", end);
		} catch(SQLException e) {
			obj = JsonErrors.createStandardJsonError();
			log.error("Error in getDateBounds", e);
		}
		return obj;
	}
	
	/**
	 * { "types":[
	         	 *   {
	         	 *     "name":"A unique name",
	         	 *     "label":"Human readable label used in UI",
	         	 *     "unit":"Unit used in UI. Optional"
	         	 *   },
	         	 *   ...
	         	 *   ]
	         	 * }
	         	 */
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getType(String name) {
		JSONObject obj = new JSONObject();
		try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
		{
			PreparedStatement stm = connection.prepareStatement("SELECT * from Type WHERE name = ?");
			stm.setString(1, name);
			stm.execute();
			ResultSet rs = stm.getResultSet();
			if(rs.next()){
				obj.put("name", rs.getString("name"));
				obj.put("label", rs.getString("label"));
				obj.put("unit", rs.getString("unit"));
			}
			
		} catch (SQLException e) {
			log.error("Error in getType({})", name, e);
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getTypes() {
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray();
		try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
		{
			PreparedStatement stm = connection.prepareStatement("SELECT * from Type ORDER BY ID ASC");
			stm.execute();
			ResultSet rs = stm.getResultSet();
			while(rs.next()){
				JSONObject type = new JSONObject();
				type.put("type", rs.getString("name"));
				type.put("label", rs.getString("label"));
				type.put("unit", rs.getString("unit"));
				array.add(type);
			}
		} catch (SQLException e) {
			log.error("Error in getTypes()", e);
		}
		obj.put("types", array);
		return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getMapData(String date) {
		Map<String, JSONObject> countries = new JSONObject();
		JSONObject notLocatableValues = new JSONObject();
		notLocatableValues.put("values", new JSONObject());
		try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
		{
			if(date == null || !Validation.validateDate(date)) {
				date = getNewestDate();
			}
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT Data.countryCode, Type.name as name, Type.label, Type.unit, " +
					"Data.value, continents.name continent, countries.name as countryLabel " + 
					"FROM Data JOIN (Type, continents, countries) "+
					"ON (Data.TypeID = Type.ID AND Data.countryCode = countries.code AND countries.continent_code = continents.code) "+
					"WHERE Data.date = '2015-03-06' " +
					"ORDER BY `Data`.`countryCode`  ASC");			
				
			stmt.setString(1, date);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while(rs.next()){
				String country = rs.getString(1);
				String countryLabel = rs.getString("countryLabel");
				String continent = rs.getString("continent");
				
				JSONObject countryObject;
				if(country == null || country.isEmpty()){
					countryObject = notLocatableValues;
				} else {
					if(countries.containsKey(country)){
						countryObject = countries.get(country);
					}else{
						countryObject = new JSONObject();
						countryObject.put("values", new JSONObject());
						countryObject.put("code", country);
						countryObject.put("label", countryLabel);
						countryObject.put("continent", continent);
						
						countries.put(country, countryObject);
					}
				}
				((JSONObject)countryObject.get("values")).put(rs.getString("name"), rs.getString("value"));
			}
			
		} catch (SQLException e) {
			log.error("Error in getMapData({})", date, e);
		}
		
		JSONObject notLocatable = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("csv", "/request/map.csv"); //TODO make this right
		JSONObject dateObj = new JSONObject();
		dateObj.put("value", date);
		dateObj.put("format", "YYYY-MM-DD");
		data.put("date", dateObj);
		data.put("types", this.getTypes());
		notLocatable.put("label", "Not Locatable");
		notLocatable.put("values", notLocatableValues);
		data.put("not-locatable", notLocatable);
		data.put("countries", countries);
		JSONObject main = new JSONObject();
		main.put("data", data);
		return main;
	}
	
	public String getDataAsCsv(String date, String type) {
		StringBuffer b = new StringBuffer();
		
		try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
		{
			if(date == null || !Validation.validateDate(date)) {
				date = getNewestDate();
			}
			PreparedStatement stmt = connection.prepareStatement(
			"SELECT countryCode, continent, name, label, unit, value FROM Type, Data " + 
			"WHERE date = ? " +
			"AND Type.name = ? " +
			"AND Type.ID = Data.TypeID " +
			"ORDER BY `Data`.`countryCode`, `Type`.`name`  ASC");
			
			stmt.setString(1, date);
			stmt.setString(2, type);
			stmt.execute();
			
			ResultSet rs = stmt.getResultSet();
			while(rs.next()){
				b.append(rs.getString(1)).append(", ");
				b.append(rs.getString(2)).append(", ");
				b.append(rs.getString(3)).append(", ");
				b.append(rs.getString(4)).append(", ");
				b.append(rs.getString(5)).append(", ");
				b.append(rs.getString(6));
				b.append("\n");
			}
			
		} catch (SQLException e) {
			log.error("Error in getSpecificMapDataAsCsv({},{})", date, type, e);
		}
			
		return b.toString();	
	}


	private String getNewestDate(){
		String start="";
		try (Connection connection = MySQLConnectionFactory.getMySQlConnection())
		{
			Statement stm = connection.createStatement();
			stm.execute("SELECT MAX(date) AS date FROM Data");
			ResultSet rs = stm.getResultSet();
			rs.next();
			start = rs.getString("date");
		} catch(SQLException e) {
			log.error("Error in getNewestDate()", e);
		}
		return start;
	}
}

package ch.uzh.kraken.ui.database.api;

import org.json.simple.JSONObject;

/**
 * @author Andri
 *
 */
/**
 * @author Andri
 *
 */
public interface IDataAdaptor {
	
	/**
	 * @return expected:
	 * {"FROM_DATE":"2014-09-10","TO_DATE":"2014-09-15"}
	 */
	public JSONObject getDateBounds();
	
	/**
	 * @param name
	 * @return expected:
	 * {
	 *   "name":"A unique name",
	 *   "label":"Human readable label used in UI",
	 *   "unit":"Unit used in UI. Optional"
	 * }
	 */
	public JSONObject getType(String name);
	
	
	/**
	 * @return expected:
	 * 
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
	public JSONObject getTypes();
	
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
	public JSONObject getMapData(String date);
	
	public String getDataAsCsv(String date, String type);
}

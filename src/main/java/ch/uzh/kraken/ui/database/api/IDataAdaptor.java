package ch.uzh.kraken.ui.database.api;

public interface IDataAdaptor {
	
	/**
	 * @return expected:
	 * {"FROM_DATE":"2014-09-10","TO_DATE":"2014-09-15"}
	 */
	public String getDateBoundsTorrentListAsJSON();
	
	
	/**
	 * @param date
	 * @return expected:
	 * {"LIST":[  
     * 		{  
     *    		"INFO_HASH":"8600313422E2D8EBC2CDA3CA04718A8A7FE33282",
     *    		"FILESIZE":"1.5 GiB",
     *    		"TITLE":"Al Filo Del Mañana [BluRay Screener][Español Castellano]",
     *    		"PUBLISH_DATE":"2014-09-09",
     *    		"OBSERVED_PEERS":3647,
     *    		"MAX_SWARM_SIZE":948
     * 		},
     * 		....
   	 *		]
	 *	}
	 */
	public String getTorrentListAsJson(String date);
	
	
	/**
	 * @param infoHash
	 * @return expected:
	 * {  
     *	"AXIS":["2014-09-10", ...],
     *	"OBSERVED_PEERS":[ 7849, ...],
     *	"SEEDERS":[ 5376, ...]
     * }
	 */
	public String getTorrentDateObservedPeersAsJson(String infoHash);
	
	
	/**
	 * @param infoHash
	 * @return expected:
	 * {  
	 *	 "FILESIZE":"1.5 GiB",
	 *	 "TITLE":"Al Filo Del Mañana [BluRay Screener][Español Castellano]",
	 *	 "PUBLISH_DATE":"2014-09-09"
	 * }
	 */
	public String getTorrentDetailsFromInfoHashAsJson(String infoHash);
	
	
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
	public String getSpecificMapDataAsJson(String date, String infoHash);
	
	
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
	public String getGenericMapDataAsJson(String date);
	
	
	/**
	 * @return expected: 2014-09-15
	 */
	public String getNewestDate();
	
	public String getSpecificMapDataAsCsv(String date, String infoHash);
	public String getGenericMapDataAsCsv(String date);
}

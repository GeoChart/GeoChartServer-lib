package ch.uzh.kraken.ui.database.influxDB;

import ch.uzh.kraken.ui.database.api.IDataAdaptor;
import ch.uzh.kraken.ui.database.api.IDataAdaptorFactory;

public class InfluxDBAdaptorFactory implements IDataAdaptorFactory{

	@Override
	public IDataAdaptor getSpecificAdaptorImpl() {
		
		return new InfluxDBAdaptor();
	}

}

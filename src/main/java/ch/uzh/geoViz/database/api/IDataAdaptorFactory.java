package ch.uzh.geoViz.database.api;

public interface IDataAdaptorFactory {

	
	public IDataAdaptor getSpecificAdaptorImpl();
}

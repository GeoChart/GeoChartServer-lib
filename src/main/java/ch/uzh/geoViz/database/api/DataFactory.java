package ch.uzh.geoViz.database.api;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.geoViz.util.Util;

public class DataFactory {
	
	private static Logger log = LoggerFactory.getLogger(DataFactory.class);
	
	private static IDataAdaptor adaptor = null;
	
	public static IDataAdaptor getDataInterface(){
		log.info("get data interface");
		if(adaptor == null)
			loadfactory();
		return adaptor;
	}
	
	private static void loadfactory(){
		Properties properties = Util.readProperties("db.properties");
		String factoryClass = properties.getProperty("dataAdapterFactory");
		log.info("Loading data adaptor factory: {}", factoryClass);
				
		try {
			IDataAdaptorFactory factory = (IDataAdaptorFactory) IDataAdaptorFactory.class.getClassLoader().loadClass(factoryClass).newInstance();
			adaptor = factory.getSpecificAdaptorImpl();
		} catch (ClassNotFoundException e) {
			log.error("Could not load factory : {}", factoryClass, e);
		} catch (InstantiationException e) {
			log.error("Could not instantiate factory : {}", factoryClass, e);
		} catch (IllegalAccessException e) {
			log.error("Could not access factory : {}", factoryClass, e);
		}
	}

}

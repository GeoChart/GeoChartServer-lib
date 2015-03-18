package ch.uzh.geoViz.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

	private static Logger log = LoggerFactory.getLogger(Util.class.getName());

	public static void writeJsonResponse(HttpServletResponse response, String jsonString) {
		try {
			response.setCharacterEncoding("utf8");
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			out.println(jsonString);
		}
		catch(IOException e) {
			log.error("Could not write to the servlet output: {}", e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void writeCsvResponse(HttpServletResponse response, String csvString, String filename) {
		try {
			response.setCharacterEncoding("utf8");
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			PrintWriter out = response.getWriter();
			out.println("\uFEFF" + csvString);
		}
		catch(IOException e) {
			log.error("Could not write to the servlet output: {}", e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	public static Properties readProperties(String propertiesFile) {
		try {
			Properties properties = new Properties();
			log.debug("loading properties from : {}", propertiesFile);

			// load a properties file from class path, inside static method
			InputStream in = Util.class.getClassLoader().getResourceAsStream(propertiesFile);
			if(in != null) {
				properties.load(in);
			}

			return properties;
		}
		catch(IOException ex) {
			log.error("Exception while loading properties file: {}", ex.getMessage());
			throw new RuntimeException(ex.getMessage());
		}
	}

	public static String getParameterValue(HttpServletRequest request, String key) {
		final Map<String, String[]> parameters = request.getParameterMap();
		if(parameters.get(key) != null) {
			return parameters.get(key)[0];
		}
		else {
			return null;
		}
	}

	public static String beautifyFilesize(long filesize) {
		// http://stackoverflow.com/a/5599842/3233827
		if(filesize <= 0) {
			return "0 B";
		}
		final String[] units = new String[] { "B", "KiB", "MiB", "GiB", "TiB" };
		int digitGroups = (int) (Math.log10(filesize) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(filesize / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
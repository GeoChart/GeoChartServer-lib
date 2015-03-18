package ch.uzh.geoViz.servlet.request;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.uzh.geoViz.database.api.DataFactory;
import ch.uzh.geoViz.util.JsonErrors;
import ch.uzh.geoViz.util.Util;

/**
 * Servlet implementation class DBMapServletCsv
 */
@WebServlet("/request/map.csv")
public class CSVMapDataServlet extends HttpServlet {

	private static final long serialVersionUID = 4123393922472928833L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String date = Util.getParameterValue(request, "date");
		final String infoHash = Util.getParameterValue(request, "infoHash");

		if(date != null && infoHash != null) {
			final String filename = "kraken-map-data-" + date + "-" + infoHash + ".csv";

			Util.writeCsvResponse(response, DataFactory.getDataInterface().getDataAsCsv(date, infoHash).toLowerCase(), filename);
		}
		else {
			Util.writeJsonResponse(response, JsonErrors.createStandardJsonError().toString());
		}
	}
}
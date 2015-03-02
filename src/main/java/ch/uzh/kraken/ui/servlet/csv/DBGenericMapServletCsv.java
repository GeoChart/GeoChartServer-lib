package ch.uzh.kraken.ui.servlet.csv;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.uzh.kraken.ui.database.api.DataFactory;
import ch.uzh.kraken.ui.util.Util;

/**
 * Servlet implementation class DBMapServletCsv
 */
@WebServlet("/request/generic-map.csv")
public class DBGenericMapServletCsv extends HttpServlet {

	private static final long serialVersionUID = 4123393922472928833L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String date = Util.getParameterValue(request, "date");
		String filename;

		if(date == null) {
			date = DataFactory.getDataInterface().getNewestDate();
		}

		filename = "kraken-map-data-" + date + ".csv";
		Util.writeCsvResponse(response, DataFactory.getDataInterface().getGenericMapDataAsCsv(date), filename);
	}
}
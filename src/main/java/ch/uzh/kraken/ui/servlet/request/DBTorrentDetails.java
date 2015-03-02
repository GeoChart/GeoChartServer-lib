package ch.uzh.kraken.ui.servlet.request;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.uzh.kraken.ui.database.api.DataFactory;
import ch.uzh.kraken.ui.util.JsonErrors;
import ch.uzh.kraken.ui.util.Util;

@WebServlet("/request/torrentDetails.json")
public class DBTorrentDetails extends HttpServlet {

	private static final long serialVersionUID = 3293315971445552420L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String infoHash = Util.getParameterValue(request, "infoHash");

		if(infoHash == null) {
			Util.writeJsonResponse(response, JsonErrors.createStandardJsonError().toString());
		}
		else {
			
			Util.writeJsonResponse(response, DataFactory.getDataInterface().getTorrentDetailsFromInfoHashAsJson(infoHash));
		}
	}
}
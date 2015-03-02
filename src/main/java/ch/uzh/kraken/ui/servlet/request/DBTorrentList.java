package ch.uzh.kraken.ui.servlet.request;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.uzh.kraken.ui.database.api.DataFactory;
import ch.uzh.kraken.ui.util.Util;

@WebServlet("/request/torrentList.json")
public class DBTorrentList extends HttpServlet {

	private static final long serialVersionUID = -479387369219520761L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String date = Util.getParameterValue(request, "date");
		Util.writeJsonResponse(response, DataFactory.getDataInterface().getTorrentListAsJson(date));
	}

}
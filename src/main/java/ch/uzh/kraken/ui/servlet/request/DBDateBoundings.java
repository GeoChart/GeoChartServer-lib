package ch.uzh.kraken.ui.servlet.request;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.uzh.kraken.ui.database.api.DataFactory;
import ch.uzh.kraken.ui.util.Util;

@WebServlet("/request/dateBounding.json")
public class DBDateBoundings extends HttpServlet {

	private static final long serialVersionUID = -7855451053390085851L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Util.writeJsonResponse(response, DataFactory.getDataInterface().getDateBoundsTorrentListAsJSON());
	}
}
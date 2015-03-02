package ch.uzh.kraken.ui.servlet.application;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.uzh.kraken.ui.util.Util;
import ch.uzh.kraken.ui.util.Validation;

/**
 * Servlet implementation class TorrentCharts
 */
@WebServlet("/torrentcharts.html")
public class TorrentChartServlet extends HttpServlet {

	private static final long serialVersionUID = 4163516268942761897L;
	private static final String url = "/WEB-INF/application/pages/torrentcharts.jsp";
	private static final String errorUrl = "/WEB-INF/application/pages/errors/torrentcharts.jsp";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String infoHash = Util.getParameterValue(request, "infoHash");
		final RequestDispatcher rq;
		if(Validation.validateInfoHash(infoHash)) {
			rq = request.getRequestDispatcher(url);
		}
		else {
			rq = request.getRequestDispatcher(errorUrl);
		}
		request.setAttribute("visualizationsSelected", "selected");
		rq.include(request, response);
	}

}

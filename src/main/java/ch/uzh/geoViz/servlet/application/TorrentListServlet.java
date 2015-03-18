package ch.uzh.geoViz.servlet.application;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TorrentList
 */
@WebServlet("/torrentlist.html")
public class TorrentListServlet extends HttpServlet {

	private static final long serialVersionUID = 7615416738872095161L;
	private static final String url = "/WEB-INF/application/pages/torrentlist.jsp";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher rq = request.getRequestDispatcher(url);
		request.setAttribute("visualizationsSelected", "selected");
		rq.include(request, response);
	}

}

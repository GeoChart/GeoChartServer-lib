package ch.uzh.geoViz.servlet.application;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SystemStatus
 */
@WebServlet("/systemstatus.html")
public class SystemStatusServlet extends HttpServlet {

	private static final long serialVersionUID = -4451714627328993012L;
	private static final String url = "/WEB-INF/application/pages/systemstatus.jsp";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher rq = request.getRequestDispatcher(url);
		request.setAttribute("systemstatusSelected", "selected");
		rq.include(request, response);
	}

}

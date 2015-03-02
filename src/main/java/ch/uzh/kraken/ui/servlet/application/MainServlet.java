package ch.uzh.kraken.ui.servlet.application;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Main
 */
@WebServlet("/main.html")
public class MainServlet extends HttpServlet {

	private static final long serialVersionUID = -6065034504606556101L;
	private static final String url = "/WEB-INF/application/pages/main.jsp";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher rq = request.getRequestDispatcher(url);
		request.setAttribute("projectDescriptionSelected", "selected");
		rq.include(request, response);
	}

}

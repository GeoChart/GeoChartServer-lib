package ch.uzh.geoViz.servlet.request;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.uzh.geoViz.database.api.DataFactory;
import ch.uzh.geoViz.util.Util;

@WebServlet("/request/types.json")
public class TypeServlet extends HttpServlet{

	
	private static final long serialVersionUID = -7855451053390085840L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String type = request.getParameter("type");
		if(type == null || type.isEmpty()){
			Util.writeJsonResponse(response, DataFactory.getDataInterface().getTypes().toJSONString());
		}else {
			Util.writeJsonResponse(response, DataFactory.getDataInterface().getType(type).toJSONString());
		}
	}
}

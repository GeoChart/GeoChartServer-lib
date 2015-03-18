package ch.uzh.geoViz.servlet.request;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.uzh.geoViz.database.api.DataFactory;
import ch.uzh.geoViz.util.Util;

@WebServlet("/request/map-data.json")
public class MapDataServlet extends HttpServlet {

	private static final long serialVersionUID = 6607701710001078316L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String date = Util.getParameterValue(request, "date");
		Util.writeJsonResponse(response, DataFactory.getDataInterface().getMapData(date).toJSONString());
	}
}
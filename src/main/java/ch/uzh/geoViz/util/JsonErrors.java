package ch.uzh.geoViz.util;

import org.json.simple.JSONObject;

public class JsonErrors {

	public static JSONObject createStandardJsonError() {
		return createJsonError("Error occured", "An error occured during execution. Please reload this page.");
	}

	@SuppressWarnings("unchecked")
	public static JSONObject createJsonError(String title, String message) {
		final JSONObject wrapper = new JSONObject();
		final JSONObject object = new JSONObject();
		object.put("title", title);
		object.put("message", message);
		wrapper.put("error", object);
		return wrapper;
	}

}
package ch.uzh.kraken.ui.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Validation {

	public static boolean validateInfoHash(String infoHash) {
		// infoHash as string is 40 characters long!
		if(infoHash == null || !infoHash.matches("[0-9A-Fa-f]{1,40}")) {
			return false;
		}
		else {
			return true;
		}
	}

	public static boolean validateDate(String date) {
		// http://stackoverflow.com/a/2151338/3233827
		if(date == null || !date.matches("\\d{4}-[01]\\d-[0-3]\\d")) {
			return false;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(false);

		try {
			sdf.parse(date);
			return true;
		}
		catch(ParseException ex) {
			return false;
		}
	}
}
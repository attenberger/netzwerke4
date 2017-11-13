package edu.hm.cs.breakfasttothelimit.google;

import edu.hm.cs.breakfasttothelimit.Transportation;
import edu.hm.cs.breakfasttothelimit.google.exception.RequestURLException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Request class for the google distance matrix.
 * 
 * @author Benjamin Eder
 */
public class GoogleDistanceMatrixRequest {

	/**
	 * URL used to access the google distance matrix API.
	 */
	private final static String API_ACCESS_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

	private final String from;
	private final String to;
	private final Transportation transportation;

	public GoogleDistanceMatrixRequest(String from, String to, Transportation transportation) {
		this.from = from;
		this.to = to;
		this.transportation = transportation;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public Transportation getTransportation() {
		return transportation;
	}

	public URL toRequestURL() throws RequestURLException {
		String url = API_ACCESS_URL + "?units=metric" +
				"&origins=" + encodeForURL(from) +
				"&destinations=" + encodeForURL(to) +
				"&key=" + GoogleDistanceService.API_TOKEN +
				"&mode=" + getMode();

		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new RequestURLException(e);
		}
	}

	private String encodeForURL(String toEncode) throws RequestURLException {
		try {
			return URLEncoder.encode(toEncode, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RequestURLException(e);
		}
	}

	private String getMode() {
		switch (transportation) {
			case CAR:
				return "driving";
			case BIKE:
				return "bicycling";
			case FOOT:
				return "walking";
			case PUBLICTRANSPORT:
				return "transit";

			default:
				throw new IllegalArgumentException();
		}
	}

}

package edu.hm.cs.breakfasttothelimit.google;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.hm.cs.breakfasttothelimit.Transportation;
import edu.hm.cs.breakfasttothelimit.google.exception.DistanceServiceException;
import edu.hm.cs.breakfasttothelimit.google.exception.RequestURLException;
import org.json.JSONObject;

/**
 * Google Distance Service used to calculate the time one needs to get from A to B.
 * 
 * @author Benjamin Eder
 */
public class GoogleDistanceService {

	/**
	 * Token used to access GoogleDistanceService Distance Matrix API.
	 */
	public final static String API_TOKEN = "AIzaSyCb0gpKjwMLrL3HVezfEe0183IMzEINXuo";

	/**
	 * Fetch google distance matrix API information using the passed parameters.
	 * @param from
	 * @param to
	 * @param transportation
	 * @return
	 * @throws DistanceServiceException
	 */
	public static GoogleDistanceMatrixResponse fetch(String from, String to, Transportation transportation) throws DistanceServiceException {
		GoogleDistanceMatrixRequest request = new GoogleDistanceMatrixRequest(from, to, transportation);

		try {
			URL url = request.toRequestURL();

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			StringBuilder sb = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
			}

			JSONObject responseJSON = new JSONObject(sb.toString());
			GoogleDistanceMatrixResponse response = new GoogleDistanceMatrixResponse(responseJSON);

			if (!response.isSuccess()) {
				throw new DistanceServiceException("Your request was not successful!");
			}

			return response;
		} catch (RequestURLException | IOException e) {
			throw new DistanceServiceException(e);
		}
	}

}

package edu.hm.cs.breakfasttothelimit.google;

import org.json.JSONObject;

public class GoogleDistanceMatrixResponse {

	/**
	 * OK Status of the response.
	 */
	private static final String STATUS_OK = "OK";

	private static final String DESTINATION_ADDRESSES_KEY = "destination_addresses";
	private static final String ORIGIN_ADDRESSES_KEY = "origin_addresses";
	private static final String ROWS_KEY = "rows";
	private static final String ELEMENTS_KEY = "elements";
	private static final String DURATION_KEY = "duration";
	private static final String DISTANCE_KEY = "distance";
	private static final String STATUS_KEY = "status";
	private static final String VALUE_KEY = "value";

	private String origin;
	private String destination;

	/**
	 * Distance in meter.
	 */
	private long distance;

	/**
	 * Duration in seconds.
	 */
	private long duration;

	/**
	 * Was the response a success?
	 */
	private boolean success;

	public GoogleDistanceMatrixResponse(JSONObject json) {
		success = true;

		String successString = json.getString(STATUS_KEY);
		if (successString != null && successString.equals(STATUS_OK)) {
			origin = json.getJSONArray(ORIGIN_ADDRESSES_KEY).getString(0);
			destination = json.getJSONArray(DESTINATION_ADDRESSES_KEY).getString(0);

			JSONObject element = json.getJSONArray(ROWS_KEY).getJSONObject(0).getJSONArray(ELEMENTS_KEY).getJSONObject(0);
			successString = element.getString(STATUS_KEY);
			if (successString != null && successString.equals(STATUS_OK)) {
				distance = element.getJSONObject(DISTANCE_KEY).getLong(VALUE_KEY);
				duration = element.getJSONObject(DURATION_KEY).getLong(VALUE_KEY);
			} else {
				success = false;
			}
		} else {
			success = false;
		}
	}

	public String getOrigin() {
		return origin;
	}

	public String getDestination() {
		return destination;
	}

	/**
	 * Get distance in metres.
	 * @return
	 */
	public long getDistance() {
		return distance;
	}

	/**
	 * Get duration in seconds.
	 * @return
	 */
	public long getDuration() {
		return duration;
	}

	public boolean isSuccess() {
		return success;
	}

	@Override
	public String toString() {
		return "[success: " + success + ", origin: " + getOrigin() + ", destination: " + getDestination() + ", distance: " + distance + ", duration: " + duration + "]";
	}
}

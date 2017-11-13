package edu.hm.cs.breakfasttothelimit.huebridge;


import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Connection to the Hue Bridge light system.
 * @author Attenberger
 */
public class HueBridgeConnection {	
	
	private final String ip;
	private final String username;
	
	/**
	 * Creates a new connection to the Hue Bridge light system.
	 * @param ip of the light system
	 * @param username username/key to get access to the light system
	 */
	public HueBridgeConnection(String ip, String username) {
		this.ip = ip;
		this.username = username;
	}
	
	/**
	 * Powers one light off.
	 * @param light Number of the light to be powered off.
	 * @throws HueBridgeException Thrown if the command could not be executed correctly.
	 */
	public void powerOff(int light) throws HueBridgeException {
		if (light <= 0 || light > 3)
			throw new IllegalArgumentException("The number of the light must between 1 and 3.");
		
		JSONObject json = new JSONObject();
		json.put("on", false);
		sendJSON(light, json);
	}
	
	/**
	 * Powers one light on.
	 * @param light Number of the light to be powered on.
	 * @param color of the light
	 * @throws HueBridgeException Thrown if the command could not be executed correctly.
	 */
	public void powerOn(int light, Color color) throws HueBridgeException {
		if (light <= 0 || light > 3)
			throw new IllegalArgumentException("The number of the light must between 1 and 3.");
		
		// Convert color from RGB-Values to HSB-Values, because the light system uses HSB-Values
		float[] hsv = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);

		// Create JSON objects to be send to the light system
		JSONObject json = new JSONObject();
		json.put("on", true); // Switch the light on (before changing the color to avoid errors).
		sendJSON(light, json);
		json = new JSONObject();
		// Create JSON object for the HSB color values. The values have to be scaled for the light system.
		json.put("hue", (int) (hsv[0] * (Short.MAX_VALUE * 2 + 1)));
		json.put("sat", (int) (hsv[1] * (Byte.MAX_VALUE * 2)));
		json.put("bri", (int) (hsv[2] * (Byte.MAX_VALUE * 2)));
		sendJSON(light, json);
	}

	/**
	 * Sends the JSON to the HueBridge to execute the commands.
	 * @param light Number of the light to be changed.
	 * @param json JSON-Object which contains the attributes to be changed.
	 * @throws HueBridgeException Thrown if the command could not be executed correctly.
	 */
	private void sendJSON(int light, JSONObject json) throws HueBridgeException {
		HttpURLConnection connection = null;
        DataOutputStream writer = null;
        BufferedReader reader = null;
         
        try {
            URL url = new URL("http://" + ip + "/api/" + username + "/lights/" + light + "/state");
            connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("PUT");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Length", Integer.toString(json.toString().getBytes().length));
            
            writer = new DataOutputStream(connection.getOutputStream());
            writer.writeBytes(json.toString());
            
            // Check JSON result if an error occurred
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                response.append(line + System.lineSeparator());
            
            JSONArray jsonResult = new JSONArray(response.toString());
            for (int i = 0; i < jsonResult.length(); i++) {
            	if (jsonResult.getJSONObject(i).keySet().contains("error"))
            		throw new HueBridgeException(jsonResult.toString());
            }
			
        }
        catch (IOException e) {
        	throw new HueBridgeException(e.getMessage());
        }
        finally {
        	try {
	        	if (connection != null)
	        		connection.disconnect();
		        if (writer != null)
		        	writer.close();
		        if (reader != null)
		            reader.close();
        	}
        	catch (IOException e) {
            	throw new HueBridgeException(e.getMessage());
            }
        }
	}
	
}

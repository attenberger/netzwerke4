package edu.hm.cs.breakfasttothelimit.huebridge;


import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Connection to the Hue Bridge light system.
 * @author Attenberger
 */
public class HueBridgeConnection {	
	
	private static final int TIMEOUT = 1000;
	
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
            connection.setConnectTimeout(TIMEOUT);
			connection.setRequestMethod("PUT");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Length", Integer.toString(json.toString().getBytes().length));
            
            writer = new DataOutputStream(connection.getOutputStream());
            writer.writeBytes(json.toString());
            
            // Check response code
            switch (connection.getResponseCode()) {
	            case 400: throw new HueBridgeException("An error occured in the communication with HUE-Bridge. Please contact the developer of the application!");
	            case 401: throw new HueBridgeException("The application is not allowed to access to the HUE-Bridge. Please make sure that username of the HUE-Bridge is " +
	            		username + ".");
	            case 404: throw new HueBridgeException("The application could not change the state of a lamp because the lamp can not be found. Please check the configuration if the correct device is called, otherwise contact the devolper!");
	            default:
	            	if (connection.getResponseCode() < 200 || connection.getResponseCode() > 299) {
	            		throw new HueBridgeException("An unexpected error occured in the communication with HUE-Bridge.\r\n" +
	            			"Return Code: " + connection.getResponseCode() + " Message: " + connection.getResponseMessage() + "\r\n" +
	            			"There might be something wrong with your HUE-Bridge!");
	            	}
            }
            
            // Check JSON result if an error occurred
            connection.setReadTimeout(TIMEOUT);
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line + System.lineSeparator());
            }
            
            JSONArray jsonResult = new JSONArray(response.toString());
            for (int i = 0; i < jsonResult.length(); i++) {
            	if (jsonResult.getJSONObject(i).keySet().contains("error"))
            		throw new HueBridgeException("An error occured in the communication with the HUE-Bridge: " +
            				jsonResult.getJSONObject(i).getJSONObject("error").getString("description") +
            				"\r\nPlease contact the software developer!");
            }
			
        }
        catch (SocketTimeoutException e) {
        	throw new HueBridgeException("The application is unable to connect or communicate with the Hue-Bridge: " + e.getMessage() +
        			"\r\nPlease check the configuration and connection.");
        }
        catch (IOException e) {
        	throw new HueBridgeException("An error occured in the connection to Hue-Bridge: " + e.getMessage() + "\r\nPlease check your the connection!");
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
            	// No useful exception handling possible
            }
        }
	}
	
}

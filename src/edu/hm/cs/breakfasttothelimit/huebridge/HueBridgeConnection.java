package edu.hm.cs.breakfasttothelimit.huebridge;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Connection to the light system.
 * @author Attenberger Robert
 *
 */
public class HueBridgeConnection {

	private static final String IP = "10.28.9.123";
	private static final String USERNAME = "2b2d3ff23d63751f10c1d8c0332d50ff";
	
	// https://developers.meethue.com/documentation/getting-started
	// http://10.28.9.120/api/197ea42c25303cef1a68c4042ed56887/lights/1
	
	
	private final String ip;
	private final String username;
	
	public HueBridgeConnection() {
		this(IP, USERNAME);
	}
	
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
		sendData(light, json);
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
		float[] hsv = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);

		JSONObject json = new JSONObject();
		json.put("hue", (int) (hsv[0] * (Short.MAX_VALUE * 2 + 1)));
		json.put("sat", (int) (hsv[1] * (Byte.MAX_VALUE * 2)));
		json.put("bri", (int) (hsv[2] * (Byte.MAX_VALUE * 2)));
		json.put("on", true);
		sendData(light, json);
	}

	/**
	 * Sends the JSON to the HueBridge to execute the commands.
	 * @param light Number of the light to be changed.
	 * @param json JSON-Object which contains the attributes to be changed.
	 * @throws HueBridgeException Thrown if the command could not be executed correctly.
	 */
	private void sendData(int light, JSONObject json) throws HueBridgeException {
			
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
            
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                response.append(line + System.lineSeparator());
            
			JSONArray jsonResult = (JSONArray)new JSONParser().parse(response.toString());
			if (!((JSONObject)jsonResult.get(0)).containsKey("success")) {
				throw new HueBridgeException("The command could not be executed at the Hue Bridge.");
			}
        }
        catch (ParseException | IOException e) {
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
        	catch (IOException e) {}
        }
	}
	
}

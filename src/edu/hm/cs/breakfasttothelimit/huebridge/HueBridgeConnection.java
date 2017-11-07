package edu.hm.cs.breakfasttothelimit.huebridge;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Connection to the light system.
 * @author Attenberger Robert
 *
 */
public class HueBridgeConnection {

	//private static final int PORT = 80;
	
	// https://developers.meethue.com/documentation/getting-started
	// http://10.28.9.120/api/197ea42c25303cef1a68c4042ed56887/lights/1
	
	
	private final String ip;
	private final String username;
	//private final Socket socket;
	
	/*public HueBridgeConnection() throws IOException {
		this(IP, USERNAME);
	}*/
	
	public HueBridgeConnection(String ip, String username) throws IOException {
		this.ip = ip;
		this.username = username;
		//socket = new Socket(ip, PORT);
	}
	
	/**
	 * Powers one light off.
	 * @param light Number of the light to be powered off.
	 * @throws HueBridgeException Thrown if the command could not be executed correctly.
	 */
	public void powerOff(int light) throws IOException {
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
	public void powerOn(int light, Color color) throws IOException {
		if (light <= 0 || light > 3)
			throw new IllegalArgumentException("The number of the light must between 1 and 3.");
		float[] hsv = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);

		JSONObject json = new JSONObject();
		json.put("on", true);
		sendJSON(light, json);
		json = new JSONObject();
		json.put("hue", (int) (hsv[0] * (Short.MAX_VALUE * 2 + 1)));
		json.put("sat", (int) (hsv[1] * (Byte.MAX_VALUE * 2)));
		json.put("bri", (int) (hsv[2] * (Byte.MAX_VALUE * 2)));
		sendJSON(light, json);
	}
	
	/*private void sendJSON(int light, JSONObject json) throws IOException {
		HTTPRequest request = new HTTPRequest("PUT", "/api/" + username + "/lights/" + light + "/state");
		request.addHeaderFild("Host", ip);
		request.addHeaderFild("Connection", "keepalive");
		request.e
		
		//try
		{
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer.print(request.getRequest());
			writer.flush();
		}
		
	}*/

	/**
	 * Sends the JSON to the HueBridge to execute the commands.
	 * @param light Number of the light to be changed.
	 * @param json JSON-Object which contains the attributes to be changed.
	 * @throws IOException Thrown if the command could not be executed correctly.
	 */
	private void sendJSON(int light, JSONObject json) throws IOException {
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
            
			//JSONArray jsonResult = (JSONArray)new JSONParser().parse(response.toString());
			//if (!((JSONObject)jsonResult.get(0)).containsKey("success"))
			if (response.toString().contains("error"))	
				throw new IOException("The command could not be executed at the Hue Bridge! " + response.toString());
			
        }
        finally {
        	if (connection != null)
        		connection.disconnect();
	        if (writer != null)
	        	writer.close();
	        if (reader != null)
	            reader.close();
        }
	}

	/*@Override
	public void close() throws IOException {
		socket.close();
	}*/
	
}

package edu.hm.cs.breakfasttothelimit;
import java.awt.Color;
import java.io.IOException;

import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeConnection;
import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeException;

public class Test {

	public static void main(String[] args) throws IOException, HueBridgeException {
		HueBridgeConnection c = new HueBridgeConnection("localhost","newdeveloper");
	
		c.powerOn(1, Color.RED);
		c.powerOn(2, Color.YELLOW);
		c.powerOn(3, Color.BLUE);
	}

}

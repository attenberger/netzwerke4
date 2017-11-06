import java.awt.Color;

import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeConnection;
import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeException;

public class Test {

	public static void main(String[] args) throws HueBridgeException {
		HueBridgeConnection c = new HueBridgeConnection("localhost","newdeveloper");
	
		c.powerOn(1, Color.BLUE);
		c.powerOn(2, Color.BLUE);
		c.powerOn(3, Color.BLUE);
	}

}

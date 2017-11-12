package edu.hm.cs.breakfasttothelimit;
import java.awt.Color;
import java.io.IOException;

import edu.hm.cs.breakfasttothelimit.google.GoogleDistanceService;
import edu.hm.cs.breakfasttothelimit.google.exception.DistanceServiceException;
import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeConnection;
import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeException;

public class Test {

	public static void main(String[] args) throws IOException, HueBridgeException, DistanceServiceException {
		/*
		HueBridgeConnection c = new HueBridgeConnection("localhost","newdeveloper");

		c.powerOn(1, Color.RED);
		c.powerOn(2, Color.YELLOW);
		c.powerOn(3, Color.BLUE);
		*/

		System.out.println(GoogleDistanceService.fetch("Weststraße 25, 84416 Moosen", "Lothstraße 64, München", Transportation.CAR));
	}

}

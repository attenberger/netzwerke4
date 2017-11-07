package edu.hm.cs.breakfasttothelimit.google;

import java.time.Duration;
import java.time.LocalTime;

import edu.hm.cs.breakfasttothelimit.Transportation;

public class Google {
	
	public Google(String address, Transportation transportation) {}
	
	public Duration getTime() {
		return Duration.ofMinutes(3);
	}
}

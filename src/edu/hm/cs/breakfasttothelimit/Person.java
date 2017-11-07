package edu.hm.cs.breakfasttothelimit;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalTime;

import edu.hm.cs.breakfasttothelimit.google.Google;
import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeConnection;

public class Person {
	
	private String name;
	private int light;
	private LocalTime timeClockIn;
	private Google route;
	private HueBridgeConnection hueBridge;
	private boolean startedOff = false;
	private Color currentLightColor = null;
	
	public Person(String name, int light, String address, LocalTime timeClockIn, Transportation transportation, HueBridgeConnection hueBridge) throws IOException {
		this.name = name;
		this.light = light;
		this.timeClockIn = timeClockIn;
		this.hueBridge = hueBridge;
		hueBridge.powerOn(light, Color.WHITE);
		route = new Google(address, transportation);
	}
	
	private void lightOn(Color color) throws IOException {
		if(this.currentLightColor == null || color != this.currentLightColor) {
			hueBridge.powerOn(light, color);
			this.currentLightColor = color;
		}
	}
	
	private void lightOff() throws IOException {
		hueBridge.powerOff(light);
		this.currentLightColor = null;
	}
	
	public String getName() {
		return name;
	}
	
	public int getNumber() {
		return light;
	}
	
	public boolean checkTime() throws IOException {
		if (startedOff)
			return true;
		long timeleft = timeClockIn.toSecondOfDay() - LocalTime.now().toSecondOfDay() - route.getTime().getSeconds();
		if (timeleft > 120) {
			if(!warn.isAlive())
				lightOn(Color.WHITE);
			return true;
		}
		else if (timeleft > 60) {
			if(!warn.isAlive())
				lightOn(Color.ORANGE);
			return true;
		}
		else if (timeleft > 0) {
			if(!warn.isAlive())
				lightOn(Color.RED);
			return true;
		}
		else 
			return false;
	}
	
	public void startWarning() {
		warn.start();
	}
	
	public void stopWarning() throws IOException {
		warn.interrupt();
		try {
			warn.join();
		} catch (InterruptedException e) {}
		if(startedOff)
			lightOff();
		else
			checkTime();
	}
	
	public void startOff() throws IOException {
		startedOff = true;
		lightOff();
	}
	
	public boolean hasStartedOff() {
		return startedOff;
	}
	
	Thread warn = new Thread(){
		public void run(){
			while(!Thread.interrupted()) {
				try {
					lightOn(Color.RED);
					Thread.sleep(1000);
					lightOff();
					Thread.sleep(1000);
				} catch (IOException e) {
					System.out.println(e.getMessage());
				} catch (InterruptedException e) {
					this.interrupt();
				}
				
			}
		}
	};
}

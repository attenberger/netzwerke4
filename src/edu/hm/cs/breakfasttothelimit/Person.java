package edu.hm.cs.breakfasttothelimit;

import java.awt.Color;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

import edu.hm.cs.breakfasttothelimit.google.GoogleDistanceMatrixResponse;
import edu.hm.cs.breakfasttothelimit.google.GoogleDistanceService;
import edu.hm.cs.breakfasttothelimit.google.exception.DistanceServiceException;
import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeConnection;
import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeException;

/**
 * Person for which the time of starting off has to be monitored.
 * @author Attenberger
 */
public class Person {

	private String name;
	private int lightnumber;
	private LocalTime timeClockIn;
	private HueBridgeConnection hueBridge;
	private boolean startedOff = false;
	private Color currentLightColor = null;
	private Thread currentWarning = null; // Warning that runs at the moment

	/*
	COMMUTE INFO
	 */
	private final String placeOfResidence;
	private final String placeOfWork;
	private final Transportation transportation;

	/**
	 * Creats a new Person
	 * @param name of the person
	 * @param lightnumber Number of the light at the Hue Bridge which belongs to the person
	 * @param placeOfResidence Address of the place of residence of the person
	 * @param placeOfWork Address of the place of work of the person
	 * @param timeClockIn Time at which the person wants to start to work
	 * @param transportation the person uses to get to work
	 * @param hueBridge Connection to the light system
	 * @throws HueBridgeException Thrown if an error ocurrs in the connection to the light system
	 */
	public Person(String name, int lightnumber, String placeOfResidence, String placeOfWork, LocalTime timeClockIn, Transportation transportation, HueBridgeConnection hueBridge) throws HueBridgeException {
		this.name = name;
		this.lightnumber = lightnumber;
		this.timeClockIn = timeClockIn;
		this.hueBridge = hueBridge;

		this.placeOfResidence = placeOfResidence;
		this.placeOfWork = placeOfWork;
		this.transportation = transportation;
	}

	/**
	 * Returns the name of the person.
	 * @return name of the person
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the number of the light of the person.
	 * @return number of the light of the person
	 */
	public int getLightNumber() {
		return lightnumber;
	}

	/**
	 * Marks a person started off. The light will be turned off.
	 * @throws HueBridgeException Thrown if an error occurs in the connection to the light system
	 */
	public void startOff() throws HueBridgeException {
		startedOff = true;
		lightOff();
	}

	/**
	 * Returns if the person already started off
	 * @return if the person already started off
	 */
	public boolean hasStartedOff() {
		return startedOff;
	}

	/**
	 * Powers the light of the person in a specific color on.
	 * @param color of the light
	 * @throws HueBridgeException Thrown if an error occurs in the connection to the light system
	 */
	private void lightOn(Color color) throws HueBridgeException {
		if(this.currentLightColor == null || color != this.currentLightColor) {
			hueBridge.powerOn(lightnumber, color);
			this.currentLightColor = color;
		}
	}

	/**
	 * Powers the light of the person off.
	 * @throws HueBridgeException Thrown if an error occurs in the connection to the light system
	 */
	private void lightOff() throws HueBridgeException {
		hueBridge.powerOff(lightnumber);
		this.currentLightColor = null;
	}

	/**
	 * Checks time of the person and signs it by light.
	 * A person already started of will not be checked again.
	 * @return False if the person is to late.
	 * @throws HueBridgeException Thrown if an error occurs in the connection to the light system
	 */
	public boolean checkTime() throws HueBridgeException {
		if (startedOff)
			return true;
		long timeleft = timeLeft();
		if (timeleft > 120) {
			if(currentWarning == null)
				lightOn(Color.WHITE);
			return true;
		} else if (timeleft > 60) {
			if(currentWarning == null)
				lightOn(Color.ORANGE);
			return true;
		} else if (timeleft > 0) {
			if(currentWarning == null)
				lightOn(Color.RED);
			return true;
		} else
			return false;
	}

	/**
	 * Checks the time the person has time to start off latest.
	 * @return number of seconds the person has left. For a started off person the max long value.
	 */
	private long timeLeft() {
		if (startedOff)
			return Long.MAX_VALUE;
		else {
			try {
				long duration = GoogleDistanceService.fetch(placeOfResidence, placeOfWork, transportation).getDuration();
				return timeClockIn.toSecondOfDay() - LocalTime.now().toSecondOfDay() - duration;
			} catch (DistanceServiceException e) {
				e.printStackTrace();
			}
		}

		return -1;
	}

	/**
	 * Starts the warning system. The warning system should be started if anyone is to late.
	 */
	public void startWarning() {
		if (currentWarning == null) {
			currentWarning = new Thread(warn);
			currentWarning.start();
		}
	}

	/**
	 * Stops the warning system. The time for this person is checked and the light will light suitable.
	 * @throws HueBridgeException Thrown if an error occurs in the connection to the light system
	 */
	public void stopWarning() throws HueBridgeException {
		if (currentWarning != null && !currentWarning.isInterrupted()) {
			currentWarning.interrupt();
			try {
				currentWarning.join();
			} catch (InterruptedException e) {}
			currentWarning = null;
		}
		if (startedOff)
			lightOff();
		else
			checkTime();
	}

	/**
	 * Warning system if anyone is to late. The lights will blink red.
	 * The light of persons who are late blink dark red.
	 */
	Runnable warn = new Runnable(){
		public void run(){
			while(!Thread.currentThread().isInterrupted()) {
				try {
					if (timeLeft() > 0)
						lightOn(Color.RED);
					else
						lightOn(new Color(150,0,0));
					Thread.sleep(1000);
					lightOff();
					Thread.sleep(1000);
				} catch (HueBridgeException e) {
					System.out.println(e.getMessage());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	};
}

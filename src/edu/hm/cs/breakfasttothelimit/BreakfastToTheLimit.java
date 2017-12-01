package edu.hm.cs.breakfasttothelimit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.Calendar;

import edu.hm.cs.breakfasttothelimit.google.GoogleDistanceService;
import edu.hm.cs.breakfasttothelimit.google.exception.DistanceServiceException;
import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeConnection;
import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeException;

/**
 * Main class of the Program Breakfast to the limit. Asks for the place of work and clock-in-time 
 * of three persons and shows by lights when the persons have to start off.
 * @author Attenberger
 */
public class BreakfastToTheLimit {
	
//	private static final String IP = "10.28.9.123";
//	private static final String USERNAME = "2b2d3ff23d63751f10c1d8c0332d50ff";
	private static final String IP = "localhost";
	private static final String USERNAME = "newdeveloper";
	public static final int NUMBEROFPERSONS = 3;
	private static final String PLACEOFRESIDENCE = "80335 München, Lothstr. 64";
	
	private static HueBridgeConnection huebridge;
	private static Person[] persons  = new Person[NUMBEROFPERSONS];

	/**
	 * Start method of the program. Asks for the data for the three persons and initiates that the time when they have to leave is monitored.
	 * It also initiates the monitoring which person started off.
	 * @param args
	 * @throws DistanceServiceException 
	 */
	public static void main(String[] args) {
		huebridge = new HueBridgeConnection(IP, USERNAME);
/*		try {
			persons = requestDataForPersons();
		} catch (IOException e) {
			System.out.println("An error occured while reading from commandline!");
			System.out.println(e.getMessage());
			System.out.println("The application will be closed");
			return;
		}
	*/	
		try {
			persons[0] = new Person("Heinz", 1, PLACEOFRESIDENCE, "Marienplatz 8, 80331 München", LocalTime.now().plusSeconds(GoogleDistanceService.fetch(PLACEOFRESIDENCE, "Marienplatz 8, 80331 München", Transportation.BIKE).getDuration() + 140), Transportation.BIKE, huebridge);
			persons[1] = new Person("Wolfgang", 2, PLACEOFRESIDENCE, "Lothstr. 34, 80335 München", LocalTime.now().plusSeconds(GoogleDistanceService.fetch(PLACEOFRESIDENCE, "Lothstr. 34, 80335 München", Transportation.FOOT).getDuration() + 150), Transportation.FOOT, huebridge);
			persons[2] = new Person("Andrea", 3, PLACEOFRESIDENCE, "Boltzmannstraße 1, 85748 Garching bei München", LocalTime.now().plusSeconds(GoogleDistanceService.fetch(PLACEOFRESIDENCE, "Boltzmannstraße 1, 85748 Garching bei München", Transportation.CAR).getDuration() + 160), Transportation.CAR, huebridge);
		} catch (DistanceServiceException e) {
			e.printStackTrace();
		}
		
		System.out.println("==================================");
		System.out.println("Enter number for persons (the light) who started off.");
		for (Person person : persons) {
			System.out.println(person.getLightNumber() + ": " + person.getName());
		}
		checkInput.start();
		checkTime.start();
		
	}
	
	/**
	 * Requests the name, the address of work, the clock-in-time and the used transportation for three persons.
	 * @return Array of three persons.
	 * @throws HueBridgeException Thrown if an error occurs in the connection to the Hue Bridge.
	 * @throws IOException Thrown if error occurs in the connection to the command line.
	 */
	private static Person[] requestDataForPersons() throws IOException {
		Person[] persons = new Person[NUMBEROFPERSONS];
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Welcome to Breakfast to the Limit");
		for (int personnr = 1; personnr <= persons.length; personnr++) {
			System.out.println("==================================");
			System.out.println("Please enter data for person " + personnr + ":");
			System.out.println("Name der Person: ");
			String name = reader.readLine();
			System.out.println("Address of work: ");
			String address = reader.readLine();
			
			// Ask for clock-in-time until the user entered a valid input
			LocalTime time = null;
			do {
				System.out.println("Begin of work (HH:MM): ");
				String[] timeParts = reader.readLine().split(":");
				if (timeParts.length == 2) {
					try {
						time = LocalTime.of(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
					}
					catch (NumberFormatException | DateTimeException e) {
						System.out.println("The format was incorrect!");
					}
				}
				else
					System.out.println("The format was incorrect!");
			} while (time == null);
			
			// Aks for used transportation until the user entered a valid input
			System.out.println("Used transport ([C]ar, [F]oot, [B]ike, [P]ublic transport): ");
			Transportation transport = null;
			do {
				switch(reader.readLine()) {
					case "C": transport = Transportation.CAR;
						break;
					case "F": transport = Transportation.FOOT;
						break;
					case "B": transport = Transportation.BIKE;
						break;
					case "P": transport = Transportation.PUBLICTRANSPORT;
						break;
					default: System.out.println("Invalid Input! Please enter 'C', 'F', 'B' or 'P'!");
				}
			} while(transport == null);
			
			// Create person with all parameters
			persons[personnr-1] = new Person(name, personnr, PLACEOFRESIDENCE, address, time, transport, huebridge);
		}
		return persons;
	}
	
	/**
	 * Returns if every person started off.
	 * @return if every person started off
	 */
	private static boolean hasEveryOneStartedOff() {
		boolean allStartedOff = true;
		synchronized (persons) {
			for(Person person : persons) {
				if (!person.hasStartedOff())
					allStartedOff = false;
			}
		}
		return allStartedOff;
	}
	
	/**
	 * Thread that checks input at the console while monitoring if everyone starts off in time.
	 * Waits until a number for a person is entered and marks the person than as started off.
	 */
	private static Thread checkInput = new Thread(){
		public void run(){
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			int personnumber;
			while(!Thread.interrupted()) {
				try {
					// Reads the number of the person started off
					personnumber = Integer.parseInt(reader.readLine());
					if(personnumber < 1 || personnumber > NUMBEROFPERSONS)
						throw new NumberFormatException();
					
					// Mark the person started off
					synchronized (persons) {
						persons[personnumber-1].startOff();
					}
					// If everyone started off the thread can be closed so that also the program is able to terminate
					if (hasEveryOneStartedOff())
						this.interrupt();
					
				} catch (NumberFormatException e) {
					System.out.println("Invalid Number! Please enter a number of a person!");
				} catch (HueBridgeException e) {
					// If an error occurs in the connection to Hue Bridge show error message and close program
					System.out.println(e.getMessage());
					this.interrupt();
					checkTime.interrupt();
				} catch (IOException e) {
					// If an io error occurs show error message and close program
					System.out.println("An error occured while reading from commandline!");
					System.out.println(e.getMessage());
					System.out.println("The application will be closed");
					this.interrupt();
					checkTime.interrupt();
				}
			}
		}
	};

	/**
	 * Checks every five seconds the time of each person it has left to start off.
	 * If a person is late the warning will be initiated.
	 */
	private static Thread checkTime = new Thread(){
		public void run(){
			try {
				boolean warningOn = false;
				while(!Thread.interrupted()) {
					synchronized(persons) {
						boolean timeOkForAll = true;
						// Check time for every person
						for (Person person : persons) {
							boolean timeOkForOne = person.checkTime();
							if (!timeOkForOne) {
								timeOkForAll = false;
							}
						}
						// Switch on warning if anyone is out of time and warning is off
						if (!timeOkForAll && !warningOn) {
							warningOn = true;
							for (Person person : persons)
								person.startWarning();
						}
						// Switch Off warning if it is on and everyone is in time
						if (timeOkForAll && warningOn) {
							for (Person personWarn : persons)
								personWarn.stopWarning();
							warningOn = false;
						}
					}
					
					// If everyone started off the thread can be closed so that also the program is able to terminate
					if (hasEveryOneStartedOff())
						this.interrupt();
					
					// Wait five secounds until next check
					Thread.sleep(5000);
				}
			}
			catch (HueBridgeException e) {
				// If an error occurs in the connection to Hue Bridge show error message and close program
				System.out.println(e.getMessage());
				this.interrupt();
				checkInput.interrupt();
			}
			catch (InterruptedException e) {
				// If the thread is interrupted, interrupt also the other thread to close the program
				this.interrupt();
				checkInput.interrupt();
			}
		}
		
	};

}

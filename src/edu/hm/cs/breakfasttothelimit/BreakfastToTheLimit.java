package edu.hm.cs.breakfasttothelimit;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DateTimeException;
import java.time.LocalTime;

import edu.hm.cs.breakfasttothelimit.google.Google;
import edu.hm.cs.breakfasttothelimit.huebridge.HueBridgeConnection;

public class BreakfastToTheLimit {
	
	//private static final String IP = "10.28.9.123";
	//private static final String USERNAME = "2b2d3ff23d63751f10c1d8c0332d50ff";
	private static final String IP = "localhost";
	private static final String USERNAME = "newdeveloper";
	private static final int NUMBEROFPERSONS = 3;
	
	private static HueBridgeConnection huebridge;
	private static Person[] persons  = new Person[NUMBEROFPERSONS];

	public static void main(String[] args) {
		try {
			huebridge = new HueBridgeConnection(IP, USERNAME);
			//requestDataForPersons(persons);
			persons[0] = new Person("Heinz", 1, "Marienplatz 8, 80331 München", LocalTime.of(23, 58), Transportation.BIKE, huebridge);
			persons[1] = new Person("Wolfgang", 2, "Lothstr. 34, 80335 München", LocalTime.of(23, 59), Transportation.FOOT, huebridge);
			persons[2] = new Person("Andrea", 3, "Boltzmannstraße 1, 85748 Garching bei München", LocalTime.of(23, 58), Transportation.CAR, huebridge);
			System.out.println("==================================");
			System.out.println("Enter number for persons who started off.");
			for (Person person : persons) {
				System.out.println(person.getNumber() + ": " + person.getName());
			}
			checkTime.start();
			checkInput.start();
		} catch (IOException e) {
			System.out.println("An error ocoured in the connection to the HUE-Bridge!\r\n" + e.getMessage());
		}
		
	}
	
	private static void requestDataForPersons(Person[] persons) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Welcome to Breakfast to the Limit");
		for (int personnr = 1; personnr <= persons.length; personnr++) {
			System.out.println("==================================");
			System.out.println("Please enter data for person " + personnr + ":");
			System.out.println("Name der Person: ");
			String name = reader.readLine();
			System.out.print("Address of work: ");
			String address = reader.readLine();
			
			LocalTime time = null;
			do {
				System.out.print("Begin of work (HH:MM): ");
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
			
			System.out.print("Used transport ([C]ar, [F]oot, [B]ike, [P]ublic transport): ");
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
			persons[personnr-1] = new Person(name, personnr, address, time, transport, huebridge);
			
		}
	}
	
	private static Thread checkInput = new Thread(){
		public void run(){
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			int personnumber;
			boolean allStartedOff = false;
			while(!Thread.interrupted() && !allStartedOff) {
				try {
					personnumber = Integer.parseInt(reader.readLine());
					if(personnumber < 1 || personnumber > NUMBEROFPERSONS)
						throw new NumberFormatException();
					synchronized (persons) {
						persons[personnumber-1].startOff();
						allStartedOff = true;
						for(Person person : persons) {
							if (!person.hasStartedOff())
								allStartedOff = false;
						}
					}
					
				}
				catch (NumberFormatException e) {
					System.out.println("Unvalid Number");
				} catch (IOException e) {
					System.out.println("An error ocoured in the connection to the HUE-Bridge!\r\n" + e.getMessage());
					this.interrupt();
				}
			}
		}
	};

	private static Thread checkTime = new Thread(){
		public void run(){
			try {
				boolean warnOn = false;
				boolean allStartedOff = false;
				while(!Thread.interrupted() && !allStartedOff) {
					synchronized(persons) {
						boolean timeOkForAll = true;
						for (Person person : persons) {
							boolean timeOkForOne = person.checkTime();
							if (!timeOkForOne) {
								timeOkForAll = false;
							}
							// Switch On warning if anyone is out of time and warning is off
							if (!timeOkForOne && !warnOn) {
								warnOn = true;
								for (Person personWarn : persons)
									personWarn.startWarning();
							}
						}
						// Switch Off warning if it is on and everyone is in time
						if (timeOkForAll && warnOn) {
							for (Person personWarn : persons)
								personWarn.stopWarning();
							warnOn = false;
						}
					}
					Thread.sleep(5000);
					
					allStartedOff = true;
					synchronized(persons) {
						for (Person person : persons) {
							if (!person.hasStartedOff())
								allStartedOff = false;
						}
					}
				}
			}
			catch (IOException e) {
				System.out.println("An error ocoured in the connection to the HUE-Bridge!\r\n" + e.getMessage());
				this.interrupt();
			}
			catch (InterruptedException e) {
				this.interrupt();
			}
		}
	};

}

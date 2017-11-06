import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DateTimeException;
import java.time.LocalTime;

public class BreakfastToTheLimit {

	public static void main(String[] args) {
		Person[] persons;
		try {
			persons = requestDataForPersons();
		} catch (IOException e) {
			System.out.println("Error while requesting data for persons! Default will be used");
			persons = new Person[3];
			persons[0] = new Person("Marienplatz 8, 80331 München", LocalTime.of(8, 0), Transportation.BIKE);
			persons[1] = new Person("Lothstr. 34, 80335 München", LocalTime.of(7, 30), Transportation.FOOT);
			persons[2] = new Person("Boltzmannstraße 1, 85748 Garching bei München", LocalTime.of(9, 5), Transportation.CAR);
		}
		
	}
	
	private static Person[] requestDataForPersons() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		Person[] persons = new Person[3];
		System.out.println("Welcome to Breakfast to the Limit");
		for (int personnr = 1; personnr <= 3; personnr++) {
			System.out.println("==================================");
			System.out.println("Please enter data for person " + personnr + ":");
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
			persons[personnr-1] = new Person(address, time, transport);
			
		}
		return persons;
	}

}

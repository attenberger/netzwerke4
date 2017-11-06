import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BreakfastToTheLimit {

	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		Person[] personen = new Person[3];
		System.out.println("Welcome to Breakfast to the Limit");
		for (int personnr = 1; personnr <= 3; personnr++) {
			System.out.println("==================================");
			System.out.println("Please enter data for person " + personnr + ":");
			System.out.print("Address of work: ");
			String address = reader.readLine();
			System.out.print("Begin of work: ");
			String time = reader.readLine();
			System.out.print("Used transport: ");
			String transport = reader.readLine();
			//personen[personnr-1] = new Person(address, time, transport);
		}
	}

}

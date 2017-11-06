import java.time.LocalTime;

public class Person {

	private final String address;
	private final LocalTime time;
	private final Transportation transportation;
	
	public Person(String address, LocalTime time, Transportation transportation) {
		this.address = address;
		this.time = time;
		this.transportation = transportation;
	}
}

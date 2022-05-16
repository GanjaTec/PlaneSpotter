package planespotter.dataclasses;

/**
 * @author Janne Matti
 * @author Lukas
 * 
 * Airline class: represents an airline
 */
public class Airline implements Data {
	private int id;
	private String iatatag;
	private String name;

	/** Constructor 
	 * 
	 * @param id int Database ID
	 * @param tag String IATA Tag
	 * @param name String Airline Name
	 */
	public Airline(int id, String tag, String name) {
		this.id = id;
		this.iatatag = tag;
		this.name = name;
	}

	//getter	
	/**
	 * @return int Database ID
	 */
	public int getID() {
		return this.id;
	}

	/**
	 * @return String IATA Tag
	 */
	public String getTag() {
		return this.iatatag;
	}

	/**
	 * @return String Airline Name
	 */
	public String getName() {
		return this.name;
	}
}

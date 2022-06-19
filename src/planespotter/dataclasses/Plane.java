package planespotter.dataclasses;

/**
 * @author Janne Matti
 * @author Lukas
 *
 * This Class is used to Represent a DB Entry from the 'Plane' Table 
 */
public record Plane(int id,
					String icao,
					String tailNr,
					String planeType,
					String registration,
					Airline airline)
		implements Data {
}

/*
public class Plane implements Data {

	private int id;
	private String icao;
	private String tailnr;
	private String planetype;
	private String registration;
	private Airline airline;

	*/
/**
	 * Constructor
	 * 
	 * @param id int Database ID
	 * @param icao String 24bit ICAO Address
	 * @param tailnr String Tailnumber
	 * @param planetype String Planetype
	 * @param registration String Registrationnumber
	 * @param airline Airline Airline the Plane belongs to
	 *//*

	public Plane (int id, String icao, String tailnr, String planetype, String registration, Airline airline) {
		this.id = id;
		this.icao = icao;
		this.tailnr = tailnr;
		this.planetype = planetype;
		this.registration = registration;
		this.airline = airline;
	}
	
	//Getter
	*/
/**
	 * @return int Database ID
	 *//*

	public int getID() {
		return this.id;
		}

	*/
/**
	 * @return String 24bit ICAO Adress
	 *//*

	public String getIcao() {
		return this.icao;
		}

	*/
/**
	 * @return String Tailnumber
	 *//*

	public String getTailnr() {
		return this.tailnr;
		}

	*/
/**
	 * @return String Planetype
	 *//*

	public String getPlanetype() {
		return this.planetype;
		}

	*/
/**
	 * @return String Registrationnumber
	 *//*

	public String getRegistration() {
		return this.registration;
		}

	*/
/**
	 * @return Airline Airline the Plane Belongs to
	 *//*

	public Airline getAirline() {
		return this.airline;
		}

}
*/

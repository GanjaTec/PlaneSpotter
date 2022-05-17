package planespotter.dataclasses;

/**
 * @author Janne Matti
 * @author Lukas
 *
 * Data Structure used to represent a DB entry from the 'Airports'-Table
 */
public class Airport implements Data {
    private int id;
    private String iatatag;
    private String name;
    private Position pos;

    /**
     * Constructor
     * 
     * @param id int Database ID
     * @param tag String IATA Tag
     * @param name String Airport Name
     * @param pos Position Airport Coordinates
     */
    public Airport(int id, String tag, String name, Position pos) {
        this.id = id;
        this.iatatag = tag;
        this.name = name;
        this.pos = pos;
    }

    //Getter
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
     * @return String Airport Name
     */
    public String getName() {
    	return this.name;
    	}

    /**
     * @return Position Airport Coordinates
     */
    public Position getPos() {
    	return this.pos;
    	}
    

}

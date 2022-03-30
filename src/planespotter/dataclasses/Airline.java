package planespotter.dataclasses;

/**
 * Airline class: represents an airline
 */
public class Airline {
    private int id;
    private String iatatag;
    private String name;

    public Airline (int id, String tag, String name) {
        this.id = id;
        this.iatatag = tag;
        this.name = name;
    }

    /**
     * getter
     */
    public int getID () {
    	return id;
    	}
    
    public String getTag() {
    	return this.iatatag;
    }
    
    public String getName () {
    	return name;
    	}


}

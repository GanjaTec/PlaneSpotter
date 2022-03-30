package planespotter.dataclasses;

public class Airport {
    private int id;
    private String iatatag;
    private String name;
    Position pos;

    
    //TODO remove
    public Airport (int id, String tag, String name) {
        this.id = id;
        this.iatatag = tag;
        this.name = name;
    }

    public Airport (int id, String tag, String name, Position pos) {
        this.id = id;
        this.iatatag = tag;
        this.name = name;
        this.pos = pos;
    }

    /**
     * getter
     */
    public int getID () {
    	return this.id;
    	}
    
    public String getTag() {
    	return this.iatatag;
    	}

    public String getName () {
    	return this.name;
    	}

    public Position getPos () {
    	return this.pos;
    	}
    

}

package planespotter.dataclasses;

public class Airport {
    private int id;
    private String name;
    Position pos;

    public Airport (int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Airport (int id, String name, Position pos) {
        this.id = id;
        this.name = name;
        this.pos = pos;
    }

    /**
     * getter
     */
    public int getID () { return id; }

    public String getName () { return name; }

    public Position getPos () { return pos; }

}

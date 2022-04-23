package planespotter.throwables;

public class DataNotFoundException extends Exception {

    /**
     * constructor, is called when this exception is thrown
     */
    public DataNotFoundException() {
        super("db-data couldn't be found!");
    }
}

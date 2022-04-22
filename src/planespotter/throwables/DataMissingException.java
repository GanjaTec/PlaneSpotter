package planespotter.throwables;

public class DataMissingException extends Exception {

    /**
     * constructor, is called when this exception is thrown
     */
    public DataMissingException () {
        super("db-data couldn't be found!");
    }
}

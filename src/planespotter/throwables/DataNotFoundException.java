package planespotter.throwables;

// TODO neuen DIalog erstellen!! wenn kein flug oder so gefunden wurde // neuer Konstructor mit param dialog
public class DataNotFoundException extends Exception {

    // exception message
    private final String MESSAGE = "db-data couldn't be found!";

    /**
     * constructor, is called when this exception is thrown
     */
    public DataNotFoundException() {
        this((String) null);
    }


    /**
     * (main) constructor with string and boolean params
     *
     * @param msg is the exception message
     */
    public DataNotFoundException(String msg) {
        super(msg);
    }

    public DataNotFoundException(DataNotFoundException cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return this.MESSAGE;
    }
}

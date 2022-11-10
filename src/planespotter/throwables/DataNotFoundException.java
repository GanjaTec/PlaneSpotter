package planespotter.throwables;

public class DataNotFoundException extends Exception {

    // exception message
    private static final String MESSAGE = "DB-data couldn't be found!";

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

    public DataNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return MESSAGE;
    }
}

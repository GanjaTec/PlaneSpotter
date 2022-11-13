package planespotter.throwables;

public class NoMatchException extends Exception {

    public NoMatchException() {
    }

    public NoMatchException(String message) {
        super(message);
    }

    public NoMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMatchException(Throwable cause) {
        super(cause);
    }
}

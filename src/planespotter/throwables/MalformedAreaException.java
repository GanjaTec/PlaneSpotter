package planespotter.throwables;

public class MalformedAreaException extends Exception {

    public MalformedAreaException() {
    }

    public MalformedAreaException(String message) {
        super(message);
    }

    public MalformedAreaException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedAreaException(Throwable cause) {
        super(cause);
    }
}

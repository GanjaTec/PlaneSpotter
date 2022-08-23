package planespotter.throwables;

public class InvalidDataException extends IllegalArgumentException {

    public InvalidDataException(final String msg) {
        super(msg);
    }

    public InvalidDataException() {
        this("");
    }

    public InvalidDataException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InvalidDataException(Throwable cause) {
        super(cause);
    }
}

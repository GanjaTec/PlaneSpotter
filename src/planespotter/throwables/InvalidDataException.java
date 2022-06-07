package planespotter.throwables;

public class InvalidDataException extends IllegalArgumentException {

    public InvalidDataException(final String msg) {
        super(msg);
    }

    public InvalidDataException() {
        this("");
    }
}

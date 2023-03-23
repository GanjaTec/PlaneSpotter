package planespotter.throwables;

public class InvalidCoordinatesException extends InvalidDataException {

    public InvalidCoordinatesException() {
        this("");
    }

    public InvalidCoordinatesException(String msg) {
        super(msg);
    }
}

package planespotter.throwables;

public class InvalidArrayException extends InvalidDataException {

    public InvalidArrayException() {
        this("");
    }

    public InvalidArrayException(String msg) {
        super(msg);
    }
}

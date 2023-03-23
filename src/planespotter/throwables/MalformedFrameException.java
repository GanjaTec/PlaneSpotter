package planespotter.throwables;

public class MalformedFrameException extends Exception {

    public MalformedFrameException() {
    }

    public MalformedFrameException(String message) {
        super(message);
    }

    public MalformedFrameException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedFrameException(Throwable cause) {
        super(cause);
    }

}

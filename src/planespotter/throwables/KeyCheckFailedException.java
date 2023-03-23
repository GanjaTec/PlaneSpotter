package planespotter.throwables;

public class KeyCheckFailedException extends Exception {

    public KeyCheckFailedException() {
    }

    public KeyCheckFailedException(String message) {
        super(message);
    }

    public KeyCheckFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyCheckFailedException(Throwable cause) {
        super(cause);
    }

    public KeyCheckFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

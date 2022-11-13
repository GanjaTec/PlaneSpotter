package planespotter.throwables;

public class URIException extends Exception {

    public URIException() {
    }

    public URIException(String message) {
        super(message);
    }

    public URIException(String message, Throwable cause) {
        super(message, cause);
    }

    public URIException(Throwable cause) {
        super(cause);
    }
}

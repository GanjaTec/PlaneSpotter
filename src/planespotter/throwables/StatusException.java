package planespotter.throwables;

public class StatusException extends Fr24Exception {

    private final int statusCode;

    public StatusException(int statusCode) {
        this(statusCode, "");
    }

    public StatusException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public StatusException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public StatusException(int statusCode, Throwable cause) {
        this(statusCode, "", cause);
    }

    public int getStatusCode() {
        return this.statusCode;
    }

}

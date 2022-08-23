package planespotter.throwables;

/**
 * @name Fr24Exception
 * @author jml04
 * @version 1.0
 * @description
 * Fr24Exception is always thrown, when there is an error with the
 * Fr24-data, which we want to collect
 */
public class Fr24Exception extends InvalidDataException {

    public Fr24Exception(String msg) {
        super(msg);
    }

    public Fr24Exception() {
        this("");
    }

    public Fr24Exception(String msg, Throwable cause) {
        super(msg, cause);
    }

    public Fr24Exception(Throwable cause) {
        this("", cause);
    }

}

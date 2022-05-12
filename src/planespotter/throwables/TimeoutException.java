package planespotter.throwables;

public class TimeoutException extends Exception {

    public TimeoutException(int max) {
        super("Timed out! Loaded over " + max + " seconds!");
    }
}

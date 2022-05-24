package planespotter.unused;

public class TaskTimeoutException extends Exception {

    public TaskTimeoutException(int max) {
        super("Timed out! Loaded over " + max + " seconds!");
    }
}

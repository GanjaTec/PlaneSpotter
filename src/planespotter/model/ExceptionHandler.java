package planespotter.model;

@FunctionalInterface
public interface ExceptionHandler {

    void handleException(final Throwable thr);
}

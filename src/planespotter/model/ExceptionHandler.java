package planespotter.model;

/**
 * @name ExceptionHandler
 * @author jml04
 * @version 1.0
 *
 * @description
 * The ExceptionHandler interface can be used to handle exceptions in a good way,
 * by implementing the {@link ExceptionHandler}, you can just call handleException(...) to
 * handle all exceptions, another opportunity would be, to give an {@link ExceptionHandler} as parameter
 */
@FunctionalInterface
public interface ExceptionHandler {

    /**
     * handles exceptions,
     * checks the exception / error and
     * does further action
     *
     * @param thr is the {@link Throwable} that was thrown and is going to be handled
     */
    void handleException(final Throwable thr);
}

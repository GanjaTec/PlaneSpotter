package planespotter.exceptions;

/**
 * semaphor error is thrown when a semaphor is set to a
 * higher rate than maximum or a lower rate than minimum
 */
public class SemaphorError extends Error {

    /**
     * message is the error message
     */
    private String message = "Semaphor rate can only be between 0 and 1";

    /**
     * default SemaphorError constructor
     */
    public SemaphorError () {
        super();
    }

    /**
     * @return the error message
     */
    @Override
    public String getMessage () {
        return message;
    }

}

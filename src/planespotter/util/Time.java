package planespotter.util;

/**
 * @name Time
 * @author jml04
 * @version 1.0
 *
 * @description
 * class Time contains easy and helpful time utility functions
 */
public abstract class Time {

    /**
     * getter for the current system time millis
     *
     * @return current system time in milliseconds
     */
    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    /**
     * returns the elapsed milliseconds since a certain start time
     *
     * @param startMillis is the start time in milliseconds
     * @return elapsed milliseconds since startTime
     */
    public static long elapsedMillis(long startMillis) {
        return nowMillis() - startMillis;
    }

    /**
     * returns the elapsed seconds since a certain start time
     *
     * @param startMillis is the start time in milliseconds
     * @return elapsed seconds since startTime
     */
    public static long elapsedSeconds(long startMillis) {
        return elapsedMillis(startMillis) / 1000;
    }

}

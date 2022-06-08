package planespotter.util;

/**
 * @name Time
 * @author jml04
 * @version 1.0
 *
 * class Time contains time utility functions
 */
public abstract class Time {

    /**
     * @return current system time in milliseconds
     */
    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    /**
     * @param startMillis is the start time in milliseconds
     * @return elapsed milliseconds since startTime
     */
    public static long elapsedMillis(long startMillis) {
        return nowMillis() - startMillis;
    }

    /**
     * @param startMillis is the start time in milliseconds
     * @return elapsed seconds since startTime
     */
    public static long elapsedSeconds(long startMillis) {
        return elapsedMillis(startMillis) / 1000;
    }

}

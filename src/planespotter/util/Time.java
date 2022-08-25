package planespotter.util;

import java.util.concurrent.TimeUnit;

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
        return timeDiff(nowMillis(), startMillis, TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    /**
     * returns the elapsed seconds since a certain start time
     *
     * @param startMillis is the start time in milliseconds
     * @return elapsed seconds since startTime
     */
    public static long elapsedSeconds(long startMillis) {
        return timeDiff(nowMillis(), startMillis, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    }

    /**
     * calculates the time difference between now and a second timestamp
     *
     * @param now is the now-timestamp in the {@link TimeUnit} of 'inputTimeUnit'
     * @param last is the last timestamp in the {@link TimeUnit} of 'inputTimeUnit'
     * @param inputTimeUnit is the input {@link TimeUnit}
     * @param outputTimeUnit is the output {@link TimeUnit}
     * @return time difference between the two timestamps, in the {@link TimeUnit} of 'outputTimeUnit'
     */
    public static long timeDiff(long now, long last, TimeUnit inputTimeUnit, TimeUnit outputTimeUnit) {
        long tDiff = now - last;
        return switch (outputTimeUnit) {
            case NANOSECONDS -> inputTimeUnit.toNanos(tDiff);
            case MICROSECONDS -> inputTimeUnit.toMicros(tDiff);
            case MILLISECONDS -> inputTimeUnit.toMillis(tDiff);
            case SECONDS -> inputTimeUnit.toSeconds(tDiff);
            case MINUTES -> inputTimeUnit.toMinutes(tDiff);
            case HOURS -> inputTimeUnit.toHours(tDiff);
            case DAYS -> inputTimeUnit.toDays(tDiff);
        };
    }

}

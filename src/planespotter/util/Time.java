package planespotter.util;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @name Time
 * @author jml04
 * @version 1.0
 *
 * @description
 * class Time contains easy and helpful time utility functions
 */
public final class Time {

    /**
     * equal to System.currentTimeMillis()
     *
     * @return the difference, measured in milliseconds, between
     *         the current time and midnight, January 1, 1970, UTC
     */
    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    /**
     * creates a {@link Date} from the current time millis
     *
     * @return the "now" {@link Date}
     */
    public static Date date() {
        return dateFrom(nowMillis());
    }

    /**
     * creates a {@link Date} from an {@link Instant} of the given epoch millis
     *
     * @param epochMillis are the epoch millis after midnight, January 1, 1970, UTC
     * @return {@link Date}, parsed from the epoch millis
     * @see Date
     * @see Instant
     */
    public static Date dateFrom(long epochMillis) {
        return Date.from(Instant.ofEpochMilli(epochMillis));
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

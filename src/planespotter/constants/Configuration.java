package planespotter.constants;

/**
 * @name Configuration
 * @author jml04
 * @version 1.0
 *
 * class Configuration is the planespotter configuration
 * // TODO setting kommen entweder hier rein oder in eine extra Klasse für "User-Settings"
 * // TODO dir could be changed in the future ( i'm not safe )
 */
public final class Configuration {

    /**
     * private constructor
     */
    private Configuration () {
    }

    // max thread pool size
    public static final int MAX_THREADPOOL_SIZE = 200;
    // thread keep-alive-time
    public static final long KEEP_ALIVE_TIME = 6L;

}

package planespotter.constants;

/**
 * @name Configuration
 * @author jml04
 * @version 1.0
 * @description
 * abstract class Configuration contains the internal planespotter-configuration
 */
public abstract class Configuration {

    // title
    public static final String TITLE = "PlaneSpotter v0.3";

    // max thread pool size
    public static final int MAX_THREADPOOL_SIZE = 80;

    // thread keep-alive-time
    public static final long KEEP_ALIVE_TIME = 4L;

    // core pool size (threads that are running constantly)
    // maybe we could do improvements here (like always using the same threads)
    public static final int CORE_POOLSIZE = 0;

    // configuration file name
    public static final String CONFIG_FILENAME = Paths.RESOURCE_PATH + "config.psc";

    // filter file name
    public static final String FILTERS_FILENAME = Paths.RESOURCE_PATH + "filters.psc";

    // 'save logs' flag, logs are saved on shutdown, if enabled
    public static final boolean SAVE_LOGS = false;
}

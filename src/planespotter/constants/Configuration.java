package planespotter.constants;

/**
 * @name Configuration
 * @author jml04
 * @version 1.0
 * @description
 * abstract class Configuration contains the internal planespotter-configuration
 */
public abstract class Configuration {

    // max thread pool size
    public static final int MAX_THREADPOOL_SIZE = 40;
    // thread keep-alive-time
    public static final long KEEP_ALIVE_TIME = 4L;
    // core pool size (threads that are running constantly)
    public static final int CORE_POOLSIZE = 0;

}

package planespotter.model.io;

/**
 * @name Keeper
 * @author jml04
 * @version 1.0
 *
 * @description
 * Functional Interface Keeper represents an abstract Database-Keeper.
 * @see planespotter.model.io.KeeperOfTheArchives
 * @see planespotter.unused.KeeperOfTheArchivesSenior
 */
@FunctionalInterface
public interface Keeper extends Runnable {

    /**
     * keep-method starts a Keeper-Pass
     */
    void keep();

    /**
     * default run method, calls the keep method
     */
    @Override
    default void run() {
        this.keep();
    }
}

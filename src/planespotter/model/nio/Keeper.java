package planespotter.model.nio;

/**
 * @name Keeper
 * @author jml04
 * @version 1.0
 *
 * Functional Interface Keeper represents an abstract Database-Keeper.
 */
@FunctionalInterface
public interface Keeper extends Runnable {

    /**
     * keep-Method starts a Keeper-Pass
     */
    void keep();

    /**
     *
     */
    @Override
    default void run() {
        this.keep();
    }
}

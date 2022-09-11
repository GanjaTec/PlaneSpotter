package planespotter.model.nio;

import planespotter.unused.ProtoSupplierADSB;

/**
 * @name Supplier
 * @author jml04
 * @author Lukas
 * @version 1.0
 *
 * @description
 * The supplier is the interface for the Suppliers,
 * which collect data and supply it to the database,
 * by deserializing it and turning it into database-friendly
 * Frames
 * @see ProtoSupplierADSB
 * @see planespotter.model.nio.Fr24Supplier
 * for examples
 */
@FunctionalInterface
public interface Supplier extends Runnable {

    /**
     * supplier-method,
     * collects data, deserializes
     * and supplies it to the database
     * @see planespotter.model.nio.ADSBSupplier
     * @see planespotter.model.nio.Fr24Supplier
     * for examples
     *
     */
    void supply();

    /**
     * default run method from the Runnable interface,
     * calls the supply-method
     */
    default void run() {
        this.supply();
    }

}

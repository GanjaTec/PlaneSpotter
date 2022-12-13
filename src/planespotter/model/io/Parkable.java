package planespotter.model.io;

import planespotter.util.Utilities;

public interface Parkable {

    /**
     * parks the current thread,
     * needs special access to Utilities.getUnsafe()
     *
     * note: use with caution, be sure you are on the right thread
     */
    default void park() {
        Utilities.getUnsafe().park(true, 5000);
    }

}

package planespotter.model.io;

import planespotter.util.Utilities;

public interface Parkable {

    default void park() {
        Utilities.getUnsafe().park(true, 5000);
    }

}

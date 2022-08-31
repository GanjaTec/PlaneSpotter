package planespotter.model;

import planespotter.unused.ProtoSupplierADSB;

/**
 * @name ADSBCollector
 * @author
 * @version 1.0
 * @description
 * ADSBCollector is a Collector-subclass for
 * collecting ADSB data with custom antenna
 */
public class ADSBCollector extends Collector<ProtoSupplierADSB> {

    public static void main(String[] args) {
        new ADSBCollector(true).start();
    }

    /**
     * ADSBCollector constructor
     *
     * @param exitOnClose indicates if the whole program should exit
     *                    when the 'X'-button is pressed
     */
    protected ADSBCollector(boolean exitOnClose) {
        super(exitOnClose, new ProtoSupplierADSB(null, 0, false)); // example
    }

    /**
     * starts the collecting-task
     * TODO implement task
     */
    @Override
    public void startCollecting() {
        super.startNewMainThread(() -> {
            //
            // start ADSBSupplier task and collect the data
            //
        }, "ADSB Supplier");
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}

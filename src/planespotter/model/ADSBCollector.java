package planespotter.model;

import org.jetbrains.annotations.TestOnly;
import planespotter.model.nio.ADSBSupplier;
import planespotter.model.nio.DataProcessor;

/**
 * @name ADSBCollector
 * @author -
 * @version 1.0
 *
 * @description
 * ADSBCollector is a Collector-subclass for
 * collecting ADSB data with custom antenna
 */
@TestOnly
public class ADSBCollector extends Collector<ADSBSupplier> {

    public static void main(String[] args) {
        new ADSBCollector("http://192.168.178.47:8080/data/aircraft.json", true).start();
    }

    /**
     * ADSBCollector constructor
     *
     * @param exitOnClose indicates if the whole program should exit
     *                    when the 'X'-button is pressed
     */
    protected ADSBCollector(String requestUri, boolean exitOnClose) {
        super(exitOnClose, new ADSBSupplier(requestUri, new DataProcessor(), null)); // example
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

package planespotter.controller;

import planespotter.display.UserSettings;
import planespotter.model.OutputWizard;

import java.util.Objects;

import static planespotter.controller.Controller.*;

public class IOWizard {

    // controller instance
    private Controller controller = Controller.getInstance();

    /**
     * loads flights into the preloadedFlights list
     * works parallel ( recursive )
     * when a there are more than 50 flights to load, new ThreadedOutputWizards are created recursively
     */
    void loadFlightsParallel () {
        int startID = 12000;
        int endID = UserSettings.getMaxLoadedFlights();
        int maxFlightsPerTask = (endID-startID)/100;
        OutputWizard outputWizard;
        if (maxFlightsPerTask <= 20) {
            outputWizard = new OutputWizard(0, startID, endID, 20);
        } else {
            outputWizard = new OutputWizard(0, startID, endID, maxFlightsPerTask);
        }
        exe.execute(outputWizard);
            while (exe.getActiveCount() > 0) {
            }
        while (!listQueue.isEmpty()) { // adding all loaded lists to the main list ( listQueue is threadSafe )
            preloadedFlights.addAll(Objects.requireNonNull(listQueue.poll()));
        }
        controller.done();
    }

}

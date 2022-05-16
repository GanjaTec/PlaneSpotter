package planespotter.controller;

import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Flight;
import planespotter.display.UserSettings;
import planespotter.model.DBOut;
import planespotter.model.OutputWizard;
import planespotter.throwables.DataNotFoundException;

import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class DataMaster {

    // controller instance
    private final Controller controller = Controller.getInstance();

    /**
     * loads flights into the preloadedFlights list
     * works parallel ( recursive )
     * when a there are more than 50 flights to load, new ThreadedOutputWizards are created recursively
     */
    void load () {
        int startID = 0;
        int endID = new UserSettings().getMaxLoadedData();
        int dataPerTask = 5000; // testen!
        var scheduler = Controller.getScheduler();
        controller.liveData = new Vector<>();
        var outputWizard = new OutputWizard(scheduler, 0, startID, endID, dataPerTask);
        scheduler.exec(outputWizard);
        controller.waitForFinish();
        controller.done();
    }

    /**
     * @param id is the flight id to search for
     * @return a flight
     */
    public Flight flightByID (final int id) {
        try {
            return new DBOut().getFlightByID(id);
        } catch (DataNotFoundException e) {
            Controller.getLogger().errorLog("flight with the ID " + id + " doesn't exist!", this);
        } return null;
    }


    private boolean onLock = false;
    public synchronized Thread dataLoader () {
        return new Thread(() -> {
            int startID = 0;
            int dataPerTask = 5000;
            int maxStartID = 20000;
            var ctrl = Controller.getInstance();
            OutputWizard outw;
            for (;;) {
                if (ctrl.loading || OutputWizard.dataQueue.isEmpty()) {
                    try {
                        this.onLock = true;
                        //this.wait(1000);
                        TimeUnit.SECONDS.sleep(1);
                        //this.notify();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        this.onLock = false;
                    }
                } else {
                    var dps = OutputWizard.dataQueue.poll();
                    var dataPointStream = dps.parallelStream(); // kann man bestimmt besser machen
                    var liveData = ctrl.liveData;
                    dataPointStream // ?
                            .filter(dp -> this.liveFlightIDs().contains(dp.getFlightID()))
                            .forEach(dp -> liveData.replaceAll(a -> (dp.getFlightID() == a.getFlightID()) ? dp : a)); // FIXME: 16.05.2022 das müsste falsch sein //??? jetzt vielleicht richtig?
                    dataPointStream
                            .filter(dp -> !this.liveFlightIDs().contains(dp.getFlightID()))
                            .forEach(liveData::add);
                    dataPointStream.close();
                }
            } // TODO alte müssen noch gelöscht werden
        });
    }

    private Vector<Integer> liveFlightIDs () {
        var ids = new Vector<Integer>();
        Controller.getInstance().liveData
                .stream()
                .forEach(dp -> ids.add(dp.getFlightID()));
        return ids;
    }

}


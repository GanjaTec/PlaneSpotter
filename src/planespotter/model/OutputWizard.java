package planespotter.model;

import planespotter.controller.Controller;
import planespotter.dataclasses.Flight;
import planespotter.throwables.ThreadOverheadError;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static planespotter.constants.GUIConstants.*;
import static planespotter.controller.Controller.exe;

public class OutputWizard extends DBOut implements Runnable {
    /**
     * class variables
     */
    private int threadNumber, from, to, flightsPerTask;
    private String threadName;

    /**
     * constructor
     */
    public OutputWizard(int tNumber, int from, int to, int flightsPerTask) {
        this.threadNumber = tNumber;
        this.threadName = "output-wizard" + this.threadNumber;
        this.from = from;
        this.to = to-1;
        this.flightsPerTask = flightsPerTask;
    }

    /**
     * ThreadedOutputWizard run method is executed when an
     * output wizard thread is executed by the threadPoolExecutor
     */
    @Override
    public void run () {
        long start = System.nanoTime();
        Thread.currentThread().setPriority(9);
        Thread.currentThread().setName(this.threadName);
        System.out.println(EKlAuf + "OutputWizard" + EKlZu + " thread " + ANSI_ORANGE + this.getName() + ANSI_RESET + " created!");
        this.loadFlights(from, to);
        System.out.println( EKlAuf +  this.getName() + ANSI_ORANGE + "@" + ANSI_RESET + Thread.currentThread().getId() +
                            EKlZu + " loaded data in " + ANSI_YELLOW + (System.nanoTime()-start)/Math.pow(1000, 3) +
                            ANSI_RESET + " seconds!");
    }

    /**
     * loads flights into the flight list queue in Controller
     * @param fromID is the start-id,
     * @param toID is the (exclusive) end-id
     */
    public void loadFlights (int fromID, int toID) {
        int flightsToLoad = toID - fromID;
        if (flightsToLoad <= flightsPerTask) {
            List<Flight> flights = super.getAllFlightsFromID(fromID, toID);
            Controller.listQueue.add(flights);
        } else {
            int newEndID = to-(flightsToLoad/2);
            OutputWizard out0 = new OutputWizard(threadNumber+1, fromID, newEndID, flightsPerTask);
            OutputWizard out1 = new OutputWizard(threadNumber+2, newEndID, toID, flightsPerTask);
            try {
                exe.execute(out0);
                exe.execute(out1);
            } catch (RejectedExecutionException e) {
                throw new ThreadOverheadError();
            }
        }
    }

    /**
     * @return name of the running threa
     */
    public String getName () {
        return this.threadName;
    }

}

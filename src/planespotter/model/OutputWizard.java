package planespotter.model;

import planespotter.controller.IOMaster;
import planespotter.dataclasses.Flight;
import planespotter.throwables.ThreadOverheadError;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static planespotter.constants.GUIConstants.*;

public class OutputWizard extends DBOut implements Runnable {
    /**
     * class variables
     */
    private final ThreadPoolExecutor executor;
    private final int threadNumber, from, to, flightsPerTask;
    private final String threadName;

    /**
     * constructor
     */
    public OutputWizard(ThreadPoolExecutor executor, int tNumber, int from, int to, int flightsPerTask) {
        this.executor = executor;
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
            IOMaster.addToQueue(flights);
        } else {
            int newEndID = to-(flightsToLoad/2);
            OutputWizard out0 = new OutputWizard(executor, threadNumber+1, fromID, newEndID, flightsPerTask);
            OutputWizard out1 = new OutputWizard(executor, threadNumber+2, newEndID, toID, flightsPerTask);
            try {
                executor.execute(out0);
                executor.execute(out1);
            } catch (RejectedExecutionException e) {
                throw new ThreadOverheadError();
            }
        }
    }

    /**
     * @return name of the running thread
     */
    public String getName () {
        return this.threadName;
    }

}

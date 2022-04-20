package planespotter.model;

import planespotter.controller.Controller;
import planespotter.dataclasses.Flight;

import java.util.Collection;
import java.util.List;

import static planespotter.constants.GUIConstants.*;

public class ThreadedOutputWizard extends DBOut implements Runnable {
    /**
     * class variables
     */
    private int threadNumber, from, to;
    private String threadName;

    /**
     * constructor
     */
    public ThreadedOutputWizard (int tNumber, int from, int to) {
        this.threadNumber = tNumber;
        this.threadName = "output-wizard" + this.threadNumber;
        this.from = from;
        this.to = to-1;
    }

    /**
     * ? ? ? ? TODO richtig machen
     */
    @Override
    public void run () {
        long start = System.nanoTime();
        Thread.currentThread().setPriority(9);
        Thread.currentThread().setName(this.threadName);
        System.out.println(EKlAuf + "OutputWizard" + EKlZu + " thread " + ANSI_ORANGE + this.getName() + ANSI_RESET + " created!");
        loadFlights(from, to);
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
        List<Flight> flights = super.getAllFlightsFromID(fromID, toID);
            Controller.listQueue.add(flights);
            Controller.ready += 10;
    }

    /**
     * @return name of the running threa
     */
    public String getName () {
        return threadName;
    }

}

package planespotter.model;

public class ThreadedOutputWizard extends DBOut implements Runnable {

    /**
     * class varisbles
     */
    private int threadNumber;
    private String threadName;

    /**
     * constructor
     */
    public ThreadedOutputWizard (int number) {
        this.threadNumber = number;
        this.threadName = "db-out" + this.threadNumber;
    }

    /**
     * ? ? ? ? TODO richtig machen
     */
    @Override
    public void run () {
        Thread.currentThread().setPriority(8);
        System.out.println("[DBOut] thread " + this.getName() + " created!");
    }

    /**
     * @return name of the running threa
     */
    public String getName () {
        return threadName;
    }

}

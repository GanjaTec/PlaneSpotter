package planespotter.model;

public class ThreadedOutputWizard extends DBOut implements Runnable {
    /**
     * class variables
     */
    private int threadNumber;
    private String threadName;

    /**
     * constructor
     */
    public ThreadedOutputWizard (int number) {
        this.threadNumber = number;
        this.threadName = "output-wizard" + this.threadNumber;
    }

    /**
     * ? ? ? ? TODO richtig machen
     */
    @Override
    public void run () {
        Thread.currentThread().setPriority(9);
        Thread.currentThread().setName(this.threadName);
        System.out.println("[OutputWizard] thread " + this.getName() + " created!");
    }

    /**
     * @return name of the running threa
     */
    public String getName () {
        return threadName;
    }

}

package planespotter.model.nio;

@FunctionalInterface
public interface Supplier extends Runnable {

    void supply();

    default void run() {
        this.supply();
    }

}

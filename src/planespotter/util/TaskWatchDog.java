package planespotter.util;

public class TaskWatchDog {

    // counters
    private long allocations, comparisons, arrays;

    public TaskWatchDog() {
        this.allocations = 0;
        this.comparisons = 0;
        this.arrays = 0;
    }

    public final TaskWatchDog allocation(final int count) {
        for (int i = 0; i < count; i++) {
            this.allocations++;
        }
        return this;
    }

    public final TaskWatchDog allocation() {
        return this.allocation(1);
    }

    public final TaskWatchDog comparison(final int count) {
        for (int i = 0; i < count; i++) {
            this.comparisons++;
        }
        return this;
    }

    public final TaskWatchDog comparison() {
        return this.comparison(1);
    }

    public final TaskWatchDog array() {
        this.arrays++;
        return this;
    }

    public void print() {
        var str = "Allocations: " + this.allocations +
                  "\nComparisons: " + this.comparisons +
                  "\nArrays: " + this.arrays;
        System.out.println(str);
    }
}

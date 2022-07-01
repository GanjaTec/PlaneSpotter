package planespotter.util;

@SuppressWarnings(value = "unused")
public class TaskWatch {

    // counters
    private long allocations, comparisons, arrays;

    public TaskWatch() {
        this.allocations = 0;
        this.comparisons = 0;
        this.arrays = 0;
    }

    public final TaskWatch allocation(final int count) {
        for (int i = 0; i < count; i++) {
            this.allocations++;
        }
        return this;
    }

    public final TaskWatch allocation() {
        return this.allocation(1);
    }

    public final TaskWatch comparison(final int count) {
        for (int i = 0; i < count; i++) {
            this.comparisons++;
        }
        return this;
    }

    public final TaskWatch comparison() {
        return this.comparison(1);
    }

    public final TaskWatch array() {
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

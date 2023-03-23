package planespotter.util.combine;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Combiner<T> {

    private T[] elements;

    private final AtomicReference<T> result;

    @SafeVarargs
    protected Combiner(T... initElements) {
        this.elements = initElements;
        this.result = new AtomicReference<>();
    }

    @NotNull
    public abstract Combiner<T> combine();

    @SafeVarargs
    public final Combiner<T> addReCalc(@NotNull T... bmps) {
        return addNoCalc(bmps).combine();
    }

    @SafeVarargs
    public final Combiner<T> addNoCalc(@NotNull T... bmps) {
        int startLen = elements();
        elements = Arrays.copyOf(elements, startLen + bmps.length);
        int newLen = elements();
        for (int i = startLen, j = 0; i < newLen; i++) {
            elements[i] = bmps[j];
        }
        return this;
    }

    public final int elements() {
        return elements.length;
    }

    public final T[] getElements() {
        return elements;
    }

    public final void setResult(T result) {
        this.result.set(result);
    }

    public final T getResult() {
        return result.get();
    }


}

package planespotter.model.simulation;

import de.gtec.util.threading.ConcurrentCollections;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Simulation<T> {

    private int currentIndex;
    private List<T> frames;

    protected Simulation() {
        this.currentIndex = 0;
    }

    protected Simulation(@NotNull List<T> frames) {
        this.currentIndex = 0;
        setFrames(frames);
    }

    protected abstract boolean processFrame();

    public final int getCurrentIndex() {
        return currentIndex;
    }

    public final int incrementAndGetIndex() {
        return ++currentIndex;
    }

    public final int getAndIncrementIndex() {
        return currentIndex++;
    }

    public final List<T> getFrames() {
        return frames;
    }

    public final void setFrames(@NotNull List<T> frames) {
        this.frames = ConcurrentCollections.list(frames);
    }

    public final int getFrameCount() {
        return frames.size();
    }
}

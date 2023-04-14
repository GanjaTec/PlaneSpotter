package planespotter.model.simulation;

import org.jetbrains.annotations.NotNull;
import planespotter.util.Bitmap;

import java.util.List;

public class BitmapSimulation extends Simulation<Bitmap> {

    protected BitmapSimulation(@NotNull List<Bitmap> frames) {
        super(frames);
    }

    @Override
    protected boolean processFrame() {
        return false;
    }

}

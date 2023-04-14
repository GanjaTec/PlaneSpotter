package planespotter.util.combine;

import org.jetbrains.annotations.NotNull;
import planespotter.util.Bitmap;

public class BitmapBinCombiner extends BitmapCombiner {

    public BitmapBinCombiner(Bitmap... initElements) {
        super(initElements);
    }

    @Override
    @NotNull
    public Combiner<Bitmap> combine() {
        setResult(combineBmp(elements(), MODE_BIN));
        return this;
    }
}

package planespotter.util.combine;

import org.jetbrains.annotations.NotNull;
import planespotter.util.Bitmap;

public class BitmapMeanCombiner extends BitmapCombiner {

    public BitmapMeanCombiner(Bitmap... initElements) {
        super(initElements);
    }

    @Override
    @NotNull
    public BitmapMeanCombiner combine() {
        setResult(combineBmp(elements(), BitmapCombiner.MODE_MEAN));
        return this;
    }

}

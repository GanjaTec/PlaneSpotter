package planespotter.util.combine;

import org.jetbrains.annotations.NotNull;
import planespotter.util.Bitmap;

public class BitmapMaxCombiner extends BitmapCombiner {

    public BitmapMaxCombiner(Bitmap... initElements) {
        super(initElements);
    }

    @Override
    @NotNull
    public BitmapMaxCombiner combine() {
        setResult(combineBmp(elements(), BitmapCombiner.MODE_MAX));
        return this;
    }

}

package planespotter.model;

import org.jetbrains.annotations.Nullable;
import planespotter.model.io.DBOut;
import planespotter.model.nio.client.http.FrameSender;

public class DataOutputManager {

    private static DataOutputManager INSTANCE;

    private static final FrameSender frameSender = new FrameSender();

    private DataOutput currentOut;

    private DataOutputManager(int mask) {
        setDataMask(mask);
    }

    public static void initialize(int dataMask) {
        INSTANCE = new DataOutputManager(dataMask);
    }

    public static DataOutputManager getOutputManager() {
        return INSTANCE;
    }

    public void setDataMask(int mask) {
        if (DataMask.isUploadMask(mask)) {
            currentOut = frameSender;

        } else if (DataMask.isLocalWriteMask(mask)) {
            currentOut = DBOut.getDBOut();

        }
    }

    @Nullable
    public DataOutput getCurrentOut() {
        return currentOut;
    }


}

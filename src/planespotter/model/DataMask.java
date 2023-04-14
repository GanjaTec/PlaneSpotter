package planespotter.model;

import planespotter.model.nio.DataProcessor;

public class DataMask {

    public static boolean isLocalWriteMask(int mask) {
        return (mask & DataProcessor.LOCAL_WRITE_MASK) == DataProcessor.LOCAL_WRITE_MASK;
    }

    public static boolean isUploadMask(int mask) {
        return (mask & DataProcessor.UPLOAD_MASK) == DataProcessor.UPLOAD_MASK;
    }

}

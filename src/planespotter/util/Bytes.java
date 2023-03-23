package planespotter.util;

import planespotter.throwables.OutOfRangeException;

public class Bytes {

    public static int toUnsignedInt(byte b) {
        return b > 0 ? b | 0x80 : b + 128;
    }

    public static byte fromUnsignedInt(int i) {
        if (i > 256) {
            throw new OutOfRangeException("Out of unsigned byte range");
        }
        return (byte) (i - 128);
    }
}

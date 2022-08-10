package planespotter.dataclasses;

import planespotter.throwables.InvalidDataException;

public class Hotkey {

    public static boolean isValid(char key) {
        return key != ' ' && Character.isAlphabetic(key);
    }

    private final char key;

    private final boolean shiftDown, ctrlDown;

    public Hotkey(char key, boolean shiftDown, boolean ctrlDown) {
        if (!isValid(key)) {
            throw new InvalidDataException("No blank key allowed");
        }
        this.key = key;
        this.shiftDown = shiftDown;
        this.ctrlDown = ctrlDown;
    }

    public char getKey() {
        return key;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }

    public boolean isCtrlDown() {
        return ctrlDown;
    }
}

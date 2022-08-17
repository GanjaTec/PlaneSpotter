package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;
import planespotter.throwables.InvalidDataException;

public class Hotkey {

    public static boolean equals(@NotNull Hotkey a, @NotNull Hotkey b) {
        if (a == b) {
            return true;
        }
        return     a.keyCode    == b.keyCode
                && a.shiftDown  == b.shiftDown
                && a.ctrlDown   == b.ctrlDown
                && a.altDown    == b.altDown;
    }

    private final int keyCode;

    private final boolean shiftDown, ctrlDown, altDown;

    public Hotkey(int keyCode, boolean shiftDown, boolean ctrlDown, boolean altDown) {
        this.keyCode = keyCode;
        this.shiftDown = shiftDown;
        this.ctrlDown = ctrlDown;
        this.altDown = altDown;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }

    public boolean isCtrlDown() {
        return ctrlDown;
    }

    public boolean isAltDown() {
        return altDown;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Hotkey hotkey && equals(this, hotkey);
    }

}

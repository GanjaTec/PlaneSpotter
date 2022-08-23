package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;

/**
 * @name Hotkey
 * @author jml04
 * @version 1.0
 *
 * @description
 * The {@link Hotkey} class represents a hotkey with a key code (int),
 * that stands for the key and flags for 'shift down', 'ctrl down' and 'alt down'.
 * The hotkeys are used as global hotkeys on the global event dispatcher
 * @see planespotter.controller.ActionHandler
 * @see java.awt.KeyEventDispatcher
 */
public class Hotkey {

    // the key code, represents a key
    private final int keyCode;

    // flags for 'shift down', 'ctrl down' and 'alt down'
    private final boolean shiftDown, ctrlDown, altDown;

    /**
     * the {@link Hotkey} constructor,
     * creates a new {@link Hotkey} object
     *
     * @param keyCode is the key code int from the event
     * @param shiftDown is the 'shift down' flag from the event
     * @param ctrlDown is the 'ctrl down' flag from the event
     * @param altDown is the 'alt down' flag from the event
     */
    public Hotkey(int keyCode, boolean shiftDown, boolean ctrlDown, boolean altDown) {
        this.keyCode = keyCode;
        this.shiftDown = shiftDown;
        this.ctrlDown = ctrlDown;
        this.altDown = altDown;
    }

    /**
     * static method equals compares two hotkeys,
     * compares with '==' first, then checks the values,
     * so it does not just check for the same object,
     * but also for the same key combination
     *
     * @param a is the first hotkey to compare with b
     * @param b is the second hotkey to compare with a
     * @return true if {@link Hotkey} a is equals b
     */
    public static boolean equals(@NotNull Hotkey a, @NotNull Hotkey b) {
        if (a == b) {
            return true;
        }
        return     a.keyCode    == b.keyCode
                && a.shiftDown  == b.shiftDown
                && a.ctrlDown   == b.ctrlDown
                && a.altDown    == b.altDown;
    }

    /**
     * getter for the key code
     *
     * @return the key code as int
     */
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * getter for the 'shift down' flag
     *
     * @return the 'shift down' flag
     */
    public boolean isShiftDown() {
        return shiftDown;
    }

    /**
     * getter for the 'ctrl down' flag
     *
     * @return the 'ctrl down' flag
     */
    public boolean isCtrlDown() {
        return ctrlDown;
    }

    /**
     * getter for the 'alt down' flag
     *
     * @return the 'alt down' flag
     */
    public boolean isAltDown() {
        return altDown;
    }

    /**
     * compares this {@link Hotkey} with another,
     * checking for type and using static Hotkey.equals method
     *
     * @param o is the {@link Hotkey} to compare, must be {@link Hotkey} type
     * @return true if the given {@link Hotkey} is equals this hotkey
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Hotkey hotkey && equals(this, hotkey);
    }

}

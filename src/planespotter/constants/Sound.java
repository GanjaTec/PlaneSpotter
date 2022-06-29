package planespotter.constants;

/**
 * @name Sound
 * @author jml04
 * @version 1.0
 * @description
 * enum Sound contains all Windows-Sounds with key strings
 */
public enum Sound {
    SOUND_ASTERISK("win.sound.asterisk"),
    SOUND_CLOSE("win.sound.close"),
    SOUND_DEFAULT("win.sound.default"),
    SOUND_EXCLAMATION("win.sound.exclamation"),
    SOUND_EXIT("win.sound.exit"),
    SOUND_HAND("win.sound.hand"),
    SOUND_MAXIMIZE("win.sound.maximize"),
    SOUND_MENU_COMMAND("win.sound.menuCommand"),
    SOUND_MENU_POPUP("win.sound.menuPopup"),
    SOUND_MINIMIZE("win.sound.minimize"),
    SOUND_OPEN("win.sound.open"),
    SOUND_QUESTION("win.sound.question"),
    SOUND_RESTORE_UP("win.sound.restoreDown"),
    SOUND_RESTOPRE_DOWN("win.sound.restoreUp"),
    SOUND_START("win.sound.src");
    // key string instance field
    private final String sound;
    // private enum constructor
    Sound(String sound) {
        this.sound = sound;
    }
    // key string getter
    public final String get() {
        return this.sound;
    }
}

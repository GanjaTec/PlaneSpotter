package planespotter.model;

import java.awt.*;

/**
 * @name Utilities
 * @author @all
 * @version 1.0
 *
 * class Utilities contains different utilities
 */
public class Utilities {

    /**
     * plays a sound from the default toolkit
     * @param sound is the sound to be played (see: GUIConstants)
     */
    public static void playSound(String sound) {
        var sound2 = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty(sound);
        if (sound2 != null) sound2.run();
    }

    /**
     *
     */
    public static int feetToMeters (int feet) {
        return (int) (feet/3.2808);
    }

}

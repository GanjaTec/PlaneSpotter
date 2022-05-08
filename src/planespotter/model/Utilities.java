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
        if (sound2 != null) {
            sound2.run();
        }
    }

    /**
     * @param feet is the input, in feet (ft)
     * @return a feet value in meters
     */
    public static int feetToMeters (int feet) {
        return (int) (feet/3.2808);
    }

    /**
     * @param kn is the input, in knots (kn)
     * @return the knots in km per hour
     */
    public static int knToKmh (int kn) {
        long kmh = Math.round(kn * 1.852);
        return (int) kmh;
    }

    /**
     * packs a string in the format '...'
     *
     * @param input is the string to pack
     * @return packed input string with 's
     */
    public static String packString (String input) {
        return "'" + input + "'";
    }

}

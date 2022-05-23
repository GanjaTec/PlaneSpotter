package planespotter.model;

import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Position;

import java.awt.*;
import java.util.Vector;

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
    public static boolean playSound (String sound) {
        if (sound == null) {
            throw new IllegalArgumentException("no sound to play, input may not be null!");
        }
        var sound2 = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty(sound);
        if (sound2 != null) {
            sound2.run();
            return true;
        }
        return false;
    }

    /**
     * @param feet is the input, in feet (ft)
     * @return a feet value in meters
     */
    public static int feetToMeters (int feet) {
        if (feet < 0) {
            throw new IllegalArgumentException("number must be higher or equals 0!");
        }
        return (int) (feet/3.2808);
    }

    /**
     * @param kn is the input, in knots (kn)
     * @return the knots in km per hour
     */
    public static int knToKmh (int kn) {
        if (kn < 0) {
            throw new IllegalArgumentException("number must be higher or equals 0!");
        }
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
        if (input == null) {
            throw new IllegalArgumentException("input cannot be null");
        }
        return "'" + input + "'";
    }

    /**
     * @param in is the string to strip
     * @return input-string, but without the "s
     */
    public static String stripString (String in) {
        if (in == null) {
            throw new IllegalArgumentException("input cannot be null!");
        }
        return in.replaceAll("\"", "");
    }

    public static Vector<Position> parsePositionVector (Vector<DataPoint> dps) {
        if (dps == null || dps.isEmpty()) {
            throw new IllegalArgumentException("input cannot be null / empty!");
        }
        var positions = new Vector<Position>();
        dps.forEach(dp -> positions.add(dp.pos()));
        if (positions.isEmpty()) {
            throw new NullPointerException("data point list is empty!");
        }
        return positions;
    }



}

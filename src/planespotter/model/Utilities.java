package planespotter.model;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Position;
import planespotter.throwables.TimeoutException;

import java.awt.*;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

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
    public void playSound(String sound) {
        if (sound == null) throw new IllegalArgumentException("no sound to play, input may not be null!");
        var sound2 = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty(sound);
        if (sound2 != null) {
            sound2.run();
        }
    }

    /**
     * @param feet is the input, in feet (ft)
     * @return a feet value in meters
     */
    public int feetToMeters (int feet) {
        return (int) (feet/3.2808);
    }

    /**
     * @param kn is the input, in knots (kn)
     * @return the knots in km per hour
     */
    public int knToKmh (int kn) {
        long kmh = Math.round(kn * 1.852);
        return (int) kmh;
    }

    /**
     * packs a string in the format '...'
     *
     * @param input is the string to pack
     * @return packed input string with 's
     */
    public String packString (String input) {
        if (input == null) {
            throw new IllegalArgumentException("input cannot be null");
        }
        return "'" + input + "'";
    }

    /**
     * @param in is the string to strip
     * @return input-string, but without the "s
     */
    public String stripString (String in) {
        if (in == null) {
            throw new IllegalArgumentException("input cannot be null");
        }
        return in.replaceAll("\"", "");
    }

    public Vector<Position> parsePositionVector (Vector<DataPoint> dps) {
        var positions = new Vector<Position>();
        dps.forEach(dp -> positions.add(dp.getPos()));
        if (positions.isEmpty()) {
            throw new NullPointerException("data point list is empty!");
        }
        return positions;
    }

    public void timeoutTask (final int time, @NotNull final TimeUnit timeUnit)
        throws TimeoutException {
        try {
            timeUnit.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new TimeoutException(time);
    }


}

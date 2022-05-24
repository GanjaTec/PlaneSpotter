package test;

import org.junit.jupiter.api.Test;
import planespotter.constants.GUIConstants;
import planespotter.util.Utilities;

import static org.junit.jupiter.api.Assertions.*;

public class UtilitiesTest {

    @Test
    public void soundTest() {
        boolean soundPlayed = Utilities.playSound(GUIConstants.Sound.SOUND_DEFAULT.get());
        assertTrue(soundPlayed, "Utilities-Sound tested sucsessfully!");
    }

    @Test
    public void feetToMetersTest() {
        int feet = 6500;
        double meters = Math.round(feet/3.2808);
        assertEquals(Utilities.feetToMeters(feet), meters, "Utilities-FeetToMeters tested sucsessfully!");
    }

    @Test
    public void knToKmhTest() {
        int kn = 120;
        double kmh = Math.round(kn * 1.852);
        assertEquals(Utilities.knToKmh(kn), kmh, "Utilities-KnToKmh tested sucsessfully!");
    }

    // stripString, packString, parsePositionVector, timeoutTask

}

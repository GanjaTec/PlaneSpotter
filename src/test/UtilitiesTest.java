package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import planespotter.constants.Sound;
import planespotter.throwables.ExtensionException;
import planespotter.throwables.IllegalInputException;
import planespotter.throwables.StatusException;
import planespotter.util.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class UtilitiesTest {

    @Test
    public void playSound() {
        final boolean soundPlayed = Utilities.playSound(Sound.SOUND_DEFAULT.get());

        assertTrue(soundPlayed, "Utilities-Sound tested successfully!");
    }

    @Test
    public void feetToMeters() {
        final int feet = 6500;
        final double meters = Math.round(feet/3.2808);

        assertEquals(Utilities.feetToMeters(feet), meters, "Utilities-FeetToMeters tested successfully!");
    }

    @Test
    public void knToKmh() {
        final int kn = 120;
        final double kmh = Math.round(kn * 1.852);

        assertEquals(Utilities.knToKmh(kn), kmh, "Utilities-KnToKmh tested successfully!");
    }

    @Test
    public void stripStringT() {
        final String expected = "testText";

        assertEquals(expected, Utilities.stripString("\"testText\""), "Utilities-stripString tested successfully!");
    }

    @Test
    void getAbsoluteRootPath() {
        final String expected = System.getProperty("user.dir");
        final String actual = Utilities.getAbsoluteRootPath();

        assertEquals(expected, actual, "Utilities-getAbsoluteRootPath tested successfully!");
    }

    @Test
    void rotate() {

        // check for null and exceptions
        // check for right background color
        // rotate back and check if it is the same as the start img
        // check size

    }

    @Test
    void maxValue() {
        final int[][] testNumbers2D = { {2, 3}, {5, 333}, {112, 1000}, {11, 344} };
        final int expected = 1000;
        final int actual = Utilities.maxValue(testNumbers2D);

        assertEquals(expected, actual, "Utilities-maxValue tested successfully!");
    }

    @Test
    void decToHex() {
    }

    @Test
    void hexStrToInt() {
    }

    @Test
    void toByteLevel() {
    }

    @Test
    void packString() {
    }

    @Test
    void checkString() {
        final Executable legal = () -> Utilities.checkString("FORTE");
        final Executable illegal = () -> Utilities.checkString("SELECT * FROM flights; --");

        assertDoesNotThrow(legal);
        assertThrows(IllegalInputException.class, illegal);
    }

    @Test
    void checkInputs() {

    }

    @Test
    void asInt() {
        AtomicInteger var1 = new AtomicInteger(),
                      var2 = new AtomicInteger();
        Executable legal1 = () -> var1.set(Utilities.asInt(3));
        Executable legal2 = () -> var2.set(Utilities.asInt(3.6));
        Executable illegal = () -> Utilities.asInt(Long.MAX_VALUE);

        assertDoesNotThrow(legal1);
        assertDoesNotThrow(legal2);

        assertEquals(var1.get(), 3);
        assertEquals(var2.get(), 3);

        assertThrows(NumberFormatException.class, illegal);
    }

    @Test
    void parsePositionVector() {
    }

    @Test
    void parseDataPointVector() {
    }

    @Test
    void parseDeque() {
        List<Integer> intList = List.of(1, 8, 3, 6, 4);
        int[] intArr = {1, 8, 3, 6, 4};
        Deque<Integer> deque = new ArrayDeque<>(intList);
        Deque<?> byList = Utilities.parseDeque(intList);
        Deque<?> byArr = Utilities.parseDeque(intArr);

        Object[] expected = deque.toArray();
        Object[] bla = byList.toArray();
        Object[] baa = byArr.toArray();

        assertArrayEquals(Arrays.stream(intArr).boxed().toArray(), bla);
        assertArrayEquals(expected, bla);
        assertArrayEquals(expected, baa);

    }

    @Test
    void parseDataPointArray() {
    }

    @Test
    void parseIntArray() {
    }

    @Test
    void fitArea() {
    }

    @Test
    void colorByAltitude() {
    }

    @Test
    void colorByLevel() {
    }

    @Test
    void scaledImage() {
    }

    @Test
    void checkStatusCode() {
        final int legal = 200;
        final int[] illegals = {1, 201, 403};

        assertDoesNotThrow(() -> Utilities.checkStatusCode(legal));
        for (int sCode : illegals) {
            assertThrows(StatusException.class, () -> Utilities.checkStatusCode(sCode));
        }
    }

    @Test
    void checkFile() {
        File nonExistingFile = new File("abc"),
             noNameFile = new File(""),
             existingFile = new File("testFile.txt");
        try {
            assert existingFile.createNewFile();
        } catch (IOException | AssertionError e) {
            throw new Error("Couldn't create new File!", e);
        }
        assertThrows(FileNotFoundException.class, () -> Utilities.checkFile(null, ".abc"));
        assertThrows(FileNotFoundException.class, () -> Utilities.checkFile(nonExistingFile, ".abc"));
        assertThrows(FileNotFoundException.class, () -> Utilities.checkFile(noNameFile, ".abc"));

        assertThrows(ExtensionException.class, () -> Utilities.checkFile(existingFile, "abc"));
        assertThrows(ExtensionException.class, () -> Utilities.checkFile(existingFile, ".abc"));

        assertDoesNotThrow(() -> Utilities.checkFile(existingFile, ".txt"));

        assert !existingFile.exists() || (existingFile.exists() && existingFile.delete()) : "Couldn't delete file!";

    }

    @Test
    void linesCode() {
    }


}

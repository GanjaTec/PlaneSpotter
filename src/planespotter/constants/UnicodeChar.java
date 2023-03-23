package planespotter.constants;

/**
 * @name UnicodeChar
 * @author jml04
 * @version 1.0
 *
 * @description
 * The {@link UnicodeChar} enum contains different Unicode characters which can be used in Strings.
 * UnicodeChars start with a backslash '\'
 */
public enum UnicodeChar {
    INFINITY('\u221E');

    // character field
    private final char character;

    /**
     * constructs a {@link UnicodeChar} object with the given char
     *
     * @param uniChar is the unicode char that starts with '\'
     */
    UnicodeChar(char uniChar) {
        this.character = uniChar;
    }

    /**
     * getter for the unicode char
     *
     * @return the char field where the unicode char is saved in
     */
    public char get() {
        return this.character;
    }
}

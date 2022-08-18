package planespotter.constants;

public enum UnicodeChar {

    INFINITY('\u221E');

    private final char character;

    UnicodeChar(char uniChar) {
        this.character = uniChar;
    }

    public char get() {
        return this.character;
    }
}

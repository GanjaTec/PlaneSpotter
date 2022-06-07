package planespotter.constants;

/**
 * ANSI-colors for System.out.println
 */
public enum ANSIColor {
    GREEN("\u001B[92m"),
    ORANGE("\u001B[33m"),
    RESET("\u001B[0m"),
    RED("\u001B[31m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    YELLOW("\u001B[33m");

    private final String colorCode;

    ANSIColor(final String colorCode) {
        this.colorCode = colorCode;
    }

    public final String get() {
        return this.colorCode;
    }

}

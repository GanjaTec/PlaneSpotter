package planespotter.constants;

/**
 * @name ANSIColor
 * @author jml04
 * @version 1.0
 *
 * ANSI-colors for System.out.println
 * note: these colors don't work on every console
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
    // color code field
    private final String colorCode;
    // private enum constructor
    ANSIColor(final String colorCode) {
        this.colorCode = colorCode;
    }
    // ANSI color getter
    public final String get() {
        return this.colorCode;
    }

}

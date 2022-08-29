package planespotter.constants;

/**
 * @name ANSIColor
 * @author jml04
 * @version 1.0
 *
 * @description
 * ANSI-colors for System.out.println
 * note: these colors don't work on every console and might create artifacts on those where it doesnt work, its heavily dependent on the IDE or Shell you use
 * tested on IntelliJ (Working) and Eclipse (not working)
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

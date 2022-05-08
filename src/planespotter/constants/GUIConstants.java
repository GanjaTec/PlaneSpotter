package planespotter.constants;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * @name GUIConstants
 * @author jml04
 * @version 1.0
 *
 * class GUIConstants contains all constants needed in the display package
 */
public final class GUIConstants {

    /**
     * default colors
     */
    public static final Color   DEFAULT_BG_COLOR = Color.GRAY,
                                DEFAULT_FG_COLOR = Color.BLACK,
                                DEFAULT_BORDER_COLOR = Color.LIGHT_GRAY,
                                DEFAULT_FONT_COLOR = new Color(230, 230,230),
                                DEFAULT_ACCENT_COLOR = new Color(85, 100, 210),
                                DEFAULT_SEARCH_ACCENT_COLOR = new Color(61, 76, 114),
                                DEFAULT_MAP_ICON_COLOR = new Color(255, 214, 51);

    /**
     * default fonts
     */
    public static final Font    TITLE_FONT = new Font("Copperplate Gothic", Font.BOLD, 40),
                                FONT_MENU = new Font("DialogInput", Font.BOLD, 16);

    /**
     * images
     */
    public static final ImageIcon   BGROUND_IMG = new ImageIcon(Paths.IMG_PATH + "background.png"),
                                    MENU_BGROUND_IMG = new ImageIcon(Paths.IMG_PATH + "menu_background.png"),
                                    TITLE_BGROUND_IMG = new ImageIcon(Paths.IMG_PATH + "title_background.jpg"),
                                    FLYING_PLANE_ICON = new ImageIcon(Paths.IMG_PATH + "flying_plane_icon.png");

    /**
     * component borders
     */
    public static final Border  LINE_BORDER = BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR, 1),
                                MENU_BORDER = BorderFactory.createLineBorder(DEFAULT_BG_COLOR);

    /**
     * ANSI-colors for System.out.println
     */
    public static final String  ANSI_GREEN = "\u001B[92m",
                                ANSI_ORANGE = "\u001B[33m",
                                ANSI_RESET = "\u001B[0m",
                                ANSI_RED = "\u001B[31m",
                                ANSI_BLUE = "\u001B[34m",
                                ANSI_PURPLE = "\u001B[35m",
                                ANSI_CYAN = "\u001B[36m",
                                ANSI_YELLOW = "\u001B[33m";
    // prepared strings which are used often
    public static final String  EKlAuf = ANSI_ORANGE + "[" + ANSI_RESET,
                                EKlZu = ANSI_ORANGE + "]" + ANSI_RESET;

    /**
     * windows sounds
     */
    public static String    SOUND_ASTERISK = "win.sound.asterisk",
                            SOUND_CLOSE = "win.sound.close",
                            SOUND_DEFAULT = "win.sound.default",
                            SOUND_EXCLAMATION = "win.sound.exclamation",
                            SOUND_EXIT = "win.sound.exit",
                            SOUND_HAND = "win.sound.hand",
                            SOUND_MAXIMIZE = "win.sound.maximize",
                            SOUND_MENU_COMMAND = "win.sound.menuCommand",
                            SOUND_MENU_POPUP = "win.sound.menuPopup",
                            SOUND_MINIMIZE = "win.sound.minimize",
                            SOUND_OPEN = "win.sound.open",
                            SOUND_QUESTION = "win.sound.question",
                            SOUND_RESTORE_UP = "win.sound.restoreDown",
                            SOUND_RESTOPRE_DOWN = "win.sound.restoreUp",
                            SOUND_START = "win.sound.start";

    /**
     * String constalts
     */
    public static final String DEFAULT_HEAD_TEXT = "PlaneSpotter > ";

}

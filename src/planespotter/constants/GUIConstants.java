package planespotter.constants;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

import static planespotter.constants.GUIConstants.DefaultColor.DEFAULT_BG_COLOR;
import static planespotter.constants.GUIConstants.DefaultColor.DEFAULT_BORDER_COLOR;

/**
 * @name GUIConstants
 * @author jml04
 * @version 1.0
 *
 * class GUIConstants contains all constants needed in the display package
 */
public final class GUIConstants {

    /**
     * default view head text
     */
    public static final String DEFAULT_HEAD_TEXT = "PlaneSpotter > ";

    /**
     * default fonts
     */
    public static final Font    TITLE_FONT = new Font("Copperplate Gothic", Font.BOLD, 40),
                                FONT_MENU = new Font("DialogInput", Font.BOLD, 16);

    /**
     * default colors
     */
    public enum DefaultColor {
        DEFAULT_BG_COLOR(Color.GRAY),
        DEFAULT_FG_COLOR(Color.BLACK),
        DEFAULT_BORDER_COLOR(Color.LIGHT_GRAY),
        DEFAULT_FONT_COLOR(new Color(230, 230,230)),
        DEFAULT_ACCENT_COLOR(new Color(44, 119, 154)),
        DEFAULT_SEARCH_ACCENT_COLOR(new Color(61, 76, 114)),
        DEFAULT_MAP_ICON_COLOR(new Color(255, 214, 51));

        private final Color color;

        DefaultColor (Color color) {
            this.color = color;
        }

        public Color get() {
            return this.color;
        }
    }

    /**
     * images
     */
    public enum Images {
        TITLE(new ImageIcon(Paths.RESSOURCE_PATH + "newTitle.png")),
        BGROUND_IMG(new ImageIcon(Paths.RESSOURCE_PATH + "background.png")),
        MENU_BGROUND_IMG(new ImageIcon(Paths.RESSOURCE_PATH + "menu_background.png")),
        TITLE_BGROUND_IMG(new ImageIcon(Paths.RESSOURCE_PATH + "title_background.jpg")),
        PAPER_PLANE_ICON(new ImageIcon(Paths.RESSOURCE_PATH + "planespotter_icon.png")),
        FLYING_PLANE_ICON(new ImageIcon(Paths.RESSOURCE_PATH + "flying_plane_icon.png"));

        private final ImageIcon img;

        Images (ImageIcon img) {
            this.img = img;
        }

        public ImageIcon get() {
            return this.img;
        }
    }

    /**
     * component borders
     */
    public static final Border  LINE_BORDER = BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR.get(), 1),
                                MENU_BORDER = BorderFactory.createLineBorder(DEFAULT_BG_COLOR.get());

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

        ANSIColor (String colorCode) {
            this.colorCode = colorCode;
        }

        public final String get() {
            return this.colorCode;
        }

    }

    /**
     * windows sounds
     */
    public enum Sound {
        SOUND_ASTERISK("win.sound.asterisk"),
        SOUND_CLOSE("win.sound.close"),
        SOUND_DEFAULT("win.sound.default"),
        SOUND_EXCLAMATION("win.sound.exclamation"),
        SOUND_EXIT("win.sound.exit"),
        SOUND_HAND ("win.sound.hand"),
        SOUND_MAXIMIZE("win.sound.maximize"),
        SOUND_MENU_COMMAND("win.sound.menuCommand"),
        SOUND_MENU_POPUP("win.sound.menuPopup"),
        SOUND_MINIMIZE("win.sound.minimize"),
        SOUND_OPEN("win.sound.open"),
        SOUND_QUESTION("win.sound.question"),
        SOUND_RESTORE_UP("win.sound.restoreDown"),
        SOUND_RESTOPRE_DOWN("win.sound.restoreUp"),
        SOUND_START("win.sound.src");

        private final String sound;

        Sound (String sound) {
            this.sound = sound;
        }

        public final String get() {
            return this.sound;
        }
    }


}

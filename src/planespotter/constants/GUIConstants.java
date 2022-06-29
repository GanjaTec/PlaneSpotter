package planespotter.constants;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

import static planespotter.constants.DefaultColor.DEFAULT_BG_COLOR;
import static planespotter.constants.DefaultColor.DEFAULT_BORDER_COLOR;

/**
 * @name GUIConstants
 * @author jml04
 * @version 1.0
 * @description
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
    public static final Font TITLE_FONT = new Font("Copperplate Gothic", Font.BOLD, 40),
                             FONT_MENU = new Font("DialogInput", Font.BOLD, 16);

    /**
     * component borders
     */
    public static final Border LINE_BORDER = BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR.get(), 1),
                               MENU_BORDER = BorderFactory.createLineBorder(DEFAULT_BG_COLOR.get());


}

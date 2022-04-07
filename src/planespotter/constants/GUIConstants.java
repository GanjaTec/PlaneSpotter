package planespotter.constants;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public final class GUIConstants {

    // default colors
    public static final Color   DEFAULT_BG_COLOR = Color.GRAY,
                                DEFAULT_FG_COLOR = Color.BLACK,
                                DEFAULT_BORDER_COLOR = Color.DARK_GRAY,
                                DEFAULT_FONT_COLOR = new Color(230, 230,230),
                                DEFAULT_ACCENT_COLOR = new Color(84, 101, 210),
                                DEFAULT_MAP_ICON_COLOR = new Color(255, 214, 51);

    // default fonts
    public static final Font    TITLE_FONT = new Font("Copperplate Gothic", Font.BOLD, 40),
                                FONT_MENU = new Font("DialogInput", Font.BOLD, 16);

    // icons / images
    public static final ImageIcon   img = new ImageIcon(Paths.SRC_PATH + "background.jpg"),
                                    title_bground_img = new ImageIcon(Paths.SRC_PATH + "title_background.jpg"),
                                    flying_plane_icon = new ImageIcon(Paths.SRC_PATH + "flying_plane_icon.png");
    // line border
    public static final Border  LINE_BORDER = BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR, 2),
                                MENU_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createLineBorder(DEFAULT_FG_COLOR));



}

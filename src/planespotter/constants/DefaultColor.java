package planespotter.constants;

import java.awt.*;

/**
 * default colors
 */
public enum DefaultColor {
    DEFAULT_BG_COLOR(Color.GRAY),
    DEFAULT_FG_COLOR(Color.BLACK),
    DEFAULT_BORDER_COLOR(Color.LIGHT_GRAY),
    DEFAULT_FONT_COLOR(new Color(230, 230, 230)),
    DEFAULT_ACCENT_COLOR(new Color(44, 110, 154)),
    DEFAULT_SEARCH_ACCENT_COLOR(new Color(61, 76, 114)),
    DEFAULT_MAP_ICON_COLOR(new Color(255, 214, 51));

    private final Color color;

    DefaultColor(final Color color) {
        this.color = color;
    }

    public Color get() {
        return this.color;
    }
}

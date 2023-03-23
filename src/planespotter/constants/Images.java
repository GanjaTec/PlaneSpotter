package planespotter.constants;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static planespotter.constants.Paths.IMAGES;

/**
 * @name Images
 * @author jml04
 * @version 1.0
 *
 * @description
 * enum Images contains all images that are used in the {@link planespotter.display.UserInterface}
 */
public enum Images {
    TITLE(new ImageIcon(IMAGES + "newTitle.png")),
    PAPER_PLANE_ICON(new ImageIcon(IMAGES + "planespotter_icon.png")),
    FLYING_PLANE_ICON(new ImageIcon(IMAGES + "flying_plane_icon.png")),
    START_SCREEN(new ImageIcon(IMAGES + "start_img.png")),
    LOADING_CYCLE_GIF(new ImageIcon(IMAGES + "loading2.gif")),
    OPEN_FILE_ICON_16x(new ImageIcon(IMAGES + "open_file_icon_16x.png")),
    SAVE_FILE_ICON_16x(new ImageIcon(IMAGES + "save_file_icon_16x.png")),
    PLANE_ICON_16x(new ImageIcon(IMAGES + "plane_icon_16x.png")),
    ANTENNA_ICON_16x(new ImageIcon(IMAGES + "antenna_icon_16x.png")),
    STATS_ICON_16x(new ImageIcon(IMAGES + "stats_icon_16x.png")),
    HEATMAP_ICON_16x(new ImageIcon(IMAGES + "heatmap_icon_16x.png")),
    MAP_ICON_16x(new ImageIcon(IMAGES + "map_icon_16x.png")),
    EXIT_ICON_16x(new ImageIcon(IMAGES + "exit_icon_16x.png")),
    AIRPLANE_ICON_8x(new ImageIcon(IMAGES + "airplane_icon_8x.png")),
    DEFAULT_AIRPLANE_ICON_16x(new ImageIcon(IMAGES + "airplane_icon_16x.png")),
    SELECTED_AIRPLANE_ICON_16x(new ImageIcon(IMAGES + "selected_airplane_icon_16x.png")),
    FULLSCREEN_ICON_16x(new ImageIcon(IMAGES + "fullscreen_icon_16x.png"));

    // image instance field
    private final ImageIcon img;

    // private enum constructor
    Images(final @NotNull ImageIcon img) {
        this.img = img;
    }

    // image getter
    @NotNull
    public ImageIcon get() {
        return this.img;
    }
}

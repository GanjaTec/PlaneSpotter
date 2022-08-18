package planespotter.constants;

import javax.swing.*;

import static planespotter.constants.Paths.IMAGE_PATH;

/**
 * @name Images
 * @author jml04
 * @version 1.0
 * @description
 * enum Images contains all images for the GUI
 */
public enum Images {
    TITLE(new ImageIcon(IMAGE_PATH + "newTitle.png")),
    BGROUND_IMG(new ImageIcon(IMAGE_PATH + "background.png")),
    MENU_BGROUND_IMG(new ImageIcon(IMAGE_PATH + "menu_background.png")),
    TITLE_BGROUND_IMG(new ImageIcon(IMAGE_PATH + "title_background.jpg")),
    PAPER_PLANE_ICON(new ImageIcon(IMAGE_PATH + "planespotter_icon.png")),
    FLYING_PLANE_ICON(new ImageIcon(IMAGE_PATH + "flying_plane_icon.png")),
    LOADING_CYCLE_GIF(new ImageIcon(IMAGE_PATH + "loadingCycle.gif")),
    OPEN_FILE_ICON_16x(new ImageIcon(IMAGE_PATH + "open_file_icon_16x.png")),
    SAVE_FILE_ICON_16x(new ImageIcon(IMAGE_PATH + "save_file_icon_16x.png")),
    PLANE_ICON_16x(new ImageIcon(IMAGE_PATH + "plane_icon_16x.png")),
    ANTENNA_ICON_16x(new ImageIcon(IMAGE_PATH + "antenna_icon_16x.png")),
    STATS_ICON_16x(new ImageIcon(IMAGE_PATH + "stats_icon_16x.png")),
    HEATMAP_ICON_16x(new ImageIcon(IMAGE_PATH + "heatmap_icon_16x.png")),
    MAP_ICON_16x(new ImageIcon(IMAGE_PATH + "map_icon_16x.png")),
    EXIT_ICON_16x(new ImageIcon(IMAGE_PATH + "exit_icon_16x.png")),
    AIRPLANE_ICON_8x(new ImageIcon(IMAGE_PATH + "airplane_icon_8x.png")),
    AIRPLANE_ICON_16x(new ImageIcon(IMAGE_PATH  + "airplane_icon_16x.png"));
    // image instance field
    private final ImageIcon img;
    // private enum constructor
    Images(final ImageIcon img) {
        this.img = img;
    }
    // image getter
    public ImageIcon get() {
        return this.img;
    }
}

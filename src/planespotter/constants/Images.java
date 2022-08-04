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
    LOADING_GIF(new ImageIcon(IMAGE_PATH + "loading.gif")),
    FILE_ICON(new ImageIcon(IMAGE_PATH + "file_icon.png"));
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

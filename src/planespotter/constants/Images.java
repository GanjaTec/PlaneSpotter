package planespotter.constants;

import javax.swing.*;

/**
 * images
 */
public enum Images {
    TITLE(new ImageIcon(Paths.RESSOURCE_PATH + "newTitle.png")),
    BGROUND_IMG(new ImageIcon(Paths.RESSOURCE_PATH + "background.png")),
    MENU_BGROUND_IMG(new ImageIcon(Paths.RESSOURCE_PATH + "menu_background.png")),
    TITLE_BGROUND_IMG(new ImageIcon(Paths.RESSOURCE_PATH + "title_background.jpg")),
    PAPER_PLANE_ICON(new ImageIcon(Paths.RESSOURCE_PATH + "planespotter_icon.png")),
    FLYING_PLANE_ICON(new ImageIcon(Paths.RESSOURCE_PATH + "flying_plane_icon.png")),
    LOADING_GIF(new ImageIcon(Paths.RESSOURCE_PATH + "loading.gif"));

    private final ImageIcon img;

    Images(final ImageIcon img) {
        this.img = img;
    }

    public ImageIcon get() {
        return this.img;
    }
}

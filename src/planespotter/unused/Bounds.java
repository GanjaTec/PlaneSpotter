package planespotter.unused;

import planespotter.display.GUI;

import java.awt.*;

/**
 *
 */
public class Bounds extends GUI {

    public static Rectangle ALL = new Rectangle(1280, 720);

    public static Rectangle MAINPANEL = new Rectangle(6, 6, ALL.width-25, ALL.height-48);

    public static Rectangle RIGHT_MAIN = new Rectangle(280, 70, MAINPANEL.width-280, MAINPANEL.height-70);

    public static Rectangle LEFT_MAIN = new Rectangle(0, 70, MAINPANEL.width-RIGHT_MAIN.width, MAINPANEL.height-70);

    public static Rectangle RIGHT = new Rectangle(0, 0, MAINPANEL.width-280, MAINPANEL.height-70);

    public static Rectangle LEFT = new Rectangle(0, 0, MAINPANEL.width-RIGHT.width, MAINPANEL.height-70);

    public static Rectangle TITLE = new Rectangle(0, 0, MAINPANEL.width, 70);

    // TODO BEARBEITEN
    public static Rectangle BIG_ITEM = new Rectangle(3, 4, ALL.width-20, ALL.height-35);

    public static Rectangle SMALL_ITEM = new Rectangle(3, 4, ALL.width-20, ALL.height-35);

    public static  Rectangle PLANE_ICON = new Rectangle(3, 4, ALL.width-20, ALL.height-35);

    public static  Rectangle TITLE_TEXT = new Rectangle(3, 4, ALL.width-20, ALL.height-35);

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param heigth
     */
    public Bounds (int x, int y, int width, int heigth) {
        //super.x = x;
        //super.y = y;
        //super.width = width;
        //super.height = heigth;
    }

    /**
     *
     * @param width
     * @param heigth
     */
    public Bounds (int width, int heigth) {
        //super.width = width;
        //super.height = heigth;
    }



}

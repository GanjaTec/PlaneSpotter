package planespotter.display.models;

import KentHipos.Kensoft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.model.Scheduler;
import planespotter.throwables.OutOfRangeException;
import planespotter.util.math.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static planespotter.util.math.MathUtils.divide;

public class LayerPane extends JLayeredPane {

    // layer constants
    private static final byte TOP_LAYER = 2;
    private static final byte DYNAMIC_LAYER = 1;
    private static final byte BOTTOM_LAYER = 0;

    // instance fields

    // default bottom component
    @Nullable private Component defaultBottomComp, defaultOverTopComp;

    /**
     * creates a new LayerPane with a background map viewer
     * and a specific size
     *
     * @param size is the panel size
     */
    public LayerPane(@NotNull Dimension size) {

        super();
        setLocation(0, 0);
        setSize(size);
    }

    /**
     * adds a component to the top layer while
     * old components are removed from the top layer
     *
     * @param comp is the component which is added to the top layer
     * @param x is the x-position of the component
     * @param y is the y-position of the component
     * @param width is the component width
     * @param height is the component height
     */
    public void addTop(@NotNull Component comp, int x, int y, int width, int height) {
        add(DYNAMIC_LAYER, comp, x, y, width, height);
    }

    /**
     * sets the default bottom component as bottom-comp
     * if it is not null
     */
    public void setBottomDefault() {
        if (defaultBottomComp != null) {
            replaceBottom(defaultBottomComp);
        }
    }

    /**
     * adds a component to the layer pane with specific layer and bounds
     *
     * @param layer is the layer in which the component is placed (0-2) use {@link LayerPane} constants here
     * @param comp is the component which is added to the top layer
     * @param x is the x-position of the component
     * @param y is the y-position of the component
     * @param width is the component width
     * @param height is the component height
     */
    private void add(int layer, @NotNull Component comp, int x, int y, int width, int height) {
        removeComps(layer);
        comp.setBounds(x, y, width, height);
        add(comp);
        setLayer(comp, layer);
    }

    /**
     * removes all components in the given layer, if there are some
     *
     * @param layer is the layer index, use LayerPane constants here
     */
    private void removeComps(int layer) {
        Component[] comps = getComponentsInLayer(layer);
        Arrays.stream(comps)
                .forEach(super::remove);
    }

    public void removeTop() {
        this.removeComps(DYNAMIC_LAYER);
    }

    public void replaceBottom(@NotNull Component comp) {
        add(BOTTOM_LAYER, comp, 0, 0, getWidth(), getHeight());
    }

    public void setDefaultBottomComponent(@NotNull Component comp) {
        this.defaultBottomComp = comp;
    }

    public void setDefaultOverTopComponent(@NotNull Component comp) {
        this.defaultOverTopComp = comp;
    }

    public void showOverTop(boolean show) {
        if (defaultOverTopComp != null) {
            defaultOverTopComp.setVisible(show);
            if (show) {
                defaultOverTopComp.setLocation((getWidth()/2) + (defaultOverTopComp.getWidth()/2), (getWidth()/2) + (defaultOverTopComp.getHeight()/2));
                add(TOP_LAYER, defaultOverTopComp, 0, 0, getWidth(), getHeight());
            } else {
                removeComps(TOP_LAYER);
            }
        }
    }

    @Nullable
    public Component getTop() {
        return getLayer(DYNAMIC_LAYER);
    }

    @Nullable
    public Component getBottom() {
        return getLayer(BOTTOM_LAYER);
    }

    @Nullable
    private Component getLayer(int layer) {
        if (layer < BOTTOM_LAYER || layer > TOP_LAYER) {
            throw new OutOfRangeException("Layer must be between 0 and 2!");
        }
        return Arrays.stream(getComponentsInLayer(layer))
                .findFirst()
                .orElse(null);
    }

    public void move(@NotNull Component comp, @NotNull MoveDirection direction, int x , int y, int amount) {
        comp.setLocation(x, y);
        Kensoft animation = new Kensoft();
        int delay = 10,
            compX = comp.getX(),
            compY = comp.getY(),
            plus  = 10;

        if (comp instanceof JLabel lbl) {
            switch (direction) {
                case UP -> animation.jLabelYUp(compY, compY+amount, delay, plus, lbl);
                case DOWN -> animation.jLabelYDown(compY, compY+amount, delay, plus, lbl);
                case LEFT -> animation.jLabelXLeft(compX, compX+amount, delay, plus, lbl);
                case RIGHT -> animation.jLabelXRight(compX, compX+amount, delay, plus, lbl);
            }

        } else if (comp instanceof JPanel pnl) {
            switch (direction) {
                case UP -> animation.jPanelYUp(compY, compY + amount, delay, plus, pnl);
                case DOWN -> animation.jPanelYDown(compY, compY + amount, delay, plus, pnl);
                case LEFT -> animation.jPanelXLeft(compX, compX + amount, delay, plus, pnl);
                case RIGHT -> animation.jPanelXRight(compX, compX + amount, delay, plus, pnl);
            }
        }
    }

    public enum MoveDirection {
        UP, DOWN, LEFT, RIGHT
    }
}

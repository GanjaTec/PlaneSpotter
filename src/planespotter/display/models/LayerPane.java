package planespotter.display.models;

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
    @Nullable
    private Component defaultBottomComp, defaultOverTopComp;

    /**
     * creates a new LayerPane with a background map viewer
     * and a specific size
     *
     * @param size is the panel size
     */
    public LayerPane(@NotNull Dimension size) {

        super();
        super.setLocation(0, 0);
        super.setSize(size);
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
        this.add(DYNAMIC_LAYER, comp, x, y, width, height);
    }

    public void setBottomDefault() {
        if (this.defaultBottomComp != null) {
            this.replaceBottom(this.defaultBottomComp);
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
        this.removeComps(layer);
        comp.setBounds(x, y, width, height);
        super.add(comp);
        super.setLayer(comp, layer);
    }

    /**
     * removes all components in the given layer, if there are some
     *
     * @param layer is the layer index, use LayerPane constants here
     */
    private void removeComps(int layer) {
        Component[] comps = super.getComponentsInLayer(layer);
        Arrays.stream(comps)
                .forEach(super::remove);
    }

    public void removeTop() {
        this.removeComps(DYNAMIC_LAYER);
    }

    public void replaceBottom(@NotNull Component comp) {
        this.add(BOTTOM_LAYER, comp, 0, 0, super.getWidth(), super.getHeight());
    }

    public void setDefaultBottomComponent(@NotNull Component comp) {
        this.defaultBottomComp = comp;
    }

    public void setDefaultOverTopComponent(@NotNull Component comp) {
        this.defaultOverTopComp = comp;
    }

    public void showOverTop(boolean show) {
        if (this.defaultOverTopComp != null) {
            this.defaultOverTopComp.setVisible(show);
            if (show) {
                this.defaultOverTopComp.setLocation((super.getWidth()/2) + (this.defaultOverTopComp.getWidth()/2), (super.getWidth()/2) + (this.defaultOverTopComp.getHeight()/2));
                this.add(TOP_LAYER, this.defaultOverTopComp, 0, 0, super.getWidth(), super.getHeight());
            } else {
                this.removeComps(TOP_LAYER);
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
        return Arrays.stream(this.getComponentsInLayer(layer))
                .findFirst()
                .orElse(null);
    }

    /**
     *
     *
     * @param startX
     * @param startY
     * @param width
     * @param height
     * @param endX
     * @param endY
     * @param timeMillis
     */
    public synchronized void moveTop(int startX, int startY, int width, int height, final int endX, final int endY, final int timeMillis) {

        this.moveTop(startX, startY, width, height, endX, endY, width, height, timeMillis);
    }

    /**
     *
     *
     * @param startX
     * @param startY
     * @param startWidth
     * @param startHeight
     * @param endX
     * @param endY
     * @param endWidth
     * @param endHeight
     * @param timeMillis
     */
    public synchronized void moveTop(int startX, int startY, int startWidth, int startHeight, final int endX, final int endY, final int endWidth, final int endHeight, final int timeMillis) {
        Component top;
        if ((top = this.getTop()) == null) {
            return;
        }
        Vector2D<Integer> direction = new Vector2D<>(endX - startX, endY - startY),
                          sizeDirection = new Vector2D<>(endWidth - startWidth, endHeight - startHeight);

        this.move(top, startX, startY, startWidth, startHeight, direction, sizeDirection, timeMillis);
    }

    /**
     *
     *
     * @param comp
     * @param startX
     * @param startY
     * @param startWidth
     * @param startHeight
     * @param direction
     * @param sizeDirection
     * @param timeMillis
     */
    private synchronized void move(@NotNull Component comp, final int startX, final int startY, final int startWidth, final int startHeight, @NotNull final Vector2D<Integer> direction, final Vector2D<Integer> sizeDirection, final int timeMillis) {

        int xVel = divide(direction.x, timeMillis),
            yVel = divide(direction.y, timeMillis),
            wVel = divide(sizeDirection.x, timeMillis),
            hVel = divide(sizeDirection.y, timeMillis);
        AtomicInteger x = new AtomicInteger(startX),
                      y = new AtomicInteger(startY),
                      w = new AtomicInteger(startWidth),
                      h = new AtomicInteger(startHeight);
        int timeout = 50;
        // do not create a new scheduler every time
        Scheduler animator = new Scheduler();
        try {
            animator.schedule(() -> {
                        //for (int t = 0; t < timeMillis; t += timeout) {
                        comp.setBounds(x.get(), y.get(), w.get(), h.get());
                        //comp.repaint(); // needed?
                        //comp.getParent().repaint();
                        comp.repaint(x.get(), y.get(), w.get(), h.get());


                        x.addAndGet(xVel);
                        y.addAndGet(yVel);
                        w.addAndGet(wVel);
                        h.addAndGet(hVel);

                        //Scheduler.sleep(50);
                    }, 0, timeout)
                    .get(timeMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
        }
        //}
    }
}

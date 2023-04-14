package planespotter.util.math;

public class Size2D extends Vector2D<Integer> {

    public Size2D(Integer width, Integer height) {
        super(width, height);
    }

    public int width() {
        return x;
    }

    public int height() {
        return y;
    }

}

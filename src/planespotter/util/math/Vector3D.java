package planespotter.util.math;

public class Vector3D<N extends Number> extends Vector2D<N> {

    public final N z;

    public Vector3D(N x, N y, N z) {
        super(x, y);
        this.z = z;
    }
}

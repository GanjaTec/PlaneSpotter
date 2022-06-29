package planespotter.util.math;

import planespotter.util.math.Vector2D;

public class Vector3D extends Vector2D {

    public final double z;

    public Vector3D(double x, double y, double z) {
        super(x, y);
        this.z = z;
    }
}

package planespotter.throwables;

import org.jetbrains.annotations.NotNull;

public class NoSuchComponentException extends NullPointerException {

    public NoSuchComponentException() {
        super();
    }

    public NoSuchComponentException(@NotNull String s) {
        super(s);
    }
}

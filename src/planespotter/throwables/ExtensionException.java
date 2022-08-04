package planespotter.throwables;

import org.jetbrains.annotations.NotNull;

public class ExtensionException extends Exception {

    public ExtensionException() {
        super();
    }

    public ExtensionException(@NotNull String message) {
        super(message);
    }
}

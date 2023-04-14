package planespotter.constants.props;

import org.jetbrains.annotations.NotNull;

public class Property {

    public final String key;
    public final Object val;

    public Property(@NotNull String key, @NotNull Object val) {
        this.key = key;
        this.val = val;
    }
}

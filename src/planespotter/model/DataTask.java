package planespotter.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.controller.Controller;

@FunctionalInterface
public interface DataTask {

    void runTask(@NotNull Controller ctrl, boolean @Nullable ... flags);

}

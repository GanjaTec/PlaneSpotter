package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.constants.Configuration;
import planespotter.throwables.InvalidDataException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class Filters {

    public static Filters read(@NotNull String filename) {
        if (filename.isBlank()) {
            throw new InvalidDataException("File name must not be blank!");
        }
        throw new UnsupportedOperationException("Not implemented yet!");
    }



    private final List<String> filters;

    public Filters() {
        this(new ArrayList<>(0));
    }

    public Filters(@NotNull List<String> filters) {
        this.filters = filters;
    }

    public void set(@NotNull String... filters) {
        if (filters == null || filters.length == 0) {
            this.filters.clear();
            return;
        }
        for (String f : filters) {
            this.add(f);
        }
    }

    public Filters add(@NotNull String filter) {
        if (filter.isBlank()) {
            throw new InvalidDataException("Filter must not be blank!");
        }
        this.filters.add(filter);
        return this;
    }

    public boolean remove(@NotNull String filter) {
        return this.filters.remove(filter);
    }

    @NotNull
    public List<String> getFilters() {
        return this.filters;
    }

    @NotNull
    public Stream<String> stream() {
        return this.filters.stream();
    }

}

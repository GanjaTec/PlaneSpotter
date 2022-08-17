package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
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
        return new Filters(new ArrayList<>(0));
    }



    private final List<String> filters;

    public Filters(@NotNull List<String> filters) {
        this.filters = filters;
    }

    public void add(@NotNull String filter) {
        if (filter.isBlank()) {
            throw new InvalidDataException("Filter must not be blank!");
        }
        filters.add(filter);
    }

    public boolean remove(@NotNull String filter) {
        return filters.remove(filter);
    }

    @NotNull
    public List<String> getFilters() {
        return filters;
    }

    @NotNull
    public Stream<String> stream() {
        return filters.stream();
    }

}

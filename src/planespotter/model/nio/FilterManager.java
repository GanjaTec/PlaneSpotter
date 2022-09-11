package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import planespotter.throwables.InvalidDataException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @name FilterManager
 * @author jml04
 * @version 1.0
 *
 * @description
 * The {@link FilterManager} class represents a manager for data filters,
 * contains managing and I/O operations for filters
 */
public class FilterManager {

    // list containing all filter strings
    private final List<String> filters;

    /**
     * constructs a new {@link FilterManager} without initial filters
     */
    public FilterManager() {
        this(new ArrayList<>(0));
    }

    /**
     * constructs a new {@link FilterManager} with specific filters
     *
     * @param filters is the initial filter list
     */
    public FilterManager(@NotNull List<String> filters) {
        this.filters = filters;
    }

    /**
     * reads a filters-file
     *
     * @param filename is the filters file name (must end with '.psc')
     * @return FilterManager object containing the filters
     */
    public static FilterManager read(@NotNull String filename) {
        if (filename.isBlank() || !filename.endsWith(".psc")) {
            throw new InvalidDataException("File name must not be blank and end with '.psc'!");
        }
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    /**
     * sets the filter list
     *
     * @param filter is an array of filter {@link String}s
     */
    public void set(@NotNull String @NotNull ... filter) {
        filters.clear();
        addAll(filter);
    }

    /**
     * adds a filter {@link String} to the filter list
     *
     * @param filter is the filter {@link String}
     * @return this {@link FilterManager} instance
     */
    public FilterManager add(@NotNull String filter) {
        if (filter.isBlank()) {
            throw new InvalidDataException("Filter must not be blank!");
        }
        filters.add(filter);
        return this;
    }

    /**
     * adds an array of filter {@link String}s to the filter list
     *
     * @param filters is an array of filter {@link String}s to add
     * @return this {@link FilterManager} instance
     */
    public FilterManager addAll(@NotNull String @NotNull ... filters) {
        for (String filter : filters) {
            add(filter);
        }
        return this;
    }

    /**
     * removes a filter {@link String} from the filter list
     *
     * @param filter is the filter {@link String} to remove
     * @return true if the filter was successfully removed, else false
     */
    public boolean remove(@NotNull String filter) {
        return filters.remove(filter);
    }

    /**
     * getter for the filter list
     *
     * @return {@link List} of filter {@link String}s
     */
    @NotNull
    public List<String> getFilters() {
        return filters;
    }

    /**
     * getter for the filter list as stream
     *
     * @return {@link Stream} of the filter list
     */
    @NotNull
    public Stream<String> stream() {
        return filters.stream();
    }

}

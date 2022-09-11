package planespotter.constants;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @name Configuration
 * @author jml04
 * @version 1.0
 * 
 * @description
 * abstract class Configuration contains the internal planespotter-configuration
 */
// TODO: 24.08.2022 MERGE WITH UserSettings class
public final class Configuration {

    public static final String CONFIG_FILENAME = Paths.RESOURCE_PATH + "config.psc";

    public static final String FILTERS_FILENAME = Paths.RESOURCE_PATH + "filters.psc";

    public static final String CONNECTIONS_FILENAME = Paths.RESOURCE_PATH + "connections.psc";

    @NotNull private final Map<String, Object> props = new HashMap<>();

    @NotNull
    public Object getProperty(@NotNull String key) {
        Object get;
        if ((get = props.get(key)) == null) {
            throw new NullPointerException("No value found!");
        }
        return get;
    }

    public void setProperty(@NotNull String key, @NotNull Object value) {
        props.put(key, value);
    }

    @NotNull
    public List<Map.Entry<String, Object>> getUserProperties() {
        Set<String> userKeys = Set.of("dataLimit", "currentMapSource", "gridSizeLat", "gridSizeLon");
        return props.entrySet()
                .stream()
                .filter(entry -> userKeys.contains(entry.getKey()))
                .toList();
    }
}

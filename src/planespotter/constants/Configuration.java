package planespotter.constants;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @name Configuration
 * @author jml04
 * @version 1.1
 * 
 * @description
 * abstract class Configuration contains the planespotter-configuration
 */
public final class Configuration {

    public static final String CONFIG_FILENAME = Paths.RESOURCE + "config.json";

    public static final String FILTERS_FILENAME = Paths.RESOURCE + "filters.psc";

    public static final String CONNECTIONS_FILENAME = Paths.RESOURCE + "connections.json";

    private final Map<String, Property> props = new HashMap<>();

    @NotNull
    public Property getProperty(@NotNull String key) {
        Property get;
        if ((get = props.get(key)) == null) {
            throw new NullPointerException("No value found!");
        }
        return get;
    }

    public void setProperty(@NotNull Property property) {
        props.put(property.key, property);
    }

    public void setProperty(@NotNull String key, @NotNull Object value) {
        setProperty(new Property(key, value));
    }

    @NotNull
    public Property[] getUserProperties() {
        // FIXME: 15.12.2022 zahlen werden plötzlich als double gespeichert
        return new Property[] {
                props.get("dataLimit"),
                props.get("currentMapSource"),
                props.get("gridSizeLat"),
                props.get("gridSizeLon")
        };
    }

    public static class Property {

        public final String key;
        public final Object val;

        public Property(@NotNull String key, @NotNull Object val) {
            this.key = key;
            this.val = val;
        }
    }

}

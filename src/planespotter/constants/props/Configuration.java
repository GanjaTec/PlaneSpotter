package planespotter.constants.props;

import org.jetbrains.annotations.NotNull;
import planespotter.constants.Paths;

import java.util.HashMap;
import java.util.Map;

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

    private final Map<String, Property> props;

    public Configuration() {
        this(new HashMap<>());
    }

    public Configuration(@NotNull Property[] props) {
        this();
        for (Property prop : props) {
            setProperty(prop);
        }
    }

    public Configuration(@NotNull Map<String, Property> props) {
        this.props = props;
    }

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

    public void merge(@NotNull Configuration config) {
        config.props.values()
                .forEach(this::setProperty);
    }

    @NotNull
    public UserProperties getUserProperties() {
        return new UserProperties(props.get("dataLimit"), props.get("currentMapSource"), props.get("gridSizeLat"), props.get("gridSizeLon"));
    }

    public int elements() {
        return props.size();
    }

}
